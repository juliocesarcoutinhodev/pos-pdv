<script setup>
import FloatingConfigurator from '@/components/FloatingConfigurator.vue';
import { ref } from 'vue';

const email = ref('');
const password = ref('');
const checked = ref(false);
</script>

<template>
    <FloatingConfigurator />
    <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-[100vw] overflow-hidden">
        <div class="flex flex-col items-center justify-center">
            <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                    <div class="text-center mb-8">
                        <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" class="mb-6 w-20 shrink-0 mx-auto">
                            <!-- Monitor body -->
                            <rect x="8" y="8" width="64" height="44" rx="6" stroke="var(--primary-color)" stroke-width="3" fill="var(--primary-color)" fill-opacity="0.1"/>
                            <!-- Screen -->
                            <rect x="15" y="14" width="50" height="28" rx="3" fill="var(--primary-color)" fill-opacity="0.15" stroke="var(--primary-color)" stroke-width="2"/>
                            <!-- Receipt lines on screen -->
                            <line x1="22" y1="20" x2="58" y2="20" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round"/>
                            <line x1="22" y1="26" x2="50" y2="26" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                            <line x1="22" y1="32" x2="44" y2="32" stroke="var(--primary-color)" stroke-width="2" stroke-linecap="round" opacity="0.3"/>
                            <!-- Dollar/price sign on screen -->
                            <text x="40" y="38" text-anchor="middle" font-size="14" font-weight="bold" fill="var(--primary-color)">$</text>
                            <!-- Monitor stand -->
                            <path d="M34 52 L46 52 L48 60 L32 60Z" fill="var(--primary-color)" stroke="var(--primary-color)" stroke-width="2" stroke-linejoin="round"/>
                            <!-- Monitor base -->
                            <rect x="24" y="60" width="32" height="4" rx="2" fill="var(--primary-color)"/>
                            <!-- Cash drawer -->
                            <rect x="18" y="66" width="44" height="8" rx="2" stroke="var(--primary-color)" stroke-width="2" fill="var(--primary-color)" fill-opacity="0.08"/>
                            <rect x="33" y="68" width="14" height="4" rx="2" stroke="var(--primary-color)" stroke-width="1.5" fill="var(--primary-color)" fill-opacity="0.2"/>
                        </svg>
                        <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Bem-vindo ao POS PDV!</div>
                        <span class="text-muted-color font-medium">Faça login para continuar</span>
                    </div>

                    <div>
                        <label for="email1" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">E-mail</label>
                        <InputText id="email1" type="text" placeholder="Seu e-mail" class="w-full md:w-[30rem] mb-8" v-model="email" />

                        <label for="password1" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Senha</label>
                        <Password id="password1" v-model="password" placeholder="Sua senha" :toggleMask="true" class="mb-4" fluid :feedback="false"></Password>

                        <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                            <div class="flex items-center">
                                <Checkbox v-model="checked" id="rememberme1" binary class="mr-2"></Checkbox>
                                <label for="rememberme1">Lembrar-me</label>
                            </div>
                            <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">Esqueceu a senha?</span>
                        </div>
                        <Button label="Entrar" class="w-full" as="router-link" to="/dashboard"></Button>
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
