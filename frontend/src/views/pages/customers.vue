<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { useCustomers } from '@/composables/useCustomers.js';
import { deactivateCustomer, reactivateCustomer } from '@/services/customerService.js';
import { downloadImageBlob } from '@/services/imageService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const router = useRouter();
const confirm = useConfirm();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const {
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
} = useCustomers();

const detailsDialogVisible = ref(false);
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

function formatTaxId(value) {
    if (!value) {
        return '-';
    }

    const digits = value.replace(/\D/g, '');
    if (digits.length === 11) {
        return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
    }

    if (digits.length === 14) {
        return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`;
    }

    return value;
}

function formatPhone(value) {
    if (!value) {
        return '-';
    }

    const digits = value.replace(/\D/g, '');
    if (digits.length === 11) {
        return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
    }

    if (digits.length === 10) {
        return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
    }

    return value;
}

function tagSeverity(active) {
    return active ? 'success' : 'danger';
}

function tagLabel(active) {
    return active ? 'Ativo' : 'Inativo';
}

function clearDetailImage() {
    if (detailImageUrl.value) {
        URL.revokeObjectURL(detailImageUrl.value);
        detailImageUrl.value = '';
    }
    detailImageLoading.value = false;
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
    router.push('/pages/customers/new');
}

function handleEditCustomer(customer) {
    router.push(`/pages/customers/${customer.id}/edit`);
}

async function handleOpenDetails(customerId) {
    detailsDialogVisible.value = true;
    clearDetailImage();

    try {
        const detail = await loadCustomerDetail(customerId);
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
    clearSelectedCustomer();
    clearDetailImage();
}

function handleToggleStatus(customer) {
    const nextActive = !customer.active;
    const actionLabel = nextActive ? 'ativar' : 'desativar';

    confirm.require({
        message: `Deseja ${actionLabel} o cliente ${customer.name}?`,
        header: `${nextActive ? 'Ativar' : 'Desativar'} cliente`,
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
                    await reactivateCustomer(customer.id);
                } else {
                    await deactivateCustomer(customer.id);
                }

                await loadCustomers();
                if (detailsDialogVisible.value && selectedCustomer.value?.id === customer.id) {
                    await loadCustomerDetail(customer.id);
                }

                toast.add({
                    severity: 'success',
                    summary: nextActive ? 'Cliente ativado' : 'Cliente desativado',
                    detail: `${customer.name} foi ${nextActive ? 'ativado' : 'desativado'} com sucesso.`,
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
        await loadCustomers();
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
        await loadCustomers();
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
            <div class="font-semibold text-xl">Clientes</div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-plus" label="Novo cliente" @click="handleOpenCreatePage" />
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined @click="handleRefresh" :loading="loading" />
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-5 gap-3 mb-4">
            <IconField>
                <InputIcon class="pi pi-user" />
                <InputText v-model="nameFilter" placeholder="Filtrar por nome" @keyup.enter="handleApplyFilters" />
            </IconField>

            <IconField>
                <InputIcon class="pi pi-id-card" />
                <InputText v-model="taxIdFilter" placeholder="Filtrar por CPF/CNPJ" @keyup.enter="handleApplyFilters" />
            </IconField>

            <IconField>
                <InputIcon class="pi pi-envelope" />
                <InputText v-model="emailFilter" placeholder="Filtrar por e-mail" @keyup.enter="handleApplyFilters" />
            </IconField>

            <Select v-model="activeFilter" :options="activeOptions" optionLabel="label" optionValue="value" placeholder="Status" />

            <div class="flex gap-2">
                <Button label="Filtrar" icon="pi pi-search" @click="handleApplyFilters" :loading="loading" />
                <Button label="Limpar" icon="pi pi-times" severity="secondary" outlined @click="handleClearFilters" :disabled="loading" />
            </div>
        </div>

        <DataTable
            class="app-sortable-table app-clickable-rows"
            :value="customers"
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
            tableStyle="min-width: 70rem"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} clientes"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhum cliente encontrado. </template>

            <Column field="name" header="Nome" sortable />
            <Column field="taxId" header="CPF/CNPJ" sortable>
                <template #body="slotProps">
                    {{ formatTaxId(slotProps.data.taxId) }}
                </template>
            </Column>
            <Column field="email" header="E-mail" />
            <Column field="phone" header="Telefone">
                <template #body="slotProps">
                    {{ formatPhone(slotProps.data.phone) }}
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
                        <Button icon="pi pi-pencil" rounded text severity="warning" aria-label="Editar cliente" @click.stop="handleEditCustomer(slotProps.data)" />
                        <Button
                            :icon="slotProps.data.active ? 'pi pi-ban' : 'pi pi-check-circle'"
                            rounded
                            text
                            :severity="slotProps.data.active ? 'danger' : 'success'"
                            :aria-label="slotProps.data.active ? 'Desativar cliente' : 'Ativar cliente'"
                            @click.stop="handleToggleStatus(slotProps.data)"
                        />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="detailsDialogVisible" modal header="Detalhes do cliente" :style="{ width: '52rem' }" @hide="closeDetailsDialog">
            <div v-if="detailLoading" class="py-4">
                <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
            </div>

            <div v-else-if="selectedCustomer" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color mb-2">Imagem do cliente</div>
                    <div class="flex items-center gap-4">
                        <div v-if="detailImageLoading" class="flex items-center justify-center w-24 h-24 border border-surface-200 dark:border-surface-700 rounded-lg">
                            <ProgressSpinner style="width: 1.5rem; height: 1.5rem" strokeWidth="6" />
                        </div>
                        <img v-else-if="detailImageUrl" :src="detailImageUrl" alt="Imagem do cliente" class="w-24 h-24 object-cover rounded-lg border border-surface-200 dark:border-surface-700" />
                        <div v-else class="w-24 h-24 rounded-lg border border-dashed border-surface-300 dark:border-surface-600 flex items-center justify-center text-muted-color">Sem imagem</div>
                        <div class="text-sm text-muted-color">
                            {{ selectedCustomer.imageId ? 'Imagem vinculada ao cadastro.' : 'Nenhuma imagem vinculada.' }}
                        </div>
                    </div>
                </div>

                <div>
                    <div class="text-sm text-muted-color">Nome</div>
                    <div class="font-medium">{{ selectedCustomer.name || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">CPF/CNPJ</div>
                    <div class="font-medium">{{ formatTaxId(selectedCustomer.taxId) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">E-mail</div>
                    <div class="font-medium">{{ selectedCustomer.email || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Telefone</div>
                    <div class="font-medium">{{ formatPhone(selectedCustomer.phone) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Status</div>
                    <Tag :severity="tagSeverity(selectedCustomer.active)" :value="tagLabel(selectedCustomer.active)" />
                </div>
                <div>
                    <div class="text-sm text-muted-color">Criado em</div>
                    <div class="font-medium">{{ formatDate(selectedCustomer.createdAt) }}</div>
                </div>
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color mb-2">Endereço</div>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                            <div class="text-xs text-muted-color">CEP</div>
                            <div class="font-medium">{{ selectedCustomer.address?.zipCode || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Rua</div>
                            <div class="font-medium">{{ selectedCustomer.address?.street || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Número</div>
                            <div class="font-medium">{{ selectedCustomer.address?.number || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Complemento</div>
                            <div class="font-medium">{{ selectedCustomer.address?.complement || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Bairro</div>
                            <div class="font-medium">{{ selectedCustomer.address?.district || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Cidade/UF</div>
                            <div class="font-medium">{{ selectedCustomer.address?.city || '-' }}{{ selectedCustomer.address?.state ? `/${selectedCustomer.address.state}` : '' }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </Dialog>
    </div>
</template>
