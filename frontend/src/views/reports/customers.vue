<script setup>
import { ref } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useErrorHandler } from '@/services/errorHandler.js';
import { downloadCustomerDetailedReport, downloadCustomerSummaryReport } from '@/services/customerReportService.js';

const toast = useToast();
const { showApiErrorToast } = useErrorHandler();

const loadingSummary = ref(false);
const loadingDetailed = ref(false);

const filters = ref({
    name: '',
    active: null,
    birthMonth: null,
    birthDateFrom: '',
    birthDateTo: ''
});

const activeOptions = [
    { label: 'Todos', value: null },
    { label: 'Ativos', value: true },
    { label: 'Inativos', value: false }
];

const monthOptions = [
    { label: 'Todos', value: null },
    { label: 'Janeiro', value: 1 },
    { label: 'Fevereiro', value: 2 },
    { label: 'Março', value: 3 },
    { label: 'Abril', value: 4 },
    { label: 'Maio', value: 5 },
    { label: 'Junho', value: 6 },
    { label: 'Julho', value: 7 },
    { label: 'Agosto', value: 8 },
    { label: 'Setembro', value: 9 },
    { label: 'Outubro', value: 10 },
    { label: 'Novembro', value: 11 },
    { label: 'Dezembro', value: 12 }
];

function clearFilters() {
    filters.value = {
        name: '',
        active: null,
        birthMonth: null,
        birthDateFrom: '',
        birthDateTo: ''
    };
}

function hasValidDateRange() {
    if (!filters.value.birthDateFrom || !filters.value.birthDateTo) {
        return true;
    }

    if (filters.value.birthDateFrom <= filters.value.birthDateTo) {
        return true;
    }

    toast.add({
        severity: 'warn',
        summary: 'Período inválido',
        detail: 'A data inicial de aniversário deve ser menor ou igual à data final.',
        life: 4000
    });
    return false;
}

function buildRequestParams() {
    return {
        name: filters.value.name,
        active: filters.value.active,
        birthMonth: filters.value.birthMonth,
        birthDateFrom: filters.value.birthDateFrom || null,
        birthDateTo: filters.value.birthDateTo || null
    };
}

function openPopupWindow() {
    const popupWindow = window.open('', '_blank');

    if (!popupWindow) {
        toast.add({
            severity: 'warn',
            summary: 'Popup bloqueado',
            detail: 'Permita popups no navegador para abrir o PDF do relatório.',
            life: 4000
        });
        return null;
    }

    popupWindow.document.write(`
        <!DOCTYPE html>
        <html lang="pt-BR">
            <head>
                <meta charset="UTF-8" />
                <title>Gerando relatório...</title>
            </head>
            <body style="font-family: Arial, sans-serif; padding: 16px;">
                Gerando relatório de clientes...
            </body>
        </html>
    `);
    popupWindow.document.close();
    return popupWindow;
}

function openPdfInWindow(blob, popupWindow) {
    const blobUrl = URL.createObjectURL(blob);

    if (popupWindow && !popupWindow.closed) {
        popupWindow.location.href = blobUrl;
        popupWindow.focus();
    } else {
        const newWindow = window.open(blobUrl, '_blank');
        if (!newWindow) {
            URL.revokeObjectURL(blobUrl);
            toast.add({
                severity: 'warn',
                summary: 'Popup bloqueado',
                detail: 'Permita popups no navegador para abrir o PDF do relatório.',
                life: 4000
            });
            return;
        }
    }

    window.setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
}

async function handleGenerateReport(type) {
    if (!hasValidDateRange()) {
        return;
    }

    const popupWindow = openPopupWindow();
    if (!popupWindow) {
        return;
    }

    if (type === 'summary') {
        loadingSummary.value = true;
    } else {
        loadingDetailed.value = true;
    }

    try {
        const params = buildRequestParams();
        const report = type === 'summary' ? await downloadCustomerSummaryReport(params) : await downloadCustomerDetailedReport(params);
        openPdfInWindow(report.blob, popupWindow);

        toast.add({
            severity: 'success',
            summary: 'Relatório gerado',
            detail: type === 'summary' ? 'Relatório resumido aberto com sucesso.' : 'Relatório detalhado aberto com sucesso.',
            life: 3000
        });
    } catch (error) {
        if (!popupWindow.closed) {
            popupWindow.close();
        }
        showApiErrorToast(toast, error);
    } finally {
        if (type === 'summary') {
            loadingSummary.value = false;
        } else {
            loadingDetailed.value = false;
        }
    }
}
</script>

<template>
    <div class="card">
        <Toast />

        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4">
            <div>
                <div class="font-semibold text-xl">Relatório de Clientes</div>
                <div class="text-sm text-muted-color">Gere relatórios em PDF (resumido e detalhado) com filtros de aniversário.</div>
            </div>
            <Button icon="pi pi-filter-slash" label="Limpar filtros" severity="secondary" outlined :disabled="loadingSummary || loadingDetailed" @click="clearFilters" />
        </div>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5 mb-5">
            <div class="font-semibold text-lg mb-3">Filtros para impressão</div>

            <div class="grid grid-cols-1 md:grid-cols-5 gap-3">
                <div>
                    <label for="customer-report-name" class="block mb-2 font-medium">Nome</label>
                    <InputText id="customer-report-name" v-model="filters.name" class="w-full" />
                </div>
                <div>
                    <label for="customer-report-active" class="block mb-2 font-medium">Status</label>
                    <Select id="customer-report-active" v-model="filters.active" :options="activeOptions" optionLabel="label" optionValue="value" class="w-full" />
                </div>
                <div>
                    <label for="customer-report-birth-month" class="block mb-2 font-medium">Mês de aniversário</label>
                    <Select id="customer-report-birth-month" v-model="filters.birthMonth" :options="monthOptions" optionLabel="label" optionValue="value" class="w-full" />
                </div>
                <div>
                    <label for="customer-report-birth-from" class="block mb-2 font-medium">Aniversário de</label>
                    <InputText id="customer-report-birth-from" v-model="filters.birthDateFrom" type="date" class="w-full" />
                </div>
                <div>
                    <label for="customer-report-birth-to" class="block mb-2 font-medium">Aniversário até</label>
                    <InputText id="customer-report-birth-to" v-model="filters.birthDateTo" type="date" class="w-full" />
                </div>
            </div>
        </section>

        <section class="border border-surface-200 dark:border-surface-700 rounded-xl p-4 md:p-5">
            <div class="font-semibold text-lg mb-3">Modelos de relatório</div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="border border-surface-200 dark:border-surface-700 rounded-lg p-4">
                    <div class="font-semibold mb-1">Resumido</div>
                    <p class="text-sm text-muted-color mb-4">Visão rápida com os dados principais dos clientes.</p>
                    <Button icon="pi pi-file-pdf" label="Gerar relatório resumido" class="w-full" :loading="loadingSummary" :disabled="loadingDetailed" @click="handleGenerateReport('summary')" />
                </div>

                <div class="border border-surface-200 dark:border-surface-700 rounded-lg p-4">
                    <div class="font-semibold mb-1">Detalhado</div>
                    <p class="text-sm text-muted-color mb-4">Ficha completa com dados pessoais, endereço e status.</p>
                    <Button icon="pi pi-file-pdf" label="Gerar relatório detalhado" severity="secondary" outlined class="w-full" :loading="loadingDetailed" :disabled="loadingSummary" @click="handleGenerateReport('detailed')" />
                </div>
            </div>
        </section>
    </div>
</template>
