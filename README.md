# PetBuddies AI — Challenge FIAP 2026 | Java Advanced

API do produto **PetBuddies**, desenvolvida com Spring Boot como parte do Challenge da disciplina de
**Java Advanced (2TDSR)** — FIAP 2026.

> **O que mudou desde a Sprint 2.** O produto era um bot de WhatsApp com o motor de cuidado atrás. O
> WhatsApp saiu como interface e o score de risco saiu do produto. Na onda 4 da Sprint 3 (ADR `s3-25`),
> este serviço absorveu **todo o registro clínico que antes vivia no `PetBuddies-API` (.NET)** —
> clínica, veterinário, responsável, animal, consulta, condição clínica, janela de atendimento,
> registro de atendimento, procedimento, prescrição e regra de prescrição — e passou a ser **a API
> única do produto**. O .NET virou o back-office da clínica: só o catálogo de protocolos e as regras
> de pontuação continuam lá.

**A IA interpreta a narrativa do tutor no check-in. Ela não decide dose em momento nenhum** — quem
decide é o motor determinístico, aplicando a regra que a veterinária escreveu e assinou. O check-in em
si (extração por IA + motor de regras sobre prescrições ativas) tem o schema pronto
(`T_PB_CHECKIN`, `T_PB_CONDICAO_OBSERVADA`) mas ainda **não tem entidade, service nem controller** —
é a próxima entrega, não o estado atual.

---

## Integrantes do Grupo

| Nome | RM |
|------|----|
| Felipe Yuiti Ishii | 565339 |
| Gabriel Nogueira Peixoto | 563925 |
| Giovanna Neri dos Santos | 566154 |
| Mariana Inoue | 565834 |

---

## O que o serviço entrega

| Capacidade | Estado |
|---|---|
| **Registro clínico completo** — clínica, veterinário, responsável, animal, consulta, condição clínica, janela de atendimento, registro de atendimento, procedimento | no ar |
| **Prescrição e regra de prescrição** — o ato clínico assinado, imutável, com a regra condicional sobre a dose | no ar |
| **Motor de planos** — instancia o plano de um animal a partir do protocolo compatível (lido do .NET) e devolve os itens | no ar |
| **Autenticação com dois perfis** (vet e tutor), com token Bearer para a API e formulário para a web | no ar |
| **Superfície web em Thymeleaf** — a dependência está no `pom.xml`; nenhum template existe ainda | não implementado |
| **Saúde consultável** — a rota é liberada no `SecurityConfig`, mas o Actuator não está no `pom.xml` | não implementado |
| **Check-in do tutor** — extração da narrativa por IA e motor de regras sobre as prescrições ativas | schema pronto, sem código |

Nada abaixo descreve endpoint que não esteja no código hoje. O catálogo de protocolos
(`/api/protocolos`, `/api/eventos-protocolo`) **saiu deste serviço na onda 4** — a clínica o cadastra
no .NET, e este serviço só o lê via HTTP ao instanciar um plano.

---

## Autenticação

Login único para os dois perfis, em `POST /api/auth/login`:

```json
{ "login": "ana@clinica.com", "senha": "petbuddies123" }
```

Devolve `200` com o token e o vínculo preenchido (o outro vem `null`):

```json
{
  "token": "…",
  "perfil": "VET",
  "usuarioId": 1,
  "responsavelId": null,
  "veterinarioId": 1
}
```

`401` para login inexistente, senha errada ou usuário inativo — sempre com a mesma mensagem, para não
vazar qual dos três aconteceu.

O token vai em `Authorization: Bearer <token>` em toda chamada a `/api/**`. **Hoje não há restrição de
papel por endpoint**: qualquer usuário autenticado, `VET` ou `TUTOR`, acessa qualquer rota de
`/api/**`. A única exceção é `/api/auth/login`, que é aberta.

---

## Endpoints

Toda resposta de recurso único ou coleção vem em envelope HATEOAS (`EntityModel` /
`CollectionModel`, com `_links` e `_embedded`) — ADR `s3-22`. Erros vêm como
`ErrorDto{ code, message }`, montado por um `@RestControllerAdvice` global.

### Autenticação

| Método | Rota |
|---|---|
| `POST` | `/api/auth/login` |

### Motor de planos

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/motor/planos/instanciar-preventivo` | cria (ou devolve o existente, idempotente) o plano preventivo do animal |
| `POST` | `/api/motor/planos/instanciar-pos-cirurgico` | idem, para o plano pós-cirúrgico vinculado a uma consulta |
| `GET` | `/api/motor/planos/{animalId}` | plano `ATIVO` do animal |
| `GET` | `/api/motor/planos/{animalId}/eventos` | itens do plano, paginado (`?page=&size=`) |

### Registro clínico

| Domínio | Rota base | Métodos |
|---|---|---|
| Clínicas | `/api/clinicas` | `GET`, `GET /buscar?cnpj=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Veterinários | `/api/veterinarios` | `GET`, `GET /buscar?crmv=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Responsáveis | `/api/responsaveis` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Animais | `/api/animais` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Condições clínicas | `/api/condicoes-clinicas` | `GET`, `GET /buscar?clinicaId=&codigo=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Consultas | `/api/consultas` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Janelas de atendimento | `/api/janelas-atendimento` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Registros de atendimento | `/api/registros-atendimento` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Procedimentos | `/api/procedimentos` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Prescrições | `/api/prescricoes` | `GET`, `GET /{id}`, `POST` (sem `PUT`/`DELETE` — ato imutável) |
| Regras de prescrição | `/api/regras-prescricao` | `GET`, `GET /{id}`, `POST` (sem `PUT`/`DELETE` — ato imutável) |

A lista de parâmetros, o corpo de cada requisição e os códigos de erro estão no Swagger
(`/swagger-ui.html`), gerado a partir do código — é a fonte que não fica desatualizada.

---

## Como o Java e o .NET se falam

**Uma única chamada, Java → .NET.** Ao instanciar um plano de cuidado, o `MotorPlanoService` pergunta
ao `ProtocoloClient` quais protocolos ativos existem para a categoria e espécie do animal
(`GET /api/protocolos` no .NET, com um token de serviço emitido pelo próprio Java). Se o .NET estiver
fora do ar, a resposta é a mesma de "nenhum protocolo compatível": o plano simplesmente não nasce
agora, sem propagar erro.

O gatilho que existia no sentido contrário — o .NET chamando `/api/motor/**` sem token quando um
animal ou uma cirurgia eram cadastrados — **foi removido na onda 4** junto com o `MotorApiClient` do
.NET (ADR `s3-25`). Hoje o Java não recebe chamada nenhuma do .NET.

---

## Modelo de dados

**16 tabelas**, todas no schema `PETBUDDIES`, criadas por `V1__baseline_schema_cuidado.sql`. Catorze
têm entidade JPA hoje; as duas do check-in ainda não.

| Tabela | Entidade | Papel |
|---|---|---|
| `T_PB_CLINICA` | `ClinicaEntity` | a clínica veterinária |
| `T_PB_RESPONSAVEL` | `ResponsavelEntity` | o tutor do animal |
| `T_PB_VETERINARIO` | `VeterinarioEntity` | quem assina o ato clínico, vinculado a uma clínica |
| `T_PB_USUARIO` | `UsuarioEntity` | credencial e perfil (`VET` / `TUTOR`), com o vínculo para um dos dois acima |
| `T_PB_ANIMAL` | `AnimalEntity` | o paciente |
| `T_PB_CONSULTA` | `ConsultaEntity` | o agendamento e o comparecimento do animal |
| `T_PB_CONDICAO_CLINICA` | `CondicaoClinicaEntity` | catálogo de condições que o check-in (quando existir) avalia, por clínica |
| `T_PB_JANELA_ATENDIMENTO` | `JanelaAtendimentoEntity` | a agenda do veterinário — slots livres ou reservados por uma consulta |
| `T_PB_REGISTRO_ATENDIMENTO` | `RegistroAtendimentoEntity` | o que aconteceu na consulta: anamnese, diagnóstico, tratamento |
| `T_PB_PROCEDIMENTO` | `ProcedimentoEntity` | vacina, exame ou cirurgia executados num atendimento |
| `T_PB_PRESCRICAO` | `PrescricaoEntity` | o ato assinado pelo veterinário — imutável depois de criado |
| `T_PB_REGRA_PRESCRICAO` | `RegraPrescricaoEntity` | condição → ação sobre a dose, com a condição congelada no momento da assinatura |
| `T_PB_PLANO_CUIDADO` | `PlanoCuidadoEntity` | o plano vivo de um animal, instanciado pelo motor a partir do protocolo do .NET |
| `T_PB_ITEM_PLANO_CUIDADO` | `ItemPlanoCuidadoEntity` | o item do plano, com data alvo, origem (protocolo ou prescrição) e status |
| `T_PB_CHECKIN` | — (sem entidade ainda) | o check-in do tutor, narrativa livre |
| `T_PB_CONDICAO_OBSERVADA` | — (sem entidade ainda) | a condição que o check-in extraiu da narrativa |

O protocolo em si (`o quê` e `quando` do cuidado) **não tem tabela neste serviço**: é catálogo do
.NET, lido por HTTP no momento em que um plano nasce (`ProtocoloClient` / `ProtocoloCatalogoDto`).

Todo enum de domínio é persistido como texto (`@Enumerated(EnumType.STRING)`) — `ORDINAL`
corromperia os dados na primeira reordenação. A lista completa de enums e valores aceitos está no
Swagger; o código-fonte é `domain/enums/`.

---

## Por que Repository e não DAO?

O Spring Data JPA já gerencia o `EntityManager` — ciclo de vida, transações, thread-safety. O
`JpaRepository` entrega o CRUD pronto por interface, sem implementação. DAO faria sentido se
precisássemos de controle fino sobre o `EntityManager`; aqui o Spring cuida disso melhor do que
faríamos à mão.

---

## Como executar

### Pré-requisitos

- Java 21+
- Maven 3.9+ (**este repositório não versiona o Maven Wrapper** — todo comando abaixo é o `mvn` do
  sistema, não `./mvnw`)
- Docker, para o Oracle local — **ou** acesso ao Oracle FIAP

### Banco local (recomendado para desenvolvimento)

O `docker-compose.yml` sobe um `gvenzl/oracle-free:23-slim` só com o schema deste serviço — o Flyway
recria as 16 tabelas a cada subida.

```bash
cp .env.example .env
# preencha ORACLE_PASSWORD, ORACLE_SYS_PASSWORD e PETBUDDIES_JWT_SECRET (mínimo 32 bytes)

docker compose up -d --wait   # sobe só o Oracle; a aplicação roda no terminal
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

`ORACLE_PORT` é parametrizável — útil para rodar mais de uma worktree ao mesmo tempo, cada uma com sua
própria porta e seu próprio `COMPOSE_PROJECT_NAME`:

```bash
COMPOSE_PROJECT_NAME=minha-worktree ORACLE_PORT=1581 docker compose up -d --wait
```

Derrube com `docker compose down -v` — o `-v` apaga o volume, e é o ciclo normal desta sprint: os
bancos são resetados a cada subida, não migrados (ADR `s3-17`).

### Oracle FIAP (alternativa)

```bash
git clone https://github.com/3BugBuddies/PetBuddies-AI
cd petbuddies-ai
cp .env.example .env
# ORACLE_URL aponta por padrão para o Oracle FIAP; preencha ORACLE_USER (seu RM) e ORACLE_PASSWORD

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

A aplicação sobe em `http://localhost:8080`, com o Swagger em `/swagger-ui.html`.

### Variáveis de ambiente

Todas em `.env.example`, nunca versionadas com valor real (`.env` está no `.gitignore`):

| Variável | Para quê |
|---|---|
| `ORACLE_URL`, `ORACLE_USER`, `ORACLE_PASSWORD` | conexão com o Oracle (local ou FIAP) — **`ORACLE_USER` em maiúsculas**: o Oracle guarda nome de schema em maiúsculas e o `default_schema` do Hibernate usa o valor literal |
| `ORACLE_SYS_PASSWORD` | senha do `SYS` do container local; só o `docker-compose.yml` usa |
| `ORACLE_PORT` | porta do Oracle no host, para rodar mais de uma worktree ao mesmo tempo |
| `PETBUDDIES_JWT_SECRET` | segredo `HS256` do token (ADR `s3-20`) — mínimo 32 bytes, **o mesmo valor byte a byte do .NET**; abaixo disso a aplicação recusa subir, de propósito |
| `GEMINI_API_KEY` | reservada para o check-in por IA; não é lida por nenhum código hoje |

### Usuários de demonstração

`V2__seed_demonstracao.sql` semeia uma clínica, um veterinário, um responsável e os dois usuários que
a banca usa para entrar:

| Login | Perfil | Senha |
|---|---|---|
| `ana@clinica.com` | `VET` | `petbuddies123` |
| `maria@email.com` | `TUTOR` | `petbuddies123` |

---

## Tecnologias

- **Java 21** · Spring Boot 3.4.5
- **Spring Data JPA + Hibernate** sobre **Oracle Database** (23 local via `gvenzl/oracle-free`, ou FIAP)
- **Flyway** — quem cria o schema (`ddl-auto=validate`, nunca `update`)
- **Spring Security** — duas cadeias (API com Bearer, web com formulário) e JWT via `jjwt`
- **Spring HATEOAS** — toda resposta de recurso em `EntityModel`/`CollectionModel`
- **Bean Validation** (Jakarta)
- **Springdoc OpenAPI 2.8.8** — Swagger UI com tags por domínio
- **Postman** — coleção em `docs/postman/petbuddies-ai-java.postman_collection.json` (hoje vazia: os
  únicos requests que existiam eram do catálogo de protocolos, removido nesta sprint — ver PR)
