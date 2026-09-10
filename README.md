# PetBuddies AI — Challenge FIAP 2026 | Java Advanced

API do produto **PetBuddies**, desenvolvida com Spring Boot como parte do Challenge da disciplina de
**Java Advanced (2TDS)** — FIAP 2026.

Desde a onda 4 da Sprint 3 (ADR `s3-25`) este serviço é **a API única do produto**: absorveu todo o
registro clínico que antes vivia no `PetBuddies-API` (.NET) — clínica, veterinário, responsável,
animal, consulta, condição clínica, janela de atendimento, registro de atendimento, procedimento,
prescrição e regra de prescrição — além do motor de planos de cuidado e do check-in narrado com
extração por IA. O .NET virou o back-office da clínica: só o catálogo de protocolos e as regras de
pontuação continuam lá, e o Java lê o catálogo por HTTP no momento em que um plano nasce.

**A IA interpreta a narrativa — nunca decide dose.** No check-in, ela lê o relato do tutor; na
prescrição, a narrativa da veterinária. Quem decide a dose é sempre o motor determinístico, aplicando
a regra que a veterinária assinou (ADRs `s3-26` e `s3-27`, adiante).

---

## Links

|                               | |
|-------------------------------|---|
| Deploy                        | *pendente — ainda não há ambiente publicado* |
| Swagger UI (produção)         | *pendente* |
| Swagger UI (local)            | `http://localhost:8080/swagger-ui.html` |
| Postman collection            | [`docs/postman/petbuddies-ai-java.postman_collection.json`](docs/postman/petbuddies-ai-java.postman_collection.json) |
| Vídeo de apresentação         | *pendente* |

---

## Integrantes do Grupo

| Nome | RM |
|------|----|
| Felipe Yuiti Ishii | 565339 |
| Gabriel Nogueira Peixoto | 563925 |
| Giovanna Neri dos Santos | 566154 |
| Mariana Inoue | 565834 |

---

## Configuração — Spring Initializr

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring Web | WEB | controllers REST e MVC no mesmo processo |
| Spring HATEOAS | WEB | `_links` / `_embedded` em toda resposta de recurso (ADR `s3-22`) |
| Thymeleaf | WEB | as 8 telas server-rendered, vet e tutor |
| Spring Data JPA | SQL | 16 entidades JPA, um repositório por agregado |
| Oracle Driver (`ojdbc11`) | SQL | Oracle 23 local (`gvenzl/oracle-free`) ou Oracle FIAP |
| Flyway (`flyway-core` + `flyway-database-oracle`) | SQL | dono do schema — `V1` cria as 16 tabelas, `ddl-auto=validate` |
| Spring Security | SEGURANÇA | duas cadeias: API com Bearer JWT, web com formulário e sessão |
| jjwt (`api`/`impl`/`jackson`) | SEGURANÇA | emissão e validação do token HS256 — mesmo segredo do .NET (ADR `s3-20`) |
| Spring AI (`spring-ai-starter-model-openai`) | AI | Gemini 2.5 Flash via camada de compatibilidade OpenAI — extração do check-in e da prescrição narrada |
| Bean Validation | I/O | validação de DTOs com anotações Jakarta |
| Springdoc OpenAPI | DEV | Swagger UI com tags por domínio |
| Lombok | DEV | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` em entidades e DTOs |
| spring-dotenv | DEV | carrega `.env` em desenvolvimento local |

---

## Stack

- **Java 21** · Spring Boot 3.4.5
- **Spring AI 1.1.6** — Gemini `gemini-2.5-flash`, `temperature=0` (sem isso a confiança da extração não calibra)
- **Spring Data JPA + Hibernate** sobre **Oracle** (23 local via `gvenzl/oracle-free`, ou FIAP)
- **Flyway** — quem cria o schema (`ddl-auto=validate`, nunca `update`)
- **Spring Security** — duas cadeias (API com Bearer, web com formulário) e JWT via `jjwt`
- **Spring HATEOAS** — toda resposta de recurso em `EntityModel`/`CollectionModel`, com assemblers dedicados
- **Bean Validation** (Jakarta)
- **Springdoc OpenAPI 2.8.8** — Swagger UI com tags por domínio
- **Lombok** — reduz boilerplate nas entidades e DTOs
- **Postman** — coleção em `docs/postman/petbuddies-ai-java.postman_collection.json`

---

## Estrutura do Projeto

```
br/com/fiap/petbuddies/
├── controller/              # REST — um subpacote por domínio
│   ├── identidade/          # AuthController
│   ├── cadastro/            # Clinica, Veterinario, Responsavel, Animal
│   ├── atendimento/         # Consulta, JanelaAtendimento, RegistroAtendimento, Procedimento, CondicaoClinica
│   ├── prescricao/          # Prescricao, RegraPrescricao, ExtracaoPrescricao (rascunho por IA)
│   ├── cuidado/             # MotorPlanoController
│   └── checkin/             # CheckinController (extração + registro)
├── web/                     # 8 telas Thymeleaf, IndexWebController (redireciona por perfil)
│   └── form/                # objeto de formulário quando o *Request de DTO não cobre a tela
├── service/                 # regra de negócio — mesmos subpacotes do controller
├── dto/                     # records/classes por domínio, flat — sem request/response aninhados
├── domain/
│   ├── entity/               # 16 entidades JPA
│   ├── enums/                 # um subpacote por domínio, @Enumerated(STRING)
│   └── repository/            # um JpaRepository por entidade
├── assembler/                # RepresentationModelAssembler (HATEOAS), um por recurso
├── infrastructure/client/    # ProtocoloClient — a única chamada ao .NET
├── security/                 # SecurityConfig, TokenService, TokenAuthenticationFilter, UsuarioDetailsService/Principal
├── exception/                 # exceções de domínio — mesmos subpacotes do controller
├── handler/                   # GlobalExceptionHandler (API) e WebExceptionHandler (telas)
└── config/                    # OpenApiConfig, CuidadoPersistenceConfig
```

---

## Como Executar

### Pré-requisitos

- Java 21+
- Maven 3.9+ (**este repositório não versiona o Maven Wrapper** — todo comando abaixo é o `mvn` do
  sistema, não `./mvnw`)
- Docker, para o Oracle local — **ou** acesso ao Oracle FIAP
- Uma chave do Gemini ([Google AI Studio](https://aistudio.google.com)) — sem ela a aplicação sobe,
  mas os endpoints de IA (check-in e rascunho de prescrição) falham em tempo de execução, ver abaixo

### Banco local (recomendado para desenvolvimento)

O `docker-compose.yml` sobe um `gvenzl/oracle-free:23-slim` só com o schema deste serviço — o Flyway
recria as 16 tabelas a cada subida.

```bash
cp .env.example .env
# preencha ORACLE_PASSWORD, ORACLE_SYS_PASSWORD, PETBUDDIES_JWT_SECRET (mínimo 32 bytes) e GEMINI_API_KEY

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
| `ORACLE_URL` | conexão com o Oracle. Tem default para o Oracle FIAP (`application.properties`); o `docker-compose.yml` aponta pro container local |
| `ORACLE_USER`, `ORACLE_PASSWORD` | credencial do schema — sem default, **a aplicação não sobe sem elas**. `ORACLE_USER` em **maiúsculas**: o Oracle guarda nome de schema em maiúsculas e o `default_schema` do Hibernate usa o valor literal |
| `ORACLE_SYS_PASSWORD` | senha do `SYS` do container local; só o `docker-compose.yml` usa |
| `ORACLE_PORT` | porta do Oracle no host, para rodar mais de uma worktree ao mesmo tempo |
| `PETBUDDIES_JWT_SECRET` | segredo `HS256` do token (ADR `s3-20`) — mínimo 32 bytes, **o mesmo valor byte a byte do .NET**; abaixo disso a aplicação recusa subir, de propósito |
| `GEMINI_API_KEY` | chave do Gemini, para o `ChatClient` do Spring AI que a extração do check-in (`CheckinExtracaoService`) e o rascunho de prescrição por IA (`POST /api/prescricoes/rascunho`) usam. Diferente de `ORACLE_URL`, a propriedade não tem default: **totalmente ausente do ambiente**, o placeholder não resolve e a aplicação não sobe; **presente mas vazia** (o estado do `.env.example`), a aplicação sobe normalmente e só a chamada ao Gemini falha em tempo de execução — verificado no `OpenAiApi.Builder` do Spring AI 1.1.6, que só recusa uma chave `null`, não uma vazia |

### Usuários de demonstração

`V2__seed_demonstracao.sql` semeia uma clínica, um veterinário, um responsável e os dois usuários que
a banca usa para entrar:

| Login | Perfil | Senha |
|---|---|---|
| `ana@clinica.com` | `VET` | `petbuddies123` |
| `maria@email.com` | `TUTOR` | `petbuddies123` |

---

## Diagrama de Classes

*Pendente.* Os três diagramas em `assets/` (`diagrama-java-entidades-bot.jpeg`,
`diagrama-java-score-motor.jpeg`, `diagrama-java-entidades-base.png`) descrevem o desenho anterior ao
`s3-25` — bot, score de risco, `ProtocoloEntity` e `EventoProtocoloEntity`, nenhum dos quais existe
mais neste serviço. Publicar um diagrama das 16 entidades atuais fica para quando alguém gerar um
novo a partir do código de hoje.

---

## Modelo de Dados

**16 tabelas**, todas no schema próprio deste serviço, criadas por `V1__baseline_schema_cuidado.sql`
(`ddl-auto=validate`, ADR `s3-17`). Todas têm entidade JPA.

| Tabela | Entidade | Papel |
|---|---|---|
| `T_PB_CLINICA` | `ClinicaEntity` | a clínica veterinária |
| `T_PB_RESPONSAVEL` | `ResponsavelEntity` | o tutor do animal |
| `T_PB_VETERINARIO` | `VeterinarioEntity` | quem assina o ato clínico, vinculado a uma clínica |
| `T_PB_USUARIO` | `UsuarioEntity` | credencial e perfil (`VET` / `TUTOR`), com o vínculo para um dos dois acima |
| `T_PB_ANIMAL` | `AnimalEntity` | o paciente |
| `T_PB_CONSULTA` | `ConsultaEntity` | o agendamento e o comparecimento do animal |
| `T_PB_CONDICAO_CLINICA` | `CondicaoClinicaEntity` | catálogo de condições que o check-in avalia, por clínica |
| `T_PB_JANELA_ATENDIMENTO` | `JanelaAtendimentoEntity` | a agenda do veterinário — slots livres ou reservados por uma consulta |
| `T_PB_REGISTRO_ATENDIMENTO` | `RegistroAtendimentoEntity` | o que aconteceu na consulta: anamnese, diagnóstico, tratamento |
| `T_PB_PROCEDIMENTO` | `ProcedimentoEntity` | vacina, exame ou cirurgia executados num atendimento |
| `T_PB_PRESCRICAO` | `PrescricaoEntity` | o ato assinado pelo veterinário — imutável depois de criado |
| `T_PB_REGRA_PRESCRICAO` | `RegraPrescricaoEntity` | condição → ação sobre a dose, com rótulo, tipo e fonte congelados no momento da assinatura |
| `T_PB_CHECKIN` | `CheckinEntity` | o relato do tutor — um por animal e por dia, ou por item quando o relato é sobre um cuidado específico |
| `T_PB_CONDICAO_OBSERVADA` | `CondicaoObservadaEntity` | a condição confirmada pelo tutor naquele check-in — imutável |
| `T_PB_PLANO_CUIDADO` | `PlanoCuidadoEntity` | o plano de cuidado vivo de um animal |
| `T_PB_ITEM_PLANO_CUIDADO` | `ItemPlanoCuidadoEntity` | o item do plano — data alvo, origem e, depois do check-in, o desfecho e a dose aplicada |

O protocolo em si (`o quê` e `quando` do cuidado) **não tem tabela neste serviço**: é catálogo do
.NET, lido por HTTP no momento em que um plano nasce (`ProtocoloClient` / `ProtocoloCatalogoDto`).
`PlanoCuidadoEntity` guarda `ID_ANIMAL`, `ID_CONSULTA` e `ID_PROTOCOLO` como ids soltos, sem
`@ManyToOne` — os dois primeiros porque o desenho nasceu para cruzar serviço antes do `s3-25` e não
foi convertido para relação JPA depois que animal e consulta viraram tabelas locais; o terceiro
porque o protocolo de fato continua remoto.

Todo enum de domínio é persistido como texto (`@Enumerated(EnumType.STRING)`) — `ORDINAL`
corromperia os dados na primeira reordenação. Cada domínio tem seu próprio subpacote em
`domain/enums/` (`identidade`, `cadastro`, `atendimento`, `prescricao`, `cuidado`, `checkin`); a lista
completa de valores está no Swagger.

### Por que Repository e não DAO?

O Spring Data JPA já gerencia o `EntityManager` — ciclo de vida, transações, thread-safety. O
`JpaRepository` entrega o CRUD pronto por interface, sem implementação. DAO faria sentido se
precisássemos de controle fino sobre o `EntityManager`; aqui o Spring cuida disso melhor do que
faríamos à mão.

---

## Autenticação e perfis

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

O token vai em `Authorization: Bearer <token>` em toda chamada a `/api/**`. Qualquer usuário
autenticado, `VET` ou `TUTOR`, acessa a maioria das rotas de `/api/**` — a única exceção hoje é
`POST /api/prescricoes/rascunho`, que exige o papel `VET` (`SecurityConfig`): o rascunho de
prescrição narrada é ato clínico, e tutor não autora prescrição. `/api/auth/login` é a única rota
aberta.

---

## Check-in narrado e motor de regras (IA)

A IA entra em dois pontos, sempre para **interpretar linguagem natural** — nunca para decidir dose:

1. **Prescrição narrada** (`POST /api/prescricoes/rascunho`, vet): a veterinária narra o
   atendimento e a IA devolve um rascunho de prescrição e de regras condicionais, nunca persistido —
   a vet revisa e assina pelo fluxo normal.
2. **Check-in do tutor** (`POST /api/checkins/extracao` → `POST /api/checkins`): o tutor narra como
   o animal está e a IA extrai as condições do vocabulário em vigor para aquele animal.

O check-in roda em dois passos, e nunca ao contrário:

```
Narrativa do tutor
  → CheckinExtracaoService (IA — Spring AI + Gemini, ADR s3-13)
      interpreta contra o vocabulário de condições em vigor; nunca grava
  → POST /api/checkins/extracao devolve o que entendeu, com "confianca" por condição
  → o tutor confirma (ou corrige) na tela
  → POST /api/checkins grava a narrativa e as condições confirmadas
  → AvaliadorRegraService (determinístico, sem chamada ao modelo)
      compara o valor confirmado com a regra que a veterinária assinou
  → grava o desfecho (dose calculada, acionar clínica, ou sem dose) no item do plano
```

**Guardrails aplicados no código** (`CheckinExtracaoService`, `AvaliadorRegraService`,
`CheckinService`):

- A extração **nunca grava** e **nunca decide** — só preenche o schema derivado do vocabulário da
  clínica; quem aplica a regra é sempre o `AvaliadorRegraService`.
- Condição não mencionada na narrativa não vira `false` — a ausência do item é o "não sei"; mencionar
  e negar gera um item explícito.
- Código fora do vocabulário oferecido, confiança fora de `[0,1]` ou valor incoerente com o tipo da
  condição são descartados no filtro do serviço, mesmo que o modelo erre a instrução do prompt.
- Condição marcada como crítica no catálogo escala para `ACIONAR_CLINICA` **antes** de qualquer regra
  rodar, independentemente de existir regra de prescrição sobre ela (ADR `s3-16`).
- Menção clínica fora do vocabulário vira `redFlags[]`, texto livre — nunca um código inventado.

Duas decisões de implementação, registradas como ADR em 2026-09-10:

- **`s3-26` — a dose base é o piso da faixa (`NR_DOSE_MIN`).** Quando nenhuma regra casa, ou quando
  uma regra pede `DOSE_PADRAO`, a dose devolvida é o mínimo da faixa que a veterinária assinou — o
  ponto mais conservador dentro do que ela já julgou seguro. A prescrição não tem coluna de dose
  "padrão" separada do mínimo.
- **`s3-27` — o limiar de confiança da extração é `0.7`.** Acima dele, a condição segue para o
  cálculo; abaixo, a extração já sinaliza baixa confiança para o app perguntar ao tutor em vez de
  assumir. Calibrado contra 108 chamadas reais do modelo (`.claude/docs/ia/matriz_clinica.py`), não
  por curva ROC — documentado como tal de propósito.

---

## Como o Java e o .NET se falam

**Uma única chamada, Java → .NET.** Ao instanciar um plano de cuidado, o `MotorPlanoService` pergunta
ao `ProtocoloClient` quais protocolos ativos existem para a categoria e espécie do animal
(`GET /api/protocolos` no .NET, com um token de serviço emitido pelo próprio Java). Se o .NET estiver
fora do ar, a resposta é a mesma de "nenhum protocolo compatível": o plano simplesmente não nasce
agora, sem propagar erro.

O gatilho que existia no sentido contrário — o .NET chamando `/api/motor/**` sem token quando um
animal ou uma cirurgia eram cadastrados — foi removido na onda 4 junto com o `MotorApiClient` do
.NET (ADR `s3-25`). Hoje o Java não recebe chamada nenhuma do .NET.

---

## Superfície web (Thymeleaf)

Oito telas server-rendered, com acesso por perfil (`SecurityConfig`): o vet abre seis, o tutor abre
uma. A raiz autenticada (`/`) redireciona cada perfil para a própria tela inicial, e cada zona devolve
`403` para o perfil que não é dela.

| Rota | Perfil | Tela |
|---|---|---|
| `/login` | público | formulário de entrada |
| `/painel` | `VET` | números da clínica e as consultas do dia |
| `/clinica` | `VET` | a clínica (única no produto) — visualizar e editar, sem lista |
| `/equipe` | `VET` | cadastro dos veterinários da clínica |
| `/tutores` | `VET` | cadastro de responsáveis |
| `/pacientes` | `VET` | cadastro de animais; na ficha, instanciar plano de cuidado a partir de protocolo |
| `/agenda` | `VET` | agenda de consultas e fechamento de atendimento |
| `/meus-animais` | `TUTOR` | os próprios animais e o plano ativo de cada um — a única tela do tutor |

---

## Recursos e Rotas

Toda resposta de recurso único ou coleção vem em envelope HATEOAS (`EntityModel` /
`CollectionModel`, com `_links` e `_embedded`) — ADR `s3-22`. Erros vêm como
`ErrorDto{ code, message }`, montado por um `@RestControllerAdvice` global. A lista de parâmetros, o
corpo de cada requisição e os códigos de erro estão no Swagger — é a fonte que não fica desatualizada.

### Autenticação

| Método | Rota |
|---|---|
| `POST` | `/api/auth/login` |

### Cadastro

| Recurso | Rota base | Métodos |
|---|---|---|
| Clínicas | `/api/clinicas` | `GET`, `GET /buscar?cnpj=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Veterinários | `/api/veterinarios` | `GET (?clinicaId=)`, `GET /buscar?crmv=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Responsáveis | `/api/responsaveis` | `GET (?nome=)`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Animais | `/api/animais` | `GET (?responsavelId=&nome=)`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |

### Atendimento

| Recurso | Rota base | Métodos |
|---|---|---|
| Consultas | `/api/consultas` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `POST /agendamentos`, `POST /{id}/cancelamento`, `POST /{id}/fechamento` |
| Janelas de atendimento | `/api/janelas-atendimento` | `GET (?veterinarioId=)`, `GET /livres`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Registros de atendimento | `/api/registros-atendimento` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Procedimentos | `/api/procedimentos` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Condições clínicas | `/api/condicoes-clinicas` | `GET (?clinicaId=)`, `GET /buscar?clinicaId=&codigo=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |

### Prescrição

| Recurso | Rota | Métodos |
|---|---|---|
| Prescrições | `/api/prescricoes` | `GET`, `GET /{id}`, `POST` (sem `PUT`/`DELETE` — ato imutável) |
| Rascunho por IA | `/api/prescricoes/rascunho` | `POST` — interpreta a narrativa da vet e devolve prescrição + regras propostas, sem gravar (exige papel `VET`) |
| Regras de prescrição | `/api/regras-prescricao` | `GET`, `GET /{id}`, `POST` (sem `PUT`/`DELETE` — ato imutável) |

### Motor de planos

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/motor/planos/instanciar-preventivo` | cria (ou devolve o existente, idempotente) o plano preventivo do animal |
| `POST` | `/api/motor/planos/instanciar-pos-cirurgico` | idem, para o plano pós-cirúrgico vinculado a uma consulta |
| `GET` | `/api/motor/planos/{animalId}` | plano `ATIVO` do animal, com os eventos pendentes |
| `GET` | `/api/motor/planos/{animalId}/eventos` | itens do plano, paginado (`?page=&size=`) |
| `GET` | `/api/motor/planos/{animalId}/protocolo-aplicado` | planos nascidos de protocolo, itens separados em realizados/pendentes/vencidos — leitura 100% local |
| `GET` | `/api/motor/planos/{animalId}/sugestoes` | próximo cuidado sugerido a partir do histórico do animal |

### Check-in

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/checkins/extracao` | passo 1 — interpreta a narrativa do tutor por IA; não grava nada |
| `POST` | `/api/checkins` | passo 2 — grava o check-in confirmado, avalia a regra congelada e grava o desfecho |
| `GET` | `/api/checkins/{id}` | busca por id |
| `GET` | `/api/checkins?animalId=` | lista os check-ins do animal, do mais recente ao mais antigo |

---

## Roteiro do Fluxo Principal

Execute na ordem abaixo para acionar o fluxo completo, usando o usuário de demonstração `VET`.

| Passo | Recurso | Método | Rota | O que observar |
|---|---|---|---|---|
| 1 | Login | `POST` | `/api/auth/login` | `200` + token; use-o como Bearer nos passos seguintes |
| 2 | Animais | `POST` | `/api/animais` | `201` — cadastre um paciente para o responsável do seed (`responsavelId: 1`) |
| 3 | Condições clínicas | `POST` | `/api/condicoes-clinicas` | `201` — o vocabulário que o check-in vai avaliar |
| 4 | Janelas de atendimento | `POST` | `/api/janelas-atendimento` | `201` — um slot livre para o veterinário do seed |
| 5 | Consultas | `POST` | `/api/consultas/agendamentos` | `201` — ocupa a janela; a consulta nasce `AGENDADA` |
| 6 | Consultas | `POST` | `/api/consultas/{id}/fechamento` | `201` — grava registro, procedimentos e prescrições com regras **numa transação só**; a consulta vira `REALIZADA` |
| 7 | Motor de planos | `POST` | `/api/motor/planos/instanciar-preventivo` | `201` (ou `200` se já existir) — lê o catálogo do .NET e materializa o plano |
| 8 | Check-in | `POST` | `/api/checkins/extracao` | `200` — a IA interpreta a narrativa do tutor contra o vocabulário do animal |
| 9 | Check-in | `POST` | `/api/checkins` | `201` — grava o check-in confirmado; o motor de regras decide a dose ou escala à clínica |

---

## Validações — respostas de erro

| Situação | Exemplo | Status |
|---|---|---|
| Campo obrigatório ausente ou fora de faixa | `POST /api/animais` sem `nome` | `400` |
| Parâmetro de query obrigatório ausente | `GET /api/checkins` sem `animalId` | `400` |
| JSON malformado ou enum inválido | `POST /api/animais` com `"especie": "INVALIDO"` | `400` |
| Faixa de dose invertida | `POST /api/prescricoes` com `doseMin > doseMax` | `400` |
| Credenciais inválidas | `POST /api/auth/login` com senha errada | `401` — mesma mensagem para login inexistente e usuário inativo |
| Papel sem permissão | `TUTOR` chamando `POST /api/prescricoes/rascunho` | `403` |
| Recurso inexistente | `GET /api/animais/999999` | `404` |
| CNPJ, CRMV ou código duplicado | `POST /api/clinicas` com CNPJ repetido | `409` |
| Janela ocupada ou consulta já realizada | `POST /api/consultas/agendamentos` numa janela ocupada | `409` |
| Check-in duplicado | `POST /api/checkins` repetido para o mesmo animal, data e item | `409` |

---

## Como Testar

### Via Swagger UI

Acesse `http://localhost:8080/swagger-ui.html` — endpoints organizados por tag de domínio, com
"Authorize" e Try it out habilitados. **O JSON do OpenAPI vive em `/api-docs`, não em `/v3/api-docs`**
— o `springdoc.api-docs.path` foi remapeado (`application.properties`) porque `/v3/api-docs` cairia
sob `/api/**` e a cadeia de segurança da API o bloquearia; `/api-docs` fica de fora e é liberado
explicitamente na cadeia web (`SecurityConfig`).

### Via Postman

Importe `docs/postman/petbuddies-ai-java.postman_collection.json`. A collection já vem com
autenticação Bearer no nível da collection (variável `token`) e `baseUrl` apontando para
`http://localhost:8080`: faça o login, copie o token para a variável, e as pastas seguintes já saem
autenticadas.

**Cobertura verificada nesta entrega:** as 15 pastas existentes (Autenticação, Clínicas,
Veterinários, Responsáveis, Animais, Consultas, Janelas de atendimento, Registros de atendimento,
Procedimentos, Prescrições, Regras de prescrição, Condições clínicas, Motor de planos) apontam para
rotas que **existem no código hoje** — nenhuma rota morta encontrada. A coleção **não cobre** rotas
adicionadas depois de montada: `POST /api/consultas/agendamentos` já está lá, mas faltam
`POST /api/consultas/{id}/cancelamento`, `GET /api/janelas-atendimento/livres`,
`GET /api/motor/planos/{animalId}/protocolo-aplicado`, `GET /api/motor/planos/{animalId}/sugestoes`,
`POST /api/prescricoes/rascunho` e a pasta inteira de **Check-in** (`/api/checkins`,
`/api/checkins/extracao`). Fica como lacuna de cobertura para uma próxima atualização da coleção —
não corrigido nesta entrega.

---

## Exemplos de Payload

#### `POST /api/auth/login`
```json
{ "login": "ana@clinica.com", "senha": "petbuddies123" }
```

#### `POST /api/animais`
```json
{ "nome": "Rex", "especie": "CACHORRO", "raca": "Vira-lata", "porte": "MEDIO", "sexo": "MACHO",
  "dataNascimento": "2021-03-15", "peso": 18.5, "castrado": true, "responsavelId": 1 }
```

#### `POST /api/motor/planos/instanciar-preventivo`
```json
{ "animalId": 1, "especie": "CACHORRO", "dataNascimento": "2021-03-15" }
```

#### `POST /api/checkins/extracao` (passo 1 — interpreta, não grava)
```json
{ "animalId": 1, "narrativa": "Ele comeu bem hoje, mas as fezes estavam mais moles que o normal." }
```
> Resposta traz `condicoes[]` com `confianca` por item (ADR `s3-27`: abaixo de `0.7`, o app deve
> confirmar com o tutor antes de seguir) e `degradado: true` se o modelo falhou ou nada devolveu.

#### `POST /api/checkins` (passo 2 — grava o confirmado)
```json
{
  "animalId": 1,
  "narrativa": "Ele comeu bem hoje, mas as fezes estavam mais moles que o normal.",
  "condicoes": [
    { "condicaoClinicaId": 3, "valorBooleano": true, "confianca": 0.92 }
  ]
}
```

#### `POST /api/consultas/{id}/fechamento`
```json
{
  "registroAtendimento": {
    "dataAtendimento": "2026-09-10T14:30:00",
    "anamnese": "Tutor relata apetite normal.",
    "diagnostico": "Gastroenterite leve.",
    "tratamento": "Dieta leve por 3 dias."
  },
  "prescricoes": [
    {
      "medicamento": "Lactulona", "doseMin": 1.0, "doseMax": 2.0, "unidade": "ml",
      "frequenciaDia": 2, "duracaoDias": 5, "dataInicio": "2026-09-10",
      "orientacao": "Se as fezes estiverem moles, aplicar a dose menor.",
      "regras": [
        { "condicaoClinicaId": 3, "acaoDose": "DOSE_MIN", "ordem": 1 }
      ]
    }
  ]
}
```
