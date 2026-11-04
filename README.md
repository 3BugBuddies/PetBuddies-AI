# PetBuddies — Sprint 4 | DevOps Tools & Cloud Computing

API REST e painel web do **PetBuddies**, publicados na Azure por pipelines CI/CD do Azure DevOps.
A aplicação Java roda num Azure Web App Linux (Java 21) e grava no **Oracle da FIAP**. O build, os testes e o deploy
são feitos pelo [`azure-pipelines.yml`](azure-pipelines.yml), que dispara a cada push na `main`.

<img src="assets/figuras/arquitetura-devops.png" alt="Arquitetura e esteira CI/CD do PetBuddies: o desenvolvedor faz o commit no GitHub, a main vai para o Azure Repos, o Azure Pipelines roda o CI com build e testes, publica o artefato e o CD faz o Zip Deploy nos Web Apps Java e .NET; em produção, a veterinária usa o painel pelo navegador e o responsável usa o app mobile, os dois chegando ao Web App Java, que usa o Oracle FIAP, o Gemini e o Web App .NET">

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
8. [Evidência do CRUD no banco](#8-evidência-do-crud-no-banco)

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
| IA | Spring AI com Gemini 2.5 Flash |
| Serviço parceiro | `PetBuddies-API` em .NET 8, num segundo Web App |
| Banco | Oracle da FIAP (as duas APIs) e Cosmos DB com a API do MongoDB (pontuação do .NET) |
| Nuvem | Azure Web App Linux, App Service Plan B2, Application Insights, região brazilsouth |
| DevOps | Azure DevOps: Boards (Scrum), Azure Repos, Azure Pipelines em YAML, Pipeline Artifacts |
| Testes | JUnit 5 com Maven no Java; xUnit com `dotnet test` no .NET |
| Clientes | navegador (painel da clínica) e app mobile em React Native com Expo |

---

## 2. Arquitetura e fluxo CI/CD

A figura do topo mostra a esteira e a produção. Fonte do desenho:
[`docs/arquitetura/arquitetura-devops.html`](docs/arquitetura/arquitetura-devops.html).

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
- Os dois Web Apps mandam requisições, falhas e tempos ao **Application Insights**.

---

## 3. Banco de dados em nuvem

O banco é o **Oracle da FIAP** (`oracle.fiap.com.br:1521/ORCL`), um dos bancos aceitos na lista do briefing. Cada
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
| Description | Projeto para entrega da Sprint 4 do professor João Menck, com os integrantes no formato `<RM> - <nome> - <turma>` |
| Visibility | Private |
| Version control | Git |
| Work item process | Scrum |
| Repositórios | `PetBuddies-AI` (Java, este) e `PetBuddies-API` (.NET) |
| Pipelines | `petbuddies-java` e `petbuddies-net` |

Link: [PetBuddies - Sprint 4 – Azure DevOps](https://dev.azure.com/RM565834/PetBuddies%20-%20Sprint%204%20%E2%80%93%20Azure%20DevOps).

---

## 5. Acesso do professor

O professor foi convidado para o projeto com o nível de acesso **Basic** (Organization settings → Users), o que dá
acesso a Boards, Repos e Pipelines.

---

## 6. Pipelines CI/CD

Arquivo: [`azure-pipelines.yml`](azure-pipelines.yml), no modo YAML. Agente `ubuntu-latest`.

- **Gatilho:** push na `main` (inclui o merge de PR).
- **Estágio CI · Build e Testes:** Java 21 (`JavaToolInstaller@0`) → `Maven@4` com `clean package`, que compila e roda
  os testes JUnit → resultados publicados (`**/surefire-reports/TEST-*.xml`, título "Testes de Código") → cópia do
  `.jar` → artefato `app` (`PublishPipelineArtifact@1`).
- **Estágio CD · Deploy no Azure Web App:** só roda se o CI passou. Job `deployment` no environment
  `petbuddies-java` → baixa o artefato → `AzureAppServiceSettings@1` grava as variáveis no Web App →
  `AzureWebApp@1` publica o `.jar` por Zip Deploy.
- **Service connection:** `sc-petbuddies-azure` (Azure Resource Manager).

Os resultados dos testes aparecem na aba **Tests** da execução, e o artefato `app` na aba **Artifacts**.

A pipeline `petbuddies-net` do repositório `PetBuddies-API` segue o mesmo desenho: gatilho na `main`, CI com
`dotnet build`, `dotnet test` e artefato publicado, e CD no environment `petbuddies-net` com Zip Deploy no Web App
`petbuddies-net-rm563925`.

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

| Recurso | Nome | SKU | Região |
|---------|------|-----|--------|
| Resource Group | `rg-petbuddies` | — | brazilsouth |
| App Service Plan | `plan-petbuddies` | B2 Linux | brazilsouth |
| Web App (Java) | `petbuddies-java-rm563925` | Java 21 | brazilsouth |
| Web App (.NET) | `petbuddies-net-rm563925` | .NET 8 | brazilsouth |
| Application Insights | um por Web App | — | brazilsouth |
| Cosmos DB (API do MongoDB) | `cosmos-petbuddies-rm563925` | free tier | — |
| Banco | Oracle da FIAP | — | fora da Azure |

---

## 8. Evidência do CRUD no banco

O CRUD do vídeo é o do **Responsável**: pelo painel (tela **Responsáveis**) ou pela API em `/api/responsavel`
(`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`). Cada operação é conferida com um SELECT no Oracle da FIAP,
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

Corpo do `POST /api/responsavel`:

```json
{ "nome": "Marina Souza", "telefone": "11988887777", "email": "marina@email.com", "endereco": "Rua Augusta, 1500, São Paulo" }
```

A API exige o token do login (`POST /api/auth/login`). Os usuários de demonstração e as demais rotas estão no
[README da `main`](https://github.com/3BugBuddies/PetBuddies-AI/blob/main/README.md).
