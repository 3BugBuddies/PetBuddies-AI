# Banco de dados — PetBuddies (Sprint 4)

Entrega da disciplina de banco de dados: o modelo relacional no Oracle, os objetos PL/SQL
empacotados, o uso deles pelo backend Java e a importação do dataset em JSON no MongoDB.

## Arquivos, na ordem de execução

| Bloco | Arquivo | O que faz |
|---|---|---|
| 01 | [01_ddl.sql](01_ddl.sql) | modelo relacional: tabelas, constraints e índices |
| 02 | [02_auditoria.sql](02_auditoria.sql) | tabela `T_PB_AUDITORIA`, que a trigger grava |
| 03 | [03_carga.sql](03_carga.sql) | carga com no mínimo 5 registros por tabela |
| 04 | [04_tipos.sql](04_tipos.sql) | tipos de objeto usados pelos pacotes |
| 05 | [05_pacotes.sql](05_pacotes.sql) | os três pacotes: funções, procedimentos e a lógica da trigger |
| 06 | [06_trigger.sql](06_trigger.sql) | trigger `PB_AUDITA_CONDICAO`, que chama o pacote de auditoria |
| 07 | [07_testes_excecoes.sql](07_testes_excecoes.sql) | caminho feliz e cada exceção de cada objeto |
| — | [mongodb/planos_cuidado_indices.js](mongodb/planos_cuidado_indices.js) | coleção e índices no MongoDB, **antes** da importação |
| — | [mongodb/planos_cuidado.js](mongodb/planos_cuidado.js) | consultas da coleção importada no MongoDB |

No SQL Developer, rode como script (F5), um bloco por vez, com a conexão do seu RM. Os arquivos
estão em **UTF-8**: se os acentos aparecerem quebrados, ajuste em *Ferramentas → Preferências →
Ambiente → Codificação* para `UTF-8` e abra o arquivo de novo.

> Os blocos 01 a 03 criam e populam o schema do zero, para a avaliação. **Não rode no schema da
> aplicação**: lá as tabelas são criadas pelas migrations do Java (Flyway), e os blocos 02, 04, 05
> e 06 são instalados pela migration `V9` (abaixo).

## O que mudou desde a Sprint 3

- **Feedback do Procedimento 1 atendido.** `PKG_PB_JSON.PLANO_JSON` agora lê as três tabelas num
  único cursor com `JOIN` explícito entre `T_PB_ANIMAL`, `T_PB_PLANO_CUIDADO` e
  `T_PB_ITEM_PLANO_CUIDADO`, e monta o documento por quebra de controle quando o plano muda. Antes
  eram consultas separadas em laços encadeados. São `LEFT JOIN`s para que as exceções de "animal
  sem plano" e "plano sem itens" continuem disparando.
- **Empacotamento.** As duas funções, os dois procedimentos e a lógica da trigger estão em três
  pacotes, agrupados por assunto (tabela abaixo). A trigger continua sendo um objeto à parte,
  porque o Oracle não deixa trigger dentro de pacote, mas agora só decide a operação e chama
  `PKG_PB_AUDITORIA`.
- **Uso pelo backend.** O Procedimento 1 devolve o JSON num parâmetro `OUT`, e o Java o executa
  numa rota da API.
- **Acentos da carga.** 76 linhas do `03_carga.sql` estavam em UTF-8 dentro de um arquivo em
  Windows-1252 e iam para o banco como "ClÃ­nica" e "SiamÃªs". Foram corrigidas.
- O atributo `TP_TIPO_CUIDADO` de `PB_PAR_JSON` voltou a se chamar `TP_TIPO`: ele guarda o tipo
  do valor JSON (TEXTO, NUMERO...), não um tipo de cuidado.

## Pacotes

| Pacote | Objeto | Tipo | O que faz |
|---|---|---|---|
| `PKG_PB_JSON` | `MONTA_JSON` | Função 1 | recebe pares (campo, valor, tipo) e devolve um objeto JSON |
| | `PLANO_JSON(p_id_animal, p_json OUT)` | Procedimento 1 | JOIN de animal, plano e itens → array JSON, um documento por plano com os eventos embutidos; animal nulo exporta o dataset inteiro |
| | `PLANO_JSON(p_id_animal)` | Procedimento 1 | o mesmo, só imprimindo (para o SQL Developer) |
| `PKG_PB_TRATAMENTO` | `DOSE_DO_DIA` | Função 2 | decide a dose do dia pela regra da prescrição e pelo check-in |
| | `ADESAO_POR_DESFECHO` | Procedimento 2 | adesão por medicamento e desfecho, com subtotal e total geral manuais |
| `PKG_PB_AUDITORIA` | `CONDICAO_JSON` | Função | a condição clínica em JSON, para a auditoria |
| | `REGISTRAR` | Procedimento | grava a linha em `T_PB_AUDITORIA` |

Os auxiliares (`imprimir`, `linha`, `cabecalho`) ficam só no corpo dos pacotes, privados.

Cada pacote tem sua faixa de erros, para quem lê um `ORA-20xxx` saber de onde veio: `-20001` a
`-20009` na Função 1, `-20011` a `-20019` na Função 2, `-20021` a `-20029` no Procedimento 1,
`-20031` a `-20039` no Procedimento 2 e `-20041` a `-20049` na auditoria.

## Procedimentos usados pelo backend

A aplicação Java usa **o mesmo banco relacional** da disciplina: as tabelas de `01_ddl.sql` são as
que as migrations do Java criam, com as mesmas colunas.

- **Instalação:** a migration
  [`V9__pacotes_plsql_e_auditoria.sql`](../../src/main/resources/db/migration/V9__pacotes_plsql_e_auditoria.sql)
  instala os blocos 02, 04, 05 e 06 quando o Java sobe. Ela é **gerada** a partir desta pasta por
  [gerar_migration.py](gerar_migration.py), para a entrega e o banco da aplicação nunca divergirem.
  A V9 já rodou em produção e está congelada.
- **Correção:** a
  [`V10__corrige_numero_inteiro_no_json.sql`](../../src/main/resources/db/migration/V10__corrige_numero_inteiro_no_json.sql)
  recompila o corpo do `PKG_PB_JSON`. A Função 1 escrevia número inteiro com um ponto sobrando
  (`"planoId":4.`), JSON inválido que o `mongoimport` recusa. A V10 também é gerada pelo
  `gerar_migration.py`, a partir do `05_pacotes.sql`. Mudança depois que ela rodar vira uma V11.
- **`PKG_PB_JSON.PLANO_JSON`** — `GET /api/exportacao/planos-cuidado` (perfil `VET`) executa o
  procedimento e devolve o arquivo `planos_cuidado.json`. Com `?animalId=1`, só os planos daquele
  animal. As exceções do procedimento chegam como resposta da API: `404` para animal inexistente
  ou sem plano e `409` para plano sem itens.
- **`PKG_PB_AUDITORIA`** — toda escrita em `T_PB_CONDICAO_CLINICA` feita pelo app
  (`POST`/`PUT`/`DELETE /api/condicao-clinica`) dispara a trigger, que chama o pacote e grava a
  auditoria.

## Exportação para JSON e importação no MongoDB

O dataset é o resultado do Procedimento 1 para todos os animais: **um documento por plano de
cuidado**, com o animal no topo e os eventos do checklist embutidos num array.

**1. Exportar do Oracle, pela aplicação.** Com o token de um `VET`
(`POST /api/auth/login` com `ana@clinica.com`):

```bash
curl -H "Authorization: Bearer <token>" -o planos_cuidado.json http://localhost:8080/api/exportacao/planos-cuidado
```

No Swagger, a mesma rota fica no grupo **banco — exportação** e oferece o arquivo para download.

**2. Criar a coleção e os índices, antes de importar:**

```bash
mongosh "mongodb://localhost:27017/petbuddies" docs/banco/mongodb/planos_cuidado_indices.js
```

`planoId` único, `animalId + status` e um índice multichave sobre os eventos. Vêm **antes** da
importação porque, no Azure Cosmos DB for MongoDB, índice único só pode ser criado com a coleção
vazia. Num MongoDB comum a ordem tanto faz, então ela serve para os dois.

**3. Importar no MongoDB.** Com o
[MongoDB Database Tools](https://www.mongodb.com/try/download/database-tools) instalado:

```bash
mongoimport --uri "mongodb://localhost:27017" --db petbuddies --collection planos_cuidado --jsonArray --file planos_cuidado.json
```

Na Azure, troque a URI pela connection string de produção. Pelo **MongoDB Compass**: banco
`petbuddies` → *Create collection* `planos_cuidado` → aba *Indexes*, crie `planoId` único → *Add
data* → *Import JSON or CSV file* → escolha o arquivo.

**4. Consultas:**

```bash
mongosh "mongodb://localhost:27017/petbuddies" docs/banco/mongodb/planos_cuidado.js
```

O script lista os índices e roda as consultas da demonstração: o plano de um pet numa leitura só,
filtro dentro do array, `$unwind` para olhar evento a evento, agregação por espécie e um campo novo
só num documento.

### Por que essa forma de documento

No relacional, ler o plano de um pet é juntar três tabelas. No MongoDB, é ler **um documento**,
porque os eventos são sempre lidos junto com o plano e não existem fora dele. Por isso foram
**embutidos**, e não guardados numa coleção à parte com referência. Os dados do animal ficam
repetidos em cada plano dele; num banco de documentos isso é a troca esperada, já que um animal
tem poucos planos e a leitura do app fica sem `JOIN`.

Trecho de um documento (plano de tratamento do Thor, com dois dos eventos da carga):

```json
{
  "planoId": 2, "categoria": "TRATAMENTO", "status": "ATIVO",
  "protocoloId": null, "consultaId": 1, "criadoEm": "2026-09-01T08:00:00",
  "animalId": 1, "animalNome": "Thor", "especie": "CACHORRO", "raca": "Labrador",
  "porte": "GRANDE", "nascimento": "2021-03-14", "pesoKg": 32.5, "totalEventos": 6,
  "eventos": [
    { "id": 1, "origem": "PRESCRICAO", "tipo": "MEDICACAO", "nome": "Amoxicilina - dia 1",
      "dataAlvo": "2026-09-01", "status": "REALIZADO", "feitoEmCasa": true,
      "prescricaoId": 1, "procedimentoId": null, "executadoEm": "2026-09-01T08:20:00" },
    { "id": 2, "origem": "PRESCRICAO", "tipo": "MEDICACAO", "nome": "Dipirona - dia 1",
      "dataAlvo": "2026-09-01", "status": "REALIZADO", "feitoEmCasa": true,
      "prescricaoId": 3, "procedimentoId": null, "executadoEm": "2026-09-01T08:25:00" }
  ]
}
```
