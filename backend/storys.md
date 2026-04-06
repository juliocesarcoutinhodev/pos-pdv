Objetivo
Preparar configurações separadas para dev e test.

Descrição
Adicionar application.yml base e arquivos de profile para evitar configurações misturadas.

Critérios de Aceite

Existem:

application.yml

application-local.yml

application-test.yml

App roda com local e testes rodam com test

PROFILE=local funciona

Tarefas

Criar arquivos de config

Ajustar portas/logs se necessário

Documentar no README

DoD

App sobe em dev

./mvnw test executa em test

