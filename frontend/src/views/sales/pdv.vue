<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { createCashMovement, createPdvSale, getCurrentCashRegister, listRecentPdvSales, lookupPdvProduct, openCashRegister } from '@/services/pdvService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loading = ref(false);
const finalizingSale = ref(false);
const cashDialogVisible = ref(false);
const movementDialogVisible = ref(false);
const movementType = ref('SUPPLY');

const codeInputRef = ref(null);
const paidAmountInputRef = ref(null);
const selectedCartItem = ref(null);
const productCode = ref('');
const discountAmount = ref(0);
const paymentMethod = ref('CASH');
const paidAmount = ref(0);
const notes = ref('');

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

const subtotalAmount = computed(() => cartItems.value.reduce((sum, item) => sum + Number(item.lineTotal || 0), 0));

const totalAmount = computed(() => Math.max(0, subtotalAmount.value - Number(discountAmount.value || 0)));
const changeAmount = computed(() => (paymentMethod.value === 'CASH' ? Math.max(0, Number(paidAmount.value || 0) - totalAmount.value) : 0));

const isCashOpen = computed(() => Boolean(currentCash.value?.sessionId));

function normalizeMoney(value) {
    const parsed = Number(value);
    if (!Number.isFinite(parsed)) {
        return 0;
    }
    return Number(parsed.toFixed(2));
}

function normalizeQuantity(value) {
    const parsed = Number(value);
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

function resetSale() {
    cartItems.value = [];
    selectedCartItem.value = null;
    productCode.value = '';
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
    const code = productCode.value?.trim();
    if (!code) {
        return;
    }

    if (!isCashOpen.value) {
        toast.add({
            severity: 'warn',
            summary: 'Caixa fechado',
            detail: 'Abra o caixa antes de iniciar as vendas.',
            life: 3000
        });
        return;
    }

    try {
        const product = await lookupPdvProduct(code);
        const existing = cartItems.value.find((item) => item.productId === product.id);

        if (existing) {
            updateCartLine(existing, existing.quantity + 1);
            selectedCartItem.value = existing;
        } else {
            const line = {
                productId: product.id,
                sku: product.sku,
                barcode: product.barcode,
                name: product.name,
                unit: product.unit,
                unitPrice: normalizeMoney(product.unitPrice),
                quantity: 1,
                lineTotal: normalizeMoney(product.unitPrice)
            };
            cartItems.value.push(line);
            selectedCartItem.value = line;
        }

        productCode.value = '';
        focusCodeInput();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
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
            paidAmount: paymentMethod.value === 'CASH' ? normalizeMoney(paidAmount.value) : normalizeMoney(totalAmount.value),
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
        focusPaidAmount();
        return;
    }

    if (event.key === 'F8') {
        event.preventDefault();
        if (!finalizingSale.value) {
            void handleFinalizeSale();
        }
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
    window.removeEventListener('keydown', handleShortcut);
});
</script>

<template>
    <div class="pdv-screen">
        <Toast />

        <section class="pdv-topbar">
            <div class="pdv-title">
                <h1>Frente de Caixa</h1>
                <span>F1 código • F2 +1 item • F3 remover • F4 pagamento • F8 finalizar</span>
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
            <InputText ref="codeInputRef" v-model="productCode" class="w-full" placeholder="Leia o código de barras ou SKU (F1)" @keyup.enter="handleAddProductByCode" />
            <Button icon="pi pi-plus" label="Adicionar item" :disabled="!isCashOpen" @click="handleAddProductByCode" />
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

                    <div class="pdv-form-grid">
                        <div>
                            <label class="block mb-2">Desconto</label>
                            <InputNumber v-model="discountAmount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" />
                        </div>
                        <div>
                            <label class="block mb-2">Pagamento</label>
                            <Select v-model="paymentMethod" :options="paymentOptions" optionLabel="label" optionValue="value" class="w-full" />
                        </div>
                        <div>
                            <label class="block mb-2">Valor recebido (F4)</label>
                            <InputNumber ref="paidAmountInputRef" v-model="paidAmount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" :disabled="paymentMethod !== 'CASH'" />
                        </div>
                        <div>
                            <label class="block mb-2">Observação</label>
                            <InputText v-model="notes" class="w-full" />
                        </div>
                    </div>

                    <Button icon="pi pi-check-circle" label="Finalizar venda (F8)" class="w-full mt-2" :disabled="!isCashOpen || finalizingSale" :loading="finalizingSale" @click="handleFinalizeSale" />
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

        <Dialog v-model:visible="cashDialogVisible" modal header="Abertura de caixa" :style="{ width: '24rem' }">
            <div class="flex flex-column gap-3">
                <div>
                    <label class="block mb-2">Suprimento inicial</label>
                    <InputNumber v-model="cashForm.openingAmount" mode="currency" currency="BRL" locale="pt-BR" class="w-full" />
                </div>
                <Button icon="pi pi-check" label="Confirmar abertura" class="w-full" @click="handleOpenCash" />
            </div>
        </Dialog>

        <Dialog v-model:visible="movementDialogVisible" modal :header="movementType === 'SUPPLY' ? 'Lançar suprimento' : 'Lançar sangria'" :style="{ width: '24rem' }">
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
}
</style>
