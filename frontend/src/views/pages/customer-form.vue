<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import { createCustomer, getCustomerById, updateCustomerPut } from '@/services/customerService.js';
import { downloadImageBlob, uploadImage } from '@/services/imageService.js';
import { lookupZip } from '@/services/supplierService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loadingInitial = ref(false);
const submitting = ref(false);
const zipLookupLoading = ref(false);
const imageUploadLoading = ref(false);
const imagePreviewLoading = ref(false);

const suppressAutoLookup = ref(false);
const lastZipLookupCode = ref('');

const imageInputRef = ref(null);
const imagePreviewUrl = ref('');

let zipLookupTimer = null;

const TAX_ID_MAX_DIGITS = 14;
const PHONE_MAX_DIGITS = 11;

const customerId = computed(() => (typeof route.params.id === 'string' ? route.params.id : null));
const isEditMode = computed(() => Boolean(customerId.value));
const pageTitle = computed(() => (isEditMode.value ? 'Editar cliente' : 'Novo cliente'));
const submitLabel = computed(() => (isEditMode.value ? 'Salvar alterações' : 'Criar cliente'));

const form = ref(createEmptyForm());

function createEmptyForm() {
    return {
        id: null,
        active: true,
        name: '',
        taxId: '',
        email: '',
        phone: '',
        imageId: null,
        address: {
            zipCode: '',
            street: '',
            number: '',
            complement: '',
            district: '',
            city: '',
            state: ''
        }
    };
}

function onlyDigits(value) {
    return (value || '').replace(/\D/g, '');
}

function normalizeTaxIdInput(value) {
    return onlyDigits(value).slice(0, TAX_ID_MAX_DIGITS);
}

function normalizePhoneInput(value) {
    return onlyDigits(value).slice(0, PHONE_MAX_DIGITS);
}

function sanitizeString(value) {
    return (value || '').trim();
}

function sanitizeOptionalString(value) {
    const sanitized = sanitizeString(value);
    return sanitized || null;
}

function normalizeState(value) {
    return sanitizeString(value).toUpperCase();
}

function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
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

function handleTaxIdInput(value) {
    form.value.taxId = normalizeTaxIdInput(value);
}

function handleCustomerPhoneInput(value) {
    form.value.phone = normalizePhoneInput(value);
}

function applyZipLookup(data) {
    if (!data) {
        return;
    }

    form.value.address.zipCode = data.code || form.value.address.zipCode;
    form.value.address.street = data.street || form.value.address.street;
    form.value.address.district = data.district || form.value.address.district;
    form.value.address.city = data.city || form.value.address.city;
    form.value.address.state = normalizeState(data.state || form.value.address.state);

    if (!form.value.address.number && data.number) {
        form.value.address.number = data.number;
    }
}

async function lookupZipAndApply({ force = false, silentErrors = false, showSuccess = false } = {}) {
    const zipCode = onlyDigits(form.value.address.zipCode);
    if (zipCode.length !== 8 || zipLookupLoading.value) {
        return;
    }

    if (!force && zipCode === lastZipLookupCode.value) {
        return;
    }

    zipLookupLoading.value = true;
    try {
        const result = await lookupZip(zipCode);
        applyZipLookup(result);
        lastZipLookupCode.value = zipCode;

        if (showSuccess) {
            toast.add({
                severity: 'success',
                summary: 'CEP consultado',
                detail: 'Dados de endereço atualizados automaticamente.',
                life: 3000
            });
        }
    } catch (error) {
        if (!silentErrors) {
            showApiErrorToast(toast, error);
        }
    } finally {
        zipLookupLoading.value = false;
    }
}

function handleManualZipLookup() {
    if (onlyDigits(form.value.address.zipCode).length !== 8) {
        toast.add({
            severity: 'warn',
            summary: 'CEP inválido',
            detail: 'Informe um CEP com 8 dígitos para consultar.',
            life: 3000
        });
        return;
    }

    void lookupZipAndApply({ force: true, showSuccess: true });
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
            detail: 'Imagem do cliente enviada com sucesso.',
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
        name: sanitizeString(form.value.name),
        taxId: normalizeTaxIdInput(form.value.taxId),
        email: sanitizeOptionalString(form.value.email),
        phone: sanitizeOptionalString(normalizePhoneInput(form.value.phone)),
        imageId: sanitizeOptionalString(form.value.imageId),
        address: {
            zipCode: onlyDigits(form.value.address.zipCode),
            street: sanitizeString(form.value.address.street),
            number: sanitizeOptionalString(form.value.address.number),
            complement: sanitizeOptionalString(form.value.address.complement),
            district: sanitizeString(form.value.address.district),
            city: sanitizeString(form.value.address.city),
            state: normalizeState(form.value.address.state)
        }
    };
}

function validatePayload(payload) {
    if (!payload.name) {
        toast.add({
            severity: 'warn',
            summary: 'Nome obrigatório',
            detail: 'Informe o nome do cliente.',
            life: 3000
        });
        return false;
    }

    if (payload.taxId.length !== 11 && payload.taxId.length !== 14) {
        toast.add({
            severity: 'warn',
            summary: 'CPF/CNPJ inválido',
            detail: 'Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).',
            life: 3000
        });
        return false;
    }

    if (payload.email && !isValidEmail(payload.email)) {
        toast.add({
            severity: 'warn',
            summary: 'E-mail inválido',
            detail: 'Informe um e-mail válido para o cliente.',
            life: 3000
        });
        return false;
    }

    if (payload.address.zipCode.length !== 8) {
        toast.add({
            severity: 'warn',
            summary: 'CEP inválido',
            detail: 'Informe um CEP válido com 8 dígitos.',
            life: 3000
        });
        return false;
    }

    if (!payload.address.street || !payload.address.district || !payload.address.city || !payload.address.state) {
        toast.add({
            severity: 'warn',
            summary: 'Endereço incompleto',
            detail: 'Preencha rua, bairro, cidade e estado.',
            life: 3500
        });
        return false;
    }

    if (payload.address.state.length !== 2) {
        toast.add({
            severity: 'warn',
            summary: 'UF inválida',
            detail: 'A UF deve possuir 2 caracteres.',
            life: 3000
        });
        return false;
    }

    return true;
}

function mapCustomerToForm(customer) {
    return {
        id: customer?.id ?? null,
        active: Boolean(customer?.active),
        name: customer?.name ?? '',
        taxId: normalizeTaxIdInput(customer?.taxId ?? ''),
        email: customer?.email ?? '',
        phone: normalizePhoneInput(customer?.phone ?? ''),
        imageId: customer?.imageId ?? null,
        address: {
            zipCode: customer?.address?.zipCode ?? '',
            street: customer?.address?.street ?? '',
            number: customer?.address?.number ?? '',
            complement: customer?.address?.complement ?? '',
            district: customer?.address?.district ?? '',
            city: customer?.address?.city ?? '',
            state: customer?.address?.state ?? ''
        }
    };
}

async function loadCustomerForEdit() {
    if (!isEditMode.value || !customerId.value) {
        return;
    }

    loadingInitial.value = true;
    try {
        const customer = await getCustomerById(customerId.value);
        suppressAutoLookup.value = true;
        form.value = mapCustomerToForm(customer);
        lastZipLookupCode.value = onlyDigits(form.value.address.zipCode);
        await loadImagePreview(form.value.imageId);
    } catch (error) {
        showApiErrorToast(toast, error);
        await router.push('/pages/customers');
    } finally {
        suppressAutoLookup.value = false;
        loadingInitial.value = false;
    }
}

async function handleSubmit() {
    const payload = buildPayload();
    if (!validatePayload(payload)) {
        return;
    }

    submitting.value = true;
    try {
        if (isEditMode.value && customerId.value) {
            await updateCustomerPut(customerId.value, payload);
            toast.add({
                severity: 'success',
                summary: 'Cliente atualizado',
                detail: 'As alterações foram salvas com sucesso.',
                life: 3000
            });
        } else {
            await createCustomer(payload);
            toast.add({
                severity: 'success',
                summary: 'Cliente criado',
                detail: 'Cliente cadastrado com sucesso.',
                life: 3000
            });
        }

        await router.push('/pages/customers');
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        submitting.value = false;
    }
}

function goBack() {
    void router.push('/pages/customers');
}

watch(
    () => form.value.address.zipCode,
    (value) => {
        if (suppressAutoLookup.value) {
            return;
        }

        const zipCode = onlyDigits(value);
        if (zipLookupTimer) {
            clearTimeout(zipLookupTimer);
        }

        if (zipCode.length !== 8 || zipCode === lastZipLookupCode.value) {
            return;
        }

        zipLookupTimer = setTimeout(() => {
            void lookupZipAndApply({ silentErrors: true });
        }, 500);
    }
);

onMounted(async () => {
    await loadCustomerForEdit();
});

onUnmounted(() => {
    if (zipLookupTimer) {
        clearTimeout(zipLookupTimer);
    }
    clearImagePreview();
});
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
            <div>
                <div class="font-semibold text-xl">{{ pageTitle }}</div>
                <div class="text-sm text-muted-color">Preencha os dados do cliente, endereço e imagem.</div>
            </div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-arrow-left" label="Voltar" severity="secondary" outlined @click="goBack" :disabled="submitting || loadingInitial" />
                <Button icon="pi pi-check" :label="submitLabel" @click="handleSubmit" :loading="submitting" :disabled="loadingInitial" />
            </div>
        </div>

        <Message v-if="isEditMode && !form.active" severity="warn" :closable="false" class="mb-4"> Este cliente está inativo. Você pode editar os dados e reativá-lo na listagem. </Message>

        <div v-if="loadingInitial" class="flex justify-center py-10">
            <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
        </div>

        <div v-else class="flex flex-col gap-6">
            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">1. Dados do cliente</div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                        <label for="customer-tax-id" class="block mb-2 font-medium">CPF/CNPJ *</label>
                        <InputText id="customer-tax-id" :modelValue="form.taxId" placeholder="Somente números" maxlength="18" class="w-full" :disabled="submitting" @update:modelValue="handleTaxIdInput" />
                    </div>

                    <div>
                        <label for="customer-name" class="block mb-2 font-medium">Nome *</label>
                        <InputText id="customer-name" v-model="form.name" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-email" class="block mb-2 font-medium">E-mail</label>
                        <InputText id="customer-email" v-model="form.email" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-phone" class="block mb-2 font-medium">Telefone</label>
                        <InputText id="customer-phone" :modelValue="form.phone" maxlength="15" class="w-full" :disabled="submitting" @update:modelValue="handleCustomerPhoneInput" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">2. Endereço</div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="md:col-span-2">
                        <label for="customer-zip-code" class="block mb-2 font-medium">CEP *</label>
                        <div class="flex flex-col sm:flex-row gap-2">
                            <InputText id="customer-zip-code" v-model="form.address.zipCode" placeholder="00000-000" class="w-full" :disabled="submitting" />
                            <Button icon="pi pi-search" label="Consultar CEP" severity="secondary" outlined @click="handleManualZipLookup" :loading="zipLookupLoading" :disabled="submitting" />
                        </div>
                        <small class="text-muted-color">Ao informar 8 dígitos, rua/bairro/cidade/UF são consultados automaticamente.</small>
                    </div>

                    <div class="md:col-span-2">
                        <label for="customer-street" class="block mb-2 font-medium">Rua *</label>
                        <InputText id="customer-street" v-model="form.address.street" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-number" class="block mb-2 font-medium">Número</label>
                        <InputText id="customer-number" v-model="form.address.number" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-complement" class="block mb-2 font-medium">Complemento</label>
                        <InputText id="customer-complement" v-model="form.address.complement" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-district" class="block mb-2 font-medium">Bairro *</label>
                        <InputText id="customer-district" v-model="form.address.district" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-city" class="block mb-2 font-medium">Cidade *</label>
                        <InputText id="customer-city" v-model="form.address.city" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="customer-state" class="block mb-2 font-medium">UF *</label>
                        <InputText id="customer-state" v-model="form.address.state" maxlength="2" class="w-full" :disabled="submitting" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">3. Imagem do cliente</div>

                <div class="flex flex-col md:flex-row gap-4 md:items-start">
                    <div class="w-32 h-32 rounded-lg border border-surface-200 dark:border-surface-700 flex items-center justify-center bg-surface-0 dark:bg-surface-900 overflow-hidden">
                        <ProgressSpinner v-if="imagePreviewLoading || imageUploadLoading" style="width: 1.8rem; height: 1.8rem" strokeWidth="6" />
                        <img v-else-if="imagePreviewUrl" :src="imagePreviewUrl" alt="Pré-visualização da imagem do cliente" class="w-full h-full object-contain" />
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
