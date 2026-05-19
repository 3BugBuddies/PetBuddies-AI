# petbuddies-ai — Challenge FIAP 2026 | Java Advanced

Bot WhatsApp + Motor de cuidado contínuo para pets desenvolvido com Spring Boot e Spring AI, como parte do Challenge da disciplina de **Java Advanced (2TDS)** — FIAP 2026.

O serviço recebe mensagens via Evolution API, classifica a intenção do tutor (cadastro, agendamento, triagem, consulta ao plano), executa tool calling com Gemini 2.5 Flash e responde pelo WhatsApp. O motor de personalização mantém planos preventivos, eventos e scores de risco por animal.

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

### Dependências

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring Web | WEB | API REST + Webhook |
| Spring Data JPA | SQL | 6 entidades motor + memória de conversa |
| Oracle Driver | SQL | Oracle FIAP |
| Spring AI OpenAI | AI | Gemini via endpoint OpenAI-compatible |
| Spring AI JDBC Memory | AI | Memória de conversa persistida |
| Bean Validation | I/O | Validação de DTOs |
| Springdoc OpenAPI | Dev | Swagger UI — grupos `bot` e `motor` |

---

## Estrutura de Packages

```
br.com.fiap.petbuddies/
  client/        — PetNetApiClient (6 métodos HTTP reais para a API .NET)
  config/        — ChatClientConfig, PetNetApiClientConfig, OpenApiConfig
  controller/    — WebhookController (/webhook/whatsapp),
                   SimulationController (/simulate-message),
                   MotorPlanoController, MotorScoreController
  domain/
    entity/      — 6 entidades JPA do motor core
    enums/       — 10 enums (Especie, Porte, Sexo, StatusPlano, Intencao, ...)
    repository/  — 6 repositórios Spring Data com buscas customizadas (@Query)
  dto/
    bot/         — IntentResult, ConversationContext,
                   SimulateMessageRequest, SimulateMessageResponse
    client/      — ResponsavelDto, AnimalDto, AnimalMotorDto, UltimaConsultaDto,
                   CadastrarResponsavelRequest, CadastrarAnimalRequest
    motor/       — PlanoPreventivoRequest, PlanoPosCirurgicoRequest,
                   RecalcularScoreRequest, PlanoResponse, ScoreResponse,
                   EventoPlanoDto, FatorRiscoDto
    ErrorDto     — raiz, usado por todos os handlers
  exception/     — PetNetApiNotFoundException, PetNetApiUnavailableException,
                   PetNetApiConflictException, PlanoNaoEncontradoException
  handler/       — GlobalExceptionHandler (@RestControllerAdvice)
  service/       — ChatService, ClassificadorService, PromptFactory, ToolsFactory,
                   EvolutionService, ProtocoloMatchService, MotorPlanoService, MotorScoreService
  tools/         — CadastroTools, AgendamentoTools, PlanoTools, TriagemTools
```

---

## Entidades JPA (Motor Core)

| Entidade | Tabela | Descrição |
|----------|--------|-----------|
| `ProtocoloEntity` | `T_PB_PROTOCOLO` | Protocolo de cuidado por espécie/porte/sexo/idade |
| `EventoProtocoloEntity` | `T_PB_EVENTO_PROTOCOLO` | Eventos template do protocolo (vacinas, consultas, etc.) |
| `PlanoCuidadoAnimalEntity` | `T_PB_PLANO_CUIDADO_ANIMAL` | Plano instanciado por animal (preventivo ou pós-cirúrgico) |
| `EventoPlanoEntity` | `T_PB_EVENTO_PLANO` | Evento concreto do plano (com data alvo e status) |
| `ScoreRiscoAnimalEntity` | `T_PB_SCORE_RISCO_ANIMAL` | Score de risco calculado por animal |
| `FatorRiscoEntity` | `T_PB_FATOR_RISCO` | Fatores individuais que compõem o score |

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Oracle FIAP (VPN ou rede local) — entidades JPA + memória de conversa
- Chave Gemini API (`GEMINI_API_KEY`)
- Evolution API rodando (Docker)
- `petbuddies-api` (.NET) rodando em `http://localhost:5297`

---

## Variáveis de Ambiente

Copie `.env.example` para `.env` e preencha:

```env
GEMINI_API_KEY=          # chave da Google AI Studio
ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521/ORCL
ORACLE_USER=             # usuário Oracle FIAP (RM)
ORACLE_PASSWORD=         # senha Oracle FIAP
EVOLUTION_API_URL=http://localhost:8081
EVOLUTION_API_KEY=       # chave da instância Evolution
EVOLUTION_API_INSTANCE=petbuddies
```

O profile `dev` aponta para `petbuddies-api` em `http://localhost:5297`.
O profile `docker` usa `http://petbuddies-net:5000`.

---

## Como Executar

### Localmente

```bash
cp .env.example .env
# preencher .env com credenciais reais

mvn spring-boot:run
```

Acesse:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Simular mensagem:** `POST http://localhost:8080/simulate-message`

### Simular mensagem sem WhatsApp

```bash
curl -X POST http://localhost:8080/simulate-message \
  -H "Content-Type: application/json" \
  -d '{"telefone":"5511999999999","texto":"Quero cadastrar meu pet"}'
```

### Com Docker

```bash
mvn package -DskipTests
docker build -t petbuddies-ai .
docker run --rm \
  -e GEMINI_API_KEY=sua_chave \
  -e ORACLE_USER=seu_usuario \
  -e ORACLE_PASSWORD=sua_senha \
  -e SPRING_PROFILES_ACTIVE=docker \
  -p 8080:8080 \
  petbuddies-ai
```

---

## Seed de Protocolos

Antes de testar os endpoints do motor, executar no Oracle FIAP:

```bash
# conectar ao Oracle e executar:
sql/seed-protocolos-exemplo.sql
```

Sem o seed, `POST /api/motor/planos/instanciar` retorna `{ "criado": false, "motivo": "SEM_PROTOCOLO_COMPATIVEL" }` — comportamento esperado.

---

## Endpoints

### Bot

| Método | Path | Status | Descrição |
|--------|------|--------|-----------|
| `POST` | `/webhook/whatsapp` | 200 | Recebe eventos Evolution API (sempre 200 — evita retry) |
| `POST` | `/simulate-message` | 200 | Simula mensagem WhatsApp para debug |

### Motor — Planos

| Método | Path | Status | Descrição |
|--------|------|--------|-----------|
| `POST` | `/api/motor/planos/instanciar` | 201 / 200 | Instancia plano preventivo (idempotente por animalId) |
| `POST` | `/api/motor/planos/pos-cirurgico` | 201 / 200 | Instancia plano pós-cirúrgico (idempotente por consultaId) |
| `GET` | `/api/motor/planos/{petNetApiAnimalId}` | 200 / 404 | Retorna plano ativo + eventos |
| `GET` | `/api/motor/planos/{petNetApiAnimalId}/eventos` | 200 | Lista eventos paginada (`?page=0&size=10`) |

### Motor — Scores

| Método | Path | Status | Descrição |
|--------|------|--------|-----------|
| `POST` | `/api/motor/scores/recalcular` | 200 | Recalcula e persiste score de risco |
| `GET` | `/api/motor/scores/{petNetApiAnimalId}` | 200 / 404 | Score mais recente |
| `GET` | `/api/motor/scores/{petNetApiAnimalId}/historico` | 200 | Histórico paginado |

### Exemplos de Request/Response

**POST `/api/motor/planos/instanciar`:**
```json
{ "petNetApiAnimalId": 1 }
```

**Response 201:**
```json
{
  "id": 1,
  "petNetApiAnimalId": 1,
  "protocoloNome": "Preventivo Cão Adulto Grande",
  "categoria": "PREVENTIVO",
  "status": "ATIVO",
  "instanciadoEm": "2026-05-19T14:30:00",
  "scoreAtual": null,
  "criado": true,
  "eventos": [ ... ]
}
```

**POST `/api/motor/scores/recalcular`:**
```json
{ "petNetApiAnimalId": 1 }
```

**Response 200:**
```json
{
  "petNetApiAnimalId": 1,
  "score": 65,
  "classificacao": "MEDIO",
  "calculadoEm": "2026-05-19T14:31:00",
  "fatores": [
    { "tipo": "IDADE", "peso": 0.3, "valor": 70.0, "contribuicao": 21.0 },
    { "tipo": "CASTRADO", "peso": 0.2, "valor": 50.0, "contribuicao": 10.0 }
  ]
}
```

---

## Arquitetura Conversacional

```
Mensagem WhatsApp
      │
      ▼
ClassificadorService     ← Gemini sem memória, retorna Intencao + confiança
      │
      ▼
PromptFactory            ← monta system prompt (BASE + complemento por intenção + contexto)
      │
      ▼
ToolsFactory             ← seleciona tools por intenção
      │
      ▼
ChatClient (Spring AI)   ← Gemini com memória JDBC + tools selecionadas
      │
      ▼
Tools (CadastroTools, AgendamentoTools, PlanoTools, TriagemTools)
      │
      ▼
PetNetApiClient          ← chamadas HTTP reais para petbuddies-api (.NET)
```

**Intenções suportadas:** `CADASTRO`, `AGENDAMENTO`, `CONSULTA_PLANO`, `TRIAGEM`, `GERAL`

---

## Diagramas

- **DER Motor Core:** [`docs/diagramas/der-motor-core.md`](docs/diagramas/der-motor-core.md)
- **Diagrama de Classes:** [`docs/diagramas/classes-motor-core.md`](docs/diagramas/classes-motor-core.md)

---

## Testes — Postman

Coleção disponível em:

```
postman/petbuddies-ai.postman_collection.json
```

Importar no Postman e configurar `base_url` para `http://localhost:8080`.

---

## Tecnologias Utilizadas

- **Java 21** / Spring Boot 3.4.5
- **Spring AI 1.1.6** — tool calling + memória JDBC
- **Gemini 2.5 Flash** via endpoint OpenAI-compatible
- **Spring Data JPA + Hibernate** — 6 entidades Oracle
- **Oracle Database** (FIAP)
- **Springdoc OpenAPI 2.8.8** (Swagger UI)
- **Bean Validation** (Jakarta)
- **Evolution API** — gateway WhatsApp
