Objetivo
Autenticar usuário LOCAL e iniciar sessão com refresh token.

Descrição
Implementar POST /api/v1/auth/login retornando access token e refresh token.

Critérios de Aceite

Credenciais válidas → retorna:

accessToken, expiresIn

refreshToken (ou cookie HttpOnly, ver SL-37)

Credenciais inválidas → erro padronizado (401)

Ao logar, criar registro de refresh token persistido (hash)

Tarefas

Use case de login

Persistência do refresh token

Testes unitários + controller (MockMvc)

DoD

Fluxo completo ok

Refresh token armazenado com hash

