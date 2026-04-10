import { ref } from 'vue';
import { getSupplierById, listSuppliers } from '@/services/supplierService.js';

const suppliers = ref([]);
const loading = ref(false);
const currentPage = ref(0);
const rowsPerPage = ref(10);
const totalRecords = ref(0);
const selectedSupplier = ref(null);
const detailLoading = ref(false);
const sortField = ref(null);
const sortOrder = ref(null);

const nameFilter = ref('');
const taxIdFilter = ref('');
const emailFilter = ref('');
const activeFilter = ref(null);

async function loadSuppliers({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    loading.value = true;

    try {
        const result = await listSuppliers({
            page,
            size,
            name: nameFilter.value,
            taxId: taxIdFilter.value,
            email: emailFilter.value,
            active: activeFilter.value,
            sortBy: sortField.value,
            sortDirection: sortOrder.value === -1 ? 'desc' : 'asc'
        });

        suppliers.value = result.content;
        currentPage.value = result.page;
        rowsPerPage.value = result.size;
        totalRecords.value = result.totalElements;
    } finally {
        loading.value = false;
    }
}

async function applyFilters() {
    currentPage.value = 0;
    await loadSuppliers({ page: 0, size: rowsPerPage.value });
}

async function clearFilters() {
    nameFilter.value = '';
    taxIdFilter.value = '';
    emailFilter.value = '';
    activeFilter.value = null;
    currentPage.value = 0;

    await loadSuppliers({ page: 0, size: rowsPerPage.value });
}

async function onPage(event) {
    await loadSuppliers({ page: event.page, size: event.rows });
}

async function onSort(event) {
    sortField.value = event.sortField || null;
    sortOrder.value = event.sortOrder || null;
    currentPage.value = 0;
    await loadSuppliers({ page: 0, size: rowsPerPage.value });
}

async function loadSupplierDetail(supplierId) {
    detailLoading.value = true;
    try {
        selectedSupplier.value = await getSupplierById(supplierId);
        return selectedSupplier.value;
    } finally {
        detailLoading.value = false;
    }
}

function clearSelectedSupplier() {
    selectedSupplier.value = null;
}

export function useSuppliers() {
    return {
        suppliers,
        loading,
        currentPage,
        rowsPerPage,
        totalRecords,
        sortField,
        sortOrder,
        selectedSupplier,
        detailLoading,
        nameFilter,
        taxIdFilter,
        emailFilter,
        activeFilter,
        loadSuppliers,
        loadSupplierDetail,
        clearSelectedSupplier,
        applyFilters,
        clearFilters,
        onPage,
        onSort
    };
}
