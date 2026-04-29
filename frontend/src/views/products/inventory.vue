<script setup>
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { listProducts } from '@/services/productService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loading = ref(false);
const products = ref([]);
const currentPage = ref(0);
const rowsPerPage = ref(20);
const totalRecords = ref(0);

const filters = ref({
    name: '',
    sku: '',
    category: '',
    stockStatus: 'ALL'
});

const stockStatusOptions = [
    { label: 'Todos', value: 'ALL' },
    { label: 'Abaixo do mínimo', value: 'LOW' },
    { label: 'Sem estoque', value: 'OUT' },
    { label: 'Normal', value: 'OK' }
];

function toNumber(value) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
}

function stockStatus(product) {
    const stock = toNumber(product.stockQuantity);
    const minimum = toNumber(product.minimumStock);

    if (stock <= 0) return 'OUT';
    if (minimum > 0 && stock <= minimum) return 'LOW';
    return 'OK';
}

function statusSeverity(status) {
    if (status === 'OUT') return 'danger';
    if (status === 'LOW') return 'warn';
    return 'success';
}

function statusLabel(status) {
    if (status === 'OUT') return 'Sem estoque';
    if (status === 'LOW') return 'Abaixo do mínimo';
    return 'Normal';
}

function formatQuantity(value) {
    return Number(value || 0).toLocaleString('pt-BR', {
        minimumFractionDigits: 3,
        maximumFractionDigits: 3
    });
}

const lowStockCount = computed(() => products.value.filter((product) => stockStatus(product) === 'LOW').length);
const outOfStockCount = computed(() => products.value.filter((product) => stockStatus(product) === 'OUT').length);

async function loadInventory({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    loading.value = true;
    try {
        const result = await listProducts({
            page,
            size,
            name: filters.value.name,
            sku: filters.value.sku,
            category: filters.value.category,
            active: true,
            sortBy: 'name',
            sortDirection: 'asc'
        });

        const filtered = filters.value.stockStatus === 'ALL' ? result.content : result.content.filter((product) => stockStatus(product) === filters.value.stockStatus);

        products.value = filtered;
        currentPage.value = result.page;
        rowsPerPage.value = result.size;
        totalRecords.value = result.totalElements;
    } finally {
        loading.value = false;
    }
}

async function handleApplyFilters() {
    try {
        currentPage.value = 0;
        await loadInventory({ page: 0, size: rowsPerPage.value });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleClearFilters() {
    filters.value = {
        name: '',
        sku: '',
        category: '',
        stockStatus: 'ALL'
    };

    try {
        currentPage.value = 0;
        await loadInventory({ page: 0, size: rowsPerPage.value });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handlePage(event) {
    try {
        await loadInventory({ page: event.page, size: event.rows });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

onMounted(async () => {
    try {
        await loadInventory();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
});
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
            <div>
                <div class="font-semibold text-xl">Controle de Estoque</div>
                <div class="text-sm text-muted-color">Acompanhamento do estoque atual com alertas de reposição.</div>
            </div>
            <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined :loading="loading" @click="loadInventory" />
        </div>

        <section class="grid grid-cols-12 gap-3 mb-5">
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                    <div class="text-sm text-muted-color mb-1">Sem estoque</div>
                    <div class="text-2xl font-semibold text-red-500">{{ outOfStockCount }}</div>
                </div>
            </div>
            <div class="col-span-12 md:col-span-6 xl:col-span-3">
                <div class="border border-surface-200 dark:border-surface-700 rounded-xl p-4">
                    <div class="text-sm text-muted-color mb-1">Abaixo do mínimo</div>
                    <div class="text-2xl font-semibold text-orange-500">{{ lowStockCount }}</div>
                </div>
            </div>
        </section>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5 mb-5">
            <div class="font-semibold text-lg mb-3">Filtros</div>
            <div class="grid grid-cols-1 md:grid-cols-5 gap-3">
                <div>
                    <label for="inventory-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="inventory-name" v-model="filters.name" class="w-full" />
                </div>
                <div>
                    <label for="inventory-sku" class="block mb-2 font-medium">SKU</label>
                    <InputText id="inventory-sku" v-model="filters.sku" class="w-full" />
                </div>
                <div>
                    <label for="inventory-category" class="block mb-2 font-medium">Categoria</label>
                    <InputText id="inventory-category" v-model="filters.category" class="w-full" />
                </div>
                <div>
                    <label for="inventory-status" class="block mb-2 font-medium">Situação</label>
                    <Select id="inventory-status" v-model="filters.stockStatus" :options="stockStatusOptions" optionLabel="label" optionValue="value" class="w-full" />
                </div>
                <div class="flex items-end gap-2">
                    <Button icon="pi pi-filter" label="Aplicar" class="w-full" @click="handleApplyFilters" />
                    <Button icon="pi pi-filter-slash" label="Limpar" severity="secondary" outlined class="w-full" @click="handleClearFilters" />
                </div>
            </div>
        </section>

        <DataTable
            :value="products"
            dataKey="id"
            lazy
            paginator
            :rows="rowsPerPage"
            :first="currentPage * rowsPerPage"
            :totalRecords="totalRecords"
            :loading="loading"
            @page="handlePage"
            tableStyle="min-width: 64rem"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} produtos"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhum produto encontrado para os filtros informados. </template>

            <Column field="sku" header="SKU" />
            <Column field="name" header="Produto" />
            <Column field="category" header="Categoria" />
            <Column header="Estoque atual">
                <template #body="{ data }">
                    {{ formatQuantity(data.stockQuantity) }}
                </template>
            </Column>
            <Column header="Estoque mínimo">
                <template #body="{ data }">
                    {{ formatQuantity(data.minimumStock) }}
                </template>
            </Column>
            <Column header="Situação">
                <template #body="{ data }">
                    <Tag :value="statusLabel(stockStatus(data))" :severity="statusSeverity(stockStatus(data))" />
                </template>
            </Column>
        </DataTable>
    </div>
</template>
