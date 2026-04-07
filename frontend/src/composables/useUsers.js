import { ref } from 'vue';
import { createUser, getUserById, listUsers, updateUserPatch, updateUserPut } from '@/services/userService.js';

const users = ref([]);
const loading = ref(false);
const currentPage = ref(0);
const rowsPerPage = ref(10);
const totalRecords = ref(0);
const selectedUser = ref(null);
const detailLoading = ref(false);

const nameFilter = ref('');
const emailFilter = ref('');
const activeFilter = ref(null);

async function loadUsers({ page = currentPage.value, size = rowsPerPage.value } = {}) {
    loading.value = true;

    try {
        const result = await listUsers({
            page,
            size,
            name: nameFilter.value,
            email: emailFilter.value,
            active: activeFilter.value
        });

        users.value = result.content;
        currentPage.value = result.page;
        rowsPerPage.value = result.size;
        totalRecords.value = result.totalElements;
    } finally {
        loading.value = false;
    }
}

async function applyFilters() {
    currentPage.value = 0;
    await loadUsers({ page: 0, size: rowsPerPage.value });
}

async function clearFilters() {
    nameFilter.value = '';
    emailFilter.value = '';
    activeFilter.value = null;
    currentPage.value = 0;

    await loadUsers({ page: 0, size: rowsPerPage.value });
}

async function onPage(event) {
    await loadUsers({ page: event.page, size: event.rows });
}

async function loadUserDetail(userId) {
    detailLoading.value = true;
    try {
        selectedUser.value = await getUserById(userId);
        return selectedUser.value;
    } finally {
        detailLoading.value = false;
    }
}

function clearSelectedUser() {
    selectedUser.value = null;
}

async function saveUserPatch(userId, payload) {
    const updatedUser = await updateUserPatch(userId, payload);
    await loadUsers();
    selectedUser.value = updatedUser;
    return updatedUser;
}

async function saveUserCreate(payload) {
    const createdUser = await createUser(payload);
    await loadUsers({ page: 0, size: rowsPerPage.value });
    selectedUser.value = createdUser;
    return createdUser;
}

async function saveUserPut(userId, payload) {
    const updatedUser = await updateUserPut(userId, payload);
    await loadUsers();
    selectedUser.value = updatedUser;
    return updatedUser;
}

async function toggleUserStatus(userId, active) {
    const updatedUser = await updateUserPatch(userId, { active });
    await loadUsers();
    if (selectedUser.value?.id === userId) {
        selectedUser.value = updatedUser;
    }
    return updatedUser;
}

export function useUsers() {
    return {
        users,
        loading,
        currentPage,
        rowsPerPage,
        totalRecords,
        selectedUser,
        detailLoading,
        nameFilter,
        emailFilter,
        activeFilter,
        loadUsers,
        loadUserDetail,
        clearSelectedUser,
        saveUserCreate,
        saveUserPatch,
        saveUserPut,
        toggleUserStatus,
        applyFilters,
        clearFilters,
        onPage
    };
}
