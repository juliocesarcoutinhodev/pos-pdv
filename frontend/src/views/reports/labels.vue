<script setup>
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { createLabelPrintJob, downloadLabelPrintJobReport, listLabelPrintJobs, listLabelSuggestions } from '@/services/labelPrintService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const suggestionsLoading = ref(false);
const historyLoading = ref(false);
const creatingJob = ref(false);
const suggestions = ref([]);
const historyJobs = ref([]);
const printQueue = ref([]);

const filters = ref({
    date: toLocalDateInput(new Date()),
    name: '',
    sku: '',
    category: ''
});

const totalQueueProducts = computed(() => printQueue.value.length);
const totalQueueLabels = computed(() => printQueue.value.reduce((total, item) => total + normalizeQuantity(item.quantity), 0));

function toLocalDateInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function normalizeQuantity(value) {
    const parsed = Number.parseInt(String(value ?? '').trim(), 10);
    if (!Number.isFinite(parsed) || parsed <= 0) {
        return 1;
    }
    return parsed;
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

function formatDate(value) {
    if (!value) {
        return '-';
    }

    return new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short'
    }).format(new Date(`${value}T00:00:00`));
}

function formatCurrency(value) {
    if (value === null || value === undefined) {
        return '-';
    }

    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(Number(value));
}

async function loadSuggestions() {
    suggestionsLoading.value = true;
    try {
        const response = await listLabelSuggestions({
            date: filters.value.date,
            name: filters.value.name,
            sku: filters.value.sku,
            category: filters.value.category,
            page: 0,
            size: 50,
            sortBy: 'name',
            sortDirection: 'asc'
        });

        suggestions.value = response.content.map((item) => ({
            ...item,
            requestedQuantity: 1
        }));
    } finally {
        suggestionsLoading.value = false;
    }
}

async function loadHistory() {
    historyLoading.value = true;
    try {
        const response = await listLabelPrintJobs({
            referenceDate: filters.value.date || null,
            page: 0,
            size: 20,
            sortBy: 'createdAt',
            sortDirection: 'desc'
        });
        historyJobs.value = response.content;
    } finally {
        historyLoading.value = false;
    }
}

async function refreshData() {
    try {
        await Promise.all([loadSuggestions(), loadHistory()]);
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

function handleSuggestionQuantityInput(item, value) {
    item.requestedQuantity = normalizeQuantity(value);
}

function handleQueueQuantityInput(item, value) {
    item.quantity = normalizeQuantity(value);
}

function addToQueue(item) {
    const quantity = normalizeQuantity(item.requestedQuantity);
    const existing = printQueue.value.find((queueItem) => queueItem.productId === item.id);

    if (existing) {
        existing.quantity += quantity;
    } else {
        printQueue.value.push({
            productId: item.id,
            sku: item.sku,
            barcode: item.barcode,
            name: item.name,
            unit: item.unit,
            salePrice: item.salePrice,
            promotionalPrice: item.promotionalPrice,
            quantity
        });
    }

    item.requestedQuantity = 1;
}

function removeFromQueue(productId) {
    printQueue.value = printQueue.value.filter((item) => item.productId !== productId);
}

function clearQueue() {
    printQueue.value = [];
}

function openPopupWindow() {
    const popupWindow = window.open('', '_blank');

    if (!popupWindow) {
        toast.add({
            severity: 'warn',
            summary: 'Popup bloqueado',
            detail: 'Permita popups no navegador para abrir o PDF de etiquetas.',
            life: 4000
        });
        return null;
    }

    popupWindow.document.write(`
        <!DOCTYPE html>
        <html lang="pt-BR">
            <head>
                <meta charset="UTF-8" />
                <title>Gerando PDF...</title>
            </head>
            <body style="font-family: Arial, sans-serif; padding: 16px;">
                Gerando PDF de etiquetas...
            </body>
        </html>
    `);
    popupWindow.document.close();
    return popupWindow;
}

function openPdfInWindow(blob, popupWindow) {
    const blobUrl = URL.createObjectURL(blob);

    if (popupWindow && !popupWindow.closed) {
        popupWindow.location.href = blobUrl;
        popupWindow.focus();
    } else {
        const newWindow = window.open(blobUrl, '_blank');
        if (!newWindow) {
            URL.revokeObjectURL(blobUrl);
            toast.add({
                severity: 'warn',
                summary: 'Popup bloqueado',
                detail: 'Permita popups no navegador para abrir o PDF de etiquetas.',
                life: 4000
            });
            return;
        }
    }

    window.setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
}

async function openJobReport(jobId, popupWindow) {
    const report = await downloadLabelPrintJobReport(jobId);
    openPdfInWindow(report.blob, popupWindow);
}

async function handleGenerateJob() {
    if (!printQueue.value.length) {
        toast.add({
            severity: 'warn',
            summary: 'Lista vazia',
            detail: 'Adicione ao menos um produto na lista para imprimir etiquetas.',
            life: 3000
        });
        return;
    }

    const popupWindow = openPopupWindow();
    if (!popupWindow) {
        return;
    }

    creatingJob.value = true;
    try {
        const payload = {
            referenceDate: filters.value.date || null,
            items: printQueue.value.map((item) => ({
                productId: item.productId,
                quantity: normalizeQuantity(item.quantity)
            }))
        };

        const createdJob = await createLabelPrintJob(payload);
        await openJobReport(createdJob.id, popupWindow);
        clearQueue();
        await loadHistory();

        toast.add({
            severity: 'success',
            summary: 'Lote gerado',
            detail: 'Lote de etiquetas gerado com sucesso.',
            life: 3000
        });
    } catch (error) {
        if (!popupWindow.closed) {
            popupWindow.close();
        }
        showApiErrorToast(toast, error);
    } finally {
        creatingJob.value = false;
    }
}

async function handleReprint(jobId) {
    const popupWindow = openPopupWindow();
    if (!popupWindow) {
        return;
    }

    try {
        await openJobReport(jobId, popupWindow);
    } catch (error) {
        if (!popupWindow.closed) {
            popupWindow.close();
        }
        showApiErrorToast(toast, error);
    }
}

onMounted(async () => {
    await refreshData();
});
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
            <div>
                <div class="font-semibold text-xl">Etiquetas de gôndola</div>
                <div class="text-sm text-muted-color">Monte a lista, gere o lote e imprima rapidamente as etiquetas.</div>
            </div>
            <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined @click="refreshData" :loading="suggestionsLoading || historyLoading" />
        </div>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5 mb-5">
            <div class="font-semibold text-lg mb-3">1. Produtos para etiqueta</div>

            <div class="grid grid-cols-1 md:grid-cols-5 gap-3 mb-3">
                <div>
                    <label for="labels-filter-date" class="block mb-2 font-medium">Data de cadastro</label>
                    <InputText id="labels-filter-date" v-model="filters.date" type="date" class="w-full" />
                </div>
                <div>
                    <label for="labels-filter-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="labels-filter-name" v-model="filters.name" class="w-full" />
                </div>
                <div>
                    <label for="labels-filter-sku" class="block mb-2 font-medium">SKU</label>
                    <InputText id="labels-filter-sku" v-model="filters.sku" class="w-full" />
                </div>
                <div>
                    <label for="labels-filter-category" class="block mb-2 font-medium">Categoria</label>
                    <InputText id="labels-filter-category" v-model="filters.category" class="w-full" />
                </div>
                <div class="flex items-end">
                    <Button icon="pi pi-search" label="Buscar" class="w-full" @click="refreshData" :loading="suggestionsLoading" />
                </div>
            </div>

            <DataTable :value="suggestions" :loading="suggestionsLoading" dataKey="id" tableStyle="min-width: 65rem">
                <template #empty>Nenhum produto encontrado para os filtros informados.</template>
                <Column field="name" header="Produto" />
                <Column field="sku" header="SKU" />
                <Column field="category" header="Categoria">
                    <template #body="slotProps">{{ slotProps.data.category || '-' }}</template>
                </Column>
                <Column field="salePrice" header="Preço de venda">
                    <template #body="slotProps">{{ formatCurrency(slotProps.data.salePrice) }}</template>
                </Column>
                <Column field="promotionalPrice" header="Promocional">
                    <template #body="slotProps">{{ formatCurrency(slotProps.data.promotionalPrice) }}</template>
                </Column>
                <Column header="Qtd. etiquetas" style="width: 9rem">
                    <template #body="slotProps">
                        <InputText :modelValue="slotProps.data.requestedQuantity" type="number" min="1" class="w-full" @update:modelValue="(value) => handleSuggestionQuantityInput(slotProps.data, value)" />
                    </template>
                </Column>
                <Column header="Ação" style="width: 8rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-plus" label="Adicionar" size="small" @click="addToQueue(slotProps.data)" />
                    </template>
                </Column>
            </DataTable>
        </section>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5 mb-5">
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-3">
                <div class="font-semibold text-lg">2. Lista para impressão</div>
                <div class="text-sm text-muted-color">Produtos: {{ totalQueueProducts }} • Etiquetas: {{ totalQueueLabels }}</div>
            </div>

            <DataTable :value="printQueue" dataKey="productId" tableStyle="min-width: 65rem">
                <template #empty>Nenhum item adicionado para impressão.</template>
                <Column field="name" header="Produto" />
                <Column field="sku" header="SKU" />
                <Column field="salePrice" header="Preço venda">
                    <template #body="slotProps">{{ formatCurrency(slotProps.data.salePrice) }}</template>
                </Column>
                <Column field="promotionalPrice" header="Promocional">
                    <template #body="slotProps">{{ formatCurrency(slotProps.data.promotionalPrice) }}</template>
                </Column>
                <Column header="Qtd. etiquetas" style="width: 9rem">
                    <template #body="slotProps">
                        <InputText :modelValue="slotProps.data.quantity" type="number" min="1" class="w-full" @update:modelValue="(value) => handleQueueQuantityInput(slotProps.data, value)" />
                    </template>
                </Column>
                <Column header="Ação" style="width: 6rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" text severity="danger" @click="removeFromQueue(slotProps.data.productId)" />
                    </template>
                </Column>
            </DataTable>

            <div class="flex flex-col sm:flex-row justify-end gap-2 mt-4">
                <Button icon="pi pi-trash" label="Limpar lista" severity="secondary" outlined @click="clearQueue" :disabled="!printQueue.length || creatingJob" />
                <Button icon="pi pi-print" label="Gerar lote e imprimir" @click="handleGenerateJob" :loading="creatingJob" :disabled="!printQueue.length" />
            </div>
        </section>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
            <div class="font-semibold text-lg mb-3">3. Histórico de lotes</div>

            <DataTable :value="historyJobs" :loading="historyLoading" dataKey="id" tableStyle="min-width: 60rem">
                <template #empty>Nenhum lote de impressão encontrado.</template>
                <Column field="id" header="Lote">
                    <template #body="slotProps">{{ slotProps.data.id }}</template>
                </Column>
                <Column field="referenceDate" header="Data referência">
                    <template #body="slotProps">{{ formatDate(slotProps.data.referenceDate) }}</template>
                </Column>
                <Column field="totalProducts" header="Produtos" />
                <Column field="totalLabels" header="Etiquetas" />
                <Column field="createdAt" header="Criado em">
                    <template #body="slotProps">{{ formatDateTime(slotProps.data.createdAt) }}</template>
                </Column>
                <Column header="Ação" style="width: 7rem">
                    <template #body="slotProps">
                        <Button icon="pi pi-print" label="Reimprimir" size="small" severity="secondary" outlined @click="handleReprint(slotProps.data.id)" />
                    </template>
                </Column>
            </DataTable>
        </section>
    </div>
</template>
