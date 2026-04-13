<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { useProducts } from '@/composables/useProducts.js';
import { deactivateProduct, reactivateProduct } from '@/services/productService.js';
import { downloadImageBlob } from '@/services/imageService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const router = useRouter();
const confirm = useConfirm();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const {
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
} = useProducts();

const detailsDialogVisible = ref(false);
const imageViewerVisible = ref(false);
const detailImageLoading = ref(false);
const detailImageUrl = ref('');

const activeOptions = [
    { label: 'Todos', value: null },
    { label: 'Ativos', value: true },
    { label: 'Inativos', value: false }
];

function formatDate(value) {
    if (!value) {
        return '-';
    }

    return new Intl.DateTimeFormat('pt-BR', {
        dateStyle: 'short',
        timeStyle: 'medium'
    }).format(new Date(value));
}

function formatUpdatedDate(updatedAt, createdAt) {
    if (!updatedAt) {
        return 'Sem atualização';
    }

    if (createdAt && new Date(updatedAt).getTime() === new Date(createdAt).getTime()) {
        return 'Sem atualização';
    }

    return formatDate(updatedAt);
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

function formatQuantity(value) {
    if (value === null || value === undefined) {
        return '-';
    }

    return new Intl.NumberFormat('pt-BR', {
        minimumFractionDigits: 3,
        maximumFractionDigits: 3
    }).format(Number(value));
}

function formatRate(value) {
    if (value === null || value === undefined) {
        return '-';
    }

    return `${Number(value).toLocaleString('pt-BR', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}%`;
}

function tagSeverity(active) {
    return active ? 'success' : 'danger';
}

function tagLabel(active) {
    return active ? 'Ativo' : 'Inativo';
}

function clearDetailImage() {
    imageViewerVisible.value = false;
    if (detailImageUrl.value) {
        URL.revokeObjectURL(detailImageUrl.value);
        detailImageUrl.value = '';
    }
    detailImageLoading.value = false;
}

function openImageViewer() {
    if (!detailImageUrl.value || detailImageLoading.value) {
        return;
    }
    imageViewerVisible.value = true;
}

async function loadDetailImage(imageId) {
    clearDetailImage();

    if (!imageId) {
        return;
    }

    detailImageLoading.value = true;
    try {
        const blob = await downloadImageBlob(imageId);
        detailImageUrl.value = URL.createObjectURL(blob);
    } catch {
        detailImageUrl.value = '';
    } finally {
        detailImageLoading.value = false;
    }
}

function handleOpenCreatePage() {
    router.push('/products/new');
}

function handleEditProduct(product) {
    router.push(`/products/${product.id}/edit`);
}

async function handleOpenDetails(productId) {
    detailsDialogVisible.value = true;
    imageViewerVisible.value = false;
    clearDetailImage();

    try {
        const detail = await loadProductDetail(productId);
        await loadDetailImage(detail?.imageId);
    } catch (error) {
        detailsDialogVisible.value = false;
        showApiErrorToast(toast, error);
    }
}

async function handleRowClick(event) {
    await handleOpenDetails(event.data.id);
}

function closeDetailsDialog() {
    detailsDialogVisible.value = false;
    clearSelectedProduct();
    clearDetailImage();
}

function handleToggleStatus(product) {
    const nextActive = !product.active;
    const actionLabel = nextActive ? 'ativar' : 'desativar';

    confirm.require({
        message: `Deseja ${actionLabel} o produto ${product.name}?`,
        header: `${nextActive ? 'Ativar' : 'Desativar'} produto`,
        icon: 'pi pi-exclamation-triangle',
        rejectProps: {
            label: 'Cancelar',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: 'Confirmar',
            severity: nextActive ? 'success' : 'danger'
        },
        accept: async () => {
            try {
                if (nextActive) {
                    await reactivateProduct(product.id);
                } else {
                    await deactivateProduct(product.id);
                }

                await loadProducts();
                if (detailsDialogVisible.value && selectedProduct.value?.id === product.id) {
                    const detail = await loadProductDetail(product.id);
                    await loadDetailImage(detail?.imageId);
                }

                toast.add({
                    severity: 'success',
                    summary: nextActive ? 'Produto ativado' : 'Produto desativado',
                    detail: `${product.name} foi ${nextActive ? 'ativado' : 'desativado'} com sucesso.`,
                    life: 3000
                });
            } catch (error) {
                showApiErrorToast(toast, error);
            }
        }
    });
}

async function handleRefresh() {
    try {
        await loadProducts();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handlePage(event) {
    try {
        await onPage(event);
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleSort(event) {
    try {
        await onSort(event);
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleApplyFilters() {
    try {
        await applyFilters();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleClearFilters() {
    try {
        await clearFilters();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

onMounted(async () => {
    try {
        await loadProducts();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
});

onUnmounted(() => {
    clearDetailImage();
});
</script>

<template>
    <div class="card">
        <Toast />
        <ConfirmDialog />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-4">
            <div class="font-semibold text-xl">Produtos</div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-plus" label="Novo produto" @click="handleOpenCreatePage" />
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined @click="handleRefresh" :loading="loading" />
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-6 gap-3 mb-4">
            <IconField>
                <InputIcon class="pi pi-tag" />
                <InputText v-model="nameFilter" placeholder="Nome" @keyup.enter="handleApplyFilters" />
            </IconField>

            <IconField>
                <InputIcon class="pi pi-hashtag" />
                <InputText v-model="skuFilter" placeholder="SKU" @keyup.enter="handleApplyFilters" />
            </IconField>

            <IconField>
                <InputIcon class="pi pi-barcode" />
                <InputText v-model="barcodeFilter" placeholder="Código de barras" @keyup.enter="handleApplyFilters" />
            </IconField>

            <IconField>
                <InputIcon class="pi pi-box" />
                <InputText v-model="categoryFilter" placeholder="Categoria" @keyup.enter="handleApplyFilters" />
            </IconField>

            <Select v-model="activeFilter" :options="activeOptions" optionLabel="label" optionValue="value" placeholder="Status" />

            <div class="flex gap-2">
                <Button label="Filtrar" icon="pi pi-search" @click="handleApplyFilters" :loading="loading" />
                <Button label="Limpar" icon="pi pi-times" severity="secondary" outlined @click="handleClearFilters" :disabled="loading" />
            </div>
        </div>

        <DataTable
            class="app-sortable-table app-clickable-rows"
            :value="products"
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
            @row-click="handleRowClick"
            tableStyle="min-width: 80rem"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} produtos"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhum produto encontrado. </template>

            <Column field="name" header="Nome" sortable />
            <Column field="sku" header="SKU" sortable />
            <Column field="barcode" header="Código de barras" sortable>
                <template #body="slotProps">
                    {{ slotProps.data.barcode || '-' }}
                </template>
            </Column>
            <Column field="category" header="Categoria" sortable>
                <template #body="slotProps">
                    {{ slotProps.data.category || '-' }}
                </template>
            </Column>
            <Column field="salePrice" header="Preço de venda" sortable>
                <template #body="slotProps">
                    {{ formatCurrency(slotProps.data.salePrice) }}
                </template>
            </Column>
            <Column field="stockQuantity" header="Estoque" sortable>
                <template #body="slotProps">
                    {{ formatQuantity(slotProps.data.stockQuantity) }}
                </template>
            </Column>
            <Column header="Imagem">
                <template #body="slotProps">
                    <Tag :severity="slotProps.data.imageId ? 'info' : 'secondary'" :value="slotProps.data.imageId ? 'Anexada' : 'Sem imagem'" />
                </template>
            </Column>
            <Column header="Status">
                <template #body="slotProps">
                    <Tag :severity="tagSeverity(slotProps.data.active)" :value="tagLabel(slotProps.data.active)" />
                </template>
            </Column>
            <Column field="createdAt" header="Criado em" sortable>
                <template #body="slotProps">
                    {{ formatDate(slotProps.data.createdAt) }}
                </template>
            </Column>
            <Column header="Atualizado em">
                <template #body="slotProps">
                    {{ formatUpdatedDate(slotProps.data.updatedAt, slotProps.data.createdAt) }}
                </template>
            </Column>
            <Column header="Ações" :exportable="false" style="width: 8rem">
                <template #body="slotProps">
                    <div class="flex items-center gap-2">
                        <Button icon="pi pi-pencil" rounded text severity="warning" aria-label="Editar produto" @click.stop="handleEditProduct(slotProps.data)" />
                        <Button
                            :icon="slotProps.data.active ? 'pi pi-ban' : 'pi pi-check-circle'"
                            rounded
                            text
                            :severity="slotProps.data.active ? 'danger' : 'success'"
                            :aria-label="slotProps.data.active ? 'Desativar produto' : 'Ativar produto'"
                            @click.stop="handleToggleStatus(slotProps.data)"
                        />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="detailsDialogVisible" modal header="Detalhes do produto" :style="{ width: '58rem' }" @hide="closeDetailsDialog">
            <div v-if="detailLoading" class="py-4">
                <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
            </div>

            <div v-else-if="selectedProduct" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color mb-2">Imagem do produto</div>
                    <div class="flex items-center gap-4">
                        <div v-if="detailImageLoading" class="flex items-center justify-center w-24 h-24 border border-surface-200 dark:border-surface-700 rounded-lg">
                            <ProgressSpinner style="width: 1.5rem; height: 1.5rem" strokeWidth="6" />
                        </div>
                        <button v-else-if="detailImageUrl" type="button" class="w-24 h-24 rounded-lg border border-surface-200 dark:border-surface-700 overflow-hidden cursor-zoom-in" @click="openImageViewer">
                            <img :src="detailImageUrl" alt="Imagem do produto" class="w-full h-full object-contain bg-surface-50 dark:bg-surface-900" />
                        </button>
                        <div v-else class="w-24 h-24 rounded-lg border border-dashed border-surface-300 dark:border-surface-600 flex items-center justify-center text-muted-color">Sem imagem</div>
                        <div class="text-sm text-muted-color">
                            {{ selectedProduct.imageId ? 'Imagem vinculada ao cadastro. Clique para ampliar.' : 'Nenhuma imagem vinculada.' }}
                        </div>
                    </div>
                </div>

                <div>
                    <div class="text-sm text-muted-color">Nome</div>
                    <div class="font-medium">{{ selectedProduct.name || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">SKU</div>
                    <div class="font-medium">{{ selectedProduct.sku || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Código de barras</div>
                    <div class="font-medium">{{ selectedProduct.barcode || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Unidade</div>
                    <div class="font-medium">{{ selectedProduct.unit || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Marca</div>
                    <div class="font-medium">{{ selectedProduct.brand || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Categoria</div>
                    <div class="font-medium">{{ selectedProduct.category || '-' }}</div>
                </div>
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color">Descrição</div>
                    <div class="font-medium">{{ selectedProduct.description || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Preço de custo</div>
                    <div class="font-medium">{{ formatCurrency(selectedProduct.costPrice) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Preço de venda</div>
                    <div class="font-medium">{{ formatCurrency(selectedProduct.salePrice) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Preço promocional</div>
                    <div class="font-medium">{{ formatCurrency(selectedProduct.promotionalPrice) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Estoque atual</div>
                    <div class="font-medium">{{ formatQuantity(selectedProduct.stockQuantity) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Estoque mínimo</div>
                    <div class="font-medium">{{ formatQuantity(selectedProduct.minimumStock) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">NCM</div>
                    <div class="font-medium">{{ selectedProduct.ncm || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">CEST</div>
                    <div class="font-medium">{{ selectedProduct.cest || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">CFOP</div>
                    <div class="font-medium">{{ selectedProduct.cfop || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Origem tributária</div>
                    <div class="font-medium">{{ selectedProduct.taxOrigin || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Situação tributária</div>
                    <div class="font-medium">{{ selectedProduct.taxSituation || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Alíquota ICMS</div>
                    <div class="font-medium">{{ formatRate(selectedProduct.icmsRate) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Situação PIS</div>
                    <div class="font-medium">{{ selectedProduct.pisSituation || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Alíquota PIS</div>
                    <div class="font-medium">{{ formatRate(selectedProduct.pisRate) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Situação COFINS</div>
                    <div class="font-medium">{{ selectedProduct.cofinsSituation || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Alíquota COFINS</div>
                    <div class="font-medium">{{ formatRate(selectedProduct.cofinsRate) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Status</div>
                    <Tag :severity="tagSeverity(selectedProduct.active)" :value="tagLabel(selectedProduct.active)" />
                </div>
                <div>
                    <div class="text-sm text-muted-color">Criado em</div>
                    <div class="font-medium">{{ formatDate(selectedProduct.createdAt) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Atualizado em</div>
                    <div class="font-medium">{{ formatUpdatedDate(selectedProduct.updatedAt, selectedProduct.createdAt) }}</div>
                </div>
            </div>
        </Dialog>

        <Dialog v-model:visible="imageViewerVisible" modal header="Imagem do produto" :style="{ width: '70vw', maxWidth: '56rem' }">
            <div class="flex items-center justify-center rounded-lg bg-surface-50 dark:bg-surface-900 p-4" style="min-height: 20rem">
                <img v-if="detailImageUrl" :src="detailImageUrl" alt="Imagem do produto ampliada" class="w-full" style="max-height: 70vh; object-fit: contain" />
            </div>
        </Dialog>
    </div>
</template>
