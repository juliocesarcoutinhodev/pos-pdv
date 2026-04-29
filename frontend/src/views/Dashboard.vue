<script setup>
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { getDashboardOverview } from '@/services/dashboardService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loading = ref(false);
const overview = ref(null);

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

function paymentMethodLabel(value) {
    const labels = {
        CASH: 'Dinheiro',
        PIX: 'PIX',
        DEBIT_CARD: 'Cartão Débito',
        CREDIT_CARD: 'Cartão Crédito',
        OTHER: 'Outros'
    };
    return labels[value] ?? value;
}

const paymentChartData = computed(() => {
    const summary = overview.value?.paymentSummary ?? [];
    const documentStyle = getComputedStyle(document.documentElement);

    return {
        labels: summary.map((item) => paymentMethodLabel(item.paymentMethod)),
        datasets: [
            {
                data: summary.map((item) => Number(item.totalAmount || 0)),
                backgroundColor: [
                    documentStyle.getPropertyValue('--p-blue-500'),
                    documentStyle.getPropertyValue('--p-green-500'),
                    documentStyle.getPropertyValue('--p-orange-500'),
                    documentStyle.getPropertyValue('--p-purple-500'),
                    documentStyle.getPropertyValue('--p-cyan-500')
                ],
                hoverBackgroundColor: [
                    documentStyle.getPropertyValue('--p-blue-400'),
                    documentStyle.getPropertyValue('--p-green-400'),
                    documentStyle.getPropertyValue('--p-orange-400'),
                    documentStyle.getPropertyValue('--p-purple-400'),
                    documentStyle.getPropertyValue('--p-cyan-400')
                ]
            }
        ]
    };
});

const paymentChartOptions = computed(() => {
    const documentStyle = getComputedStyle(document.documentElement);
    return {
        plugins: {
            legend: {
                labels: {
                    color: documentStyle.getPropertyValue('--text-color')
                }
            }
        }
    };
});

async function loadOverview() {
    loading.value = true;
    try {
        overview.value = await getDashboardOverview();
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        loading.value = false;
    }
}

onMounted(async () => {
    await loadOverview();
});
</script>

<template>
    <div class="card">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-5">
            <div>
                <div class="font-semibold text-2xl">Painel Operacional</div>
                <div class="text-sm text-muted-color">Visão consolidada das vendas e operação do caixa.</div>
            </div>
            <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined :loading="loading" @click="loadOverview" />
        </div>

        <ProgressSpinner v-if="loading && !overview" style="width: 2rem; height: 2rem" strokeWidth="6" class="block mx-auto my-8" />

        <template v-else-if="overview">
            <section class="grid grid-cols-12 gap-4 mb-5">
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Vendas hoje</div>
                        <div class="text-2xl font-semibold">{{ overview.todaySalesCount }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Faturamento hoje</div>
                        <div class="text-2xl font-semibold">{{ formatCurrency(overview.todaySalesAmount) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Ticket médio hoje</div>
                        <div class="text-2xl font-semibold">{{ formatCurrency(overview.todayAverageTicket) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-3">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Caixas abertos</div>
                        <div class="text-2xl font-semibold">{{ overview.openCashRegisters }}</div>
                    </div>
                </div>
            </section>

            <section class="grid grid-cols-12 gap-4 mb-5">
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Usuários ativos</div>
                        <div class="text-xl font-semibold">{{ overview.activeUsers }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Clientes ativos</div>
                        <div class="text-xl font-semibold">{{ overview.activeCustomers }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Fornecedores ativos</div>
                        <div class="text-xl font-semibold">{{ overview.activeSuppliers }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-2">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Produtos ativos</div>
                        <div class="text-xl font-semibold">{{ overview.activeProducts }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-6 xl:col-span-4">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="text-sm text-muted-color mb-1">Alertas de estoque</div>
                        <div class="text-xl font-semibold">{{ overview.lowStockProducts }} com estoque baixo</div>
                    </div>
                </div>
            </section>

            <section class="grid grid-cols-12 gap-4">
                <div class="col-span-12 xl:col-span-7">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="font-semibold text-lg mb-3">Vendas recentes</div>
                        <DataTable :value="overview.recentSales" size="small" responsiveLayout="scroll">
                            <Column header="Data/Hora">
                                <template #body="{ data }">
                                    {{ formatDateTime(data.createdAt) }}
                                </template>
                            </Column>
                            <Column field="userName" header="Operador" />
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

                <div class="col-span-12 xl:col-span-5">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="font-semibold text-lg mb-3">Distribuição por pagamento (30 dias)</div>
                        <Chart type="doughnut" :data="paymentChartData" :options="paymentChartOptions" class="h-72" />
                    </div>
                </div>

                <div class="col-span-12">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="font-semibold text-lg mb-3">Produtos mais vendidos (30 dias)</div>
                        <DataTable :value="overview.topProducts" size="small" responsiveLayout="scroll">
                            <Column field="sku" header="SKU" />
                            <Column field="name" header="Produto" />
                            <Column header="Quantidade">
                                <template #body="{ data }">
                                    {{ Number(data.totalQuantity || 0).toLocaleString('pt-BR', { minimumFractionDigits: 3, maximumFractionDigits: 3 }) }}
                                </template>
                            </Column>
                            <Column header="Faturamento">
                                <template #body="{ data }">
                                    {{ formatCurrency(data.totalAmount) }}
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </div>
            </section>
        </template>
    </div>
</template>
