<script setup>
import { onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { getOpenCashRegisterMonitoringSummary, listOpenCashRegistersForMonitoring } from '@/services/pdvService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loading = ref(false);
const cashRegisters = ref([]);

const summaryLoading = ref(false);
const summaryDialogVisible = ref(false);
const selectedSummary = ref(null);

function todayString() {
    return new Date().toISOString().slice(0, 10);
}

const dateFrom = ref(todayString());
const dateTo = ref(todayString());

const paymentMethodLabels = {
    CASH: 'Dinheiro',
    PIX: 'PIX',
    DEBIT_CARD: 'Cartão Débito',
    CREDIT_CARD: 'Cartão Crédito',
    OTHER: 'Outros'
};

function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
}

function formatDateTime(value) {
    if (!value) return '-';
    return new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short',
        timeStyle: 'short'
    }).format(new Date(value));
}

function paymentMethodLabel(value) {
    return paymentMethodLabels[value] ?? value;
}

async function loadCashRegisters() {
    loading.value = true;
    try {
        cashRegisters.value = await listOpenCashRegistersForMonitoring({
            dateFrom: dateFrom.value,
            dateTo: dateTo.value
        });
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        loading.value = false;
    }
}

function statusLabel(status) {
    return status === 'CLOSED' ? 'Fechado' : 'Aberto';
}

function statusSeverity(status) {
    return status === 'CLOSED' ? 'contrast' : 'success';
}

async function openSummary(sessionId) {
    summaryLoading.value = true;
    summaryDialogVisible.value = true;
    selectedSummary.value = null;
    try {
        selectedSummary.value = await getOpenCashRegisterMonitoringSummary(sessionId);
    } catch (error) {
        summaryDialogVisible.value = false;
        showApiErrorToast(toast, error);
    } finally {
        summaryLoading.value = false;
    }
}

onMounted(() => {
    loadCashRegisters();
});
</script>

<template>
    <div class="card">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
            <div class="font-semibold text-xl">Monitoramento dos Caixas</div>
            <div class="flex flex-wrap items-center gap-2">
                <div class="flex items-center gap-2">
                    <label class="text-sm text-muted-color whitespace-nowrap">De</label>
                    <InputText
                        v-model="dateFrom"
                        type="date"
                        size="small"
                        :max="dateTo || undefined"
                        style="width: 8rem"
                    />
                </div>
                <div class="flex items-center gap-2">
                    <label class="text-sm text-muted-color whitespace-nowrap">Até</label>
                    <InputText
                        v-model="dateTo"
                        type="date"
                        size="small"
                        :min="dateFrom || undefined"
                        style="width: 8rem"
                    />
                </div>
                <Button icon="pi pi-search" label="Filtrar" size="small" :loading="loading" @click="loadCashRegisters" />
                <Button icon="pi pi-refresh" outlined size="small" :loading="loading" @click="loadCashRegisters" />
            </div>
        </div>

        <ProgressSpinner v-if="loading" style="width: 2rem; height: 2rem" strokeWidth="6" class="block mx-auto my-8" />

        <Message v-else-if="cashRegisters.length === 0" severity="info" :closable="false">Nenhum caixa encontrado para o período selecionado.</Message>

        <div v-else class="grid grid-cols-12 gap-4">
            <div v-for="cashRegister in cashRegisters" :key="cashRegister.sessionId" class="col-span-12 md:col-span-6 xl:col-span-4">
                <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full cursor-pointer hover:border-primary transition-colors" @click="openSummary(cashRegister.sessionId)">
                    <div class="flex items-start justify-between mb-3 gap-3">
                        <div>
                            <div class="text-sm text-muted-color">Caixa</div>
                            <div class="text-lg font-semibold">{{ cashRegister.userName }}</div>
                        </div>
                        <Tag :value="statusLabel(cashRegister.status)" :severity="statusSeverity(cashRegister.status)" />
                    </div>

                    <div class="space-y-2 text-sm">
                        <div class="flex items-center justify-between">
                            <span class="text-muted-color">Abertura</span>
                            <span>{{ formatDateTime(cashRegister.openedAt) }}</span>
                        </div>
                        <div class="flex items-center justify-between">
                            <span class="text-muted-color">Fechamento</span>
                            <span>{{ formatDateTime(cashRegister.closedAt) }}</span>
                        </div>
                        <div class="flex items-center justify-between">
                            <span class="text-muted-color">Saldo esperado</span>
                            <span class="font-semibold">{{ formatCurrency(cashRegister.cashBalance) }}</span>
                        </div>
                        <div class="flex items-center justify-between">
                            <span class="text-muted-color">Valor fechado</span>
                            <span class="font-semibold">{{ cashRegister.closingAmount != null ? formatCurrency(cashRegister.closingAmount) : '-' }}</span>
                        </div>
                        <div class="flex items-center justify-between">
                            <span class="text-muted-color">Diferença</span>
                            <span class="font-semibold">{{ cashRegister.differenceAmount != null ? formatCurrency(cashRegister.differenceAmount) : '-' }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <Dialog v-model:visible="summaryDialogVisible" modal maximizable header="Resumo do Caixa" :style="{ width: 'min(1000px, 95vw)' }">
        <ProgressSpinner v-if="summaryLoading" style="width: 2rem; height: 2rem" strokeWidth="6" class="block mx-auto my-8" />

        <template v-else-if="selectedSummary">
            <div class="grid grid-cols-12 gap-3 mb-5">
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Operador</div>
                        <div class="font-semibold">{{ selectedSummary.userName }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Status</div>
                        <div class="font-semibold">{{ statusLabel(selectedSummary.status) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Vendas</div>
                        <div class="font-semibold">{{ selectedSummary.salesCount }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Total vendido</div>
                        <div class="font-semibold">{{ formatCurrency(selectedSummary.salesAmount) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Saldo esperado</div>
                        <div class="font-semibold">{{ formatCurrency(selectedSummary.cashBalance) }}</div>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-12 gap-3 mb-5">
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Aberto em</div>
                        <div class="font-semibold">{{ formatDateTime(selectedSummary.openedAt) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Fechado em</div>
                        <div class="font-semibold">{{ formatDateTime(selectedSummary.closedAt) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Valor fechado</div>
                        <div class="font-semibold">{{ selectedSummary.closingAmount != null ? formatCurrency(selectedSummary.closingAmount) : '-' }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="p-3 rounded-lg border border-surface-200 dark:border-surface-700">
                        <div class="text-sm text-muted-color">Diferença</div>
                        <div class="font-semibold">{{ selectedSummary.differenceAmount != null ? formatCurrency(selectedSummary.differenceAmount) : '-' }}</div>
                    </div>
                </div>
            </div>

            <div class="space-y-5">
                <div>
                    <div class="font-semibold mb-2">Formas de pagamento</div>
                    <DataTable :value="selectedSummary.paymentSummary" size="small">
                        <Column header="Forma">
                            <template #body="{ data }">
                                {{ paymentMethodLabel(data.paymentMethod) }}
                            </template>
                        </Column>
                        <Column field="salesCount" header="Vendas" />
                        <Column header="Total">
                            <template #body="{ data }">
                                {{ formatCurrency(data.totalAmount) }}
                            </template>
                        </Column>
                    </DataTable>
                </div>

                <div>
                    <div class="font-semibold mb-2">Últimas vendas</div>
                    <DataTable :value="selectedSummary.recentSales" size="small" paginator :rows="5">
                        <Column header="Data/Hora">
                            <template #body="{ data }">
                                {{ formatDateTime(data.createdAt) }}
                            </template>
                        </Column>
                        <Column header="Pagamento">
                            <template #body="{ data }">
                                {{ paymentMethodLabel(data.paymentMethod) }}
                            </template>
                        </Column>
                        <Column field="itemsCount" header="Itens" />
                        <Column header="Total">
                            <template #body="{ data }">
                                {{ formatCurrency(data.totalAmount) }}
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </div>
        </template>
    </Dialog>
</template>
