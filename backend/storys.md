Revise o código como especialista em DDD, Hexagonal Architecture e boas práticas REST.
Os pontos principais que devem ser corrigidos e padronizados:

Regras de domínio:
Todo comportamento ou lógica de negócio deve estar no domínio, não no use case ou handler.
Use o use case apenas para orquestrar chamadas e coordenar fluxos, mantendo o core do sistema isolado.
Criação de objetos de domínio:
Objetos de domínio não devem ser criados dentro do use case; a construção deve respeitar o encapsulamento do domínio.
Controllers / REST:
Retorne sempre um ResponseEntity ao invés de usar HttpStatus como anotação.
Garanta que o controller apenas adapte o resultado do use case para o protocolo HTTP, sem conter lógica de negócio.
Records e DTOs:
Não deixe Records aninhados dentro do handler; mova-os para um pacote separado, organizado e limpo.
Validações podem ser feitas tanto na camada de Validation quanto dentro do domínio, garantindo consistência sem quebrar as regras de negócio.
Padronização e organização:
Separe responsabilidades corretamente entre camadas: Domínio, Aplicação (Use Case), Infraestrutura (Controller, Repositórios).
Certifique-se de que nada do core (domínio) dependa da camada externa.