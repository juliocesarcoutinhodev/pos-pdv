<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { useAuth } from '@/composables/useAuth.js';
import { useErrorHandler } from '@/services/errorHandler.js';
import { listProducts } from '@/services/productService.js';
import { createCashMovement, createPdvSale, getCurrentCashRegister, listRecentPdvSales, lookupPdvProduct, openCashRegister } from '@/services/pdvService.js';

const confirm = useConfirm();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();
const { logout } = useAuth();

const loading = ref(false);
const finalizingSale = ref(false);
const cashDialogVisible = ref(false);
const movementDialogVisible = ref(false);
const paymentDialogVisible = ref(false);
const movementType = ref('SUPPLY');
const productLookupLoading = ref(false);

const codeInputRef = ref(null);
const paidAmountInputRef = ref(null);
const selectedCartItem = ref(null);
const productCode = ref('');
const productSuggestions = ref([]);
const pendingQuantityMultiplier = ref(null);
const discountAmount = ref(0);
const paymentMethod = ref('CASH');
const paidAmount = ref(0);
const notes = ref('');
let productLookupTimer = null;

const PRODUCT_LOOKUP_DEBOUNCE_MS = 250;

const cashForm = ref({
    openingAmount: 0
});

const movementForm = ref({
    amount: 0,
    note: ''
});

const currentCash = ref(null);
const recentSales = ref([]);
const cartItems = ref([]);

const paymentOptions = [
    { label: 'Dinheiro', value: 'CASH' },
    { label: 'PIX', value: 'PIX' },
    { label: 'Cartão Débito', value: 'DEBIT_CARD' },
    { label: 'Cartão Crédito', value: 'CREDIT_CARD' },
    { label: 'Outro', value: 'OTHER' }
];

function toNumericValue(value) {
    if (typeof value === 'number') {
        return Number.isFinite(value) ? value : 0;
    }

    if (typeof value === 'string') {
        const trimmed = value.trim();
        if (!trimmed) {
            return 0;
        }

        const cleaned = trimmed.replace(/[^\d,.-]/g, '');
        if (!cleaned) {
            return 0;
        }

        let normalized = cleaned;
        const lastComma = normalized.lastIndexOf(',');
        const lastDot = normalized.lastIndexOf('.');

        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                normalized = normalized.replace(/\./g, '').replace(',', '.');
            } else {
                normalized = normalized.replace(/,/g, '');
            }
        } else if (lastComma >= 0) {
            normalized = normalized.replace(',', '.');
        }

        const parsed = Number(normalized);
        return Number.isFinite(parsed) ? parsed : 0;
    }

    return 0;
}

const subtotalAmount = computed(() => cartItems.value.reduce((sum, item) => sum + toNumericValue(item.lineTotal), 0));

const totalAmount = computed(() => Math.max(0, subtotalAmount.value - toNumericValue(discountAmount.value)));
const changeAmount = computed(() => (paymentMethod.value === 'CASH' ? Math.max(0, toNumericValue(paidAmount.value) - totalAmount.value) : 0));

const isCashOpen = computed(() => Boolean(currentCash.value?.sessionId));

function normalizeMoney(value) {
    return Number(toNumericValue(value).toFixed(2));
}

function normalizeQuantity(value) {
    const parsed = toNumericValue(value);
    if (!Number.isFinite(parsed) || parsed <= 0) {
        return 1;
    }
    return Number(parsed.toFixed(3));
}

function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
}

function paymentMethodLabel(value) {
    const option = paymentOptions.find((item) => item.value === value);
    return option?.label ?? value;
}

function clearProductLookupTimer() {
    if (productLookupTimer) {
        clearTimeout(productLookupTimer);
        productLookupTimer = null;
    }
}

function normalizeLookupText(value) {
    if (value && typeof value === 'object') {
        return String(value?.name ?? value?.sku ?? value?.barcode ?? '').trim();
    }
    return String(value ?? '').trim();
}

function parseQuantityValue(value) {
    const normalized = String(value ?? '').replace(',', '.');
    const parsed = Number(normalized);
    if (!Number.isFinite(parsed) || parsed <= 0) {
        return null;
    }
    return normalizeQuantity(parsed);
}

function parseQuantityCommand(value) {
    const input = normalizeLookupText(value).replace(/\s+/g, '');
    if (!input) {
        return null;
    }

    const armOnlyMatch = input.match(/^(\d+(?:[.,]\d{1,3})?)x$/i);
    if (armOnlyMatch) {
        const quantity = parseQuantityValue(armOnlyMatch[1]);
        return quantity ? { quantity, code: null, armOnly: true } : null;
    }

    const inlineMatch = input.match(/^(\d+(?:[.,]\d{1,3})?)x(.+)$/i);
    if (inlineMatch) {
        const quantity = parseQuantityValue(inlineMatch[1]);
        const code = inlineMatch[2]?.trim();
        if (!quantity || !code) {
            return null;
        }
        return { quantity, code, armOnly: false };
    }

    return null;
}

function resetSale() {
    cartItems.value = [];
    selectedCartItem.value = null;
    productCode.value = '';
    productSuggestions.value = [];
    pendingQuantityMultiplier.value = null;
    discountAmount.value = 0;
    paymentMethod.value = 'CASH';
    paidAmount.value = 0;
    notes.value = '';
}

function updateCartLine(item, quantity) {
    const normalized = normalizeQuantity(quantity);
    item.quantity = normalized;
    item.lineTotal = normalizeMoney(item.unitPrice * normalized);
}

function focusCodeInput() {
    const inputElement = codeInputRef.value?.$el?.querySelector('input');
    inputElement?.focus();
    inputElement?.select();
}

function focusPaidAmount() {
    const inputElement = paidAmountInputRef.value?.$el?.querySelector('input');
    inputElement?.focus();
    inputElement?.select();
}

function resolvePaidAmountForSale() {
    const modelValue = toNumericValue(paidAmount.value);
    if (modelValue > 0) {
        return modelValue;
    }

    const rawInputValue = paidAmountInputRef.value?.$el?.querySelector('input')?.value;
    return toNumericValue(rawInputValue);
}

function ensureCashOpen() {
    if (isCashOpen.value) {
        return true;
    }

    toast.add({
        severity: 'warn',
        summary: 'Caixa fechado',
        detail: 'Abra o caixa antes de iniciar as vendas.',
        life: 3000
    });
    return false;
}

function upsertProductInCart(product, quantityToAdd = 1) {
    const incrementQuantity = normalizeQuantity(quantityToAdd);
    const existing = cartItems.value.find((item) => item.productId === product.id);

    if (existing) {
        updateCartLine(existing, existing.quantity + incrementQuantity);
        selectedCartItem.value = existing;
        return;
    }

    const unitPrice = normalizeMoney(product.unitPrice ?? product.salePrice);
    const line = {
        productId: product.id,
        sku: product.sku,
        barcode: product.barcode,
        name: product.name,
        unit: product.unit,
        unitPrice,
        quantity: incrementQuantity,
        lineTotal: normalizeMoney(unitPrice * incrementQuantity)
    };
    cartItems.value.push(line);
    selectedCartItem.value = line;
}

async function loadProductSuggestions(query) {
    const normalizedQuery = String(query ?? '').trim();
    if (normalizedQuery.length < 2) {
        productSuggestions.value = [];
        return;
    }

    productLookupLoading.value = true;
    try {
        const result = await listProducts({
            page: 0,
            size: 10,
            active: true,
            name: normalizedQuery,
            sortBy: 'name',
            sortDirection: 'asc'
        });
        productSuggestions.value = result.content.filter((product) => product.id);
    } catch {
        productSuggestions.value = [];
    } finally {
        productLookupLoading.value = false;
    }
}

function handleProductLookup(event) {
    clearProductLookupTimer();
    const query = String(event?.query ?? '').trim();
    productLookupTimer = setTimeout(() => {
        void loadProductSuggestions(query);
    }, PRODUCT_LOOKUP_DEBOUNCE_MS);
}

async function handleProductDropdownClick(event) {
    clearProductLookupTimer();
    const query = String(event?.query ?? '').trim();
    await loadProductSuggestions(query);
}

function clearProductLookupInput() {
    productCode.value = '';
    productSuggestions.value = [];
}

async function resolveProductFromInput(inputValue) {
    if (inputValue && typeof inputValue === 'object' && inputValue.id) {
        return inputValue;
    }

    const code = normalizeLookupText(inputValue);
    if (!code) {
        return null;
    }

    try {
        return await lookupPdvProduct(code);
    } catch (lookupError) {
        const result = await listProducts({
            page: 0,
            size: 1,
            active: true,
            name: code,
            sortBy: 'name',
            sortDirection: 'asc'
        });
        if (result.content.length > 0) {
            return result.content[0];
        }
        throw lookupError;
    }
}

function confirmExitSystem() {
    confirm.require({
        header: 'Sair do sistema',
        message: 'Deseja sair do sistema? O caixa continuará aberto.',
        icon: 'pi pi-exclamation-triangle',
        rejectLabel: 'Cancelar',
        acceptLabel: 'Sair',
        accept: () => {
            void logout();
        }
    });
}

function closeTopmostDialog() {
    if (paymentDialogVisible.value) {
        paymentDialogVisible.value = false;
        return true;
    }

    if (movementDialogVisible.value) {
        movementDialogVisible.value = false;
        return true;
    }

    if (cashDialogVisible.value) {
        cashDialogVisible.value = false;
        return true;
    }

    return false;
}

async function openPaymentDialog() {
    if (!ensureCashOpen()) {
        return;
    }

    if (!cartItems.value.length) {
        toast.add({
            severity: 'warn',
            summary: 'Carrinho vazio',
            detail: 'Adicione itens para abrir a finalização.',
            life: 3000
        });
        return;
    }

    paymentDialogVisible.value = true;
    if (paymentMethod.value === 'CASH') {
        await nextTick();
        focusPaidAmount();
    }
}

async function loadCurrentCash() {
    try {
        currentCash.value = await getCurrentCashRegister();
    } catch (error) {
        if (error?.response?.status === 404) {
            currentCash.value = null;
            return;
        }
        throw error;
    }
}

async function loadRecentSales() {
    if (!isCashOpen.value) {
        recentSales.value = [];
        return;
    }
    recentSales.value = await listRecentPdvSales(10);
}

async function refreshPdvState() {
    loading.value = true;
    try {
        await loadCurrentCash();
        await loadRecentSales();
    } finally {
        loading.value = false;
    }
}

async function handleOpenCash() {
    try {
        await openCashRegister({
            openingAmount: normalizeMoney(cashForm.value.openingAmount)
        });
        cashDialogVisible.value = false;
        cashForm.value.openingAmount = 0;
        await refreshPdvState();
        toast.add({
            severity: 'success',
            summary: 'Caixa aberto',
            detail: 'Caixa iniciado com sucesso.',
            life: 3000
        });
        focusCodeInput();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

function openMovementDialog(type) {
    movementType.value = type;
    movementForm.value = { amount: 0, note: '' };
    movementDialogVisible.value = true;
}

async function handleCashMovement() {
    try {
        await createCashMovement({
            type: movementType.value,
            amount: normalizeMoney(movementForm.value.amount),
            note: movementForm.value.note
        });
        movementDialogVisible.value = false;
        await refreshPdvState();
        toast.add({
            severity: 'success',
            summary: movementType.value === 'SUPPLY' ? 'Suprimento lançado' : 'Sangria lançada',
            detail: 'Movimentação registrada com sucesso.',
            life: 3000
        });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleAddProductByCode() {
    const normalizedInput = normalizeLookupText(productCode.value);
    if (!normalizedInput) {
        return;
    }

    const quantityCommand = parseQuantityCommand(normalizedInput);
    if (quantityCommand?.armOnly) {
        pendingQuantityMultiplier.value = quantityCommand.quantity;
        clearProductLookupInput();
        toast.add({
            severity: 'info',
            summary: 'Multiplicador aplicado',
            detail: `Próximo item será adicionado com quantidade ${quantityCommand.quantity}.`,
            life: 2500
        });
        focusCodeInput();
        return;
    }

    if (!ensureCashOpen()) {
        return;
    }

    try {
        const inputForLookup = quantityCommand?.code ?? productCode.value;
        const product = await resolveProductFromInput(inputForLookup);
        if (!product) {
            return;
        }
        const quantityToAdd = quantityCommand?.quantity ?? pendingQuantityMultiplier.value ?? 1;
        upsertProductInCart(product, quantityToAdd);
        pendingQuantityMultiplier.value = null;
        clearProductLookupInput();
        focusCodeInput();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleProductSuggestionSelect(event) {
    if (!ensureCashOpen()) {
        return;
    }

    const selectedProduct = event?.value;
    if (!selectedProduct?.id) {
        return;
    }

    const quantityToAdd = pendingQuantityMultiplier.value ?? 1;
    upsertProductInCart(selectedProduct, quantityToAdd);
    pendingQuantityMultiplier.value = null;
    clearProductLookupInput();
    focusCodeInput();
}

function removeSelectedItem() {
    if (!selectedCartItem.value) {
        return;
    }
    cartItems.value = cartItems.value.filter((item) => item !== selectedCartItem.value);
    selectedCartItem.value = null;
}

async function handleFinalizeSale() {
    if (!cartItems.value.length) {
        toast.add({
            severity: 'warn',
            summary: 'Carrinho vazio',
            detail: 'Adicione itens para finalizar a venda.',
            life: 3000
        });
        return;
    }

    finalizingSale.value = true;
    try {
        const payload = {
            paymentMethod: paymentMethod.value,
            discountAmount: normalizeMoney(discountAmount.value),
            paidAmount: paymentMethod.value === 'CASH' ? normalizeMoney(resolvePaidAmountForSale()) : normalizeMoney(totalAmount.value),
            notes: notes.value,
            items: cartItems.value.map((item) => ({
                productId: item.productId,
                quantity: normalizeQuantity(item.quantity)
            }))
        };

        await createPdvSale(payload);
        toast.add({
            severity: 'success',
            summary: 'Venda finalizada',
            detail: 'Venda registrada com sucesso.',
            life: 3000
        });
        paymentDialogVisible.value = false;
        resetSale();
        await refreshPdvState();
        focusCodeInput();
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        finalizingSale.value = false;
    }
}

function handleShortcut(event) {
    if (event.key === 'F1') {
        event.preventDefault();
        focusCodeInput();
        return;
    }

    if (event.key === 'F2') {
        event.preventDefault();
        if (selectedCartItem.value) {
            updateCartLine(selectedCartItem.value, selectedCartItem.value.quantity + 1);
        }
        return;
    }

    if (event.key === 'F3') {
        event.preventDefault();
        removeSelectedItem();
        return;
    }

    if (event.key === 'F4') {
        event.preventDefault();
        void openPaymentDialog();
        return;
    }

    if (event.key === 'F8') {
        event.preventDefault();
        if (!paymentDialogVisible.value) {
            void openPaymentDialog();
            return;
        }

        if (!finalizingSale.value) {
            void handleFinalizeSale();
        }
        return;
    }

    if (event.key === 'Escape') {
        event.preventDefault();
        if (closeTopmostDialog()) {
            return;
        }
        confirmExitSystem();
    }
}

onMounted(async () => {
    window.addEventListener('keydown', handleShortcut);
    try {
        await refreshPdvState();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
    focusCodeInput();
});

onUnmounted(() => {
    clearProductLookupTimer();
    window.removeEventListener('keydown', handleShortcut);
});
</script>

<template>
    <div class="pdv-screen">
        <Toast />
        <ConfirmDialog />

        <section class="pdv-topbar">
            <div class="pdv-title">
                <h1>Frente de Caixa</h1>
                <span>F1 código • F2 +1 item • F3 remover • F4 pagamento • F8 abrir/confirmar • Esc sair • 2x multiplicador</span>
            </div>

            <div class="pdv-topbar-actions">
                <Tag :severity="isCashOpen ? 'success' : 'danger'" :value="isCashOpen ? 'Caixa aberto' : 'Caixa fechado'" />
                <div v-if="isCashOpen" class="pdv-balance">
                    Saldo em caixa
                    <strong>{{ formatCurrency(currentCash.cashBalance) }}</strong>
                </div>
                <div class="pdv-actions-group">
                    <Button icon="pi pi-lock-open" label="Abrir caixa" severity="success" @click="cashDialogVisible = true" />
                    <Button icon="pi pi-plus-circle" label="Suprimento" severity="info" outlined :disabled="!isCashOpen" @click="openMovementDialog('SUPPLY')" />
                    <Button icon="pi pi-minus-circle" label="Sangria" severity="warn" outlined :disabled="!isCashOpen" @click="openMovementDialog('WITHDRAWAL')" />
                </div>
            </div>
        </section>

        <section class="pdv-scanner">
            <AutoComplete
                ref="codeInputRef"
                v-model="productCode"
                :suggestions="productSuggestions"
                optionLabel="name"
                dropdown
                dropdownMode="current"
                class="w-full"
                placeholder="Leia código/SKU, digite nome ou use 2x para multiplicar"
                :loading="productLookupLoading"
                @complete="handleProductLookup"
                @dropdown-click="handleProductDropdownClick"
                @item-select="handleProductSuggestionSelect"
                @keyup.enter="handleAddProductByCode"
            >
                <template #option="slotProps">
                    <div class="flex flex-column">
                        <span class="font-medium">{{ slotProps.option.name }}</span>
                        <small class="text-500">SKU: {{ slotProps.option.sku || '—' }} • Código: {{ slotProps.option.barcode || '—' }}</small>
                    </div>
                </template>
            </AutoComplete>
            <Button icon="pi pi-plus" label="Adicionar item" :disabled="!isCashOpen" @click="handleAddProductByCode" />

            <div v-if="pendingQuantityMultiplier" class="pdv-multiplier-badge">Multiplicador ativo: {{ pendingQuantityMultiplier }}x</div>
        </section>

        <section class="pdv-body">
            <article class="pdv-cart-panel">
                <div class="pdv-panel-title">
                    <strong>Itens da venda</strong>
                    <Button icon="pi pi-trash" label="Remover item (F3)" severity="danger" text :disabled="!selectedCartItem" @click="removeSelectedItem" />
                </div>

                <DataTable v-model:selection="selectedCartItem" :value="cartItems" selectionMode="single" dataKey="productId" class="p-datatable-sm pdv-cart-table" scrollable scrollHeight="flex" :loading="loading">
                    <Column field="sku" header="SKU" style="min-width: 8rem" />
                    <Column field="name" header="Produto" style="min-width: 16rem" />
                    <Column field="unitPrice" header="Preço">
                        <template #body="{ data }">
                            {{ formatCurrency(data.unitPrice) }}
                        </template>
                    </Column>
                    <Column field="quantity" header="Qtd" style="width: 8rem">
                        <template #body="{ data }">
                            <InputNumber :modelValue="data.quantity" inputClass="w-full text-center" :min="0.001" :step="1" :minFractionDigits="3" :maxFractionDigits="3" @update:modelValue="(value) => updateCartLine(data, value)" />
                        </template>
                    </Column>
                    <Column field="lineTotal" header="Total">
                        <template #body="{ data }">
                            {{ formatCurrency(data.lineTotal) }}
                        </template>
                    </Column>
                </DataTable>
            </article>

            <aside class="pdv-checkout-panel">
                <div class="pdv-checkout-card">
                    <div class="pdv-panel-title">
                        <strong>Fechamento</strong>
                    </div>
                    <div class="pdv-summary-line">
                        <span>Subtotal</span>
                        <strong>{{ formatCurrency(subtotalAmount) }}</strong>
                    </div>
                    <div class="pdv-summary-line total">
                        <span>Total</span>
                        <strong>{{ formatCurrency(totalAmount) }}</strong>
                    </div>
                    <div class="pdv-summary-line">
                        <span>Troco</span>
                        <strong>{{ formatCurrency(changeAmount) }}</strong>
                    </div>
                    <Button icon="pi pi-wallet" label="Pagamento (F4/F8)" class="w-full mt-2" :disabled="!isCashOpen" @click="openPaymentDialog" />
                </div>

                <div class="pdv-checkout-card recent">
                    <div class="pdv-panel-title">
                        <strong>Últimas vendas</strong>
                    </div>
                    <div v-if="!recentSales.length" class="text-600 text-sm">Nenhuma venda registrada no caixa atual.</div>
                    <div v-else class="recent-sales-list">
                        <div v-for="sale in recentSales" :key="sale.id" class="recent-sale-item">
                            <div class="flex justify-content-between align-items-center">
                                <span class="text-sm text-600">{{ new Date(sale.createdAt).toLocaleTimeString('pt-BR') }}</span>
                                <strong>{{ formatCurrency(sale.totalAmount) }}</strong>
                            </div>
                            <div class="text-xs text-500">{{ paymentMethodLabel(sale.paymentMethod) }} • {{ sale.items.length }} item(ns)</div>
                        </div>
                    </div>
                </div>
            </aside>
        </section>

        <Dialog v-model:visible="cashDialogVisible" modal :closeOnEscape="false" header="Abertura de caixa" :style="{ width: '24rem' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block mb-2">Suprimento inicial</label>
                    <InputNumber v-model="cashForm.openingAmount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" />
                </div>
                <Button icon="pi pi-check" label="Confirmar abertura" class="w-full" @click="handleOpenCash" />
            </div>
        </Dialog>

        <Dialog v-model:visible="movementDialogVisible" modal :closeOnEscape="false" :header="movementType === 'SUPPLY' ? 'Lançar suprimento' : 'Lançar sangria'" :style="{ width: '24rem' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block mb-2">Valor</label>
                    <InputNumber v-model="movementForm.amount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" />
                </div>
                <div>
                    <label class="block mb-2">Observação</label>
                    <InputText v-model="movementForm.note" class="w-full" />
                </div>
                <Button icon="pi pi-check" label="Confirmar movimentação" class="w-full" @click="handleCashMovement" />
            </div>
        </Dialog>

        <Dialog v-model:visible="paymentDialogVisible" modal :closeOnEscape="false" header="Finalização da venda" :style="{ width: '42rem' }" :breakpoints="{ '960px': '90vw', '640px': '95vw' }">
            <div class="payment-dialog">
                <div class="payment-totals">
                    <div class="payment-total-card">
                        <span>Subtotal</span>
                        <strong>{{ formatCurrency(subtotalAmount) }}</strong>
                    </div>
                    <div class="payment-total-card highlight">
                        <span>Total</span>
                        <strong>{{ formatCurrency(totalAmount) }}</strong>
                    </div>
                    <div class="payment-total-card">
                        <span>Troco</span>
                        <strong>{{ formatCurrency(changeAmount) }}</strong>
                    </div>
                </div>

                <div class="payment-fields">
                    <div class="payment-field">
                        <label class="block mb-2">Desconto</label>
                        <InputNumber v-model="discountAmount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" />
                    </div>
                    <div class="payment-field">
                        <label class="block mb-2">Pagamento</label>
                        <Select v-model="paymentMethod" :options="paymentOptions" optionLabel="label" optionValue="value" class="w-full" />
                    </div>
                    <div class="payment-field">
                        <label class="block mb-2">Valor recebido (F4)</label>
                        <InputNumber ref="paidAmountInputRef" v-model="paidAmount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" :disabled="paymentMethod !== 'CASH'" />
                    </div>
                    <div class="payment-field">
                        <label class="block mb-2">Observação</label>
                        <InputText v-model="notes" class="w-full" />
                    </div>
                </div>

                <div class="payment-actions">
                    <Button label="Cancelar" severity="secondary" outlined @click="paymentDialogVisible = false" />
                    <Button icon="pi pi-check-circle" label="Finalizar venda (F8)" :loading="finalizingSale" @click="handleFinalizeSale" />
                </div>
            </div>
        </Dialog>
    </div>
</template>

<style scoped>
.pdv-screen {
    min-height: 100dvh;
    padding: 1rem;
    background: var(--surface-ground);
    display: grid;
    grid-template-rows: auto auto 1fr;
    gap: 0.75rem;
    overflow: hidden;
}

.pdv-topbar,
.pdv-scanner,
.pdv-cart-panel,
.pdv-checkout-card {
    background: var(--surface-card);
    border: 1px solid var(--surface-border);
    border-radius: 14px;
}

.pdv-topbar {
    padding: 0.9rem 1rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;
}

.pdv-title h1 {
    margin: 0;
    font-size: 1.25rem;
}

.pdv-title span {
    color: var(--text-color-secondary);
    font-size: 0.85rem;
}

.pdv-topbar-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.pdv-balance {
    display: flex;
    flex-direction: column;
    gap: 0.1rem;
    font-size: 0.78rem;
    color: var(--text-color-secondary);
}

.pdv-balance strong {
    font-size: 1rem;
    color: var(--text-color);
}

.pdv-actions-group {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
}

.pdv-scanner {
    padding: 0.75rem;
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 0.65rem;
    align-items: center;
}

.pdv-body {
    min-height: 0;
    display: grid;
    grid-template-columns: minmax(0, 2.2fr) minmax(320px, 1fr);
    gap: 0.75rem;
}

.pdv-multiplier-badge {
    grid-column: 1 / -1;
    justify-self: start;
    margin-top: -0.25rem;
    font-size: 0.82rem;
    color: var(--primary-color);
    background: color-mix(in srgb, var(--primary-color) 14%, transparent);
    border: 1px solid color-mix(in srgb, var(--primary-color) 40%, transparent);
    border-radius: 999px;
    padding: 0.25rem 0.65rem;
}

.pdv-cart-panel {
    min-height: 0;
    padding: 0.75rem;
    display: grid;
    grid-template-rows: auto 1fr;
    gap: 0.65rem;
}

.pdv-panel-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
}

.pdv-cart-table {
    min-height: 0;
}

:deep(.pdv-cart-table .p-datatable-table-container) {
    max-height: calc(100dvh - 255px);
}

.pdv-checkout-panel {
    min-height: 0;
    display: grid;
    grid-template-rows: auto 1fr;
    gap: 0.75rem;
}

.pdv-checkout-card {
    padding: 0.85rem;
}

.pdv-summary-line {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.45rem;
}

.pdv-summary-line.total {
    margin-top: 0.3rem;
    font-size: 1.12rem;
    color: var(--primary-color);
}

.pdv-form-grid {
    display: grid;
    gap: 0.55rem;
}

.payment-dialog {
    display: grid;
    gap: 1rem;
}

.payment-totals {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0.6rem;
}

.payment-total-card {
    border: 1px solid var(--surface-border);
    border-radius: 12px;
    background: var(--surface-ground);
    padding: 0.65rem 0.75rem;
    display: flex;
    flex-direction: column;
    gap: 0.2rem;
}

.payment-total-card span {
    font-size: 0.78rem;
    color: var(--text-color-secondary);
}

.payment-total-card strong {
    font-size: 1rem;
}

.payment-total-card.highlight {
    border-color: color-mix(in srgb, var(--primary-color) 45%, var(--surface-border));
    background: color-mix(in srgb, var(--primary-color) 10%, var(--surface-card));
}

.payment-total-card.highlight strong {
    color: var(--primary-color);
    font-size: 1.1rem;
}

.payment-fields {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 0.8rem;
}

.payment-field {
    min-width: 0;
}

.payment-actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    flex-wrap: wrap;
}

:deep(.payment-dialog .p-inputnumber),
:deep(.payment-dialog .p-select) {
    width: 100%;
}

.pdv-checkout-card.recent {
    min-height: 0;
    display: grid;
    grid-template-rows: auto 1fr;
}

.recent-sales-list {
    min-height: 0;
    overflow: auto;
    display: grid;
    gap: 0.45rem;
}

.recent-sale-item {
    border: 1px solid var(--surface-border);
    border-radius: 10px;
    padding: 0.5rem;
}

@media (max-width: 1200px) {
    .pdv-screen {
        overflow: auto;
    }

    .pdv-body {
        grid-template-columns: 1fr;
    }

    :deep(.pdv-cart-table .p-datatable-table-container) {
        max-height: 48vh;
    }
}

@media (max-width: 768px) {
    .pdv-topbar {
        flex-direction: column;
        align-items: flex-start;
    }

    .pdv-topbar-actions {
        justify-content: flex-start;
    }

    .pdv-scanner {
        grid-template-columns: 1fr;
    }

    .payment-totals,
    .payment-fields {
        grid-template-columns: 1fr;
    }

    .payment-actions {
        justify-content: stretch;
    }

    .payment-actions :deep(.p-button) {
        width: 100%;
    }
}
</style>
