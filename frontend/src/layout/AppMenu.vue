<script setup>
import { ref, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth.js';
import AppMenuItem from './AppMenuItem.vue';

const { user, logout } = useAuth();
const model = ref([]);

onMounted(() => {
    model.value = [
        {
            label: 'Início',
            items: [{ label: 'Painel', icon: 'pi pi-fw pi-home', to: '/dashboard' }]
        },
        {
            label: 'Vendas',
            icon: 'pi pi-fw pi-shopping-cart',
            items: [
                { label: 'PDV', icon: 'pi pi-fw pi-desktop', to: '/sales/pos' },
                { label: 'Histórico de Vendas', icon: 'pi pi-fw pi-list', to: '/sales/history' }
            ]
        },
        {
            label: 'Produtos',
            icon: 'pi pi-fw pi-box',
            items: [
                { label: 'Lista de Produtos', icon: 'pi pi-fw pi-box', to: '/products/list' },
                { label: 'Estoque', icon: 'pi pi-fw pi-inbox', to: '/products/inventory' }
            ]
        },
        {
            label: 'Cadastros',
            icon: 'pi pi-fw pi-users',
            items: [
                { label: 'Clientes', icon: 'pi pi-fw pi-user', to: '/entities/customers' },
                { label: 'Fornecedores', icon: 'pi pi-fw pi-users', to: '/entities/suppliers' },
                { label: 'Usuários', icon: 'pi pi-fw pi-user-plus', to: '/entities/users' },
                { label: 'Funções', icon: 'pi pi-fw pi-id-card', to: '/entities/profiles' }
            ]
        },
        {
            label: 'Relatórios',
            icon: 'pi pi-fw pi-chart-bar',
            items: [
                { label: 'Vendas do Dia', icon: 'pi pi-fw pi-calendar', to: '/reports/daily-sales' },
                { label: 'Fechamento', icon: 'pi pi-fw pi-calculator', to: '/reports/closing' }
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
