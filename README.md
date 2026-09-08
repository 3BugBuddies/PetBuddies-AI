# petbuddies-ai — Challenge FIAP 2026 | Java Advanced

Serviço Java do **PetBuddies**, o app de cuidado contínuo para pets do time BugBuddies.
Este serviço é o lado do **cuidado — o que deve acontecer**: catálogo de protocolos, plano vivo por
animal, itens com data alvo, autenticação com dois perfis e o motor de regras do check-in.

O par dele é o `PetBuddies-API` (.NET), que guarda o **registro — o que aconteceu**: consulta,
atendimento, procedimento, prescrição e check-in. Os dois serviços apontam para o **mesmo Oracle**, e
este aqui lê as tabelas do outro **por projeção, não por HTTP**.

> **O que mudou desde a 2ª Sprint.** O produto era um bot de WhatsApp com o motor atrás. O WhatsApp
> saiu como interface e o score de risco saiu do produto; o motor ficou, e ganhou agendamento por
> âncora e recorrência, login e a derivação de itens a partir de prescrição. O código do bot, da
> triagem e do score foi **removido**, não comentado.

**A IA interpreta a narrativa do tutor. Ela não decide dose em momento nenhum** — quem decide é o
motor determinístico, aplicando a regra que a veterinária escreveu e assinou.

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
| **Catálogo de protocolos** — o molde de cuidado e as ações previstas nele | no ar |
| **Motor de planos** — instancia o plano de um animal a partir do protocolo compatível e devolve os itens | no ar |
| **Autenticação com dois perfis** (vet e tutor), com token para o app e formulário para a web | em implementação |
| **Superfície web** — catálogo de protocolos e painel de acompanhamento, em Thymeleaf | em implementação |
| **Saúde consultável** — endereço aberto que diz se o serviço e o banco estão de pé | em implementação |
| **Check-in do tutor** — extração da narrativa por IA e motor de regras determinístico sobre as prescrições ativas | em implementação |

As quatro últimas linhas ainda **não existem no código**. Elas estão especificadas e são a entrega
desta sprint; nada neste README descreve endpoint que não esteja no ar.

---

## Endpoints no ar hoje

Base local: `http://localhost:8080` · Swagger: `http://localhost:8080/swagger-ui.html`

### Motor de cuidado

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/motor/planos/instanciar-preventivo` | escolhe o protocolo preventivo compatível com o animal e instancia o plano |
| `POST` | `/api/motor/planos/instanciar-pos-cirurgico` | instancia o plano pós-cirúrgico a partir da cirurgia registrada |
| `GET` | `/api/motor/planos/{animalId}` | devolve o plano ativo do animal |
| `GET` | `/api/motor/planos/{animalId}/eventos` | devolve os itens do plano |

Os dois `POST` são **idempotentes**: com um plano `ATIVO` já existente, a resposta é o plano que
existe, não um segundo plano.

### Catálogo de protocolos

| Método | Rota |
|---|---|
| `GET` `POST` | `/api/protocolos` |
| `GET` | `/api/protocolos/buscar?categoria=&especie=&porte=&sexo=` |
| `GET` `PUT` `DELETE` | `/api/protocolos/{id}` |
| `GET` `POST` | `/api/protocolos/{protocoloId}/eventos` |
| `GET` `PUT` `DELETE` | `/api/eventos-protocolo/{id}` |

O evento é filho do protocolo, então nasce em `POST /api/protocolos/{protocoloId}/eventos` — nunca em
`POST /api/eventos-protocolo`.

---

## Roteiro de avaliação — catálogo de protocolos

> Estes endpoints são **autocontidos**: não dependem da API .NET. O avaliador precisa apenas deste
> serviço rodando com o Oracle configurado.

**Protocolo** é um modelo de cuidado — por exemplo, o preventivo de cachorro filhote ou o
pós-cirúrgico de gato. Ele define a categoria (`PREVENTIVO` ou `POS_CIRURGICO`) e os critérios de
aplicação: espécie, porte, sexo, castração e faixa de idade.

**Evento de protocolo** é uma ação prevista dentro do modelo — vacinação, vermifugação, exame,
retorno, medicação, higiene. Ele descreve **quando** a ação acontece por três peças:

| Peça | Campos | Exemplo |
|---|---|---|
| A data-base | `ancora` — `INSTANCIACAO`, `NASCIMENTO` ou `DATA_CIRURGIA` | a partir do nascimento |
| O deslocamento | `offset` + `unidadeOffset` | 1 mês depois |
| A recorrência | `intervalo` + `unidadeIntervalo` + `repeticoes` | de mês em mês, seis vezes |

`repeticoes` é obrigatório: "para sempre" precisa virar um número, senão a expansão do plano não tem
onde parar. Sem `intervalo`, a ocorrência é única.

### Fluxo sugerido

| Ordem | No projeto | Método | Rota | Retorno esperado |
|---|---|---|---|---|
| 1 | Cadastra um modelo de cuidado | `POST` | `/api/protocolos` | `201` com o protocolo criado |
| 2 | Mostra os modelos disponíveis para o motor | `GET` | `/api/protocolos` | `200` com a lista de protocolos ativos |
| 3 | Localiza os modelos de uma categoria | `GET` | `/api/protocolos/buscar?categoria=PREVENTIVO` | `200` com os protocolos da categoria |
| 4 | Simula a seleção de regra para um perfil de animal | `GET` | `/api/protocolos/buscar?categoria=PREVENTIVO&especie=CACHORRO` | `200` com o filtro combinado |
| 5 | Consulta um modelo específico | `GET` | `/api/protocolos/{id}` | `200` com o protocolo |
| 6 | Ajusta uma regra já cadastrada | `PUT` | `/api/protocolos/{id}` | `200` com os dados atualizados |
| 7 | Acrescenta uma ação ao modelo | `POST` | `/api/protocolos/{protocoloId}/eventos` | `201` com o evento criado |
| 8 | Mostra a sequência de cuidado do protocolo | `GET` | `/api/protocolos/{protocoloId}/eventos` | `200` com os eventos do protocolo |
| 9 | Isola as ações de um tipo | `GET` | `/api/protocolos/{protocoloId}/eventos?tipo=VACINACAO` | `200` com os eventos filtrados |
| 10 | Consulta uma ação específica | `GET` | `/api/eventos-protocolo/{id}` | `200` com o evento |
| 11 | Ajusta prazo ou descrição da ação | `PUT` | `/api/eventos-protocolo/{id}` | `200` com o evento atualizado |
| 12 | Remove uma ação do modelo | `DELETE` | `/api/eventos-protocolo/{id}` | `204` sem corpo |
| 13 | Remove o modelo de teste | `DELETE` | `/api/protocolos/{id}` | `204` sem corpo |

### Validações e respostas de erro

| Situação | Método | Rota | Retorno esperado |
|---|---|---|---|
| Protocolo sem campos obrigatórios | `POST` | `/api/protocolos` com `{}` | `400` com `ErrorDto` |
| Protocolo inexistente | `GET` | `/api/protocolos/999999` | `404` com `ErrorDto` |
| Evento sem tipo, nome, deslocamento, âncora ou repetições | `POST` | `/api/protocolos/{protocoloId}/eventos` com `{}` | `400` com `ErrorDto` |
| Evento com enum inválido | `POST` | `/api/protocolos/{protocoloId}/eventos` com `"tipo": "VACINA"` | `400` com os valores aceitos |
| Evento inexistente | `GET` | `/api/eventos-protocolo/999999` | `404` com `ErrorDto` |

O corpo de erro é sempre `ErrorDto{ code, message }`, montado por um `@RestControllerAdvice` global.

---

## Modelo de dados — cinco entidades

| Entidade | Tabela | Papel |
|---|---|---|
| `UsuarioEntity` | `T_PB_USUARIO` | credencial, perfil (`VET` / `TUTOR`) e o vínculo com a identidade do registro |
| `ProtocoloEntity` | `T_PB_PROTOCOLO` | o molde de cuidado e seus critérios de aplicação |
| `EventoProtocoloEntity` | `T_PB_EVENTO_PROTOCOLO` | a ação prevista no molde, com âncora, deslocamento e recorrência |
| `PlanoCuidadoAnimalEntity` | `T_PB_PLANO_CUIDADO_ANIMAL` | o plano vivo de um animal |
| `EventoPlanoEntity` | `T_PB_EVENTO_PLANO` | o item do plano, com data alvo, origem e status |

As colunas que apontam para o outro serviço — `ID_ANIMAL`, `ID_CONSULTA`, `ID_PROCEDIMENTO`,
`ID_PRESCRICAO`, `ID_VETERINARIO`, `ID_RESPONSAVEL` — são **ids soltos, sem relação JPA**: as tabelas
de destino são escritas pelo .NET.

### Enums

| Enum | Valores |
|---|---|
| `CategoriaProtocolo` | `PREVENTIVO`, `POS_CIRURGICO` |
| `TipoEventoProtocolo` | `VACINACAO`, `VERMIFUGACAO`, `EXAME`, `RETORNO`, `CIRURGIA`, `MEDICACAO`, `HIGIENE` |
| `TipoAncora` | `INSTANCIACAO`, `NASCIMENTO`, `DATA_CIRURGIA` |
| `UnidadeTempo` | `DIAS`, `SEMANAS`, `MESES` |
| `TipoOrigemItem` | `PROTOCOLO`, `PRESCRICAO` |
| `StatusPlano` | `ATIVO`, `CONCLUIDO`, `CANCELADO` |
| `StatusEventoPlano` | `PENDENTE`, `REALIZADO`, `CANCELADO`, `ATRASADO` |
| `PerfilUsuario` | `VET`, `TUTOR` |
| `Especie` | `CACHORRO`, `GATO`, `PASSARO`, `COELHO`, `HAMSTER`, `OUTRO` |
| `Porte` | `MINI`, `PEQUENO`, `MEDIO`, `GRANDE`, `GIGANTE` |
| `Sexo` | `MACHO`, `FEMEA` |

Todo enum é persistido como texto (`@Enumerated(EnumType.STRING)`) — `ORDINAL` corromperia os dados
na primeira reordenação.

---

## Como o .NET aciona este serviço

O cadastro clínico é canônico no .NET. Quando um animal é cadastrado lá, o `MotorApiClient` chama
`POST /api/motor/planos/instanciar-preventivo` aqui; quando uma cirurgia é registrada, chama o
pós-cirúrgico. As duas chamadas são **best-effort**: falha deste lado não desfaz o cadastro clínico do
outro, e a idempotência do motor evita plano duplicado numa nova tentativa.

**Este serviço não chama o .NET por HTTP.** A leitura dos dados de registro é feita direto no banco,
por um contexto de persistência somente-leitura.

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
- Maven 3.9+
- Acesso ao Oracle FIAP

### Passos

```bash
git clone https://github.com/3BugBuddies/PetBudies-AI
cd petbuddies-ai

cp .env.example .env
# preencher ORACLE_USER e ORACLE_PASSWORD

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

A aplicação sobe em `http://localhost:8080`, com o Swagger em `/swagger-ui.html`.

### Variáveis de ambiente

```env
ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521/ORCL
ORACLE_USER=             # seu RM (ex: rm123456)
ORACLE_PASSWORD=         # sua senha Oracle FIAP
```

A chave da IA volta ao `.env.example` junto com o check-in, e o segredo do token junto com a
autenticação. **Nenhum segredo é versionado**: o `.env` está no `.gitignore`.

### Banco

O schema das cinco tabelas do cuidado é criado pelo próprio serviço — hoje pelo Hibernate
(`ddl-auto=update`) e, ainda nesta sprint, por um baseline Flyway com validação de schema na subida.
Este serviço **não** cria as tabelas do registro, que são do .NET: os dois conjuntos não se cruzam.

---

## Tecnologias

- **Java 21** · Spring Boot 3.4.5
- **Spring Data JPA + Hibernate** sobre **Oracle Database** (FIAP)
- **Bean Validation** (Jakarta)
- **Springdoc OpenAPI 2.8.8** — Swagger UI com tags por domínio
- **Postman** — coleção em `docs/postman/petbuddies-ai-java.postman_collection.json`
