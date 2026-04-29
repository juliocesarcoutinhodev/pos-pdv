<script setup>
import { onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { getCashClosingReport } from '@/services/pdvService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loading = ref(false);
const report = ref(null);
const selectedDate = ref(new Date().toISOString().slice(0, 10));

function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value || 0));
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    return new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short',
        timeStyle: 'short'
    }).format(new Date(value));
}

function statusSeverity(status) {
    return status === 'OPEN' ? 'success' : 'contrast';
}

function statusLabel(status) {
    return status === 'OPEN' ? 'Aberto' : 'Fechado';
}

async function loadReport() {
    loading.value = true;
    try {
        report.value = await getCashClosingReport(selectedDate.value);
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        loading.value = false;
    }
}

onMounted(async () => {
    await loadReport();
});
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
            <div>
                <div class="font-semibold text-xl">Fechamento de Caixa</div>
                <div class="text-sm text-muted-color">Consolidação diária por sessão de caixa.</div>
            </div>
            <div class="flex gap-2 items-end">
                <div>
                    <label for="closing-date" class="block mb-2 font-medium">Data</label>
                    <InputText id="closing-date" v-model="selectedDate" type="date" class="w-full md:w-52" />
                </div>
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined :loading="loading" @click="loadReport" />
            </div>
        </div>

        <ProgressSpinner v-if="loading && !report" style="width: 2rem; height: 2rem" strokeWidth="6" class="block mx-auto my-8" />

        <template v-else-if="report">
            <section class="grid grid-cols-12 gap-4 mb-5">
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Sessões</div>
                        <div class="text-2xl font-semibold">{{ report.totalSessions }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Abertas</div>
                        <div class="text-2xl font-semibold">{{ report.openSessions }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Fechadas</div>
                        <div class="text-2xl font-semibold">{{ report.closedSessions }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Abertura total</div>
                        <div class="text-lg font-semibold">{{ formatCurrency(report.totalOpeningAmount) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Suprimentos</div>
                        <div class="text-lg font-semibold">{{ formatCurrency(report.totalSuppliesAmount) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Sangrias</div>
                        <div class="text-lg font-semibold">{{ formatCurrency(report.totalWithdrawalsAmount) }}</div>
                    </div>
                </div>
            </section>

            <section class="grid grid-cols-12 gap-4 mb-5">
                <div class="col-span-12 md:col-span-6">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Vendas no período</div>
                        <div class="text-2xl font-semibold">{{ formatCurrency(report.totalSalesAmount) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Saldo consolidado</div>
                        <div class="text-2xl font-semibold">{{ formatCurrency(report.totalCashBalance) }}</div>
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                <div class="font-semibold text-lg mb-3">Sessões de caixa</div>
                <DataTable :value="report.sessions" size="small" tableStyle="min-width: 72rem">
                    <Column header="Status">
                        <template #body="{ data }">
                            <Tag :value="statusLabel(data.status)" :severity="statusSeverity(data.status)" />
                        </template>
                    </Column>
                    <Column field="userName" header="Operador" />
                    <Column header="Abertura">
                        <template #body="{ data }">
                            {{ formatDateTime(data.openedAt) }}
                        </template>
                    </Column>
                    <Column header="Fechamento">
                        <template #body="{ data }">
                            {{ formatDateTime(data.closedAt) }}
                        </template>
                    </Column>
                    <Column header="Valor abertura">
                        <template #body="{ data }">
                            {{ formatCurrency(data.openingAmount) }}
                        </template>
                    </Column>
                    <Column header="Suprimentos">
                        <template #body="{ data }">
                            {{ formatCurrency(data.suppliesAmount) }}
                        </template>
                    </Column>
                    <Column header="Sangrias">
                        <template #body="{ data }">
                            {{ formatCurrency(data.withdrawalsAmount) }}
                        </template>
                    </Column>
                    <Column header="Vendas">
                        <template #body="{ data }">
                            {{ formatCurrency(data.salesAmount) }}
                        </template>
                    </Column>
                    <Column field="salesCount" header="Qtd vendas" />
                    <Column header="Saldo">
                        <template #body="{ data }">
                            {{ formatCurrency(data.cashBalance) }}
                        </template>
                    </Column>
                </DataTable>
            </section>
        </template>
    </div>
</template>
