# PetBuddies — Sprint 4 | DevOps Tools & Cloud Computing

API REST e painel web do **PetBuddies**, publicados na Azure por pipelines CI/CD do Azure DevOps.
A aplicação Java roda num Azure Web App Linux (Java 21) e grava no **Oracle da FIAP**. O build, os testes e o deploy
são feitos pelo [`azure-pipelines.yml`](azure-pipelines.yml), que dispara a cada push na `main`.

---

## Links

| Recurso | URL |
|---------|-----|
| Repositório GitHub | https://github.com/3BugBuddies/PetBuddies-AI |
| Azure DevOps (Boards, Repos, Pipelines) | [PetBuddies - Sprint 4 – Azure DevOps](https://dev.azure.com/RM565834/PetBuddies%20-%20Sprint%204%20%E2%80%93%20Azure%20DevOps) |
| Aplicação (Azure Web App) | https://petbuddies-java-rm563925.azurewebsites.net |
| Swagger UI | https://petbuddies-java-rm563925.azurewebsites.net/swagger-ui.html |
| Saúde | https://petbuddies-java-rm563925.azurewebsites.net/actuator/health |
| Vídeo | a preencher |
| Serviço .NET (PetBuddies-API) | https://github.com/3BugBuddies/PetBuddies-API |

---

## Integrantes

| Nome | RM | Turma |
|------|----|-------|
| Felipe Yuiti Ishii | 565339 | 2TDSR |
| Gabriel Nogueira Peixoto | 563925 | 2TDSR |
| Giovanna Neri dos Santos | 566154 | 2TDSR |
| Mariana Inoue | 565834 | 2TDSR |

---

## Índice

1. [Descrição da solução](#1-descrição-da-solução)
2. [Arquitetura e fluxo CI/CD](#2-arquitetura-e-fluxo-cicd)
3. [Banco de dados em nuvem](#3-banco-de-dados-em-nuvem)
4. [Projeto no Azure DevOps](#4-projeto-no-azure-devops)
5. [Acesso do professor](#5-acesso-do-professor)
6. [Pipelines CI/CD](#6-pipelines-cicd)
7. [Recursos na Azure](#7-recursos-na-azure)
8. [How To · deploy na Azure](#8-how-to--deploy-na-azure)
9. [Testes](#9-testes)
10. [CRUD do Responsável](#10-crud-do-responsável)
11. [Evidência do CRUD no banco](#11-evidência-do-crud-no-banco)
12. [Credenciais de teste](#12-credenciais-de-teste)

---

## 1. Descrição da solução

O PetBuddies cuida do pet entre uma consulta e outra. A veterinária atende, prescreve e monta o plano de cuidado pelo
painel da clínica. Em casa, o responsável conta pelo app como o animal está, e o serviço transforma esse relato na
conduta que a veterinária deixou assinada.

- **Painel da clínica:** cadastro de responsáveis, pacientes e equipe, agenda, atendimento e prescrição.
- **Plano de cuidado:** vacinas, vermífugos e retornos gerados a partir dos protocolos da clínica.
- **Check-in do responsável:** a IA lê o relato e um motor de regras decide a dose do dia ou aciona a clínica.
- **Pontuação do responsável:** calculada pelo serviço .NET a cada consulta realizada.

### Stack

| Camada | Tecnologia |
|--------|------------|
| Aplicação (este repositório) | Java 21, Spring Boot 3.4.5, Spring Data JPA, Spring Security com JWT, Thymeleaf, Flyway |
| IA | Spring AI com Gemini 2.5 Flash (Java); Groq Whisper para a transcrição de voz (app mobile) |
| Serviço parceiro | `PetBuddies-API` em .NET 8, num segundo Web App |
| Banco | Oracle da FIAP (as duas APIs) e Cosmos DB com a API do MongoDB (pontuação do .NET) |
| Nuvem | Azure Web App Linux, App Service Plan B2, Application Insights, região brazilsouth |
| DevOps | Azure DevOps: Boards (Scrum), Azure Repos, Azure Pipelines em YAML, Pipeline Artifacts |
| Testes | JUnit 5 com Maven no Java; xUnit com `dotnet test` no .NET |
| Clientes | navegador (painel da clínica) e app mobile em React Native com Expo |

### Estrutura do repositório

```
PetBuddies-AI/
├── azure-pipelines.yml          pipeline CI/CD (estágios CI e CD)
├── infra/
│   └── infra-app.sh             cria os recursos do Java na Azure (rodar uma vez)
├── docs/
│   ├── arquitetura/             fonte do desenho de arquitetura
│   ├── banco/                   scripts Oracle (DDL, carga, pacotes)
│   └── postman/                 coleção do Postman
├── assets/figuras/              imagens do README
├── Dockerfile · docker-compose.yml   Oracle local e a aplicação em container
├── .env.example                 modelo das variáveis locais
├── pom.xml
└── src/
    ├── main/java/…/petbuddies/  controllers, services, entidades, repositórios, segurança
    ├── main/resources/          application.yml, migrations do Flyway e telas Thymeleaf
    └── test/java/…/petbuddies/  testes unitários (JUnit)
```

---

## 2. Arquitetura e fluxo CI/CD

<img src="assets/figuras/arquitetura-devops.png" alt="Arquitetura e esteira CI/CD do PetBuddies: o desenvolvedor faz o commit no GitHub, a main vai para o Azure Repos, o Azure Pipelines roda o CI com build e testes, publica o artefato e o CD faz o Zip Deploy nos Web Apps Java e .NET; em produção, a veterinária usa o painel pelo navegador e o responsável usa o app mobile, os dois chegando ao Web App Java, que usa o Oracle FIAP, o Gemini e o Web App .NET">

<sub>Fonte do desenho: <a href="docs/arquitetura/arquitetura-devops.html"><code>docs/arquitetura/arquitetura-devops.html</code></a></sub>

**Esteira CI/CD, na ordem:**

| Passo | Ferramenta | O que acontece |
|:-----:|------------|----------------|
| 1 | GitHub | o desenvolvedor faz o commit e o PR entra na `main` |
| 2 | Azure Repos | a `main` é enviada aos repositórios `PetBuddies-AI` e `PetBuddies-API` do Azure DevOps |
| 3 | Azure Pipelines · CI | o push na `main` dispara o build e os testes automáticos |
| 4 | Pipeline Artifact | o CI publica o artefato `app` (o `.jar` no Java, o pacote publicado no .NET) |
| 5 | Azure Pipelines · CD | com o artefato novo, o CD grava as variáveis secretas como configuração do Web App |
| 6 | Azure Web App | o CD faz o Zip Deploy do artefato e a versão nova sobe |

**Em produção:**

- **Personas:** a **veterinária** usa o painel da clínica pelo navegador (sessão) e o **responsável** usa o app
  mobile (token JWT). Os dois chegam ao Web App Java por HTTPS.
- O Web App Java grava no **Oracle FIAP**, chama o **Gemini** pelo Spring AI e consulta o Web App .NET por REST
  (catálogo de protocolos e pontuação).
- O Web App .NET usa o Oracle FIAP e o **Cosmos DB**, onde guarda a memória de cálculo da pontuação.
- O app mobile manda o áudio da fala ao **Groq** (Whisper), que devolve o texto.
- Cada Web App manda requisições, falhas e tempos ao próprio **Application Insights**.

---

## 3. Banco de dados em nuvem

O banco é o **Oracle da FIAP** (`oracle.fiap.com.br:1521/ORCL`), um banco gerenciado em nuvem. Cada
serviço tem o próprio schema.

- O schema do Java é criado e versionado pelo **Flyway** (`src/main/resources/db/migration`), na subida da aplicação.
  O Hibernate só valida (`ddl-auto=validate`).
- A conexão vem das variáveis `ORACLE_URL`, `ORACLE_USER` e `ORACLE_PASSWORD`, gravadas no Web App pelo CD. Usuário e
  senha são variáveis secretas da pipeline.

O Cosmos DB (API do MongoDB) é um armazenamento auxiliar do serviço .NET, só para a memória de cálculo da pontuação.

---

## 4. Projeto no Azure DevOps

| Campo | Valor |
|-------|-------|
| Project name | PetBuddies - Sprint 4 – Azure DevOps |
| Description | Projeto para entrega da Sprint 4 do professor João Menk<br>RM565339 - Felipe Yuiti Ishii - 2TDSR<br>RM563925 - Gabriel Nogueira Peixoto - 2TDSR<br>RM566154 - Giovanna Neri dos Santos - 2TDSR<br>RM565834 - Mariana Inoue - 2TDSR |
| Visibility | Private |
| Version control | Git |
| Work item process | Scrum |
| Repositórios | `PetBuddies-AI` (Java, este) e `PetBuddies-API` (.NET) |
| Pipelines | `petbuddies-java` e `petbuddies-net` |

Link: [PetBuddies - Sprint 4 – Azure DevOps](https://dev.azure.com/RM565834/PetBuddies%20-%20Sprint%204%20%E2%80%93%20Azure%20DevOps).

<img src="assets/prints/devops-projeto.png" alt="Página Summary do projeto PetBuddies - Sprint 4 – Azure DevOps, com a descrição do professor e dos integrantes e a lista de membros">

---

## 5. Acesso do professor

O professor João Menk é membro do projeto com o nível de acesso **Basic** (Organization settings → Users), o que dá
acesso a Boards, Repos e Pipelines. Os quatro integrantes também estão no projeto. A lista de membros aparece no print
da [seção 4](#4-projeto-no-azure-devops).

---

## 6. Pipelines CI/CD

Arquivo: [`azure-pipelines.yml`](azure-pipelines.yml), no modo YAML. Gatilho: push na `main`, o que inclui o merge de
PR. Service connection: `sc-petbuddies-azure` (Azure Resource Manager). A figura mostra a execução real do build 21,
de 04/11/2026, com o tempo de cada task.

<img src="assets/figuras/anatomia-pipeline.png" alt="Anatomia da pipeline petbuddies-java no build 21: o push na main dispara o stage CI (checkout, Java 21, Maven com 25 testes, cópia do jar e publicação do artefato app, 1 min 22 s); o stage CD só roda se o CI passou e baixa o artefato, grava as variáveis no Web App e faz o Zip Deploy (1 min 13 s); total de 3 min 13 s">

<sub>Fonte do desenho: <a href="docs/arquitetura/anatomia-pipeline.html"><code>docs/arquitetura/anatomia-pipeline.html</code></a> · tempos da timeline do build 21 na REST do Azure DevOps</sub>

A mesma execução no Azure DevOps, com os stages CI e CD verdes:

<img src="assets/prints/pipeline-build-21.png" alt="Execução do build 21 da pipeline petbuddies-java no Azure DevOps, com os stages CI - Build e Testes e CD - Deploy no Azure Web App concluídos com sucesso">

Os resultados dos testes estão na aba **Tests** da execução (ver a [seção 9](#9-testes)), e o artefato `app` está na aba
**Artifacts**.

A pipeline `petbuddies-net` do repositório `PetBuddies-API` segue o mesmo desenho: gatilho na `main`, CI com
`dotnet build`, `dotnet test` (xUnit) e artefato publicado, e CD no environment `petbuddies-net` com Zip Deploy no Web
App `petbuddies-net-rm563925`. As variáveis secretas dela são `ORACLE_USER`, `ORACLE_PASS`, `PETBUDDIES_JWT_SECRET` e
`MONGODB_CONNECTION_STRING`.

### Regras da pipeline

| Regra do briefing | Onde é atendida |
|-------------------|-----------------|
| I. Configurada e conectada ao repositório da aplicação | pipeline `petbuddies-java` lendo o `azure-pipelines.yml` do Azure Repos `PetBuddies-AI` |
| II. CI dispara a cada alteração na branch principal | `trigger` na `main` ([`azure-pipelines.yml:1`](azure-pipelines.yml#L1)) |
| III. CD dispara após novo artefato gerado | estágio CD com `dependsOn: CI` e `condition: succeeded()` ([`azure-pipelines.yml:59`](azure-pipelines.yml#L59)), baixando o artefato da execução |
| IV. Variáveis de ambiente protegidas | usuário, senha, chave e segredos são variáveis secretas da pipeline (tabela abaixo); nenhum valor fica no repositório |
| V. Artefato gerado e publicado no Azure DevOps | `PublishPipelineArtifact@1`, artefato `app` ([`azure-pipelines.yml:50`](azure-pipelines.yml#L50)) |
| VI. Execução de testes no CI | `Maven@4` com `clean package` e `publishJUnitResults: true` ([`azure-pipelines.yml:30`](azure-pipelines.yml#L30)) |
| VII. Deploy em Azure Web App | `AzureWebApp@1` com `deploymentMethod: zipDeploy` ([`azure-pipelines.yml:93`](azure-pipelines.yml#L93)) |

### Variáveis da pipeline

Cadastradas em Pipelines → Edit → Variables. O CD grava cada uma como configuração do Web App.

| Variável | Secreta | Conteúdo |
|----------|:-------:|----------|
| `NOME_WEBAPP` | não | nome do Web App de destino (`petbuddies-java-rm563925`) |
| `ORACLE_URL` | não | JDBC do Oracle da FIAP |
| `ORACLE_USER` | sim | usuário do Oracle |
| `ORACLE_PASSWORD` | sim | senha do Oracle |
| `GEMINI_API_KEY` | sim | chave da API do Gemini |
| `PETBUDDIES_JWT_SECRET` | sim | chave de assinatura do token JWT |
| `PETNETAPI_URL` | não | URL do Web App .NET |

O pool de conexões (`SPRING_DATASOURCE_HIKARI_*`), o fuso (`TZ`) e o tempo de subida
(`WEBSITES_CONTAINER_START_TIME_LIMIT`) são fixos no YAML e não são segredo.

---

## 7. Recursos na Azure

Script: [`infra/infra-app.sh`](infra/infra-app.sh), para o lado Java. O Web App do .NET e o Cosmos DB são criados pelo
repositório `PetBuddies-API` e usam o mesmo resource group e o mesmo plano.

<img src="assets/prints/portal-rg-petbuddies.png" alt="Resource group rg-petbuddies no Portal da Azure, com o plano, os dois Web Apps, os Application Insights e o Cosmos DB">

| Recurso | Nome | SKU | Região |
|---------|------|-----|--------|
| Resource Group | `rg-petbuddies` | — | brazilsouth |
| App Service Plan | `plan-petbuddies` | B2 Linux | brazilsouth |
| Web App (Java) | `petbuddies-java-rm563925` | `JAVA:21-java21` | brazilsouth |
| Application Insights (Java) | `ai-petbuddies-java` | web | brazilsouth |
| Web App (.NET) | `petbuddies-net-rm563925` | .NET 8 | brazilsouth |
| Application Insights (.NET) | `ai-petbuddies-net` | web | brazilsouth |
| Cosmos DB (API do MongoDB 4.2) | `cosmos-petbuddies-rm563925` | free tier | brazilsouth |
| Banco | Oracle da FIAP | — | fora da Azure |

O script:

- registra os providers `Microsoft.Web`, `Microsoft.Insights` e `Microsoft.OperationalInsights`;
- cria o resource group, o Application Insights, o plano Linux e o Web App Java 21, com o Always On ligado;
- grava no Web App só as app settings do Application Insights, que não são segredo;
- conecta o Web App ao Application Insights.

O script cria o plano em B1. A produção foi escalada para B2: no B1, o Java levava cerca de 6 minutos para subir.

Banco, Gemini, JWT e a URL do .NET não ficam no script: a pipeline grava esses valores a partir das variáveis secretas.

---

## 8. How To · deploy na Azure

Pré-requisitos: Azure CLI, acesso à assinatura e ao projeto do Azure DevOps.

**1. Entrar na assinatura certa**

```bash
az login
az account show
az account set --subscription "<nome-da-assinatura>"   # se precisar trocar
```

**2. Criar a infraestrutura (uma vez)**

```bash
bash infra/infra-app.sh
```

**3. Criar a service connection**

Azure DevOps → Project settings → Service connections → New service connection → Azure Resource Manager.
Escopo: assinatura. Nome: `sc-petbuddies-azure`.

**4. Criar a pipeline**

Pipelines → New pipeline → Azure Repos Git → repositório `PetBuddies-AI` → *Existing Azure Pipelines YAML file* →
`/azure-pipelines.yml`.

**5. Cadastrar as variáveis**

Edit → Variables. Cadastre `NOME_WEBAPP`, `ORACLE_URL` e `PETNETAPI_URL`. Cadastre como secretas ("Keep this value
secret"): `ORACLE_USER`, `ORACLE_PASSWORD`, `GEMINI_API_KEY` e `PETBUDDIES_JWT_SECRET`. A tabela está em
[Variáveis da pipeline](#variáveis-da-pipeline).

**6. Rodar a pipeline**

Faça um push na `main` ou clique em Run pipeline. O estágio CI compila e roda os testes (aba **Tests**) e publica o
artefato (aba **Artifacts**). O estágio CD publica no Web App. Na primeira execução, autorize o uso da service
connection e do environment `petbuddies-java`, se o Azure DevOps pedir.

**7. Acessar o app**

https://petbuddies-java-rm563925.azurewebsites.net

Na primeira subida, o Flyway cria as tabelas no Oracle e semeia os dados de demonstração. `GET /actuator/health`
responde `UP` quando o banco está no ar.

---

## 9. Testes

`mvn test` roda os testes unitários (JUnit 5 com Mockito, sem banco e sem rede), em `src/test/java/`. A pipeline roda
os mesmos testes no `clean package` e publica o resultado na aba **Tests**, com o título "Testes de Código". No build
21, passaram 25 de 25.

| Classe de teste | O que cobre | Testes |
|-----------------|-------------|:------:|
| `PontuacaoServiceTest` | envio da consulta ao .NET para pontuar (pendente, enviado, recusado, reenvio), saldo, extrato e memória de cálculo, com o .NET fora do ar ou recusando o token | 15 |
| `ExportacaoDatasetServiceTest` | exportação do dataset dos planos de cuidado pelo procedimento do Oracle, e os erros que sobem dele | 6 |
| `ResumoPeriodoTest` | resumo do cuidado entre duas consultas: doses com check-in, dias distintos e período vazio | 4 |
| **Total** | | **25** |

<img src="assets/prints/pipeline-testes.png" alt="Aba Tests do build 21 no Azure DevOps, com os 25 testes da execução Testes de Código aprovados">

---

## 10. CRUD do Responsável

Base URL: `https://petbuddies-java-rm563925.azurewebsites.net`. Autenticação: `POST /api/auth/login` devolve o token;
as demais rotas usam `Authorization: Bearer <token>`. O mesmo CRUD está no painel, na tela **Responsáveis**.

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/auth/login` | devolve o token JWT |
| `GET` | `/api/responsavel?nome=` | lista os responsáveis, com filtro opcional por nome |
| `GET` | `/api/responsavel/{id}` | busca por id |
| `POST` | `/api/responsavel` | cria (201) |
| `PUT` | `/api/responsavel/{id}` | atualiza (200) |
| `DELETE` | `/api/responsavel/{id}` | exclui (204) |

Corpo do login:

```json
{ "login": "ana@clinica.com", "senha": "petbuddies123" }
```

Corpo do `POST` e do `PUT`:

```json
{ "nome": "Marina Souza", "telefone": "11988887777", "email": "marina@email.com", "endereco": "Rua Augusta, 1500, São Paulo" }
```

A coleção do Postman com todas as rotas está em
[`docs/postman/petbuddies-ai-java.postman_collection.json`](docs/postman/petbuddies-ai-java.postman_collection.json).

---

## 11. Evidência do CRUD no banco

No vídeo, cada operação do [CRUD do Responsável](#10-crud-do-responsável) é conferida com um SELECT no Oracle da FIAP,
no schema do Java.

```sql
-- depois de inserir, atualizar ou excluir
SELECT ID_RESPONSAVEL, NM_NOME_RESPONSAVEL, TL_TELEFONE, EM_EMAIL, DS_ENDERECO, CA_CREATED_AT, AT_UPDATED_AT
FROM T_PB_RESPONSAVEL
ORDER BY ID_RESPONSAVEL DESC;

-- um responsável pelo id
SELECT * FROM T_PB_RESPONSAVEL WHERE ID_RESPONSAVEL = <id>;

-- migrations aplicadas pelo Flyway na subida
SELECT "version", "description", "success" FROM "flyway_schema_history" ORDER BY "installed_rank";
```

Depois do `DELETE`, o responsável some do primeiro SELECT.

---

## 12. Credenciais de teste

| Perfil | Login | Senha |
|--------|-------|-------|
| `VET` (veterinária) | `ana@clinica.com` | `petbuddies123` |
| `TUTOR` (responsável) | `maria@email.com` | `petbuddies123` |

Os dois usuários são criados pelo seed do Flyway (`V2__seed_demonstracao.sql`). As demais rotas da API estão no
[README da `main`](https://github.com/3BugBuddies/PetBuddies-AI/blob/main/README.md).
