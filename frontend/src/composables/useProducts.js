import { ref } from 'vue';
import { getProductById, listProducts } from '@/services/productService.js';

const products = ref([]);
const loading = ref(false);
const currentPage = ref(0);
const rowsPerPage = ref(10);
const totalRecords = ref(0);
const selectedProduct = ref(null);
const detailLoading = ref(false);
const sortField = ref(null);
const sortOrder = ref(null);

const nameFilter = ref('');
const skuFilter = ref('');
const barcodeFilter = ref('');
const categoryFilter = ref('');
const activeFilter = ref(null);

async function loadProducts({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    loading.value = true;

    try {
        const result = await listProducts({
            page,
            size,
            name: nameFilter.value,
            sku: skuFilter.value,
            barcode: barcodeFilter.value,
            category: categoryFilter.value,
            active: activeFilter.value,
            sortBy: sortField.value,
            sortDirection: sortOrder.value === -1 ? 'desc' : 'asc'
        });

        products.value = result.content;
        currentPage.value = result.page;
        rowsPerPage.value = result.size;
        totalRecords.value = result.totalElements;
    } finally {
        loading.value = false;
    }
}

async function applyFilters() {
    currentPage.value = 0;
    await loadProducts({ page: 0, size: rowsPerPage.value });
}

async function clearFilters() {
    nameFilter.value = '';
    skuFilter.value = '';
    barcodeFilter.value = '';
    categoryFilter.value = '';
    activeFilter.value = null;
    currentPage.value = 0;

    await loadProducts({ page: 0, size: rowsPerPage.value });
}

async function onPage(event) {
    await loadProducts({ page: event.page, size: event.rows });
}

async function onSort(event) {
    sortField.value = event.sortField || null;
    sortOrder.value = event.sortOrder || null;
    currentPage.value = 0;
    await loadProducts({ page: 0, size: rowsPerPage.value });
}

async function loadProductDetail(productId) {
    detailLoading.value = true;
    try {
        selectedProduct.value = await getProductById(productId);
        return selectedProduct.value;
    } finally {
        detailLoading.value = false;
    }
}

function clearSelectedProduct() {
    selectedProduct.value = null;
}

export function useProducts() {
    return {
        products,
        loading,
        currentPage,
        rowsPerPage,
        totalRecords,
        sortField,
        sortOrder,
        selectedProduct,
        detailLoading,
        nameFilter,
        skuFilter,
        barcodeFilter,
        categoryFilter,
        activeFilter,
        loadProducts,
        loadProductDetail,
        clearSelectedProduct,
        applyFilters,
        clearFilters,
        onPage,
        onSort
    };
}
