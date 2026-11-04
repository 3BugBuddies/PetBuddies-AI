# Roteiro do vídeo — Banco de dados (Sprint 4)

Cobre os três itens que o enunciado pede no vídeo, mais o empacotamento:

1. a execução das procedures sendo utilizadas no backend;
2. a inserção dos dados no banco relacional;
3. a exportação do dataset para JSON e sua importação no MongoDB.

Duração estimada: 8 a 10 minutos. Cada cena diz **o que mostrar** e **o que falar**.

## Antes de gravar

- [ ] Java rodando contra o Oracle do schema da aplicação. No log da subida, o Flyway mostra
      `Migrating schema ... to version "9 - pacotes plsql e auditoria"` (só na primeira vez) ou
      `Schema ... is up to date`.
- [ ] SQL Developer conectado no **mesmo** schema, com o `07_testes_excecoes.sql` aberto.
- [ ] Swagger aberto em `http://localhost:8080/swagger-ui.html`, autorizado com o token da `ana@clinica.com`.
- [ ] MongoDB no ar (local ou Cosmos DB), com o **MongoDB Database Tools** (`mongoimport`) e o
      `mongosh` instalados, ou o MongoDB Compass aberto.
- [ ] A coleção `planos_cuidado` **vazia ou inexistente**, para a importação aparecer do zero.
- [ ] Terminal aberto na raiz do repositório `PetBuddies-AI`.

---

## Cena 1 — Modelo e empacotamento (≈ 1 min)

**Mostrar:** no SQL Developer, a árvore *Pacotes* com `PKG_PB_JSON`, `PKG_PB_TRATAMENTO` e
`PKG_PB_AUDITORIA`. Rode o **bloco 0** do `07_testes_excecoes.sql`: todos os objetos com status
`VALID`. Abra a especificação do `PKG_PB_JSON`.

**Falar:** "O backend usa o mesmo banco relacional da disciplina. As funções, os procedimentos e a
lógica da trigger estão em três pacotes, agrupados por assunto: JSON, tratamento e auditoria. A
especificação é o contrato público; os auxiliares ficam privados no corpo."

## Cena 2 — Inserção dos dados no banco relacional, pela aplicação (≈ 2 min)

**Mostrar:**
1. No Swagger, `POST /api/condicao-clinica` com:
   ```json
   { "codigo": "LETARGIA_VIDEO", "rotulo": "O pet está mais quieto que o normal?",
     "tipoDado": "BOOLEANO", "fonteValor": "RELATO", "critica": false, "ativo": true,
     "clinicaId": 1, "veterinarioAutorId": 1 }
   ```
   A resposta é `201`. Para gravar de novo, troque o `codigo`: ele é único por clínica.
2. No SQL Developer:
   ```sql
   SELECT ID_CONDICAO_CLINICA, CD_CODIGO, DS_ROTULO FROM T_PB_CONDICAO_CLINICA WHERE CD_CODIGO = 'LETARGIA_VIDEO';
   SELECT ID_AUDITORIA, TP_OPERACAO, NM_TABELA, ID_REGISTRO, TX_VALORES_NOVOS
     FROM T_PB_AUDITORIA ORDER BY ID_AUDITORIA DESC FETCH FIRST 1 ROWS ONLY;
   ```

**Falar:** "A aplicação inseriu a condição no Oracle. A trigger disparou com o INSERT e chamou o
pacote `PKG_PB_AUDITORIA`, que montou o JSON da linha com a Função 1 e gravou a auditoria. Ou
seja, o pacote rodou a partir de uma escrita do backend."

> Para mostrar a carga completa da disciplina, rode o bloco 6 do `07_testes_excecoes.sql`
> (conferência dos 5 registros mínimos por tabela).

## Cena 3 — Procedimento executado pelo backend (≈ 2 min)

**Mostrar:**
1. No `05_pacotes.sql`, o cursor do `PLANO_JSON`: **um** `SELECT` com `LEFT JOIN` entre
   `T_PB_ANIMAL`, `T_PB_PLANO_CUIDADO` e `T_PB_ITEM_PLANO_CUIDADO`.
2. No Swagger, `GET /api/exportacao/planos-cuidado` com `animalId = 1` → `200` e o JSON do Thor.
3. No terminal do Java, a linha
   `[DATASET] PKG_PB_JSON.PLANO_JSON executada (animalId=1): ... caracteres`.
4. A mesma rota com `animalId = 9999` → `404` com a mensagem
   `PLANO_JSON: animal 9999 não existe.`, que vem do `RAISE_APPLICATION_ERROR` do procedimento.

**Falar:** "Este é o Procedimento 1, corrigido conforme o feedback: a consulta principal agora é
um JOIN explícito das três tabelas, e o documento é montado por quebra de controle. O Java chama
o procedimento por JDBC e recebe o JSON pelo parâmetro de saída. Quando o procedimento levanta uma
exceção, ela chega ao cliente da API como resposta de erro."

## Cena 4 — Exportação do dataset para JSON (≈ 1 min)

**Mostrar:** a mesma rota **sem** `animalId` e o download de `planos_cuidado.json`. Abra o arquivo
e mostre um documento: dados do animal no topo e o array `eventos`.

Pelo terminal, o equivalente é:

```bash
curl -H "Authorization: Bearer <token>" -o planos_cuidado.json http://localhost:8080/api/exportacao/planos-cuidado
```

**Falar:** "Sem o filtro de animal, o procedimento exporta o dataset inteiro: um documento por
plano de cuidado, com os eventos embutidos. Esse arquivo é o que vai para o MongoDB."

## Cena 5 — Importação no MongoDB (≈ 2 min)

**Mostrar:**

```bash
mongoimport --uri "mongodb://localhost:27017" --db petbuddies --collection planos_cuidado --jsonArray --file planos_cuidado.json
```

A saída diz quantos documentos entraram. Abra a coleção no Compass e expanda um documento,
mostrando o array `eventos`.

**Falar:** "A estrutura segue o modelo de documentos: o que o app sempre lê junto fica junto. No
Oracle, ler o plano de um pet é um JOIN de três tabelas; aqui é ler um documento só."

## Cena 6 — Índices e consultas no MongoDB (≈ 1 a 2 min)

**Mostrar:**

```bash
mongosh "mongodb://localhost:27017/petbuddies" docs/banco/mongodb/planos_cuidado.js
```

Comente as saídas, na ordem do script:
- **índices**: `planoId` único, para reimportar sem duplicar, e um índice multichave sobre os eventos;
- **2.1** o plano do Thor inteiro, numa leitura;
- **2.2** filtro dentro do array (planos com evento pendente);
- **2.3** `$unwind` + `$group`: adesão ao tratamento em casa;
- **2.4** agregação por espécie e categoria;
- **2.5** um campo novo em um documento só, sem `ALTER TABLE`.

**Falar:** "Os índices seguem as perguntas que o app faz. O índice multichave permite buscar
dentro do array de eventos, e o esquema flexível aceita um campo novo só onde ele faz sentido."

## Encerramento (≈ 30 s)

**Falar:** "Recapitulando: os objetos PL/SQL estão empacotados; o backend insere dados no Oracle,
o que dispara o pacote de auditoria, e executa o Procedimento 1 para exportar o dataset em JSON;
e o dataset foi importado no MongoDB com uma estrutura de documentos, índices e consultas."
