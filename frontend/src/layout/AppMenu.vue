<script setup>
import { ref, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth.js';
import AppMenuItem from './AppMenuItem.vue';

const { user, logout } = useAuth();
const model = ref([]);

function normalizedRoles() {
    const roles = Array.isArray(user.value?.roles) ? user.value.roles : [];
    return roles.map((role) => String(role).toUpperCase());
}

function hasAdminRole() {
    return normalizedRoles().includes('ADMIN');
}

function hasCashierRole() {
    return normalizedRoles().some((role) => role === 'CAIXA' || role === 'CASHIER');
}

onMounted(() => {
    const isAdmin = hasAdminRole();
    const isCashierOnly = hasCashierRole() && !isAdmin;
    const salesItems = [{ label: 'Histórico de Vendas', icon: 'pi pi-fw pi-list', to: '/sales/history' }];

    if (isCashierOnly) {
        salesItems.unshift({ label: 'PDV', icon: 'pi pi-fw pi-desktop', to: '/sales/pos' });
    }

    if (isAdmin) {
        salesItems.unshift({ label: 'Monitoramento de Caixas', icon: 'pi pi-fw pi-desktop', to: '/sales/monitoring' });
    }

    model.value = [
        {
            label: 'Início',
            items: [{ label: 'Painel', icon: 'pi pi-fw pi-home', to: '/dashboard' }]
        },
        {
            label: 'Vendas',
            icon: 'pi pi-fw pi-shopping-cart',
            items: salesItems
        },
        {
            label: 'Produtos',
            icon: 'pi pi-fw pi-box',
            items: [{ label: 'Produtos', icon: 'pi pi-fw pi-box', to: '/products/list' }]
        },
        {
            label: 'Cadastros',
            icon: 'pi pi-fw pi-users',
            items: [
                { label: 'Clientes', icon: 'pi pi-fw pi-user', to: '/pages/customers' },
                { label: 'Fornecedores', icon: 'pi pi-fw pi-users', to: '/pages/suppliers' },
                { label: 'Usuários', icon: 'pi pi-fw pi-user-plus', to: '/pages/users' },
                { label: 'Funções', icon: 'pi pi-fw pi-id-card', to: '/pages/profiles' }
            ]
        },
        {
            label: 'Relatórios',
            icon: 'pi pi-fw pi-chart-bar',
            items: [
                { label: 'Vendas do Dia', icon: 'pi pi-fw pi-calendar', to: '/reports/daily-sales' },
                { label: 'Fechamento', icon: 'pi pi-fw pi-calculator', to: '/reports/closing' },
                { label: 'Etiquetas de Gôndola', icon: 'pi pi-fw pi-tags', to: '/reports/labels' },
                { label: 'Clientes', icon: 'pi pi-fw pi-id-card', to: '/reports/customers' }
            ]
        },
        {
            label: `${user.value?.name ?? 'Usuário'}`,
            icon: 'pi pi-fw pi-user',
            items: [{ label: 'Sair', icon: 'pi pi-fw pi-sign-out', command: () => logout() }]
        }
    ];
});
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
