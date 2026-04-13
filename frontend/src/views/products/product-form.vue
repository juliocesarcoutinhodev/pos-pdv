<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import { createProduct, getNextProductSku, getProductById, updateProductPut } from '@/services/productService.js';
import { downloadImageBlob, uploadImage } from '@/services/imageService.js';
import { getSupplierById, listSuppliers } from '@/services/supplierService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loadingInitial = ref(false);
const submitting = ref(false);
const imageUploadLoading = ref(false);
const imagePreviewLoading = ref(false);
const supplierLookupLoading = ref(false);
const supplierSuggestions = ref([]);
const selectedSupplier = ref(null);
const pricingReferenceField = ref('sale');

const SUPPLIER_LOOKUP_DEBOUNCE_MS = 250;
let supplierLookupTimer = null;

const imageInputRef = ref(null);
const imagePreviewUrl = ref('');

const productId = computed(() => (typeof route.params.id === 'string' ? route.params.id : null));
const isEditMode = computed(() => Boolean(productId.value));
const pageTitle = computed(() => (isEditMode.value ? 'Editar produto' : 'Novo produto'));
const submitLabel = computed(() => (isEditMode.value ? 'Salvar alterações' : 'Criar produto'));

const form = ref(createEmptyForm());

function createEmptyForm() {
    return {
        id: null,
        active: true,
        sku: '',
        barcode: '',
        name: '',
        description: '',
        brand: '',
        category: '',
        supplierId: null,
        unit: 'UN',
        costPrice: '',
        marginPercentage: '',
        salePrice: '',
        promotionalPrice: '',
        stockQuantity: '',
        minimumStock: '',
        ncm: '',
        cest: '',
        cfop: '',
        taxOrigin: '',
        taxSituation: '',
        icmsRate: '',
        pisSituation: '',
        pisRate: '',
        cofinsSituation: '',
        cofinsRate: '',
        imageId: null
    };
}

function sanitizeString(value) {
    return (value || '').trim();
}

function sanitizeOptionalString(value) {
    const sanitized = sanitizeString(value);
    return sanitized || null;
}

function onlyDigits(value) {
    return (value || '').replace(/\D/g, '');
}

function normalizeSkuInput(value) {
    return sanitizeString(value).toUpperCase();
}

function generateFallbackSku() {
    return String(Math.floor(100000 + Math.random() * 900000));
}

function normalizeUnitInput(value) {
    return sanitizeString(value).toUpperCase();
}

function normalizeTaxCode(value, maxLength) {
    return sanitizeString(value).toUpperCase().slice(0, maxLength);
}

function normalizeDigitsInput(value, maxLength) {
    return onlyDigits(value).slice(0, maxLength);
}

function formatTaxId(value) {
    if (!value) {
        return '-';
    }

    const digits = value.replace(/\D/g, '');
    if (digits.length !== 14) {
        return value;
    }

    return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`;
}

function toSupplierOption(supplier) {
    if (!supplier?.id) {
        return null;
    }

    return {
        id: supplier.id,
        name: supplier.name ?? '',
        taxId: supplier.taxId ?? ''
    };
}

function clearSupplierLookupTimer() {
    if (supplierLookupTimer) {
        clearTimeout(supplierLookupTimer);
        supplierLookupTimer = null;
    }
}

function handleSupplierSelection(value) {
    const selectedOption = value && typeof value === 'object' && value.id ? value : null;
    selectedSupplier.value = selectedOption;
    form.value.supplierId = selectedOption?.id ?? null;
}

async function loadSupplierSuggestions(query) {
    supplierLookupLoading.value = true;
    try {
        const params = {
            page: 0,
            size: 10,
            sortBy: 'name',
            sortDirection: 'asc'
        };

        if (query) {
            params.name = query;
        }

        const response = await listSuppliers(params);
        supplierSuggestions.value = response.content.map(toSupplierOption).filter((option) => option?.id);
    } catch {
        supplierSuggestions.value = [];
    } finally {
        supplierLookupLoading.value = false;
    }
}

async function handleSupplierLookup(event) {
    const query = sanitizeString(event?.query);
    clearSupplierLookupTimer();
    supplierLookupTimer = setTimeout(() => {
        void loadSupplierSuggestions(query);
    }, SUPPLIER_LOOKUP_DEBOUNCE_MS);
}

async function handleSupplierDropdownClick(event) {
    clearSupplierLookupTimer();
    const query = sanitizeString(event?.query);
    await loadSupplierSuggestions(query);
}

async function loadSelectedSupplier(supplierId) {
    if (!supplierId) {
        selectedSupplier.value = null;
        return;
    }

    try {
        const supplier = await getSupplierById(supplierId);
        selectedSupplier.value = toSupplierOption(supplier);
    } catch {
        selectedSupplier.value = null;
        form.value.supplierId = null;
    }
}

function parseDecimalInput(value) {
    const normalized = sanitizeString(String(value ?? '')).replace(',', '.');
    if (!normalized) {
        return null;
    }

    const parsed = Number(normalized);
    return Number.isFinite(parsed) ? parsed : Number.NaN;
}

function roundDecimal(value, scale = 2) {
    if (!Number.isFinite(value)) {
        return null;
    }
    return Number(value.toFixed(scale));
}

function calculateSalePriceFromMargin(costPrice, marginPercentage) {
    if (!Number.isFinite(costPrice) || !Number.isFinite(marginPercentage)) {
        return null;
    }

    return roundDecimal(costPrice * (1 + marginPercentage / 100), 2);
}

function calculateMarginPercentageFromSale(costPrice, salePrice) {
    if (!Number.isFinite(costPrice) || !Number.isFinite(salePrice)) {
        return null;
    }

    if (costPrice === 0) {
        return salePrice === 0 ? 0 : null;
    }

    return roundDecimal(((salePrice - costPrice) / costPrice) * 100, 2);
}

function decimalToInput(value) {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value);
}

function syncSalePriceFromMargin() {
    const costPrice = parseDecimalInput(form.value.costPrice);
    const marginPercentage = parseDecimalInput(form.value.marginPercentage);
    if (costPrice === null || marginPercentage === null || Number.isNaN(costPrice) || Number.isNaN(marginPercentage)) {
        form.value.salePrice = '';
        return;
    }

    const calculatedSalePrice = calculateSalePriceFromMargin(costPrice, marginPercentage);
    if (calculatedSalePrice === null) {
        form.value.salePrice = '';
        return;
    }

    form.value.salePrice = decimalToInput(calculatedSalePrice);
}

function syncMarginFromSalePrice() {
    const costPrice = parseDecimalInput(form.value.costPrice);
    const salePrice = parseDecimalInput(form.value.salePrice);
    if (costPrice === null || salePrice === null || Number.isNaN(costPrice) || Number.isNaN(salePrice)) {
        form.value.marginPercentage = '';
        return;
    }

    const calculatedMarginPercentage = calculateMarginPercentageFromSale(costPrice, salePrice);
    form.value.marginPercentage = decimalToInput(calculatedMarginPercentage);
}

function handleCostPriceInput(value) {
    form.value.costPrice = value;

    if (pricingReferenceField.value === 'margin') {
        syncSalePriceFromMargin();
        return;
    }

    syncMarginFromSalePrice();
}

function handleMarginInput(value) {
    form.value.marginPercentage = value;
    pricingReferenceField.value = 'margin';
    syncSalePriceFromMargin();
}

function handleSalePriceInput(value) {
    form.value.salePrice = value;
    pricingReferenceField.value = 'sale';
    syncMarginFromSalePrice();
}

function clearImagePreview() {
    if (imagePreviewUrl.value) {
        URL.revokeObjectURL(imagePreviewUrl.value);
        imagePreviewUrl.value = '';
    }
}

async function loadImagePreview(imageId) {
    clearImagePreview();
    if (!imageId) {
        return;
    }

    imagePreviewLoading.value = true;
    try {
        const blob = await downloadImageBlob(imageId);
        imagePreviewUrl.value = URL.createObjectURL(blob);
    } catch {
        imagePreviewUrl.value = '';
    } finally {
        imagePreviewLoading.value = false;
    }
}

function handleSkuInput(value) {
    form.value.sku = normalizeSkuInput(value);
}

function handleBarcodeInput(value) {
    form.value.barcode = normalizeDigitsInput(value, 20);
}

function handleUnitInput(value) {
    form.value.unit = normalizeUnitInput(value);
}

function handleNcmInput(value) {
    form.value.ncm = normalizeDigitsInput(value, 8);
}

function handleCestInput(value) {
    form.value.cest = normalizeDigitsInput(value, 7);
}

function handleCfopInput(value) {
    form.value.cfop = normalizeDigitsInput(value, 4);
}

function handleTaxOriginInput(value) {
    form.value.taxOrigin = normalizeTaxCode(value, 2);
}

function handleTaxSituationInput(value) {
    form.value.taxSituation = normalizeTaxCode(value, 10);
}

function handlePisSituationInput(value) {
    form.value.pisSituation = normalizeTaxCode(value, 4);
}

function handleCofinsSituationInput(value) {
    form.value.cofinsSituation = normalizeTaxCode(value, 4);
}

function triggerImageSelection() {
    imageInputRef.value?.click();
}

async function handleImageSelected(event) {
    const file = event.target?.files?.[0];
    if (!file) {
        return;
    }

    if (!file.type.startsWith('image/')) {
        toast.add({
            severity: 'warn',
            summary: 'Arquivo inválido',
            detail: 'Selecione um arquivo de imagem.',
            life: 3000
        });
        event.target.value = '';
        return;
    }

    imageUploadLoading.value = true;
    try {
        const uploaded = await uploadImage(file);
        form.value.imageId = uploaded.imageId;
        clearImagePreview();
        imagePreviewUrl.value = URL.createObjectURL(file);

        toast.add({
            severity: 'success',
            summary: 'Imagem enviada',
            detail: 'Imagem do produto enviada com sucesso.',
            life: 3000
        });
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        imageUploadLoading.value = false;
        event.target.value = '';
    }
}

function removeImage() {
    form.value.imageId = null;
    clearImagePreview();
}

function buildPayload() {
    return {
        sku: normalizeSkuInput(form.value.sku),
        barcode: sanitizeOptionalString(normalizeDigitsInput(form.value.barcode, 20)),
        name: sanitizeString(form.value.name),
        description: sanitizeOptionalString(form.value.description),
        brand: sanitizeOptionalString(form.value.brand),
        category: sanitizeOptionalString(form.value.category),
        supplierId: form.value.supplierId,
        unit: normalizeUnitInput(form.value.unit),
        costPrice: parseDecimalInput(form.value.costPrice),
        salePrice: parseDecimalInput(form.value.salePrice),
        marginPercentage: parseDecimalInput(form.value.marginPercentage),
        promotionalPrice: parseDecimalInput(form.value.promotionalPrice),
        stockQuantity: parseDecimalInput(form.value.stockQuantity),
        minimumStock: parseDecimalInput(form.value.minimumStock),
        ncm: sanitizeOptionalString(normalizeDigitsInput(form.value.ncm, 8)),
        cest: sanitizeOptionalString(normalizeDigitsInput(form.value.cest, 7)),
        cfop: sanitizeOptionalString(normalizeDigitsInput(form.value.cfop, 4)),
        taxOrigin: sanitizeOptionalString(normalizeTaxCode(form.value.taxOrigin, 2)),
        taxSituation: sanitizeOptionalString(normalizeTaxCode(form.value.taxSituation, 10)),
        icmsRate: parseDecimalInput(form.value.icmsRate),
        pisSituation: sanitizeOptionalString(normalizeTaxCode(form.value.pisSituation, 4)),
        pisRate: parseDecimalInput(form.value.pisRate),
        cofinsSituation: sanitizeOptionalString(normalizeTaxCode(form.value.cofinsSituation, 4)),
        cofinsRate: parseDecimalInput(form.value.cofinsRate),
        imageId: sanitizeOptionalString(form.value.imageId)
    };
}

function validateNumericField(value, label, { required = false } = {}) {
    if (value === null) {
        if (required) {
            toast.add({
                severity: 'warn',
                summary: `${label} obrigatório`,
                detail: `Informe o campo ${label.toLowerCase()}.`,
                life: 3000
            });
            return false;
        }
        return true;
    }

    if (Number.isNaN(value)) {
        toast.add({
            severity: 'warn',
            summary: `${label} inválido`,
            detail: `Informe um valor numérico válido para ${label.toLowerCase()}.`,
            life: 3000
        });
        return false;
    }

    if (value < 0) {
        toast.add({
            severity: 'warn',
            summary: `${label} inválido`,
            detail: `${label} não pode ser negativo.`,
            life: 3000
        });
        return false;
    }

    return true;
}

function validatePayload(payload) {
    if (!payload.sku) {
        toast.add({
            severity: 'warn',
            summary: 'SKU obrigatório',
            detail: 'Informe o SKU do produto.',
            life: 3000
        });
        return false;
    }

    if (!payload.name) {
        toast.add({
            severity: 'warn',
            summary: 'Nome obrigatório',
            detail: 'Informe o nome do produto.',
            life: 3000
        });
        return false;
    }

    if (!payload.unit) {
        toast.add({
            severity: 'warn',
            summary: 'Unidade obrigatória',
            detail: 'Informe a unidade de venda do produto.',
            life: 3000
        });
        return false;
    }

    if (payload.ncm && payload.ncm.length !== 8) {
        toast.add({
            severity: 'warn',
            summary: 'NCM inválido',
            detail: 'O NCM deve conter 8 dígitos.',
            life: 3000
        });
        return false;
    }

    if (payload.cest && payload.cest.length !== 7) {
        toast.add({
            severity: 'warn',
            summary: 'CEST inválido',
            detail: 'O CEST deve conter 7 dígitos.',
            life: 3000
        });
        return false;
    }

    if (payload.cfop && payload.cfop.length !== 4) {
        toast.add({
            severity: 'warn',
            summary: 'CFOP inválido',
            detail: 'O CFOP deve conter 4 dígitos.',
            life: 3000
        });
        return false;
    }

    if (!validateNumericField(payload.salePrice, 'Preço de venda')) {
        return false;
    }

    if (!validateNumericField(payload.costPrice, 'Preço de custo')) {
        return false;
    }
    if (!validateNumericField(payload.marginPercentage, 'Margem')) {
        return false;
    }
    if (!validateNumericField(payload.promotionalPrice, 'Preço promocional')) {
        return false;
    }
    if (!validateNumericField(payload.stockQuantity, 'Estoque atual')) {
        return false;
    }
    if (!validateNumericField(payload.minimumStock, 'Estoque mínimo')) {
        return false;
    }
    if (!validateNumericField(payload.icmsRate, 'Alíquota ICMS')) {
        return false;
    }
    if (!validateNumericField(payload.pisRate, 'Alíquota PIS')) {
        return false;
    }
    if (!validateNumericField(payload.cofinsRate, 'Alíquota COFINS')) {
        return false;
    }

    if (payload.salePrice === null && payload.marginPercentage === null) {
        toast.add({
            severity: 'warn',
            summary: 'Preço de venda ou margem obrigatórios',
            detail: 'Informe o preço de venda ou a margem para calcular o preço automaticamente.',
            life: 3000
        });
        return false;
    }

    if (payload.marginPercentage !== null && payload.costPrice === null) {
        toast.add({
            severity: 'warn',
            summary: 'Preço de custo obrigatório',
            detail: 'Informe o preço de custo para calcular o preço de venda pela margem.',
            life: 3000
        });
        return false;
    }

    if (payload.promotionalPrice !== null && !Number.isNaN(payload.promotionalPrice) && payload.salePrice !== null && !Number.isNaN(payload.salePrice) && payload.promotionalPrice > payload.salePrice) {
        toast.add({
            severity: 'warn',
            summary: 'Preço promocional inválido',
            detail: 'O preço promocional não pode ser maior que o preço de venda.',
            life: 3000
        });
        return false;
    }

    return true;
}

function mapProductToForm(product) {
    const marginPercentage = product?.marginPercentage ?? calculateMarginPercentageFromSale(product?.costPrice ?? null, product?.salePrice ?? null);

    return {
        id: product?.id ?? null,
        active: Boolean(product?.active),
        sku: product?.sku ?? '',
        barcode: product?.barcode ?? '',
        name: product?.name ?? '',
        description: product?.description ?? '',
        brand: product?.brand ?? '',
        category: product?.category ?? '',
        supplierId: product?.supplierId ?? null,
        unit: product?.unit ?? 'UN',
        costPrice: decimalToInput(product?.costPrice),
        marginPercentage: decimalToInput(marginPercentage),
        salePrice: decimalToInput(product?.salePrice),
        promotionalPrice: decimalToInput(product?.promotionalPrice),
        stockQuantity: decimalToInput(product?.stockQuantity),
        minimumStock: decimalToInput(product?.minimumStock),
        ncm: product?.ncm ?? '',
        cest: product?.cest ?? '',
        cfop: product?.cfop ?? '',
        taxOrigin: product?.taxOrigin ?? '',
        taxSituation: product?.taxSituation ?? '',
        icmsRate: decimalToInput(product?.icmsRate),
        pisSituation: product?.pisSituation ?? '',
        pisRate: decimalToInput(product?.pisRate),
        cofinsSituation: product?.cofinsSituation ?? '',
        cofinsRate: decimalToInput(product?.cofinsRate),
        imageId: product?.imageId ?? null
    };
}

async function loadProductForEdit() {
    if (!isEditMode.value || !productId.value) {
        return;
    }

    loadingInitial.value = true;
    try {
        const product = await getProductById(productId.value);
        form.value = mapProductToForm(product);
        pricingReferenceField.value = 'sale';
        await loadImagePreview(form.value.imageId);
        await loadSelectedSupplier(form.value.supplierId);
    } catch (error) {
        showApiErrorToast(toast, error);
        await router.push('/products/list');
    } finally {
        loadingInitial.value = false;
    }
}

async function loadAutoSkuForCreate() {
    if (isEditMode.value || form.value.sku) {
        return;
    }

    try {
        const suggestedSku = await getNextProductSku();
        form.value.sku = normalizeSkuInput(suggestedSku || generateFallbackSku());
    } catch {
        form.value.sku = generateFallbackSku();
    }
}

async function handleSubmit() {
    const payload = buildPayload();
    if (!validatePayload(payload)) {
        return;
    }

    submitting.value = true;
    try {
        if (isEditMode.value && productId.value) {
            await updateProductPut(productId.value, payload);
            toast.add({
                severity: 'success',
                summary: 'Produto atualizado',
                detail: 'As alterações foram salvas com sucesso.',
                life: 3000
            });
        } else {
            await createProduct(payload);
            toast.add({
                severity: 'success',
                summary: 'Produto criado',
                detail: 'Produto cadastrado com sucesso.',
                life: 3000
            });
        }

        await router.push('/products/list');
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        submitting.value = false;
    }
}

function goBack() {
    void router.push('/products/list');
}

onMounted(async () => {
    if (isEditMode.value) {
        await loadProductForEdit();
        return;
    }

    pricingReferenceField.value = 'sale';
    selectedSupplier.value = null;
    supplierSuggestions.value = [];
    await loadAutoSkuForCreate();
});

onUnmounted(() => {
    clearImagePreview();
    clearSupplierLookupTimer();
});
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
            <div>
                <div class="font-semibold text-xl">{{ pageTitle }}</div>
                <div class="text-sm text-muted-color">Preencha os dados comerciais, estoque, fiscal e imagem do produto.</div>
            </div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-arrow-left" label="Voltar" severity="secondary" outlined @click="goBack" :disabled="submitting || loadingInitial" />
                <Button icon="pi pi-check" :label="submitLabel" @click="handleSubmit" :loading="submitting" :disabled="loadingInitial" />
            </div>
        </div>

        <Message v-if="isEditMode && !form.active" severity="warn" :closable="false" class="mb-4"> Este produto está inativo. Você pode editar os dados e reativá-lo na listagem. </Message>

        <div v-if="loadingInitial" class="flex justify-center py-10">
            <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
        </div>

        <div v-else class="flex flex-col gap-6">
            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">1. Dados do produto</div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label for="product-sku" class="block mb-2 font-medium">SKU *</label>
                        <InputText id="product-sku" :modelValue="form.sku" class="w-full" :disabled="submitting" @update:modelValue="handleSkuInput" />
                    </div>

                    <div>
                        <label for="product-barcode" class="block mb-2 font-medium">Código de barras</label>
                        <InputText id="product-barcode" :modelValue="form.barcode" class="w-full" :disabled="submitting" @update:modelValue="handleBarcodeInput" />
                    </div>

                    <div>
                        <label for="product-unit" class="block mb-2 font-medium">Unidade *</label>
                        <InputText id="product-unit" :modelValue="form.unit" maxlength="10" class="w-full" :disabled="submitting" @update:modelValue="handleUnitInput" />
                    </div>

                    <div class="md:col-span-2">
                        <label for="product-name" class="block mb-2 font-medium">Nome *</label>
                        <InputText id="product-name" v-model="form.name" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="product-brand" class="block mb-2 font-medium">Marca</label>
                        <InputText id="product-brand" v-model="form.brand" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="product-category" class="block mb-2 font-medium">Categoria</label>
                        <InputText id="product-category" v-model="form.category" class="w-full" :disabled="submitting" />
                    </div>

                    <div class="md:col-span-2">
                        <label for="product-supplier" class="block mb-2 font-medium">Fornecedor</label>
                        <AutoComplete
                            id="product-supplier"
                            :modelValue="selectedSupplier"
                            :suggestions="supplierSuggestions"
                            optionLabel="name"
                            dropdown
                            dropdownMode="current"
                            forceSelection
                            emptySearchMessage="Nenhum fornecedor encontrado"
                            emptySelectionMessage="Nenhum fornecedor encontrado"
                            class="w-full"
                            :disabled="submitting"
                            :loading="supplierLookupLoading"
                            @complete="handleSupplierLookup"
                            @dropdown-click="handleSupplierDropdownClick"
                            @update:modelValue="handleSupplierSelection"
                        >
                            <template #option="slotProps">
                                <div class="flex flex-col">
                                    <span class="font-medium">{{ slotProps.option.name }}</span>
                                    <small class="text-muted-color">CNPJ: {{ formatTaxId(slotProps.option.taxId) }}</small>
                                </div>
                            </template>
                        </AutoComplete>
                    </div>

                    <div class="md:col-span-3">
                        <label for="product-description" class="block mb-2 font-medium">Descrição</label>
                        <Textarea id="product-description" v-model="form.description" rows="4" class="w-full" :disabled="submitting" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">2. Comercial e estoque</div>

                <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
                    <div>
                        <label for="product-cost-price" class="block mb-2 font-medium">Preço de custo</label>
                        <InputText id="product-cost-price" :modelValue="form.costPrice" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" @update:modelValue="handleCostPriceInput" />
                    </div>

                    <div>
                        <label for="product-margin-percentage" class="block mb-2 font-medium">Margem (%)</label>
                        <InputText id="product-margin-percentage" :modelValue="form.marginPercentage" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" @update:modelValue="handleMarginInput" />
                    </div>

                    <div>
                        <label for="product-sale-price" class="block mb-2 font-medium">Preço de venda</label>
                        <InputText id="product-sale-price" :modelValue="form.salePrice" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" @update:modelValue="handleSalePriceInput" />
                    </div>

                    <div>
                        <label for="product-promotional-price" class="block mb-2 font-medium">Preço promocional</label>
                        <InputText id="product-promotional-price" v-model="form.promotionalPrice" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="product-stock-quantity" class="block mb-2 font-medium">Estoque atual</label>
                        <InputText id="product-stock-quantity" v-model="form.stockQuantity" type="number" step="0.001" min="0" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="product-minimum-stock" class="block mb-2 font-medium">Estoque mínimo</label>
                        <InputText id="product-minimum-stock" v-model="form.minimumStock" type="number" step="0.001" min="0" class="w-full" :disabled="submitting" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">3. Fiscal</div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label for="product-ncm" class="block mb-2 font-medium">NCM</label>
                        <InputText id="product-ncm" :modelValue="form.ncm" maxlength="8" class="w-full" :disabled="submitting" @update:modelValue="handleNcmInput" />
                    </div>

                    <div>
                        <label for="product-cest" class="block mb-2 font-medium">CEST</label>
                        <InputText id="product-cest" :modelValue="form.cest" maxlength="7" class="w-full" :disabled="submitting" @update:modelValue="handleCestInput" />
                    </div>

                    <div>
                        <label for="product-cfop" class="block mb-2 font-medium">CFOP</label>
                        <InputText id="product-cfop" :modelValue="form.cfop" maxlength="4" class="w-full" :disabled="submitting" @update:modelValue="handleCfopInput" />
                    </div>

                    <div>
                        <label for="product-tax-origin" class="block mb-2 font-medium">Origem tributária</label>
                        <InputText id="product-tax-origin" :modelValue="form.taxOrigin" maxlength="2" class="w-full" :disabled="submitting" @update:modelValue="handleTaxOriginInput" />
                    </div>

                    <div>
                        <label for="product-tax-situation" class="block mb-2 font-medium">Situação tributária</label>
                        <InputText id="product-tax-situation" :modelValue="form.taxSituation" maxlength="10" class="w-full" :disabled="submitting" @update:modelValue="handleTaxSituationInput" />
                    </div>

                    <div>
                        <label for="product-icms-rate" class="block mb-2 font-medium">Alíquota ICMS (%)</label>
                        <InputText id="product-icms-rate" v-model="form.icmsRate" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="product-pis-situation" class="block mb-2 font-medium">Situação PIS</label>
                        <InputText id="product-pis-situation" :modelValue="form.pisSituation" maxlength="4" class="w-full" :disabled="submitting" @update:modelValue="handlePisSituationInput" />
                    </div>

                    <div>
                        <label for="product-pis-rate" class="block mb-2 font-medium">Alíquota PIS (%)</label>
                        <InputText id="product-pis-rate" v-model="form.pisRate" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="product-cofins-situation" class="block mb-2 font-medium">Situação COFINS</label>
                        <InputText id="product-cofins-situation" :modelValue="form.cofinsSituation" maxlength="4" class="w-full" :disabled="submitting" @update:modelValue="handleCofinsSituationInput" />
                    </div>

                    <div>
                        <label for="product-cofins-rate" class="block mb-2 font-medium">Alíquota COFINS (%)</label>
                        <InputText id="product-cofins-rate" v-model="form.cofinsRate" type="number" step="0.01" min="0" class="w-full" :disabled="submitting" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">4. Imagem do produto</div>

                <div class="flex flex-col md:flex-row gap-4 md:items-start">
                    <div class="w-32 h-32 rounded-lg border border-surface-200 dark:border-surface-700 flex items-center justify-center bg-surface-0 dark:bg-surface-900 overflow-hidden">
                        <ProgressSpinner v-if="imagePreviewLoading || imageUploadLoading" style="width: 1.8rem; height: 1.8rem" strokeWidth="6" />
                        <img v-else-if="imagePreviewUrl" :src="imagePreviewUrl" alt="Pré-visualização da imagem do produto" class="w-full h-full object-contain" />
                        <div v-else class="text-center text-muted-color text-sm px-2">Sem imagem</div>
                    </div>

                    <div class="flex-1 flex flex-col gap-3">
                        <input ref="imageInputRef" type="file" accept="image/*" class="hidden" @change="handleImageSelected" />

                        <div class="flex flex-col sm:flex-row gap-2">
                            <Button icon="pi pi-upload" label="Selecionar imagem" severity="secondary" outlined @click="triggerImageSelection" :loading="imageUploadLoading" :disabled="submitting || imageUploadLoading" />
                            <Button icon="pi pi-times" label="Remover imagem" severity="danger" text @click="removeImage" :disabled="submitting || imageUploadLoading || (!form.imageId && !imagePreviewUrl)" />
                        </div>

                        <small class="text-muted-color">Após o upload, a imagem já fica vinculada ao cadastro e exibida na tela.</small>
                        <small v-if="form.imageId" class="text-muted-color">ID da imagem: {{ form.imageId }}</small>
                    </div>
                </div>
            </section>

            <div class="flex flex-col sm:flex-row justify-end gap-2">
                <Button label="Cancelar" severity="secondary" outlined @click="goBack" :disabled="submitting" />
                <Button :label="submitLabel" icon="pi pi-check" @click="handleSubmit" :loading="submitting" />
            </div>
        </div>
    </div>
</template>
