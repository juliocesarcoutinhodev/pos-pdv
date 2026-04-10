<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import { createSupplier, getSupplierById, lookupCnpj, lookupZip, updateSupplierPut } from '@/services/supplierService.js';
import { useErrorHandler } from '@/services/errorHandler.js';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loadingInitial = ref(false);
const submitting = ref(false);
const cnpjLookupLoading = ref(false);
const zipLookupLoading = ref(false);

const suppressAutoLookup = ref(false);
const lastCnpjLookupTaxId = ref('');
const lastZipLookupCode = ref('');

let cnpjLookupTimer = null;
let zipLookupTimer = null;

const CNPJ_MAX_DIGITS = 14;
const PHONE_MAX_DIGITS = 11;

const supplierId = computed(() => (typeof route.params.id === 'string' ? route.params.id : null));
const isEditMode = computed(() => Boolean(supplierId.value));
const pageTitle = computed(() => (isEditMode.value ? 'Editar fornecedor' : 'Novo fornecedor'));
const submitLabel = computed(() => (isEditMode.value ? 'Salvar alterações' : 'Criar fornecedor'));

const form = ref(createEmptyForm());

function createEmptyContact() {
    return {
        name: '',
        email: '',
        phone: ''
    };
}

function createEmptyForm() {
    return {
        id: null,
        active: true,
        name: '',
        taxId: '',
        email: '',
        phone: '',
        address: {
            zipCode: '',
            street: '',
            number: '',
            complement: '',
            district: '',
            city: '',
            state: ''
        },
        contacts: []
    };
}

function onlyDigits(value) {
    return (value || '').replace(/\D/g, '');
}

function normalizeTaxIdInput(value) {
    return onlyDigits(value).slice(0, CNPJ_MAX_DIGITS);
}

function normalizePhoneInput(value) {
    return onlyDigits(value).slice(0, PHONE_MAX_DIGITS);
}

function sanitizeString(value) {
    return (value || '').trim();
}

function sanitizeOptionalString(value) {
    const sanitized = sanitizeString(value);
    return sanitized || null;
}

function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value);
}

function composePhone(phone) {
    if (!phone) {
        return '';
    }

    return `${phone.area || ''}${phone.number || ''}`.trim();
}

function normalizeState(value) {
    return sanitizeString(value).toUpperCase();
}

function handleTaxIdInput(value) {
    form.value.taxId = normalizeTaxIdInput(value);
}

function handleSupplierPhoneInput(value) {
    form.value.phone = normalizePhoneInput(value);
}

function handleContactPhoneInput(index, value) {
    if (!form.value.contacts[index]) {
        return;
    }

    form.value.contacts[index].phone = normalizePhoneInput(value);
}

function addContact() {
    form.value.contacts.push(createEmptyContact());
}

function removeContact(index) {
    form.value.contacts.splice(index, 1);
}

function applyCnpjLookup(data) {
    if (data.taxId) {
        form.value.taxId = normalizeTaxIdInput(data.taxId);
    }

    const supplierName = sanitizeString(data.alias || data.name);
    if (supplierName) {
        form.value.name = supplierName;
    }

    const firstEmail = sanitizeString(data.emails?.[0]?.address);
    if (firstEmail) {
        form.value.email = firstEmail;
    }

    const firstPhone = composePhone(data.phones?.[0]);
    if (firstPhone) {
        form.value.phone = normalizePhoneInput(firstPhone);
    }

    const cnpjAddress = data.address;
    if (!cnpjAddress) {
        return;
    }

    form.value.address.zipCode = cnpjAddress.zipCode || form.value.address.zipCode;
    form.value.address.street = cnpjAddress.street || form.value.address.street;
    form.value.address.number = cnpjAddress.number || form.value.address.number;
    form.value.address.complement = cnpjAddress.complement || form.value.address.complement;
    form.value.address.district = cnpjAddress.district || form.value.address.district;
    form.value.address.city = cnpjAddress.city || form.value.address.city;
    form.value.address.state = normalizeState(cnpjAddress.state || form.value.address.state);
}

function applyZipLookup(data) {
    if (!data) {
        return;
    }

    form.value.address.zipCode = data.code || form.value.address.zipCode;
    form.value.address.street = data.street || form.value.address.street;
    form.value.address.district = data.district || form.value.address.district;
    form.value.address.city = data.city || form.value.address.city;
    form.value.address.state = normalizeState(data.state || form.value.address.state);

    if (!form.value.address.number && data.number) {
        form.value.address.number = data.number;
    }
}

async function lookupCnpjAndApply({ force = false, silentErrors = false, showSuccess = false } = {}) {
    const taxId = onlyDigits(form.value.taxId);
    if (taxId.length !== CNPJ_MAX_DIGITS || cnpjLookupLoading.value) {
        return;
    }

    if (!force && taxId === lastCnpjLookupTaxId.value) {
        return;
    }

    cnpjLookupLoading.value = true;
    try {
        const result = await lookupCnpj(taxId);
        applyCnpjLookup(result);
        lastCnpjLookupTaxId.value = taxId;

        if (showSuccess) {
            toast.add({
                severity: 'success',
                summary: 'CNPJ consultado',
                detail: 'Dados do fornecedor preenchidos automaticamente.',
                life: 3000
            });
        }
    } catch (error) {
        if (!silentErrors) {
            showApiErrorToast(toast, error);
        }
    } finally {
        cnpjLookupLoading.value = false;
    }
}

async function lookupZipAndApply({ force = false, silentErrors = false, showSuccess = false } = {}) {
    const zipCode = onlyDigits(form.value.address.zipCode);
    if (zipCode.length !== 8 || zipLookupLoading.value) {
        return;
    }

    if (!force && zipCode === lastZipLookupCode.value) {
        return;
    }

    zipLookupLoading.value = true;
    try {
        const result = await lookupZip(zipCode);
        applyZipLookup(result);
        lastZipLookupCode.value = zipCode;

        if (showSuccess) {
            toast.add({
                severity: 'success',
                summary: 'CEP consultado',
                detail: 'Dados de endereço atualizados automaticamente.',
                life: 3000
            });
        }
    } catch (error) {
        if (!silentErrors) {
            showApiErrorToast(toast, error);
        }
    } finally {
        zipLookupLoading.value = false;
    }
}

function handleManualCnpjLookup() {
    if (onlyDigits(form.value.taxId).length !== CNPJ_MAX_DIGITS) {
        toast.add({
            severity: 'warn',
            summary: 'CNPJ inválido',
            detail: 'Informe um CNPJ com 14 dígitos para consultar.',
            life: 3000
        });
        return;
    }

    void lookupCnpjAndApply({ force: true, showSuccess: true });
}

function handleManualZipLookup() {
    if (onlyDigits(form.value.address.zipCode).length !== 8) {
        toast.add({
            severity: 'warn',
            summary: 'CEP inválido',
            detail: 'Informe um CEP com 8 dígitos para consultar.',
            life: 3000
        });
        return;
    }

    void lookupZipAndApply({ force: true, showSuccess: true });
}

function buildPayload() {
    const contacts = form.value.contacts
        .map((contact) => ({
            name: sanitizeString(contact.name),
            email: sanitizeOptionalString(contact.email),
            phone: sanitizeOptionalString(normalizePhoneInput(contact.phone))
        }))
        .filter((contact) => contact.name || contact.email || contact.phone);

    return {
        name: sanitizeString(form.value.name),
        taxId: normalizeTaxIdInput(form.value.taxId),
        email: sanitizeOptionalString(form.value.email),
        phone: sanitizeOptionalString(normalizePhoneInput(form.value.phone)),
        address: {
            zipCode: onlyDigits(form.value.address.zipCode),
            street: sanitizeString(form.value.address.street),
            number: sanitizeOptionalString(form.value.address.number),
            complement: sanitizeOptionalString(form.value.address.complement),
            district: sanitizeString(form.value.address.district),
            city: sanitizeString(form.value.address.city),
            state: normalizeState(form.value.address.state)
        },
        contacts
    };
}

function validatePayload(payload) {
    if (payload.taxId.length !== CNPJ_MAX_DIGITS) {
        toast.add({
            severity: 'warn',
            summary: 'CNPJ inválido',
            detail: 'Informe um CNPJ válido com 14 dígitos.',
            life: 3000
        });
        return false;
    }

    if (!payload.name) {
        toast.add({
            severity: 'warn',
            summary: 'Nome obrigatório',
            detail: 'Informe o nome do fornecedor.',
            life: 3000
        });
        return false;
    }

    if (payload.email && !isValidEmail(payload.email)) {
        toast.add({
            severity: 'warn',
            summary: 'E-mail inválido',
            detail: 'Informe um e-mail válido para o fornecedor.',
            life: 3000
        });
        return false;
    }

    if (payload.address.zipCode.length !== 8) {
        toast.add({
            severity: 'warn',
            summary: 'CEP inválido',
            detail: 'Informe um CEP válido com 8 dígitos.',
            life: 3000
        });
        return false;
    }

    if (!payload.address.street || !payload.address.district || !payload.address.city || !payload.address.state) {
        toast.add({
            severity: 'warn',
            summary: 'Endereço incompleto',
            detail: 'Preencha rua, bairro, cidade e estado.',
            life: 3500
        });
        return false;
    }

    if (payload.address.state.length !== 2) {
        toast.add({
            severity: 'warn',
            summary: 'UF inválida',
            detail: 'A UF deve possuir 2 caracteres.',
            life: 3000
        });
        return false;
    }

    for (let index = 0; index < payload.contacts.length; index += 1) {
        const contact = payload.contacts[index];

        if (!contact.name) {
            toast.add({
                severity: 'warn',
                summary: 'Contato inválido',
                detail: `Preencha o nome do contato #${index + 1}.`,
                life: 3000
            });
            return false;
        }

        if (contact.email && !isValidEmail(contact.email)) {
            toast.add({
                severity: 'warn',
                summary: 'E-mail do contato inválido',
                detail: `Informe um e-mail válido para o contato #${index + 1}.`,
                life: 3000
            });
            return false;
        }
    }

    return true;
}

function mapSupplierToForm(supplier) {
    return {
        id: supplier?.id ?? null,
        active: Boolean(supplier?.active),
        name: supplier?.name ?? '',
        taxId: normalizeTaxIdInput(supplier?.taxId ?? ''),
        email: supplier?.email ?? '',
        phone: normalizePhoneInput(supplier?.phone ?? ''),
        address: {
            zipCode: supplier?.address?.zipCode ?? '',
            street: supplier?.address?.street ?? '',
            number: supplier?.address?.number ?? '',
            complement: supplier?.address?.complement ?? '',
            district: supplier?.address?.district ?? '',
            city: supplier?.address?.city ?? '',
            state: supplier?.address?.state ?? ''
        },
        contacts: Array.isArray(supplier?.contacts)
            ? supplier.contacts.map((contact) => ({
                  name: contact?.name ?? '',
                  email: contact?.email ?? '',
                  phone: normalizePhoneInput(contact?.phone ?? '')
              }))
            : []
    };
}

async function loadSupplierForEdit() {
    if (!isEditMode.value || !supplierId.value) {
        return;
    }

    loadingInitial.value = true;
    try {
        const supplier = await getSupplierById(supplierId.value);
        suppressAutoLookup.value = true;
        form.value = mapSupplierToForm(supplier);
        lastCnpjLookupTaxId.value = onlyDigits(form.value.taxId);
        lastZipLookupCode.value = onlyDigits(form.value.address.zipCode);
    } catch (error) {
        showApiErrorToast(toast, error);
        await router.push('/pages/suppliers');
    } finally {
        suppressAutoLookup.value = false;
        loadingInitial.value = false;
    }
}

async function handleSubmit() {
    const payload = buildPayload();
    if (!validatePayload(payload)) {
        return;
    }

    submitting.value = true;
    try {
        if (isEditMode.value && supplierId.value) {
            await updateSupplierPut(supplierId.value, payload);
            toast.add({
                severity: 'success',
                summary: 'Fornecedor atualizado',
                detail: 'As alterações foram salvas com sucesso.',
                life: 3000
            });
        } else {
            await createSupplier(payload);
            toast.add({
                severity: 'success',
                summary: 'Fornecedor criado',
                detail: 'Fornecedor cadastrado com sucesso.',
                life: 3000
            });
        }

        await router.push('/pages/suppliers');
    } catch (error) {
        showApiErrorToast(toast, error);
    } finally {
        submitting.value = false;
    }
}

function goBack() {
    void router.push('/pages/suppliers');
}

watch(
    () => form.value.taxId,
    (value) => {
        if (suppressAutoLookup.value) {
            return;
        }

        const taxId = onlyDigits(value);
        if (cnpjLookupTimer) {
            clearTimeout(cnpjLookupTimer);
        }

        if (taxId.length !== CNPJ_MAX_DIGITS || taxId === lastCnpjLookupTaxId.value) {
            return;
        }

        cnpjLookupTimer = setTimeout(() => {
            void lookupCnpjAndApply({ silentErrors: true });
        }, 500);
    }
);

watch(
    () => form.value.address.zipCode,
    (value) => {
        if (suppressAutoLookup.value) {
            return;
        }

        const zipCode = onlyDigits(value);
        if (zipLookupTimer) {
            clearTimeout(zipLookupTimer);
        }

        if (zipCode.length !== 8 || zipCode === lastZipLookupCode.value) {
            return;
        }

        zipLookupTimer = setTimeout(() => {
            void lookupZipAndApply({ silentErrors: true });
        }, 500);
    }
);

onMounted(async () => {
    await loadSupplierForEdit();
});

onUnmounted(() => {
    if (cnpjLookupTimer) {
        clearTimeout(cnpjLookupTimer);
    }
    if (zipLookupTimer) {
        clearTimeout(zipLookupTimer);
    }
});
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-6">
            <div>
                <div class="font-semibold text-xl">{{ pageTitle }}</div>
                <div class="text-sm text-muted-color">Preencha os dados do fornecedor, endereço e contatos.</div>
            </div>
            <div class="flex flex-col sm:flex-row gap-2">
                <Button icon="pi pi-arrow-left" label="Voltar" severity="secondary" outlined @click="goBack" :disabled="submitting || loadingInitial" />
                <Button icon="pi pi-check" :label="submitLabel" @click="handleSubmit" :loading="submitting" :disabled="loadingInitial" />
            </div>
        </div>

        <Message v-if="isEditMode && !form.active" severity="warn" :closable="false" class="mb-4"> Este fornecedor está inativo. Você pode editar os dados e reativá-lo na listagem. </Message>

        <div v-if="loadingInitial" class="flex justify-center py-10">
            <ProgressSpinner style="width: 2rem; height: 2rem" strokeWidth="6" />
        </div>

        <div v-else class="flex flex-col gap-6">
            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">1. Dados do fornecedor</div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="md:col-span-2">
                        <label for="supplier-tax-id" class="block mb-2 font-medium">CNPJ *</label>
                        <div class="flex flex-col sm:flex-row gap-2">
                            <InputText id="supplier-tax-id" :modelValue="form.taxId" placeholder="00.000.000/0000-00" maxlength="18" class="w-full" :disabled="submitting" @update:modelValue="handleTaxIdInput" />
                            <Button icon="pi pi-search" label="Consultar CNPJ" severity="secondary" outlined @click="handleManualCnpjLookup" :loading="cnpjLookupLoading" :disabled="submitting" />
                        </div>
                        <small class="text-muted-color">Ao informar 14 dígitos, a consulta é feita automaticamente.</small>
                    </div>

                    <div class="md:col-span-2">
                        <label for="supplier-name" class="block mb-2 font-medium">Nome *</label>
                        <InputText id="supplier-name" v-model="form.name" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-email" class="block mb-2 font-medium">E-mail</label>
                        <InputText id="supplier-email" v-model="form.email" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-phone" class="block mb-2 font-medium">Telefone</label>
                        <InputText id="supplier-phone" :modelValue="form.phone" maxlength="15" class="w-full" :disabled="submitting" @update:modelValue="handleSupplierPhoneInput" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="font-semibold text-lg mb-4">2. Endereço</div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div class="md:col-span-2">
                        <label for="supplier-zip-code" class="block mb-2 font-medium">CEP *</label>
                        <div class="flex flex-col sm:flex-row gap-2">
                            <InputText id="supplier-zip-code" v-model="form.address.zipCode" placeholder="00000-000" class="w-full" :disabled="submitting" />
                            <Button icon="pi pi-search" label="Consultar CEP" severity="secondary" outlined @click="handleManualZipLookup" :loading="zipLookupLoading" :disabled="submitting" />
                        </div>
                        <small class="text-muted-color">Ao informar 8 dígitos, rua/bairro/cidade/UF são consultados automaticamente.</small>
                    </div>

                    <div class="md:col-span-2">
                        <label for="supplier-street" class="block mb-2 font-medium">Rua *</label>
                        <InputText id="supplier-street" v-model="form.address.street" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-number" class="block mb-2 font-medium">Número</label>
                        <InputText id="supplier-number" v-model="form.address.number" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-complement" class="block mb-2 font-medium">Complemento</label>
                        <InputText id="supplier-complement" v-model="form.address.complement" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-district" class="block mb-2 font-medium">Bairro *</label>
                        <InputText id="supplier-district" v-model="form.address.district" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-city" class="block mb-2 font-medium">Cidade *</label>
                        <InputText id="supplier-city" v-model="form.address.city" class="w-full" :disabled="submitting" />
                    </div>

                    <div>
                        <label for="supplier-state" class="block mb-2 font-medium">UF *</label>
                        <InputText id="supplier-state" v-model="form.address.state" maxlength="2" class="w-full" :disabled="submitting" />
                    </div>
                </div>
            </section>

            <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
                <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
                    <div class="font-semibold text-lg">3. Contatos</div>
                    <Button icon="pi pi-plus" label="Adicionar contato" severity="secondary" outlined @click="addContact" :disabled="submitting" />
                </div>

                <div v-if="form.contacts.length === 0" class="text-muted-color">Nenhum contato adicionado. Você pode salvar sem contatos.</div>

                <div v-else class="flex flex-col gap-4">
                    <div v-for="(contact, index) in form.contacts" :key="`contact-${index}`" class="border border-surface-200 dark:border-surface-700 rounded-lg p-4">
                        <div class="flex items-center justify-between mb-3">
                            <div class="font-medium">Contato #{{ index + 1 }}</div>
                            <Button icon="pi pi-trash" severity="danger" text rounded @click="removeContact(index)" :disabled="submitting" />
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
                            <div>
                                <label :for="`contact-name-${index}`" class="block mb-2 font-medium">Nome *</label>
                                <InputText :id="`contact-name-${index}`" v-model="contact.name" class="w-full" :disabled="submitting" />
                            </div>
                            <div>
                                <label :for="`contact-email-${index}`" class="block mb-2 font-medium">E-mail</label>
                                <InputText :id="`contact-email-${index}`" v-model="contact.email" class="w-full" :disabled="submitting" />
                            </div>
                            <div>
                                <label :for="`contact-phone-${index}`" class="block mb-2 font-medium">Telefone</label>
                                <InputText :id="`contact-phone-${index}`" :modelValue="contact.phone" maxlength="15" class="w-full" :disabled="submitting" @update:modelValue="(value) => handleContactPhoneInput(index, value)" />
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <div class="flex flex-col sm:flex-row justify-end gap-2">
                <Button label="Cancelar" severity="secondary" outlined @click="goBack" :disabled="submitting" />
                <Button :label="submitLabel" icon="pi pi-check" @click="handleSubmit" :loading="submitting" />
            </div>
        </div>
    </div>
</template>
