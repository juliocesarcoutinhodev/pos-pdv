<script setup>
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { getDailySalesReport } from '@/services/pdvService.js';

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

const hourlyChartData = computed(() => {
    const items = report.value?.hourlySummary ?? [];
    const documentStyle = getComputedStyle(document.documentElement);

    return {
        labels: items.map((item) => `${String(item.hourOfDay).padStart(2, '0')}h`),
        datasets: [
            {
                label: 'Faturamento',
                data: items.map((item) => Number(item.totalAmount || 0)),
                backgroundColor: documentStyle.getPropertyValue('--p-primary-400'),
                borderColor: documentStyle.getPropertyValue('--p-primary-500'),
                borderWidth: 1
            }
        ]
    };
});

const hourlyChartOptions = computed(() => {
    const documentStyle = getComputedStyle(document.documentElement);
    const borderColor = documentStyle.getPropertyValue('--surface-border');
    const textColor = documentStyle.getPropertyValue('--text-color-secondary');

    return {
        maintainAspectRatio: false,
        plugins: {
            legend: {
                labels: {
                    color: documentStyle.getPropertyValue('--text-color')
                }
            }
        },
        scales: {
            x: {
                ticks: { color: textColor },
                grid: { color: borderColor }
            },
            y: {
                ticks: { color: textColor },
                grid: { color: borderColor }
            }
        }
    };
});

async function loadReport() {
    loading.value = true;
    try {
        report.value = await getDailySalesReport(selectedDate.value);
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
                <div class="font-semibold text-xl">Vendas do Dia</div>
                <div class="text-sm text-muted-color">Acompanhamento diário de faturamento, formas de pagamento e produtos.</div>
            </div>
            <div class="flex gap-2 items-end">
                <div>
                    <label for="daily-sales-date" class="block mb-2 font-medium">Data</label>
                    <InputText id="daily-sales-date" v-model="selectedDate" type="date" class="w-full md:w-52" />
                </div>
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined :loading="loading" @click="loadReport" />
            </div>
        </div>

        <ProgressSpinner v-if="loading && !report" style="width: 2rem; height: 2rem" strokeWidth="6" class="block mx-auto my-8" />

        <template v-else-if="report">
            <section class="grid grid-cols-12 gap-4 mb-5">
                <div class="col-span-12 md:col-span-4">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Quantidade de vendas</div>
                        <div class="text-2xl font-semibold">{{ report.salesCount }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-4">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Faturamento total</div>
                        <div class="text-2xl font-semibold">{{ formatCurrency(report.totalAmount) }}</div>
                    </div>
                </div>
                <div class="col-span-12 md:col-span-4">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                        <div class="text-sm text-muted-color mb-1">Ticket médio</div>
                        <div class="text-2xl font-semibold">{{ formatCurrency(report.averageTicket) }}</div>
                    </div>
                </div>
            </section>

            <section class="grid grid-cols-12 gap-4 mb-5">
                <div class="col-span-12 xl:col-span-7">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="font-semibold text-lg mb-3">Vendas por hora</div>
                        <Chart type="bar" :data="hourlyChartData" :options="hourlyChartOptions" class="h-80" />
                    </div>
                </div>
                <div class="col-span-12 xl:col-span-5">
                    <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 h-full">
                        <div class="font-semibold text-lg mb-3">Resumo por pagamento</div>
                        <DataTable :value="report.paymentSummary" size="small">
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
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                <div class="font-semibold text-lg mb-3">Produtos mais vendidos no dia</div>
                <DataTable :value="report.topProducts" size="small" tableStyle="min-width: 48rem">
                    <Column field="sku" header="SKU" />
                    <Column field="name" header="Produto" />
                    <Column header="Quantidade">
                        <template #body="{ data }">
                            {{ Number(data.totalQuantity || 0).toLocaleString('pt-BR', { minimumFractionDigits: 3, maximumFractionDigits: 3 }) }}
                        </template>
                    </Column>
                    <Column header="Total">
                        <template #body="{ data }">
                            {{ formatCurrency(data.totalAmount) }}
                        </template>
                    </Column>
                </DataTable>
            </section>
        </template>
    </div>
</template>
