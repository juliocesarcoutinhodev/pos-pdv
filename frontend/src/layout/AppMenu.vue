<script setup>
import { ref } from 'vue';
import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: 'Início',
        items: [
            { label: 'Painel', icon: 'pi pi-fw pi-home', to: '/dashboard' }
        ]
    },
    {
        label: 'Vendas',
        icon: 'pi pi-fw pi-shopping-cart',
        items: [
            { label: 'PDV', icon: 'pi pi-fw pi-desktop', to: '/vendas/pdv' },
            { label: 'Histórico de Vendas', icon: 'pi pi-fw pi-list', to: '/vendas/historico' }
        ]
    },
    {
        label: 'Produtos',
        icon: 'pi pi-fw pi-box',
        items: [
            { label: 'Lista de Produtos', icon: 'pi pi-fw pi-box', to: '/produtos/lista' },
            { label: 'Estoque', icon: 'pi pi-fw pi-inbox', to: '/produtos/estoque' }
        ]
    },
    {
        label: 'Cadastros',
        icon: 'pi pi-fw pi-users',
        items: [
            { label: 'Clientes', icon: 'pi pi-fw pi-user', to: '/entidades/clientes' },
            { label: 'Fornecedores', icon: 'pi pi-fw pi-users', to: '/entidades/fornecedores' }
        ]
    },
    {
        label: 'Relatórios',
        icon: 'pi pi-fw pi-chart-bar',
        items: [
            { label: 'Vendas do Dia', icon: 'pi pi-fw pi-calendar', to: '/relatorios/vendas-dia' },
            { label: 'Fechamento', icon: 'pi pi-fw pi-calculator', to: '/relatorios/fechamento' }
        ]
    },
    {
        label: 'Sistema',
        icon: 'pi pi-fw pi-cog',
        items: [
            { label: 'Sair', icon: 'pi pi-fw pi-sign-out', to: '/' }
        ]
    }
]);
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
