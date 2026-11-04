// =============================================================================
// planos_cuidado.js — prepara e consulta a coleção importada do Oracle
// =============================================================================
// Roda DEPOIS do mongoimport (passo a passo em docs/banco/README.md):
//
//   mongosh "mongodb://localhost:27017/petbuddies" docs/banco/mongodb/planos_cuidado.js
//
// Na Azure, troque a URI pela connection string do Cosmos DB, com /petbuddies
// antes do "?".
//
// A COLEÇÃO: um documento por plano de cuidado, com os dados do animal no topo
// e os eventos do checklist EMBUTIDOS num array. É a forma que o procedimento
// PKG_PB_JSON.PLANO_JSON monta no Oracle a partir do JOIN de T_PB_ANIMAL,
// T_PB_PLANO_CUIDADO e T_PB_ITEM_PLANO_CUIDADO. No relacional, ler um plano é
// juntar três tabelas; aqui é ler UM documento — o app abre o plano do pet com
// uma só leitura, sem JOIN.
//
// As datas vêm como texto ISO (2026-09-01): comparar e ordenar texto nesse
// formato dá o mesmo resultado que comparar datas.
// =============================================================================

const planos = db.getCollection("planos_cuidado");

print(`\nColeção planos_cuidado: ${planos.countDocuments()} documento(s)\n`);

// -----------------------------------------------------------------------------
// 1. Índices — um por pergunta que o app faz, nenhum "por via das dúvidas".
// -----------------------------------------------------------------------------
// planoId é a chave que vem do Oracle: único, para reimportar o mesmo arquivo
// não duplicar plano.
planos.createIndex({ planoId: 1 }, { unique: true, name: "ux_plano" });

// "os planos ativos deste pet" — a tela do tutor.
planos.createIndex({ animalId: 1, status: 1 }, { name: "ix_animal_status" });

// "o que vence nos próximos dias" — índice multichave: o MongoDB indexa cada
// elemento do array de eventos, então dá para buscar DENTRO do documento.
planos.createIndex({ "eventos.status": 1, "eventos.dataAlvo": 1 }, { name: "ix_eventos_status_data" });

print("Índices:");
planos.getIndexes().forEach(i => print(`  ${i.name}  ${JSON.stringify(i.key)}`));

// -----------------------------------------------------------------------------
// 2. Consultas
// -----------------------------------------------------------------------------

// 2.1 O plano de um pet, inteiro, numa leitura só (no Oracle: JOIN de 3 tabelas).
print("\n2.1 Planos do animal 1 (Thor), com os eventos embutidos:");
planos.find(
    { animalId: 1 },
    { _id: 0, planoId: 1, categoria: 1, status: 1, animalNome: 1, totalEventos: 1,
      "eventos.nome": 1, "eventos.dataAlvo": 1, "eventos.status": 1 }
).forEach(p => printjson(p));

// 2.2 Filtro DENTRO do array: planos com algum evento ainda pendente.
print("\n2.2 Planos com evento PENDENTE ou ATRASADO:");
planos.find(
    { "eventos.status": { $in: ["PENDENTE", "ATRASADO"] } },
    { _id: 0, planoId: 1, animalNome: 1, categoria: 1 }
).forEach(p => printjson(p));

// 2.3 Desmontar o array ($unwind) para olhar evento a evento: o tratamento
// feito em casa, por status — a adesão do tutor ao remédio.
print("\n2.3 Itens de tratamento em casa, por status:");
planos.aggregate([
    { $unwind: "$eventos" },
    { $match: { "eventos.feitoEmCasa": true } },
    { $group: { _id: "$eventos.status", quantidade: { $sum: 1 } } },
    { $sort: { quantidade: -1 } }
]).forEach(r => printjson(r));

// 2.4 Visão da clínica: planos e eventos por espécie e categoria.
print("\n2.4 Planos e eventos por espécie e categoria:");
planos.aggregate([
    { $group: {
        _id: { especie: "$especie", categoria: "$categoria" },
        planos: { $sum: 1 },
        eventos: { $sum: "$totalEventos" }
    } },
    { $sort: { "_id.especie": 1, "_id.categoria": 1 } }
]).forEach(r => printjson(r));

// 2.5 Flexibilidade do documento: um campo novo entra só onde faz sentido, sem
// ALTER TABLE e sem mexer nos outros documentos.
print("\n2.5 Acrescenta 'observacaoClinica' só no plano 1 e lê de volta:");
planos.updateOne({ planoId: 1 }, { $set: { observacaoClinica: "Revisar dose na próxima consulta." } });
printjson(planos.findOne({ planoId: 1 }, { _id: 0, planoId: 1, animalNome: 1, observacaoClinica: 1 }));
print(`Documentos com o campo: ${planos.countDocuments({ observacaoClinica: { $exists: true } })} de ${planos.countDocuments()}`);
