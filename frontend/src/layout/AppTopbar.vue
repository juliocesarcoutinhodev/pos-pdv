<script setup>
import { useLayout } from '@/layout/composables/layout';
import { useAuth } from '@/composables/useAuth.js';
import AppConfigurator from './AppConfigurator.vue';

const { toggleMenu, toggleDarkMode, isDarkTheme } = useLayout();
const { logout } = useAuth();
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <button class="layout-menu-button layout-topbar-action" @click="toggleMenu">
                <i class="pi pi-bars"></i>
            </button>
            <router-link to="/dashboard" class="layout-topbar-logo">
                <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-8 h-8">
                    <rect x="8" y="8" width="64" height="44" rx="6" stroke="var(--primary-color)" stroke-width="3" fill="var(--primary-color)" fill-opacity="0.1" />
                    <rect x="15" y="14" width="50" height="28" rx="3" fill="var(--primary-color)" fill-opacity="0.15" stroke="var(--primary-color)" stroke-width="2" />
                    <line x1="22" y1="20" x2="58" y2="20" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round" />
                    <line x1="22" y1="26" x2="50" y2="26" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round" opacity="0.6" />
                    <line x1="22" y1="32" x2="44" y2="32" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round" opacity="0.3" />
                    <text x="40" y="38" text-anchor="middle" font-size="14" font-weight="bold" fill="var(--primary-color)">$</text>
                    <path d="M34 52 L46 52 L48 60 L32 60Z" fill="var(--primary-color)" stroke="var(--primary-color)" stroke-width="2" stroke-linejoin="round" />
                    <rect x="24" y="60" width="32" height="4" rx="2" fill="var(--primary-color)" />
                    <rect x="18" y="66" width="44" height="8" rx="2" stroke="var(--primary-color)" stroke-width="2" fill="var(--primary-color)" fill-opacity="0.08" />
                    <rect x="33" y="68" width="14" height="4" rx="2" stroke="var(--primary-color)" stroke-width="1.5" fill="var(--primary-color)" fill-opacity="0.2" />
                </svg>

                <span>POS PDV</span>
            </router-link>
        </div>

        <div class="layout-topbar-actions">
            <div class="layout-config-menu">
                <button type="button" class="layout-topbar-action" @click="toggleDarkMode">
                    <i :class="['pi', { 'pi-moon': isDarkTheme, 'pi-sun': !isDarkTheme }]"></i>
                </button>
                <div class="relative">
                    <button
                        v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'p-anchored-overlay-enter-active', leaveToClass: 'hidden', leaveActiveClass: 'p-anchored-overlay-leave-active', hideOnOutsideClick: true }"
                        type="button"
                        class="layout-topbar-action layout-topbar-action-highlight"
                    >
                        <i class="pi pi-palette"></i>
                    </button>
                    <AppConfigurator />
                </div>
            </div>

            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{ selector: '@next', enterFromClass: 'hidden', enterActiveClass: 'p-anchored-overlay-enter-active', leaveToClass: 'hidden', leaveActiveClass: 'p-anchored-overlay-leave-active', hideOnOutsideClick: true }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <button type="button" class="layout-topbar-action" @click="logout">
                        <i class="pi pi-sign-out"></i>
                        <span>Sair</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
