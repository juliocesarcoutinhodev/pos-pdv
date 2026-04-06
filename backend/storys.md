Vamos implementar um CRUD completo de usuários, seguindo DDD e arquitetura hexagonal, com foco em boas práticas e código limpo. O CRUD deve incluir:

Buscar todos os usuários com paginação e filtros opcionais (ex: nome, email, status).
Buscar usuário por ID.
Salvar um novo usuário via cadastro administrativo, já associando um perfil de usuário padrão.
Atualizar usuário (full update).
Atualizar usuário parcialmente (partial update / patch).
Deletar usuário (remoção lógica ou física, explique a abordagem escolhida).

Regras e critérios de implementação:

Utilize DTOs para transferência de dados e mapeamento via adapter/mapper.
Valide e trate exceções de forma consistente (ex: usuário não encontrado, dados inválidos).
Encapsule regras de negócio no domínio, não nos casos de uso ou controllers.
Controllers devem expor endpoints RESTful, usando ResponseEntity corretamente.
Seguir SOLID e princípios de Clean Code.
Após implementação, atualize README e collections de testes/insomnia/postman.

Comece implementando a camada de domínio e casos de uso, depois siga para o adapter/controller e finalmente para persistência, garantindo separação de responsabilidades.