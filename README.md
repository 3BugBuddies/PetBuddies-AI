# PetBuddies — Componente de IA | Disruptive Architectures: IoT, IoB & Generative IA

Challenge FIAP 2026 · Clyvo Vet · 2TDS · Sprint 3

**O tutor conta. A IA entende. A veterinária decide.** Um LLM transforma a fala livre do tutor e da veterinária em
dados estruturados; um motor de regras em Java aplica o que a veterinária assinou e decide a dose do dia ou manda
procurar a clínica.

| | |
|---|---|
| Vídeo de apresentação | *link do YouTube (não listado)* |
| Swagger UI (local) | `http://localhost:8080/swagger-ui.html` |

> Esta branch reúne a entrega da disciplina de IA. O código é o mesmo da `main`, cujo README descreve a API para
> Java Advanced.

| Nome | RM |
|------|----|
| Felipe Yuiti Ishii | 565339 |
| Gabriel Nogueira Peixoto | 563925 |
| Giovanna Neri dos Santos | 566154 |
| Mariana Inoue | 565834 |

---

## 1. Problema e abordagem

A consulta termina, mas o tratamento continua em casa. A prescrição diz "de 1 a 2 ml; se as fezes estiverem moles,
a menor dose", e no quarto dia quem escolhe a dose é o tutor, sem formação para isso. A clínica não fica sabendo, e o
que o tutor observa não chega ao prontuário.

**Abordagem: NLP com LLM e saída estruturada, sobre um vocabulário fechado, mais um motor de regras.**

- **O LLM entende** (Gemini 2.5 Flash via Spring AI): lê a narrativa com negação, gíria e ordem livre e devolve JSON
  tipado, só com códigos do catálogo da clínica, citando o trecho que embasa cada campo. Não decide nada.
- **O motor de regras decide** (Java, sem modelo): aplica a regra da prescrição e devolve a dose dentro da faixa,
  "acione a clínica" ou "suspenda".
- **Por que não um modelo treinado:** não há base aberta de dose veterinária, e dose é ato da veterinária
  (Resolução CFMV nº 1.465/2022). **Por que não um chatbot:** conversa aberta deixaria o modelo orientar conduta.

**O que a IA agrega:** apoio à decisão (o tutor deixa de escolher a dose sozinho), personalização (a mesma fala gera
respostas diferentes conforme a regra de cada paciente) e priorização (condição crítica escala antes de qualquer
dose). A mesma camada gera o rascunho de prescrição a partir da fala da veterinária.

## 2. Dados e arquitetura

![Dados e arquitetura](assets/ia/arquitetura.png)

| Dado | Origem | Quem usa |
|---|---|---|
| Narrativa (comportamento, sintomas) | app, texto livre | **o modelo** |
| Vocabulário de condições clínicas | `T_PB_CONDICAO_CLINICA` | **o modelo**, só as condições da prescrição ativa e as críticas |
| Prescrição e regras (medicamentos) | `T_PB_PRESCRICAO`, `T_PB_REGRA_PRESCRICAO` | o motor de regras |
| Perfil, consultas, histórico, vacinas | `T_PB_ANIMAL`, `T_PB_CONSULTA`, `T_PB_REGISTRO_ATENDIMENTO`, `T_PB_PLANO_CUIDADO` | a veterinária, ao prescrever |
| Check-in, condições confirmadas e desfecho | `T_PB_CHECKIN`, `T_PB_CONDICAO_OBSERVADA`, `T_PB_ITEM_PLANO_CUIDADO` | prontuário |

A IA está em `infrastructure/ia/ExtratorIA.java` (chamada ao modelo), `service/checkin/CheckinExtracaoService.java`
e `service/prescricao/PrescricaoExtracaoService.java` (prompts e validação) e `service/checkin/AvaliadorRegraService.java`
(motor de regras). Configuração: `temperature=0` e até 3 tentativas com backoff.

## 3. Demonstração

![Check-in do tutor e prescrição da veterinária](assets/ia/demonstracao.png)

`POST /api/checkin/extracao` devolve o que a IA entendeu, sem gravar. O tutor confirma, `POST /api/checkin` grava e o
motor aplica a regra. `POST /api/prescricao/rascunho` faz o mesmo com a fala da veterinária.

## 4. Resultados parciais

Matriz de **108 cenários contra o Gemini real**: 6 espécies, 18 quadros clínicos, 6 tipos de relato (normal, bem,
mal, grave, ambíguo, ruído), percorrendo narrativa → extração → regra → desfecho.

![Números da matriz](assets/ia/matriz-numeros.png)

![Desfechos por tipo de relato](assets/ia/matriz-desfechos.png)

A confiança por campo passou a discriminar depois de o prompt exigir o trecho exato e faixas nomeadas; antes, voltava
1,0 em tudo.

![Distribuição da confiança](assets/ia/matriz-confianca.png)

**Onde o Java segura o modelo:** código fora do vocabulário é descartado; valor de tipo errado nunca chega ao banco
(foi a única falha da matriz, corrigida no prompt e barrada em Java); nada é gravado sem o tutor confirmar; gravidade
é decidida pelo catálogo da clínica, não pelo modelo; e falha do modelo vira resposta 200 degradada, nunca erro 500.

---

## Como executar

Pré-requisitos: Java 21, Maven 3.9, Docker (ou Oracle FIAP) e uma chave do Gemini
([Google AI Studio](https://aistudio.google.com)).

```bash
cp .env.example .env
# preencha ORACLE_PASSWORD, ORACLE_SYS_PASSWORD, PETBUDDIES_JWT_SECRET e GEMINI_API_KEY

docker compose up -d --wait                          # sobe o Oracle local
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Para o Oracle FIAP, preencha `ORACLE_USER` (RM) e `ORACLE_PASSWORD` no `.env` e rode só o `mvn`.

### Testando a IA pelo Swagger

Usuários do seed, senha `petbuddies123`: `ana@clinica.com` (`VET`) e `maria@email.com` (`TUTOR`).

1. **Como `VET`:** cadastre o animal, uma condição clínica (ex. `FEZES_MOLES`, `BOOLEANO`), uma janela e uma consulta,
   e feche o atendimento com uma prescrição de 1 a 2 ml e a regra `FEZES_MOLES → DOSE_MIN`, com início hoje.
2. **Como `VET`:** `POST /api/prescricao/rascunho` com a fala da veterinária.
3. **Como `TUTOR`:** `POST /api/checkin/extracao`:
   ```json
   { "animalId": 1, "narrativa": "Dei o remédio às 20h, mas as fezes tavam moles de novo." }
   ```
4. **Como `TUTOR`:** `POST /api/checkin` com as condições confirmadas; a resposta traz o desfecho e a dose.

**Tecnologias:** Java 21 · Spring Boot 3.4.5 · Spring AI 1.1.6 · Gemini 2.5 Flash · Spring Data JPA · Oracle ·
Flyway · Spring Security (JWT) · Springdoc OpenAPI
