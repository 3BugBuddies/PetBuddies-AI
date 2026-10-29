# PetBuddies AI — Challenge FIAP 2026 | Java Advanced

API REST e painel web do **PetBuddies**, o cuidado contínuo de pets entre a clínica veterinária e o responsável. Challenge de **Java Advanced (2TDS)**, FIAP 2026.

A veterinária atende, prescreve e monta o plano de cuidado pelo painel da clínica. Em casa, o responsável conta pelo app como o animal está, e o serviço transforma esse relato na conduta que a veterinária deixou assinada.

- **Cadastro e agenda da clínica:** clínica, equipe, responsáveis, pacientes, horários de atendimento e consultas, numa agenda em grade semanal.
- **Atendimento e prescrição:** a consulta fecha em quatro etapas (registro, procedimento, prescrição e revisão) numa operação só. A receita pode partir de uma receita-modelo da clínica, e a IA monta o rascunho a partir da fala da veterinária.
- **Plano de cuidado:** vacinas, vermífugos e retornos gerados a partir dos protocolos que a clínica configura no `PetBuddies-API` (.NET).
- **Check-in do responsável:** a IA reconhece no relato as condições clínicas que a veterinária definiu, e um motor determinístico aplica as regras dela para decidir a dose do dia ou acionar a clínica. Um sinal de alarme suspende o tratamento até a clínica avaliar.
- **Dois perfis:** `VET` e `TUTOR`. O app mobile usa token JWT para os dois. O painel web é da clínica, com sessão.

<img src="assets/figuras/arquitetura.png" alt="Arquitetura e esteira CI/CD: o desenvolvedor faz push na main, o Azure Pipelines compila, testa, publica o artefato e faz o deploy nos Web Apps Java e .NET; em produção, a veterinária usa o painel pelo navegador e o responsável usa o app, os dois chegando ao Web App Java, que usa o Oracle FIAP, o Gemini e lê o catálogo do .NET">

## Índice

1. [Integrantes do Grupo](#integrantes-do-grupo)
2. [Configuração — Spring Initializr](#configuração--spring-initializr)
3. [Stack](#stack)
4. [Estrutura do Projeto](#estrutura-do-projeto)
5. [Como Executar](#como-executar)
6. [Deploy e CI/CD](#deploy-e-cicd)
7. [Diagrama de classes](#diagrama-de-classes)
8. [Autenticação e perfis](#autenticação-e-perfis)
9. [Fluxos Principais](#fluxos-principais)
10. [Painel da clínica (Thymeleaf)](#painel-da-clínica-thymeleaf)
11. [Recursos e Rotas](#recursos-e-rotas)
12. [Como Testar](#como-testar)
13. [Exemplos de Payload](#exemplos-de-payload)

---

## Links

|                               | |
|-------------------------------|---|
| Painel da clínica (produção)  | https://petbuddies-java-rm563925.azurewebsites.net |
| Swagger UI (produção)         | https://petbuddies-java-rm563925.azurewebsites.net/swagger-ui.html |
| Saúde (produção)              | https://petbuddies-java-rm563925.azurewebsites.net/actuator/health |
| Azure DevOps                  | [Sprint 4 – Azure DevOps](https://dev.azure.com/RM565834/Sprint%204%20%E2%80%93%20Azure%20DevOps) |
| Swagger UI (local)            | `http://localhost:8080/swagger-ui.html` |
| Postman collection            | [`docs/postman/petbuddies-ai-java.postman_collection.json`](docs/postman/petbuddies-ai-java.postman_collection.json) |
| Vídeo de apresentação         | https://youtu.be/VrChl50hDlo |

---

## Integrantes do Grupo

| Nome | RM | Turma |
|------|----|-------|
| Felipe Yuiti Ishii | 565339 | 2TDS Fevereiro |
| Gabriel Nogueira Peixoto | 563925 | 2TDS Fevereiro |
| Giovanna Neri dos Santos | 566154 | 2TDS Fevereiro |
| Mariana Inoue | 565834 | 2TDS Fevereiro |

---

## Configuração — Spring Initializr

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring Web | WEB | controllers REST e MVC no mesmo processo |
| Spring HATEOAS | WEB | `_links` / `_embedded` em toda resposta de recurso |
| Thymeleaf | WEB | o painel da clínica, server-rendered |
| Spring Data JPA | SQL | 18 entidades JPA, um repositório por agregado |
| Oracle Driver (`ojdbc11`) | SQL | Oracle 23 local (`gvenzl/oracle-free`) ou Oracle FIAP |
| Flyway (`flyway-core` + `flyway-database-oracle`) | SQL | dono do schema: `V1` cria as tabelas, `V2` semeia a demonstração, `V3`–`V6` evoluem o modelo; `ddl-auto=validate` |
| Spring Security | SEGURANÇA | duas cadeias: API com Bearer JWT, web com formulário e sessão |
| jjwt (`api`/`impl`/`jackson`) | SEGURANÇA | emissão e validação do token HS256 |
| Spring AI (`spring-ai-starter-model-openai`) | AI | Gemini 2.5 Flash via camada de compatibilidade OpenAI |
| Bean Validation | I/O | validação de DTOs com anotações Jakarta |
| Spring Boot Actuator | OPS | `GET /actuator/health` aberto, com o status da aplicação e do banco |
| Springdoc OpenAPI | DEV | Swagger UI com tags por domínio |
| Lombok | DEV | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` em entidades e DTOs |
| spring-dotenv | DEV | carrega `.env` em desenvolvimento local |

---

## Stack

- **Java 21** · Spring Boot 3.4.5
- **Spring AI 1.1.6** — Gemini `gemini-2.5-flash`, `temperature=0`
- **Spring Data JPA + Hibernate** sobre **Oracle** (23 local via `gvenzl/oracle-free`, ou FIAP)
- **Flyway** — dono do schema (`ddl-auto=validate`, nunca `update`)
- **Spring Security** — duas cadeias (API com Bearer, web com formulário) e JWT via `jjwt`
- **Spring HATEOAS** — respostas em `EntityModel`/`CollectionModel`, com assemblers dedicados
- **Spring Boot Actuator** — só o `health` exposto
- **Bean Validation** (Jakarta) · **Springdoc OpenAPI 2.8.8** · **Lombok**
- **Azure** — Web App Linux (Java 21) no plano B2, Application Insights e Oracle FIAP
- **Azure DevOps** — Azure Repos e Azure Pipelines em YAML (`azure-pipelines.yml`)

---

## Estrutura do Projeto

```
br/com/fiap/petbuddies/
├── controller/              # REST — um subpacote por domínio
│   ├── identidade/          # AuthController
│   ├── cadastro/            # Clinica, Veterinario, Responsavel, Animal
│   ├── atendimento/         # Consulta, JanelaAtendimento, RegistroAtendimento, Procedimento, CondicaoClinica
│   ├── prescricao/          # Prescricao, RegraPrescricao, ReceitaModelo, ExtracaoPrescricao
│   ├── cuidado/             # MotorPlanoController
│   └── checkin/             # CheckinController
├── web/                     # painel da clínica (Thymeleaf), um controller por tela
│   └── form/                # objeto de formulário quando o DTO de request não cobre a tela
├── service/                 # regra de negócio — mesmos subpacotes do controller
├── dto/                     # um subpacote por domínio, flat
├── domain/
│   ├── entity/              # 18 entidades JPA
│   ├── embeddable/          # 6 valores @Embeddable, gravados nas colunas da entidade dona
│   ├── enums/               # um subpacote por domínio, @Enumerated(STRING)
│   └── repository/          # um JpaRepository por entidade
├── assembler/               # RepresentationModelAssembler (HATEOAS), um por recurso
├── infrastructure/client/   # ProtocoloClient — a chamada ao .NET
├── security/                # SecurityConfig, TokenService, TokenAuthenticationFilter, UsuarioPrincipal
├── exception/               # exceções de domínio — mesmos subpacotes do controller
├── handler/                 # GlobalExceptionHandler (API) e WebExceptionHandler (telas)
└── config/                  # OpenApiConfig, CuidadoPersistenceConfig
```

---

## Como Executar

### Pré-requisitos

- Java 21+ · Maven 3.9+ (este repositório não versiona o Maven Wrapper — use o `mvn` do sistema)
- Docker, para o Oracle local — **ou** acesso ao Oracle FIAP
- Uma chave do Gemini ([Google AI Studio](https://aistudio.google.com))

### Banco local (recomendado)

```bash
cp .env.example .env
# preencha ORACLE_PASSWORD, ORACLE_SYS_PASSWORD, PETBUDDIES_JWT_SECRET e GEMINI_API_KEY

docker compose up -d --wait                          # sobe só o Oracle
mvn spring-boot:run
```

Para rodar mais de uma instância em paralelo, parametrize porta e projeto:

```bash
COMPOSE_PROJECT_NAME=minha-instancia ORACLE_PORT=1581 docker compose up -d --wait
```

Derrube com `docker compose down -v`. Os bancos desta sprint são resetados a cada subida, não migrados.

### Oracle FIAP (alternativa)

```bash
cp .env.example .env
# ORACLE_URL já aponta para o Oracle FIAP; preencha ORACLE_USER (seu RM) e ORACLE_PASSWORD

mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`, com o Swagger em `/swagger-ui.html`.

### Variáveis de ambiente

Todas em `.env.example`; `.env` está no `.gitignore`.

| Variável | Para quê |
|---|---|
| `ORACLE_URL` | conexão com o Oracle. Default aponta para o Oracle FIAP; o `docker-compose.yml` aponta para o container local |
| `ORACLE_USER`, `ORACLE_PASSWORD` | credencial do schema. **A aplicação não sobe sem elas.** `ORACLE_USER` em maiúsculas — o Oracle guarda nome de schema assim |
| `ORACLE_SYS_PASSWORD` | senha do `SYS` do container local; só o `docker-compose.yml` usa |
| `ORACLE_PORT` | porta do Oracle no host, para rodar instâncias em paralelo |
| `PETBUDDIES_JWT_SECRET` | segredo `HS256` do token — mínimo 32 bytes, o mesmo valor do .NET. Abaixo disso a aplicação recusa subir |
| `GEMINI_API_KEY` | chave do Gemini. Ausente do ambiente, a aplicação não sobe; presente mas vazia, sobe e só as chamadas de IA falham |
| `PETNETAPI_URL` | endereço do .NET, lido para o catálogo de protocolos. Default `http://localhost:5297` |

### Usuários de demonstração

`V2__seed_demonstracao.sql` semeia uma clínica, um veterinário, um responsável e dois usuários:

| Login | Perfil | Senha |
|---|---|---|
| `ana@clinica.com` | `VET` | `petbuddies123` |
| `maria@email.com` | `TUTOR` | `petbuddies123` |

---

## Deploy e CI/CD

A aplicação roda num **Azure Web App** Linux com Java 21, no grupo `rg-petbuddies` (Brazil South), sobre o banco **Oracle da FIAP**. O deploy é feito pelo **Azure Pipelines**, a partir do repositório no **Azure Repos** do projeto [Sprint 4 – Azure DevOps](https://dev.azure.com/RM565834/Sprint%204%20%E2%80%93%20Azure%20DevOps).

A pipeline está em [`azure-pipelines.yml`](azure-pipelines.yml) e **dispara sozinha a cada push na `main`**. A figura do topo mostra a esteira numerada.

| Estágio | Tarefas | Resultado |
|---|---|---|
| **CI · Build e testes** | instala o Java 21, roda `mvn clean package` (com a publicação dos resultados de teste), copia o `.jar` e publica o artefato | artefato `app` no Azure DevOps |
| **CD · Deploy no Web App** | só roda se o CI passou. Grava as variáveis de ambiente no Web App e publica o artefato por Zip Deploy | versão nova no ar |

**Segredos:** usuário e senha do Oracle, chave do Gemini e segredo do JWT ficam como **variáveis secretas da pipeline**. Não aparecem no YAML nem no log, e o CD os grava como configuração do Web App.

**Monitoramento:** cada Web App tem um Application Insights, e `GET /actuator/health` responde `UP` com o banco no ar.

---

## Diagrama de classes

As 18 entidades JPA em seis pacotes de domínio, com os seis valores `@Embeddable` de `domain/embeddable/`. No código, cada entidade leva o sufixo `Entity`.

| Notação | Significado |
|---|---|
| seta cheia `——>` | `@ManyToOne` navegável, com a multiplicidade em cada ponta |
| losango cheio `◆——` | composição: `PlanoCuidado` e seus itens, `ReceitaModelo` e suas regras (`cascade = ALL`) |
| losango laranja `◆——` | valor `@Embeddable` gravado nas colunas da própria entidade |
| seta tracejada `- - >` | referência por id: um `Long` sem relação JPA |
| caixa tracejada `«Pacote» Classe` | classe de outro pacote, apontada a partir deste |

Clique numa miniatura para abrir o diagrama inteiro.

<table>
<tr>
<td width="33%" valign="top"><img src="assets/diagrama-classes/mapa.png" alt="Mapa de dependências entre os pacotes: todos apontam para Cadastro"><br><b>Pacotes</b> · a seta sai de quem referencia; o número conta as referências</td>
<td width="33%" valign="top"><img src="assets/diagrama-classes/cadastro.png" alt="Pacote Cadastro: Clinica, Veterinario, Responsavel e Animal, com o valor Contato"><br><b>Cadastro</b> · clínica, veterinário, responsável e animal</td>
<td width="33%" valign="top"><img src="assets/diagrama-classes/atendimento.png" alt="Pacote Atendimento: JanelaAtendimento, Consulta, RegistroAtendimento e Procedimento"><br><b>Atendimento</b> · janela, consulta, registro e procedimento</td>
</tr>
<tr>
<td width="33%" valign="top"><img src="assets/diagrama-classes/prescricao.png" alt="Pacote Prescrição: CondicaoClinica, Prescricao, RegraPrescricao, ReceitaModelo e RegraReceitaModelo, com os valores FaixaDose e CondicaoCongelada"><br><b>Prescrição</b> · condição clínica, prescrição, regra e receita-modelo</td>
<td width="33%" valign="top"><img src="assets/diagrama-classes/checkin.png" alt="Pacote Check-in: Checkin e CondicaoObservada, com o valor ValorObservado"><br><b>Check-in</b> · check-in e condição observada</td>
<td width="33%" valign="top"><img src="assets/diagrama-classes/cuidado.png" alt="Pacote Cuidado: PlanoCuidado e ItemPlanoCuidado, com o valor Desfecho"><br><b>Cuidado</b> · plano e itens do plano</td>
</tr>
<tr>
<td width="33%" valign="top"><img src="assets/diagrama-classes/identidade.png" alt="Pacote Identidade: Usuario"><br><b>Identidade</b> · usuário, perfil e vínculo</td>
<td width="33%" valign="top"><img src="assets/diagrama-classes/dotnet.png" alt="Catálogo do PetBuddies-API: Protocolo e RegraProtocolo"><br><b>Catálogo</b> · protocolo e regra, lidos do .NET por HTTP</td>
<td width="33%" valign="top"><img src="assets/diagrama-classes/comum.png" alt="Valor comum: Auditoria"><br><b>Auditoria</b> · embutida em 16 entidades, mostrada uma vez</td>
</tr>
</table>

---

## Autenticação e perfis

Duas cadeias de segurança no mesmo processo:

| Cadeia | Quem usa | Como autentica |
|---|---|---|
| `/api/**` | app mobile e integrações | `Authorization: Bearer <JWT>` (HS256) |
| painel Thymeleaf | navegador | formulário em `/login`, sessão e logout |

O token carrega o perfil (`VET` ou `TUTOR`) e o vínculo (`veterinarioId` ou `responsavelId`), e é o mesmo aceito pelo `PetBuddies-API` (.NET) — o segredo é compartilhado.

**Rotas por perfil na API:** a regra segue o que o app faz com cada perfil. Os dois perfis cadastram, editam e removem animal, editam o responsável, agendam e cancelam consulta. O veterinário é o único que abre consulta direta, fecha atendimento, registra atendimento e procedimento, prescreve, mantém receitas-modelo, cria regra e condição clínica, abre janela, instancia plano e cadastra veterinário e clínica. O check-in é só do responsável (`TUTOR`). Todo `GET` aceita qualquer token válido. Perfil sem permissão recebe `403`, nunca `401`, e a remoção de animal ainda confere o dono.

**Rotas por perfil na web:** todo o painel exige `VET`. O responsável que entra pelo navegador cai em `/acesso-app`, que o orienta a usar o app, e recebe `403` nas rotas da clínica.

---

## Fluxos Principais

### Plano de cuidado a partir de protocolo

O único fluxo que atravessa os dois serviços.

<img src="assets/figuras/fluxo-plano-de-cuidado.png" alt="Fluxo do plano de cuidado em cinco passos: pedido da veterinária, checagem de plano ativo, leitura do catálogo no .NET, gravação do plano e resposta 201">

### Check-in narrado

A IA interpreta; **o motor determinístico decide a dose**.

<img src="assets/figuras/fluxo-checkin.png" alt="Fluxo do check-in em seis passos: relato do responsável, extração pelo Gemini, confirmação, gravação do check-in, motor de regras e desfecho">

Uma regra de comportamento que a integração precisa conhecer: **cada condição vem com `confianca` entre 0 e 1.** Abaixo de `0.7` a leitura é uma inferência do modelo, não algo que o responsável disse — o app deve confirmar antes de seguir.

Quando o relato traz uma condição marcada como **sinal de alarme**, o desfecho é acionar a clínica e o plano de tratamento fica `SUSPENSO` até a veterinária avaliar. O relato aparece no início do painel, em "Para ler hoje".

---

## Painel da clínica (Thymeleaf)

O painel é a ferramenta da veterinária: telas server-rendered, com layout compartilhado por fragmentos e menu em três grupos. O responsável usa o app mobile.

| Rota | O que faz |
|---|---|
| `/login` | formulário de acesso. A veterinária vai para o início; o responsável, para `/acesso-app` |
| `/painel` | início: consultas do dia, relatos para ler (com sinal de alarme e sem regra), check-ins e cuidados atrasados |
| `/agenda` | grade semanal com consultas e horários livres, agendamento |
| `/agenda/{id}/fechar` | fechamento do atendimento em quatro etapas: registro, procedimento, prescrição e revisão |
| `/horarios` | modelo semanal de atendimento, com prévia antes de abrir as janelas |
| `/pacientes` | lista com filtro por plano, cadastro e ficha em linha do tempo, com os planos ativos |
| `/condicoes` | catálogo de condições clínicas usadas no check-in e na receita, com sinal de alarme |
| `/receitas-modelo` | receitas prontas da clínica, com dose, duração e regras |
| `/modelos-plano` | modelos de plano preventivo lidos do back-office (.NET), só leitura |
| `/equipe` | veterinários: cadastro, edição, desativação e reativação |
| `/responsaveis` | responsáveis e acesso ao app |
| `/clinica` | dados da clínica |

Erro de negócio numa tela devolve página HTML, não JSON — o `WebExceptionHandler` intercepta antes do handler da API.

### Telas

Capturadas do deploy em produção, com a veterinária de demonstração.

<table>
<tr>
<td width="33%" valign="top"><img src="assets/telas/01-login.png" alt="Tela de login"><br><b>Login</b> · e-mail e senha</td>
<td width="33%" valign="top"><img src="assets/telas/02-inicio.png" alt="Início do painel com os indicadores do dia"><br><b>Início</b> · consultas, relatos e cuidados atrasados</td>
<td width="33%" valign="top"><img src="assets/telas/03-agenda.png" alt="Agenda em grade semanal"><br><b>Agenda</b> · grade da semana com horários livres</td>
</tr>
<tr>
<td valign="top"><img src="assets/telas/04-horarios.png" alt="Horários de atendimento por dia da semana"><br><b>Horários</b> · faixas de atendimento por dia</td>
<td valign="top"><img src="assets/telas/05-pacientes.png" alt="Lista de pacientes com plano e próximo cuidado"><br><b>Pacientes</b> · plano e próximo cuidado</td>
<td valign="top"><img src="assets/telas/06-ficha.png" alt="Ficha do paciente com os planos ativos"><br><b>Ficha</b> · planos de tratamento e preventivo</td>
</tr>
<tr>
<td valign="top"><img src="assets/telas/07-fechar-atendimento.png" alt="Fechamento de atendimento na etapa de registro"><br><b>Fechar atendimento</b> · quatro etapas</td>
<td valign="top"><img src="assets/telas/08-condicoes.png" alt="Catálogo de condições clínicas"><br><b>Condições</b> · com sinal de alarme</td>
<td valign="top"><img src="assets/telas/09-receitas-modelo.png" alt="Receitas-modelo da clínica"><br><b>Receitas-modelo</b> · dose, duração e regras</td>
</tr>
<tr>
<td valign="top"><img src="assets/telas/10-modelos-plano.png" alt="Modelos de plano preventivo por espécie"><br><b>Modelos de plano</b> · lidos do back-office</td>
<td valign="top"><img src="assets/telas/11-responsaveis.png" alt="Lista de responsáveis com acesso ao app"><br><b>Responsáveis</b> · contato, pets e acesso ao app</td>
<td valign="top"><img src="assets/telas/12-equipe.png" alt="Equipe da clínica"><br><b>Equipe</b> · veterinários da clínica</td>
</tr>
</table>

---

## Recursos e Rotas

Respostas de recurso vêm em envelope HATEOAS (`EntityModel` / `CollectionModel`). Erros vêm como `ErrorDto{ code, message }`. Parâmetros, corpos e códigos de cada rota estão no Swagger.

### Saúde

| Método | Rota |
|---|---|
| `GET` | `/actuator/health` |

Aberta, sem token. Responde `200 {"status":"UP"}` com o banco no ar e `503 {"status":"DOWN"}` com o banco fora — sem detalhe dos componentes. É a rota que o health check do `PetBuddies-API` consulta para saber se este serviço está de pé.

### Autenticação

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/auth/login` | login dos dois perfis, devolve o token |
| `POST` | `/api/auth/registro` | cadastra responsável ou veterinário e já devolve o token |
| `POST` | `/api/auth/senha` | troca a senha do usuário do token |

### Cadastro

| Recurso | Rota base | Métodos |
|---|---|---|
| Clínicas | `/api/clinica` | `GET`, `GET /buscar?cnpj=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Veterinários | `/api/veterinario` | `GET (?clinicaId=)`, `GET /buscar?crmv=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Responsáveis | `/api/responsavel` | `GET (?nome=)`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Animais | `/api/animal` | `GET (?responsavelId=&nome=)`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |

### Atendimento

| Recurso | Rota base | Métodos |
|---|---|---|
| Consultas | `/api/consulta` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`, `POST /agendamento`, `POST /{id}/cancelamento`, `POST /{id}/fechamento` |
| Janelas de atendimento | `/api/janela-atendimento` | `GET (?veterinarioId=)`, `GET /livres?veterinarioId=&data=` (a partir de agora), `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Registros de atendimento | `/api/registro-atendimento` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Procedimentos | `/api/procedimento` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |
| Condições clínicas | `/api/condicao-clinica` | `GET (?clinicaId=)`, `GET /buscar?clinicaId=&codigo=`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` |

### Prescrição

| Recurso | Rota | Métodos |
|---|---|---|
| Prescrições | `/api/prescricao` | `GET`, `GET /{id}`, `POST` — o `POST` recebe `{"prescricoes": [ … ]}` e grava as N do mesmo atendimento numa transação. Sem `PUT`/`DELETE`, é ato imutável |
| Rascunho por IA | `/api/prescricao/rascunho` | `POST` — interpreta a narrativa da vet e devolve prescrição + regras propostas, sem gravar. Exige `VET` |
| Regras de prescrição | `/api/regra-prescricao` | `GET`, `GET /{id}`, `POST` — sem `PUT`/`DELETE` |
| Receitas-modelo | `/api/receita-modelo` | `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}` — receitas prontas da clínica, com regras. Exige `VET` |

### Motor de planos

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/motor/plano/instanciar-preventivo` | cria o plano preventivo do animal, ou devolve o existente |
| `POST` | `/api/motor/plano/instanciar-pos-cirurgico` | idem, para o plano vinculado a uma consulta |
| `GET` | `/api/motor/plano/{animalId}` | plano `ATIVO` do animal, com os itens pendentes |
| `GET` | `/api/motor/plano/{animalId}/eventos` | itens do plano, paginado (`?page=&size=`) |
| `GET` | `/api/motor/plano/{animalId}/protocolo-aplicado` | itens separados em realizados, pendentes e vencidos |
| `GET` | `/api/motor/plano/{animalId}/sugestoes` | próximo cuidado sugerido pelo histórico do animal |

### Check-in

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/checkin/extracao` | passo 1 — interpreta a narrativa; não grava |
| `POST` | `/api/checkin` | passo 2 — grava o confirmado, avalia a regra e grava o desfecho |
| `GET` | `/api/checkin/{id}` | busca por id |
| `GET` | `/api/checkin?animalId=` | check-ins do animal, do mais recente ao mais antigo |

### Roteiro do Fluxo Principal

Na ordem abaixo, com o usuário `VET` de demonstração:

| Passo | Método | Rota | O que observar |
|---|---|---|---|
| 1 | `POST` | `/api/auth/login` | `200` + token; use como Bearer nos passos seguintes |
| 2 | `POST` | `/api/animal` | `201` — paciente para o responsável do seed (`responsavelId: 1`) |
| 3 | `POST` | `/api/condicao-clinica` | `201` — o vocabulário que o check-in vai avaliar |
| 4 | `POST` | `/api/janela-atendimento` | `201` — um slot livre para o veterinário do seed |
| 5 | `POST` | `/api/consulta/agendamento` | `201` — ocupa a janela; a consulta nasce `AGENDADA` |
| 6 | `POST` | `/api/consulta/{id}/fechamento` | `201` — registro, procedimentos e prescrições numa transação; a consulta vira `REALIZADA` |
| 7 | `POST` | `/api/motor/plano/instanciar-preventivo` | `201` — lê o catálogo do .NET e materializa o plano |
| 8 | `POST` | `/api/checkin/extracao` | `200` — a IA interpreta a narrativa contra o vocabulário |
| 9 | `POST` | `/api/checkin` | `201` — o motor decide a dose ou escala à clínica |

> O passo 8 só reconhece condições de uma prescrição ativa. Sem o passo 6, a extração devolve lista vazia — é o escopo do vocabulário funcionando.

### Validações — respostas de erro

| Situação | Exemplo | Status |
|---|---|---|
| Campo obrigatório ausente ou fora de faixa | `POST /api/animal` sem `nome` | `400` |
| Parâmetro de query obrigatório ausente | `GET /api/checkin` sem `animalId` | `400` |
| JSON malformado ou enum inválido | `"especie": "INVALIDO"` | `400` |
| Faixa de dose invertida | `POST /api/prescricao` com `doseMin > doseMax` | `400` |
| Credenciais inválidas | senha errada | `401` — mesma mensagem para login inexistente e usuário inativo |
| Perfil sem permissão | `TUTOR` chamando `POST /api/prescricao/rascunho` | `403` |
| Animal de outro responsável | `TUTOR` chamando `DELETE /api/animal/{id}` num animal que não é dele | `403` |
| Recurso inexistente | `GET /api/animal/999999` | `404` |
| CNPJ, CRMV ou código duplicado | CNPJ repetido | `409` |
| Janela ocupada ou consulta já realizada | agendar em janela ocupada | `409` |
| Check-in duplicado | mesmo animal, data e item | `409` |

---

## Como Testar

### Via Swagger UI

Em produção, `https://petbuddies-java-rm563925.azurewebsites.net/swagger-ui.html`; local, `http://localhost:8080/swagger-ui.html`. Endpoints por tag de domínio, com "Authorize" e Try it out. O JSON do OpenAPI fica em **`/api-docs`**, não em `/v3/api-docs`.

### Via Postman

Importe `docs/postman/petbuddies-ai-java.postman_collection.json`, com `baseUrl` em `http://localhost:8080` ou no endereço de produção.

1. Rode **01 · Autenticação → Entrar como veterinária** e **Entrar como tutora** (a responsável de demonstração). Cada login guarda o próprio token: `tokenVet` e `tokenTutor`.
2. As pastas estão numeradas na ordem de uso: clínica e equipe, janelas, responsáveis e animais, consultas e fechamento, prescrição, plano de cuidado e check-in.
3. As pastas herdam `tokenVet`, e a pasta **14 · Check-in** usa `tokenTutor`, porque o check-in é só do responsável. Todo `GET` aceita qualquer um dos dois tokens.
4. **15 · Saúde** chama `GET /actuator/health`, sem token.

A coleção cobre as rotas da API, exceto as de receita-modelo, inclusive agendamento, cancelamento e fechamento de consulta, janelas livres, rascunho de prescrição, protocolo aplicado, sugestões e check-in.

---

## Exemplos de Payload

#### `POST /api/auth/login`
```json
{ "login": "ana@clinica.com", "senha": "petbuddies123" }
```

#### `POST /api/animal`
```json
{ "nome": "Rex", "especie": "CACHORRO", "raca": "Vira-lata", "porte": "MEDIO", "sexo": "MACHO",
  "dataNascimento": "2021-03-15", "peso": 18.5, "castrado": true, "responsavelId": 1 }
```

#### `POST /api/motor/plano/instanciar-preventivo`
```json
{ "animalId": 1, "especie": "CACHORRO", "dataNascimento": "2021-03-15" }
```

#### `POST /api/checkin/extracao` — passo 1, interpreta e não grava
```json
{ "animalId": 1, "narrativa": "Ele comeu bem hoje, mas as fezes estavam mais moles que o normal." }
```

A resposta traz `condicoes[]` com `confianca` por item, e `degradado: true` se o modelo falhou.

#### `POST /api/checkin` — passo 2, grava o confirmado
```json
{
  "animalId": 1,
  "narrativa": "Ele comeu bem hoje, mas as fezes estavam mais moles que o normal.",
  "condicoes": [
    { "condicaoClinicaId": 3, "valorBooleano": true, "confianca": 0.92 }
  ]
}
```

#### `POST /api/consulta/{id}/fechamento`
```json
{
  "registroAtendimento": {
    "dataAtendimento": "2026-09-10T14:30:00",
    "anamnese": "Responsável relata apetite normal.",
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
