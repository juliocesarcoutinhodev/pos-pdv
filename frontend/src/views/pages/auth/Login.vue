<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { useAuth } from '@/composables/useAuth.js';
import { useErrorHandler } from '@/services/errorHandler.js';
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import { useToast } from 'primevue/usetoast';

const email = ref('');
const password = ref('');
const checked = ref(false);
const { loading, handleLogin } = useAuth();
const { extractRateLimit, showApiErrorToast } = useErrorHandler();
const toast = useToast();
const route = useRoute();

const rateLimitActive = ref(false);
const countdownSeconds = ref(0);
let countdownTimer = null;

const sessionExpired = computed(() => route.query.sessionExpired === 'true');

function startRateLimitCountdown(retryAfter) {
    rateLimitActive.value = true;
    countdownSeconds.value = retryAfter;

    if (countdownTimer) clearInterval(countdownTimer);
    countdownTimer = setInterval(() => {
        countdownSeconds.value--;
        if (countdownSeconds.value <= 0) {
            rateLimitActive.value = false;
            clearInterval(countdownTimer);
        }
    }, 1000);
}

async function onLogin() {
    if (!email.value || !password.value) {
        toast.add({
            severity: 'warn',
            summary: 'Campos obrigatórios',
            detail: 'Informe seu e-mail e senha',
            life: 3000
        });
        return;
    }

    try {
        await handleLogin(email.value, password.value);
    } catch (error) {
        const rateLimit = extractRateLimit(error);
        if (rateLimit) {
            startRateLimitCountdown(rateLimit.retryAfter);
            return;
        }

        showApiErrorToast(toast, error);
    }
}
</script>

<template>
    <FloatingConfigurator />
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen">
        <Toast />
        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-6 w-20 shrink-0 mx-auto">
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
                        <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Bem-vindo ao POS PDV!</div>
                        <span class="text-muted-color font-medium">Faça login para continuar</span>
                    </div>

                    <Message v-if="sessionExpired" severity="warn" :closable="false" class="mb-4"> Sua sessão expirou. Faça login novamente. </Message>

                    <div>
                        <label for="email1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">E-mail</label>
                        <InputText id="email1" type="text" placeholder="Seu e-mail" class="w-full md:w-[30rem] mb-8" v-model="email" :disabled="loading || rateLimitActive" @keyup.enter="onLogin" />

                        <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Senha</label>
                        <Password id="password1" v-model="password" placeholder="Sua senha" :toggleMask="true" :feedback="false" fluid :disabled="loading || rateLimitActive" @keyup.enter="onLogin" />

                        <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                            <div class="flex items-center">
                                <Checkbox v-model="checked" id="rememberme1" binary class="mr-2"></Checkbox>
                                <label for="rememberme1">Lembrar-me</label>
                            </div>
                            <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">Esqueceu a senha?</span>
                        </div>
                        <Button label="Entrar" class="w-full" :loading="loading" :disabled="rateLimitActive" @click="onLogin"></Button>

                        <div v-if="rateLimitActive" class="flex items-center gap-2 mt-4 justify-center">
                            <i class="pi pi-exclamation-triangle text-yellow-500" />
                            <span class="text-sm text-muted-color">
                                Muitas tentativas. Tente novamente em <strong>{{ countdownSeconds }}</strong
                                >s
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pi-eye {
    transform: scale(1.6);
    margin-right: 1rem;
}

.pi-eye-slash {
    transform: scale(1.6);
    margin-right: 1rem;
}
</style>
