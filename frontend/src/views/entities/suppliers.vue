<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { useSuppliers } from '@/composables/useSuppliers.js';
import { deactivateSupplier, reactivateSupplier } from '@/services/supplierService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const router = useRouter();
const confirm = useConfirm();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const {
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
} = useSuppliers();

const detailsDialogVisible = ref(false);

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
    if (digits.length !== 14) {
        return value;
    }

    return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`;
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

function handleOpenCreatePage() {
    router.push('/entities/suppliers/new');
}

function handleEditSupplier(supplier) {
    router.push(`/entities/suppliers/${supplier.id}/edit`);
}

async function handleOpenDetails(supplierId) {
    detailsDialogVisible.value = true;

    try {
        await loadSupplierDetail(supplierId);
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
    clearSelectedSupplier();
}

function handleToggleStatus(supplier) {
    const nextActive = !supplier.active;
    const actionLabel = nextActive ? 'ativar' : 'desativar';

    confirm.require({
        message: `Deseja ${actionLabel} o fornecedor ${supplier.name}?`,
        header: `${nextActive ? 'Ativar' : 'Desativar'} fornecedor`,
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
                    await reactivateSupplier(supplier.id);
                } else {
                    await deactivateSupplier(supplier.id);
                }

                await loadSuppliers();
                if (detailsDialogVisible.value && selectedSupplier.value?.id === supplier.id) {
                    await loadSupplierDetail(supplier.id);
                }

                toast.add({
                    severity: 'success',
                    summary: nextActive ? 'Fornecedor ativado' : 'Fornecedor desativado',
                    detail: `${supplier.name} foi ${nextActive ? 'ativado' : 'desativado'} com sucesso.`,
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
        await loadSuppliers();
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
        await loadSuppliers();
    } catch (error) {
        showApiErrorToast(toast, error);
    }
});
</script>

<template>
    <div class="card">
        <Toast />
        <ConfirmDialog />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-4">
            <div class="font-semibold text-xl">Fornecedores</div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-plus" label="Novo fornecedor" @click="handleOpenCreatePage" />
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined @click="handleRefresh" :loading="loading" />
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-5 gap-3 mb-4">
            <IconField>
                <InputIcon class="pi pi-building" />
                <InputText v-model="nameFilter" placeholder="Filtrar por nome" @keyup.enter="handleApplyFilters" />
            </IconField>

            <IconField>
                <InputIcon class="pi pi-id-card" />
                <InputText v-model="taxIdFilter" placeholder="Filtrar por CNPJ" @keyup.enter="handleApplyFilters" />
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
            :value="suppliers"
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
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} fornecedores"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhum fornecedor encontrado. </template>

            <Column field="name" header="Nome" sortable />
            <Column field="taxId" header="CNPJ" sortable>
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
                        <Button icon="pi pi-pencil" rounded text severity="warning" aria-label="Editar fornecedor" @click.stop="handleEditSupplier(slotProps.data)" />
                        <Button
                            :icon="slotProps.data.active ? 'pi pi-ban' : 'pi pi-check-circle'"
                            rounded
                            text
                            :severity="slotProps.data.active ? 'danger' : 'success'"
                            :aria-label="slotProps.data.active ? 'Desativar fornecedor' : 'Ativar fornecedor'"
                            @click.stop="handleToggleStatus(slotProps.data)"
                        />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="detailsDialogVisible" modal header="Detalhes do fornecedor" :style="{ width: '52rem' }" @hide="closeDetailsDialog">
            <div v-if="detailLoading" class="py-4">
                <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
            </div>

            <div v-else-if="selectedSupplier" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <div class="text-sm text-muted-color">Nome</div>
                    <div class="font-medium">{{ selectedSupplier.name || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">CNPJ</div>
                    <div class="font-medium">{{ formatTaxId(selectedSupplier.taxId) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">E-mail</div>
                    <div class="font-medium">{{ selectedSupplier.email || '-' }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Telefone</div>
                    <div class="font-medium">{{ formatPhone(selectedSupplier.phone) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Status</div>
                    <Tag :severity="tagSeverity(selectedSupplier.active)" :value="tagLabel(selectedSupplier.active)" />
                </div>
                <div>
                    <div class="text-sm text-muted-color">Criado em</div>
                    <div class="font-medium">{{ formatDate(selectedSupplier.createdAt) }}</div>
                </div>
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color mb-2">Endereço</div>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
                        <div>
                            <div class="text-xs text-muted-color">CEP</div>
                            <div class="font-medium">{{ selectedSupplier.address?.zipCode || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Rua</div>
                            <div class="font-medium">{{ selectedSupplier.address?.street || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Número</div>
                            <div class="font-medium">{{ selectedSupplier.address?.number || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Complemento</div>
                            <div class="font-medium">{{ selectedSupplier.address?.complement || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Bairro</div>
                            <div class="font-medium">{{ selectedSupplier.address?.district || '-' }}</div>
                        </div>
                        <div>
                            <div class="text-xs text-muted-color">Cidade/UF</div>
                            <div class="font-medium">{{ selectedSupplier.address?.city || '-' }}{{ selectedSupplier.address?.state ? `/${selectedSupplier.address.state}` : '' }}</div>
                        </div>
                    </div>
                </div>
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color mb-2">Contatos</div>
                    <div v-if="selectedSupplier.contacts.length === 0" class="text-muted-color">Sem contatos cadastrados.</div>
                    <div v-else class="flex flex-col gap-2">
                        <div v-for="(contact, index) in selectedSupplier.contacts" :key="contact.id || `${contact.name}-${index}`" class="border border-surface-200 dark:border-surface-700 rounded-lg p-3">
                            <div class="font-medium">{{ contact.name || '-' }}</div>
                            <div class="text-sm text-muted-color">{{ contact.email || 'Sem e-mail' }} • {{ formatPhone(contact.phone) }}</div>
                        </div>
                    </div>
                </div>
            </div>
        </Dialog>
    </div>
</template>
