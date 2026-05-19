# petbuddies-ai

Bot WhatsApp + Motor de cuidado contínuo para pets — FIAP Challenge 2026.

Recebe mensagens via Evolution API, processa com Spring AI + Gemini 2.5 Flash e responde ao tutor pelo WhatsApp. Memória de conversa persistida no Oracle FIAP via JDBC.

---

## Stack

- Java 21 / Spring Boot 3.4.5 / Spring AI 1.1.6
- Gemini 2.5 Flash via endpoint OpenAI-compatible
- Oracle FIAP — entidades JPA + memória de conversa (`SPRING_AI_CHAT_MEMORY`)
- Evolution API — gateway WhatsApp
- Springdoc OpenAPI 2.8.8 (Swagger UI)

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Docker (opcional, para rodar em container)
- Conta Oracle FIAP com credenciais
- Chave Gemini API (`GEMINI_API_KEY`)
- Evolution API rodando e configurada

---

## Variáveis de ambiente

Copie `.env.example` para `.env` e preencha:

```env
GEMINI_API_KEY=          # chave da Google AI Studio
ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521/ORCL
ORACLE_USER=             # usuário Oracle FIAP
ORACLE_PASSWORD=         # senha Oracle FIAP
VET_API_URL=http://localhost:5000   # URL do petbuddies-net
EVOLUTION_API_URL=http://localhost:8081
EVOLUTION_API_KEY=       # chave da instância Evolution
EVOLUTION_API_INSTANCE=petbuddies
```

---

## Como rodar localmente

```bash
cp .env.example .env
# preencher .env com credenciais reais

mvn spring-boot:run
```

Acesse:
- Swagger UI: http://localhost:8080/swagger-ui.html
- Simular mensagem: `POST http://localhost:8080/simulate-message`

### Simular mensagem sem WhatsApp

```bash
curl -X POST http://localhost:8080/simulate-message \
  -H "Content-Type: application/json" \
  -d '{"telefone":"5511999999999","texto":"Oi, o que você faz?"}'
```

---

## Como rodar com Docker

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

## Estrutura de packages

```
br.com.fiap.petbuddies/
  client/        — PetNetApiClient (integração .NET; stubs até PRD 04)
  config/        — ChatClientConfig, PetNetApiClientConfig, OpenApiConfig
  controller/    — WebhookController, SimulationController,
                   MotorPlanoController, MotorScoreController
  domain/
    entity/      — 6 entidades JPA do motor core
    enums/       — 9 enums
    repository/  — 6 repositórios Spring Data
  dto/
    motor/
      request/   — InstanciarPlanoRequest, InstanciarPosCirurgicoRequest,
                   RecalcularScoreRequest
      response/  — PlanoResult, PlanoDetalheDto, EventoPlanoResumoDto,
                   ScoreResult, ScoreResumoDto, FatorRiscoDto
    response/    — ResponsavelDto, AnimalMotorDto, UltimaConsultaDto
  exception/     — PetNetApiNotFoundException, PetNetApiUnavailableException,
                   PlanoNaoEncontradoException
  handler/       — GlobalExceptionHandler (@RestControllerAdvice)
  service/       — ChatService, EvolutionService,
                   ProtocoloMatchService, MotorPlanoService, MotorScoreService
```

---

## Endpoints

### Bot

| Método | Path | Descrição |
|--------|------|-----------|
| POST | `/webhook/whatsapp` | Recebe eventos da Evolution API |
| POST | `/simulate-message` | Simula mensagem WhatsApp (debug) |

### Motor — Planos

| Método | Path | Status | Descrição |
|--------|------|--------|-----------|
| POST | `/api/motor/planos/instanciar` | 201 / 200 | Instancia plano preventivo (idempotente) |
| POST | `/api/motor/planos/pos-cirurgico` | 201 / 200 | Instancia plano pós-cirúrgico (idempotente por consultaId) |
| GET | `/api/motor/planos/{petNetApiAnimalId}` | 200 / 404 | Retorna plano preventivo ativo + eventos |
| GET | `/api/motor/planos/{petNetApiAnimalId}/eventos` | 200 | Lista eventos paginada (`?page=0&size=10`) |

### Motor — Scores

| Método | Path | Status | Descrição |
|--------|------|--------|-----------|
| POST | `/api/motor/scores/recalcular` | 200 | Recalcula score de risco (persiste histórico) |
| GET | `/api/motor/scores/{petNetApiAnimalId}` | 200 / 404 | Score mais recente do animal |
| GET | `/api/motor/scores/{petNetApiAnimalId}/historico` | 200 | Histórico de scores paginado |

Swagger com dois grupos: **bot** (`/webhook/**`, `/simulate-message`) e **motor** (`/api/motor/**`).

---

## Seed de protocolos

Antes de testar os endpoints do motor, executar no Oracle FIAP:

```bash
# conectar ao Oracle e rodar:
sql/seed-protocolos-exemplo.sql
```

Sem o seed, `POST /instanciar` retorna `{ "criado": false, "motivo": "SEM_PROTOCOLO_COMPATIVEL" }` — comportamento esperado.
