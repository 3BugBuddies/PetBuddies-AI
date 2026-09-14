# PetBuddies — Componente de IA | Disruptive Architectures: IoT, IoB & Generative IA

Challenge FIAP 2026 · Clyvo Vet · 2TDS · Sprint 3

**O tutor conta. A IA entende. A veterinária decide.** Um LLM transforma a fala livre do tutor e da veterinária em
dados estruturados; um motor de regras em Java aplica o que a veterinária assinou e decide a dose do dia ou manda
procurar a clínica.

| | |
|---|---|
| Vídeo de apresentação | `https://youtu.be/BV3-_UwLYpA`|
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

A consulta termina, mas o tratamento continua em casa. A prescrição diz "de 1 a 2 ml; se ela comer pouco, a menor
dose", e no quarto dia quem escolhe a dose é o tutor, sem formação para isso. A clínica não fica sabendo, e o
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

Cenário: a veterinária prescreveu Lactulona de 1 a 2 ml com a regra **apetite baixo → menor dose**, e a clínica marca
**sem comer há 24 h** como condição crítica. Os JSON abaixo são respostas reais da API com o Gemini, resumidas aos campos que importam.

| Tela | Requisição | O que a API devolve |
|---|---|---|
| A tutora conta como a Luna passou | `POST /api/checkin/extracao` | as condições que a IA reconheceu, com trecho e confiança; nada é gravado |
| A dose de hoje | `POST /api/checkin` | o desfecho da regra: `DOSE_CALCULADA`, 1 ml |
| Relato preocupante | `POST /api/checkin` | `ACIONAR_CLINICA`, sem dose, com o telefone da clínica |
| A veterinária dita a prescrição | `POST /api/prescricao/rascunho` | o formulário preenchido e as regras propostas, para revisar e assinar |

<details>
<summary><b>Extração</b>: "Dei o remédio às 20h, mas ela não quis o jantar."</summary>

```json
{
  "condicoes": [
    {
      "codigo": "APETITE_BAIXO",
      "rotulo": "Comeu pouco ou recusou a refeição",
      "valorBooleano": true,
      "confianca": 0.95,
      "critica": false,
      "trecho": "não quis o jantar",
      "literal": true
    }
  ],
  "redFlags": [],
  "degradado": false
}
```
</details>

<details>
<summary><b>Check-in confirmado</b>: a regra devolve a menor dose</summary>

```json
{
  "condicoesObservadas": [{ "codigoCongelado": "APETITE_BAIXO", "valorBooleano": true, "confianca": 0.95 }],
  "desfechos": [
    { "medicamento": "Lactulona", "desfecho": "DOSE_CALCULADA", "doseAplicada": 1, "unidade": "ml", "regraAplicadaId": 1 }
  ],
  "escalado": false
}
```
</details>

<details>
<summary><b>Relato preocupante</b>: "Ela não comeu nada desde ontem de manhã e está muito parada."</summary>

Extração: a condição crítica foi inferida (`literal: false`), e o que não está no vocabulário vira observação em texto.

```json
{
  "condicoes": [
    { "codigo": "APETITE_BAIXO", "valorBooleano": true, "confianca": 0.95, "critica": false, "trecho": "não comeu nada", "literal": true },
    { "codigo": "SEM_COMER_24H", "valorBooleano": true, "confianca": 0.8, "critica": true, "trecho": "não comeu nada desde ontem de manhã", "literal": false }
  ],
  "redFlags": ["está muito parada"]
}
```

Check-in: a condição crítica escala antes de qualquer regra.

```json
{
  "observacoesGerais": "Sinal de atenção identificado (Passou 24 horas ou mais sem comer). Procure a clínica: 1140028922.",
  "desfechos": [{ "medicamento": "Lactulona", "desfecho": "ACIONAR_CLINICA", "doseAplicada": null }],
  "escalado": true
}
```
</details>

<details>
<summary><b>Rascunho de prescrição</b>: "Lactulona, de 1 a 2 ml, uma vez ao dia, por 14 dias, começando hoje. Se ela comer pouco, usar a dose mínima."</summary>

`dataInicio` vale 0,6 porque "começando hoje" foi resolvido pelo modelo, não dito como data.

```json
{
  "extracaoDisponivel": true,
  "prescricao": { "medicamento": "Lactulona", "doseMin": 1.0, "doseMax": 2.0, "unidade": "ml", "frequenciaDia": 1, "duracaoDias": 14, "dataInicio": "2026-09-13" },
  "confiancaPorCampo": { "medicamento": 1.0, "doseMin": 1.0, "doseMax": 1.0, "unidade": 1.0, "frequenciaDia": 1.0, "duracaoDias": 1.0, "dataInicio": 0.6 },
  "regrasPropostas": [{ "condicaoClinicaId": 1, "acaoDose": "DOSE_MIN", "ordem": 1 }],
  "condicoesDescartadas": []
}
```
</details>

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

### Testando a IA pelo Postman

Com Docker, o banco e a API sobem juntos (`.env` com `GEMINI_API_KEY` e `PETBUDDIES_JWT_SECRET`):

```bash
docker compose -f docker-compose.demo.yml up -d --build
```

Importe `docs/postman/petbuddies-ai-java.postman_collection.json` e rode a pasta **Demo IA** (Run folder). Ela prepara
o cenário como veterinária (`ana@clinica.com`), gera o rascunho de prescrição, faz o check-in como tutora
(`maria@email.com`) e termina no relato crítico. Os ids e tokens passam de uma requisição para a outra sozinhos. Senha
dos dois usuários: `petbuddies123`.

**Tecnologias:** Java 21 · Spring Boot 3.4.5 · Spring AI 1.1.6 · Gemini 2.5 Flash · Spring Data JPA · Oracle ·
Flyway · Spring Security (JWT) · Springdoc OpenAPI
