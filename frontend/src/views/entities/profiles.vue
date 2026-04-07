<script setup>
import { onMounted, ref } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { createProfile, deleteProfile, getProfileById, listProfiles, updateProfilePut } from '@/services/profileService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const confirm = useConfirm();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const profiles = ref([]);
const loading = ref(false);
const currentPage = ref(0);
const rowsPerPage = ref(10);
const totalRecords = ref(0);

const selectedProfile = ref(null);
const detailLoading = ref(false);

const createDialogVisible = ref(false);
const createLoading = ref(false);
const createForm = ref({
    name: '',
    description: ''
});

const editDialogVisible = ref(false);
const editLoading = ref(false);
const editForm = ref({
    id: null,
    name: '',
    description: ''
});

const detailsDialogVisible = ref(false);

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

function normalizeProfilePayload(form) {
    return {
        name: form.name.trim(),
        description: form.description.trim()
    };
}

function validateProfilePayload(payload) {
    if (!payload.name || !payload.description) {
        toast.add({
            severity: 'warn',
            summary: 'Campos obrigatórios',
            detail: 'Informe nome e descrição para continuar.',
            life: 3000
        });
        return false;
    }

    return true;
}

async function loadProfiles({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    loading.value = true;

    try {
        const result = await listProfiles({ page, size });
        profiles.value = result.content;
        currentPage.value = result.page;
        rowsPerPage.value = result.size;
        totalRecords.value = result.totalElements;
    } finally {
        loading.value = false;
    }
}

async function loadProfileDetail(profileId) {
    detailLoading.value = true;

    try {
        selectedProfile.value = await getProfileById(profileId);
        return selectedProfile.value;
    } finally {
        detailLoading.value = false;
    }
}

function closeCreateDialog() {
    createDialogVisible.value = false;
    createLoading.value = false;
    createForm.value = { name: '', description: '' };
}

function closeEditDialog() {
    editDialogVisible.value = false;
    editLoading.value = false;
    editForm.value = { id: null, name: '', description: '' };
}

function closeDetailsDialog() {
    detailsDialogVisible.value = false;
    selectedProfile.value = null;
}

function handleOpenCreateDialog() {
    createDialogVisible.value = true;
}

async function handleRowClick(event) {
    detailsDialogVisible.value = true;

    try {
        await loadProfileDetail(event.data.id);
    } catch (error) {
        detailsDialogVisible.value = false;
        showApiErrorToast(toast, error);
    }
}

async function handleEditProfile(profile) {
    editDialogVisible.value = true;
    editLoading.value = true;

    try {
        const detail = await loadProfileDetail(profile.id);
        editForm.value = {
            id: detail.id,
            name: detail.name,
            description: detail.description
        };
    } catch (error) {
        editDialogVisible.value = false;
        showApiErrorToast(toast, error);
    } finally {
        editLoading.value = false;
    }
}

async function handleSaveCreate() {
    const payload = normalizeProfilePayload(createForm.value);

    if (!validateProfilePayload(payload)) {
        return;
    }

    try {
        createLoading.value = true;
        await createProfile(payload);
        await loadProfiles({ page: 0, size: rowsPerPage.value });
        toast.add({
            severity: 'success',
            summary: 'Função criada',
            detail: 'Nova função criada com sucesso.',
            life: 3000
        });
        closeCreateDialog();
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        createLoading.value = false;
    }
}

async function handleSaveEdit() {
    if (!editForm.value.id) {
        return;
    }

    const payload = normalizeProfilePayload(editForm.value);

    if (!validateProfilePayload(payload)) {
        return;
    }

    try {
        editLoading.value = true;
        const updatedProfile = await updateProfilePut(editForm.value.id, payload);
        await loadProfiles();
        selectedProfile.value = updatedProfile;
        toast.add({
            severity: 'success',
            summary: 'Função atualizada',
            detail: 'Atualização concluída com sucesso.',
            life: 3000
        });
        closeEditDialog();
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        editLoading.value = false;
    }
}

function handleDeleteProfile(profile) {
    confirm.require({
        message: `Deseja remover a função ${profile.name}?`,
        header: 'Remover função',
        icon: 'pi pi-exclamation-triangle',
        rejectProps: {
            label: 'Cancelar',
            severity: 'secondary',
            outlined: true
        },
        acceptProps: {
            label: 'Remover',
            severity: 'danger'
        },
        accept: async () => {
            try {
                await deleteProfile(profile.id);
                await loadProfiles({ page: 0, size: rowsPerPage.value });
                if (selectedProfile.value?.id === profile.id) {
                    selectedProfile.value = null;
                    detailsDialogVisible.value = false;
                }
                toast.add({
                    severity: 'success',
                    summary: 'Função removida',
                    detail: `${profile.name} foi removida com sucesso.`,
                    life: 3000
                });
            } catch (error) {
                showApiErrorToast(toast, error);
            }
        }
    });
}

async function handlePage(event) {
    try {
        await loadProfiles({ page: event.page, size: event.rows });
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

onMounted(async () => {
    try {
        await loadProfiles();
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
            <div class="font-semibold text-xl">Funções</div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-plus" label="Nova função" @click="handleOpenCreateDialog" />
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined @click="loadProfiles" :loading="loading" />
            </div>
        </div>

        <DataTable
            :value="profiles"
            dataKey="id"
            lazy
            paginator
            :rows="rowsPerPage"
            :first="currentPage * rowsPerPage"
            :totalRecords="totalRecords"
            :loading="loading"
            @page="handlePage"
            @row-click="handleRowClick"
            tableStyle="min-width: 52rem"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} funções"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhuma função encontrada. </template>

            <Column field="name" header="Nome" />
            <Column field="description" header="Descrição" />

            <Column header="Criada em">
                <template #body="slotProps">
                    {{ formatDate(slotProps.data.createdAt) }}
                </template>
            </Column>

            <Column header="Atualizada em">
                <template #body="slotProps">
                    {{ formatUpdatedDate(slotProps.data.updatedAt, slotProps.data.createdAt) }}
                </template>
            </Column>

            <Column header="Ações" :exportable="false" style="width: 8rem">
                <template #body="slotProps">
                    <div class="flex items-center gap-2">
                        <Button icon="pi pi-pencil" rounded text severity="warning" aria-label="Editar função" @click.stop="handleEditProfile(slotProps.data)" />
                        <Button icon="pi pi-trash" rounded text severity="danger" aria-label="Remover função" @click.stop="handleDeleteProfile(slotProps.data)" />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="createDialogVisible" modal header="Nova função" :style="{ width: '32rem' }" @hide="closeCreateDialog">
            <div class="flex flex-col gap-4">
                <div>
                    <label for="create-profile-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="create-profile-name" v-model="createForm.name" class="w-full" :disabled="createLoading" />
                </div>

                <div>
                    <label for="create-profile-description" class="block mb-2 font-medium">Descrição</label>
                    <Textarea id="create-profile-description" v-model="createForm.description" class="w-full" rows="4" :disabled="createLoading" />
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" severity="secondary" outlined @click="closeCreateDialog" :disabled="createLoading" />
                <Button label="Criar" icon="pi pi-check" @click="handleSaveCreate" :loading="createLoading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="editDialogVisible" modal header="Editar função" :style="{ width: '32rem' }" @hide="closeEditDialog">
            <div class="flex flex-col gap-4">
                <div>
                    <label for="edit-profile-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="edit-profile-name" v-model="editForm.name" class="w-full" :disabled="editLoading" />
                </div>

                <div>
                    <label for="edit-profile-description" class="block mb-2 font-medium">Descrição</label>
                    <Textarea id="edit-profile-description" v-model="editForm.description" class="w-full" rows="4" :disabled="editLoading" />
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" severity="secondary" outlined @click="closeEditDialog" :disabled="editLoading" />
                <Button label="Salvar" icon="pi pi-check" @click="handleSaveEdit" :loading="editLoading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="detailsDialogVisible" modal header="Detalhes da função" :style="{ width: '36rem' }" @hide="closeDetailsDialog">
            <div v-if="detailLoading" class="py-4">
                <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
            </div>

            <div v-else-if="selectedProfile" class="grid grid-cols-1 gap-4">
                <div>
                    <div class="text-sm text-muted-color">Nome</div>
                    <div class="font-medium">{{ selectedProfile.name }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Descrição</div>
                    <div class="font-medium">{{ selectedProfile.description }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Criada em</div>
                    <div class="font-medium">{{ formatDate(selectedProfile.createdAt) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Atualizada em</div>
                    <div class="font-medium">{{ formatUpdatedDate(selectedProfile.updatedAt, selectedProfile.createdAt) }}</div>
                </div>
            </div>
        </Dialog>
    </div>
</template>

<style scoped>
:deep(.p-datatable-tbody > tr) {
    cursor: pointer;
}
</style>
