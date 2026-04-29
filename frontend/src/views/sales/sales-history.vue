<script setup>
import { computed, onMounted, ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useAuth } from '@/composables/useAuth.js';
import { useErrorHandler } from '@/services/errorHandler.js';
import { listPdvSalesHistory } from '@/services/pdvService.js';
import { listUsers } from '@/services/userService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();
const { user } = useAuth();

const loading = ref(false);
const history = ref([]);
const currentPage = ref(0);
const rowsPerPage = ref(20);
const totalRecords = ref(0);
const sortField = ref('createdAt');
const sortOrder = ref(-1);

const userOptions = ref([]);
const usersLoading = ref(false);

const filters = ref({
    dateFrom: '',
    dateTo: '',
    paymentMethod: null,
    userId: null
});

const paymentOptions = [
    { label: 'Todas', value: null },
    { label: 'Dinheiro', value: 'CASH' },
    { label: 'PIX', value: 'PIX' },
    { label: 'Cartão Débito', value: 'DEBIT_CARD' },
    { label: 'Cartão Crédito', value: 'CREDIT_CARD' },
    { label: 'Outros', value: 'OTHER' }
];

const isAdmin = computed(() => {
    const roles = Array.isArray(user.value?.roles) ? user.value.roles : [];
    return roles.map((role) => String(role).toUpperCase()).includes('ADMIN');
});

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

function hasValidDateRange() {
    if (!filters.value.dateFrom || !filters.value.dateTo) {
        return true;
    }

    if (filters.value.dateFrom <= filters.value.dateTo) {
        return true;
    }

    toast.add({
        severity: 'warn',
        summary: 'Período inválido',
        detail: 'A data inicial deve ser menor ou igual à data final.',
        life: 3500
    });
    return false;
}

async function loadUserOptions() {
    if (!isAdmin.value) {
        userOptions.value = [];
        return;
    }

    usersLoading.value = true;
    try {
        const result = await listUsers({
            page: 0,
            size: 100,
            active: true,
            sortBy: 'name',
            sortDirection: 'asc'
        });

        userOptions.value = [
            { label: 'Todos', value: null },
            ...result.content.map((item) => ({
                label: item.name,
                value: item.id
            }))
        ];
    } finally {
        usersLoading.value = false;
    }
}

async function loadHistory({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    if (!hasValidDateRange()) {
        return;
    }

    loading.value = true;
    try {
        const result = await listPdvSalesHistory({
            page,
            size,
            dateFrom: filters.value.dateFrom || null,
            dateTo: filters.value.dateTo || null,
            paymentMethod: filters.value.paymentMethod,
            userId: isAdmin.value ? filters.value.userId : null,
            sortBy: sortField.value || 'createdAt',
            sortDirection: sortOrder.value === 1 ? 'asc' : 'desc'
        });

        history.value = result.content;
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
        await loadHistory({ page: 0, size: rowsPerPage.value });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleClearFilters() {
    filters.value = {
        dateFrom: '',
        dateTo: '',
        paymentMethod: null,
        userId: null
    };

    try {
        currentPage.value = 0;
        await loadHistory({ page: 0, size: rowsPerPage.value });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handlePage(event) {
    try {
        await loadHistory({ page: event.page, size: event.rows });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleSort(event) {
    sortField.value = event.sortField || 'createdAt';
    sortOrder.value = event.sortOrder || -1;
    try {
        await loadHistory({ page: 0, size: rowsPerPage.value });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

onMounted(async () => {
    try {
        await loadUserOptions();
        await loadHistory();
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
                <div class="font-semibold text-xl">Histórico de Vendas</div>
                <div class="text-sm text-muted-color">Consulta paginada das vendas registradas no PDV.</div>
            </div>
            <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined :loading="loading" @click="loadHistory" />
        </div>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5 mb-5">
            <div class="font-semibold text-lg mb-3">Filtros</div>

            <div class="grid grid-cols-1 md:grid-cols-5 gap-3">
                <div>
                    <label for="history-date-from" class="block mb-2 font-medium">Data inicial</label>
                    <InputText id="history-date-from" v-model="filters.dateFrom" type="date" class="w-full" />
                </div>
                <div>
                    <label for="history-date-to" class="block mb-2 font-medium">Data final</label>
                    <InputText id="history-date-to" v-model="filters.dateTo" type="date" class="w-full" />
                </div>
                <div>
                    <label for="history-payment" class="block mb-2 font-medium">Pagamento</label>
                    <Select id="history-payment" v-model="filters.paymentMethod" :options="paymentOptions" optionLabel="label" optionValue="value" class="w-full" />
                </div>
                <div v-if="isAdmin">
                    <label for="history-user" class="block mb-2 font-medium">Operador</label>
                    <Select id="history-user" v-model="filters.userId" :options="userOptions" optionLabel="label" optionValue="value" class="w-full" :loading="usersLoading" />
                </div>
                <div class="flex items-end gap-2">
                    <Button icon="pi pi-filter" label="Aplicar" class="w-full" @click="handleApplyFilters" />
                    <Button icon="pi pi-filter-slash" label="Limpar" severity="secondary" outlined class="w-full" @click="handleClearFilters" />
                </div>
            </div>
        </section>

        <DataTable
            :value="history"
            dataKey="id"
            lazy
            paginator
            :rows="rowsPerPage"
            :first="currentPage * rowsPerPage"
            :totalRecords="totalRecords"
            :sortField="sortField"
            :sortOrder="sortOrder"
            :loading="loading"
            @page="handlePage"
            @sort="handleSort"
            tableStyle="min-width: 64rem"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} vendas"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhuma venda encontrada para os filtros informados. </template>

            <Column field="createdAt" header="Data/Hora" sortable>
                <template #body="{ data }">
                    {{ formatDateTime(data.createdAt) }}
                </template>
            </Column>
            <Column field="userName" header="Operador" />
            <Column field="paymentMethod" header="Pagamento" sortable>
                <template #body="{ data }">
                    {{ paymentMethodLabel(data.paymentMethod) }}
                </template>
            </Column>
            <Column field="itemsCount" header="Itens" />
            <Column field="totalAmount" header="Total" sortable>
                <template #body="{ data }">
                    {{ formatCurrency(data.totalAmount) }}
                </template>
            </Column>
            <Column field="paidAmount" header="Pago">
                <template #body="{ data }">
                    {{ formatCurrency(data.paidAmount) }}
                </template>
            </Column>
            <Column field="changeAmount" header="Troco">
                <template #body="{ data }">
                    {{ formatCurrency(data.changeAmount) }}
                </template>
            </Column>
            <Column field="notes" header="Observações">
                <template #body="{ data }">
                    <span class="text-sm">{{ data.notes || '-' }}</span>
                </template>
            </Column>
        </DataTable>
    </div>
</template>
