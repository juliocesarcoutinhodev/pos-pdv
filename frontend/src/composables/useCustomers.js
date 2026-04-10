import { ref } from 'vue';
import { getCustomerById, listCustomers } from '@/services/customerService.js';

const customers = ref([]);
const loading = ref(false);
const currentPage = ref(0);
const rowsPerPage = ref(10);
const totalRecords = ref(0);
const selectedCustomer = ref(null);
const detailLoading = ref(false);
const sortField = ref(null);
const sortOrder = ref(null);

const nameFilter = ref('');
const taxIdFilter = ref('');
const emailFilter = ref('');
const activeFilter = ref(null);

async function loadCustomers({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    loading.value = true;

    try {
        const result = await listCustomers({
            page,
            size,
            name: nameFilter.value,
            taxId: taxIdFilter.value,
            email: emailFilter.value,
            active: activeFilter.value,
            sortBy: sortField.value,
            sortDirection: sortOrder.value === -1 ? 'desc' : 'asc'
        });

        customers.value = result.content;
        currentPage.value = result.page;
        rowsPerPage.value = result.size;
        totalRecords.value = result.totalElements;
    } finally {
        loading.value = false;
    }
}

async function applyFilters() {
    currentPage.value = 0;
    await loadCustomers({ page: 0, size: rowsPerPage.value });
}

async function clearFilters() {
    nameFilter.value = '';
    taxIdFilter.value = '';
    emailFilter.value = '';
    activeFilter.value = null;
    currentPage.value = 0;

    await loadCustomers({ page: 0, size: rowsPerPage.value });
}

async function onPage(event) {
    await loadCustomers({ page: event.page, size: event.rows });
}

async function onSort(event) {
    sortField.value = event.sortField || null;
    sortOrder.value = event.sortOrder || null;
    currentPage.value = 0;
    await loadCustomers({ page: 0, size: rowsPerPage.value });
}

async function loadCustomerDetail(customerId) {
    detailLoading.value = true;
    try {
        selectedCustomer.value = await getCustomerById(customerId);
        return selectedCustomer.value;
    } finally {
        detailLoading.value = false;
    }
}

function clearSelectedCustomer() {
    selectedCustomer.value = null;
}

export function useCustomers() {
    return {
        customers,
        loading,
        currentPage,
        rowsPerPage,
        totalRecords,
        sortField,
        sortOrder,
        selectedCustomer,
        detailLoading,
        nameFilter,
        taxIdFilter,
        emailFilter,
        activeFilter,
        loadCustomers,
        loadCustomerDetail,
        clearSelectedCustomer,
        applyFilters,
        clearFilters,
        onPage,
        onSort
    };
}
