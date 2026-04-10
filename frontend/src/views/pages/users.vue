<script setup>
import { onMounted, ref } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { useUsers } from '@/composables/useUsers.js';
import { listProfiles } from '@/services/profileService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const confirm = useConfirm();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const {
    users,
    loading,
    currentPage,
    rowsPerPage,
    totalRecords,
    sortField,
    sortOrder,
    selectedUser,
    detailLoading,
    nameFilter,
    emailFilter,
    activeFilter,
    loadUsers,
    loadUserDetail,
    clearSelectedUser,
    saveUserCreate,
    saveUserPut,
    toggleUserStatus,
    applyFilters,
    clearFilters,
    onPage,
    onSort
} = useUsers();

const detailsDialogVisible = ref(false);
const createDialogVisible = ref(false);
const createLoading = ref(false);
const editDialogVisible = ref(false);
const editLoading = ref(false);
const profilesLoading = ref(false);
const profileOptions = ref([]);
const createForm = ref({
    name: '',
    email: '',
    password: '',
    profileIds: []
});
const editForm = ref({
    id: null,
    name: '',
    email: '',
    password: '',
    profileIds: []
});

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

function tagSeverity(active) {
    return active ? 'success' : 'danger';
}

function tagLabel(active) {
    return active ? 'Ativo' : 'Inativo';
}

function normalizeProfileIds(profileIds) {
    return [...new Set((profileIds || []).map((profileId) => profileId.trim()).filter(Boolean))].sort();
}

function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

async function loadProfileOptions() {
    profilesLoading.value = true;

    try {
        const result = await listProfiles({ page: 0, size: 100 });
        profileOptions.value = result.content.map((profile) => ({
            label: profile.name,
            value: profile.id
        }));
    } finally {
        profilesLoading.value = false;
    }
}

async function ensureProfileOptionsLoaded() {
    if (profileOptions.value.length > 0 || profilesLoading.value) {
        return;
    }

    await loadProfileOptions();
}

async function handleOpenDetails(userId) {
    detailsDialogVisible.value = true;

    try {
        await loadUserDetail(userId);
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
    clearSelectedUser();
}

function closeCreateDialog() {
    createDialogVisible.value = false;
    createLoading.value = false;
    createForm.value = { name: '', email: '', password: '', profileIds: [] };
}

function closeEditDialog() {
    editDialogVisible.value = false;
    editLoading.value = false;
    editForm.value = { id: null, name: '', email: '', password: '', profileIds: [] };
}

async function handleOpenCreateDialog() {
    try {
        await ensureProfileOptionsLoaded();
        createDialogVisible.value = true;
    } catch (error) {
        showApiErrorToast(toast, error);
    }
}

async function handleEditUser(user) {
    try {
        await ensureProfileOptionsLoaded();
        editDialogVisible.value = true;
        editLoading.value = true;
        const detail = await loadUserDetail(user.id);
        const profileIds = profileOptions.value.filter((option) => detail.roles.includes(option.label)).map((option) => option.value);

        editForm.value = {
            id: detail.id,
            name: detail.name,
            email: detail.email,
            password: '',
            profileIds
        };
    } catch (error) {
        editDialogVisible.value = false;
        showApiErrorToast(toast, error);
    } finally {
        editLoading.value = false;
    }
}

async function handleSaveCreate() {
    const nextName = createForm.value.name.trim();
    const nextEmail = createForm.value.email.trim();
    const nextPassword = createForm.value.password.trim();
    const nextProfileIds = normalizeProfileIds(createForm.value.profileIds);

    if (!nextName || !nextEmail || !nextPassword) {
        toast.add({
            severity: 'warn',
            summary: 'Campos obrigatórios',
            detail: 'Informe nome, e-mail e senha para criar o usuário.',
            life: 3000
        });
        return;
    }

    if (!isValidEmail(nextEmail)) {
        toast.add({
            severity: 'warn',
            summary: 'E-mail inválido',
            detail: 'Informe um e-mail válido para continuar.',
            life: 3000
        });
        return;
    }

    if (nextPassword.length < 6) {
        toast.add({
            severity: 'warn',
            summary: 'Senha inválida',
            detail: 'A senha deve ter pelo menos 6 caracteres.',
            life: 3000
        });
        return;
    }

    if (nextProfileIds.length === 0) {
        toast.add({
            severity: 'warn',
            summary: 'Funções obrigatórias',
            detail: 'Selecione ao menos uma função para criar o usuário.',
            life: 3500
        });
        return;
    }

    try {
        createLoading.value = true;

        await saveUserCreate({
            name: nextName,
            email: nextEmail,
            password: nextPassword,
            roleIds: nextProfileIds
        });

        toast.add({
            severity: 'success',
            summary: 'Usuário criado',
            detail: 'Novo usuário criado com sucesso.',
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
    if (!selectedUser.value || !editForm.value.id) {
        return;
    }

    const nextName = editForm.value.name.trim();
    const nextEmail = editForm.value.email.trim();
    const nextPassword = editForm.value.password.trim();
    const nextProfileIds = normalizeProfileIds(editForm.value.profileIds);

    if (!nextName || !nextEmail) {
        toast.add({
            severity: 'warn',
            summary: 'Campos obrigatórios',
            detail: 'Informe nome e e-mail para salvar.',
            life: 3000
        });
        return;
    }

    if (!isValidEmail(nextEmail)) {
        toast.add({
            severity: 'warn',
            summary: 'E-mail inválido',
            detail: 'Informe um e-mail válido para continuar.',
            life: 3000
        });
        return;
    }

    if (nextPassword && nextPassword.length < 6) {
        toast.add({
            severity: 'warn',
            summary: 'Senha inválida',
            detail: 'A senha deve ter pelo menos 6 caracteres.',
            life: 3000
        });
        return;
    }

    if (nextProfileIds.length === 0) {
        toast.add({
            severity: 'warn',
            summary: 'Funções obrigatórias',
            detail: 'Selecione ao menos uma função para atualização do usuário.',
            life: 3500
        });
        return;
    }

    try {
        editLoading.value = true;

        const putPayload = {
            name: nextName,
            email: nextEmail,
            roleIds: nextProfileIds
        };

        if (nextPassword) {
            putPayload.password = nextPassword;
        }

        await saveUserPut(editForm.value.id, putPayload);

        toast.add({
            severity: 'success',
            summary: 'Usuário atualizado',
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

function handleToggleStatus(user) {
    const nextActive = !user.active;
    const actionLabel = nextActive ? 'ativar' : 'desativar';

    confirm.require({
        message: `Deseja ${actionLabel} o usuário ${user.name}?`,
        header: `${nextActive ? 'Ativar' : 'Desativar'} usuário`,
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
                await toggleUserStatus(user.id, nextActive);
                toast.add({
                    severity: 'success',
                    summary: nextActive ? 'Usuário ativado' : 'Usuário desativado',
                    detail: `${user.name} foi ${nextActive ? 'ativado' : 'desativado'} com sucesso.`,
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
        await Promise.all([loadUsers(), loadProfileOptions()]);
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
            <div class="font-semibold text-xl">Usuários</div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-user-plus" label="Novo usuário" @click="handleOpenCreateDialog" :disabled="profilesLoading" />
                <Button icon="pi pi-refresh" label="Atualizar" severity="secondary" outlined @click="loadUsers" :loading="loading" />
            </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-4 gap-3 mb-4">
            <IconField>
                <InputIcon class="pi pi-user" />
                <InputText v-model="nameFilter" placeholder="Filtrar por nome" @keyup.enter="handleApplyFilters" />
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
            :value="users"
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
            tableStyle="min-width: 60rem"
            paginatorTemplate="RowsPerPageDropdown FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
            currentPageReportTemplate="Exibindo {first} a {last} de {totalRecords} usuários"
            :rowsPerPageOptions="[10, 20, 50]"
        >
            <template #empty> Nenhum usuário encontrado. </template>

            <Column field="name" header="Nome" sortable />
            <Column field="email" header="E-mail" />
            <Column field="provider" header="Provider" />

            <Column header="Status">
                <template #body="slotProps">
                    <Tag :severity="tagSeverity(slotProps.data.active)" :value="tagLabel(slotProps.data.active)" />
                </template>
            </Column>

            <Column header="Criado em">
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
                        <Button icon="pi pi-pencil" rounded text severity="warning" aria-label="Editar usuário" @click.stop="handleEditUser(slotProps.data)" />
                        <Button
                            :icon="slotProps.data.active ? 'pi pi-user-minus' : 'pi pi-user-plus'"
                            rounded
                            text
                            :severity="slotProps.data.active ? 'danger' : 'success'"
                            :aria-label="slotProps.data.active ? 'Desativar usuário' : 'Ativar usuário'"
                            @click.stop="handleToggleStatus(slotProps.data)"
                        />
                    </div>
                </template>
            </Column>
        </DataTable>

        <Dialog v-model:visible="createDialogVisible" modal header="Novo usuário" :style="{ width: '32rem' }" @hide="closeCreateDialog">
            <div class="flex flex-col gap-4">
                <div>
                    <label for="create-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="create-name" v-model="createForm.name" class="w-full" :disabled="createLoading" />
                </div>

                <div>
                    <label for="create-email" class="block mb-2 font-medium">E-mail</label>
                    <InputText id="create-email" v-model="createForm.email" class="w-full" :disabled="createLoading" />
                </div>

                <div>
                    <label for="create-password" class="block mb-2 font-medium">Senha</label>
                    <Password id="create-password" v-model="createForm.password" class="w-full" :feedback="false" toggleMask fluid :disabled="createLoading" />
                    <small class="text-muted-color">A senha inicial deve ter pelo menos 6 caracteres.</small>
                </div>

                <div>
                    <label for="create-profile-ids" class="block mb-2 font-medium">Funções</label>
                    <MultiSelect
                        id="create-profile-ids"
                        v-model="createForm.profileIds"
                        :options="profileOptions"
                        optionLabel="label"
                        optionValue="value"
                        display="chip"
                        filter
                        placeholder="Selecione as funções"
                        class="w-full"
                        :disabled="createLoading || profilesLoading"
                    />
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" severity="secondary" outlined @click="closeCreateDialog" :disabled="createLoading" />
                <Button label="Criar" icon="pi pi-check" @click="handleSaveCreate" :loading="createLoading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="editDialogVisible" modal header="Editar usuário" :style="{ width: '32rem' }" @hide="closeEditDialog">
            <div class="flex flex-col gap-4">
                <div>
                    <label for="edit-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="edit-name" v-model="editForm.name" class="w-full" :disabled="editLoading" />
                </div>

                <div>
                    <label for="edit-email" class="block mb-2 font-medium">E-mail</label>
                    <InputText id="edit-email" v-model="editForm.email" class="w-full" :disabled="editLoading" />
                </div>

                <div>
                    <label for="edit-password" class="block mb-2 font-medium">Nova senha (opcional)</label>
                    <Password id="edit-password" v-model="editForm.password" class="w-full" :feedback="false" toggleMask fluid :disabled="editLoading" />
                    <small class="text-muted-color">Preencha apenas se quiser alterar a senha (min. 6 caracteres).</small>
                </div>

                <div>
                    <label for="edit-profile-ids" class="block mb-2 font-medium">Funções</label>
                    <MultiSelect
                        id="edit-profile-ids"
                        v-model="editForm.profileIds"
                        :options="profileOptions"
                        optionLabel="label"
                        optionValue="value"
                        display="chip"
                        filter
                        placeholder="Selecione as funções"
                        class="w-full"
                        :disabled="editLoading || profilesLoading"
                    />
                </div>
            </div>

            <template #footer>
                <Button label="Cancelar" severity="secondary" outlined @click="closeEditDialog" :disabled="editLoading" />
                <Button label="Salvar" icon="pi pi-check" @click="handleSaveEdit" :loading="editLoading" />
            </template>
        </Dialog>

        <Dialog v-model:visible="detailsDialogVisible" modal header="Detalhes do usuário" :style="{ width: '40rem' }" @hide="closeDetailsDialog">
            <div v-if="detailLoading" class="py-4">
                <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
            </div>

            <div v-else-if="selectedUser" class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                    <div class="text-sm text-muted-color">Nome</div>
                    <div class="font-medium">{{ selectedUser.name }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">E-mail</div>
                    <div class="font-medium">{{ selectedUser.email }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Provider</div>
                    <div class="font-medium">{{ selectedUser.provider }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Status</div>
                    <Tag :severity="tagSeverity(selectedUser.active)" :value="tagLabel(selectedUser.active)" />
                </div>
                <div>
                    <div class="text-sm text-muted-color">Criado em</div>
                    <div class="font-medium">{{ formatDate(selectedUser.createdAt) }}</div>
                </div>
                <div>
                    <div class="text-sm text-muted-color">Atualizado em</div>
                    <div class="font-medium">{{ formatUpdatedDate(selectedUser.updatedAt, selectedUser.createdAt) }}</div>
                </div>
                <div class="md:col-span-2">
                    <div class="text-sm text-muted-color mb-2">Funções</div>
                    <div class="flex flex-wrap gap-2">
                        <Tag v-for="role in selectedUser.roles" :key="role" :value="role" severity="contrast" />
                        <span v-if="selectedUser.roles.length === 0" class="text-muted-color">Sem funções atribuídas.</span>
                    </div>
                </div>
            </div>
        </Dialog>
    </div>
</template>

<style scoped></style>
