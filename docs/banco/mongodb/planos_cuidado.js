// =============================================================================
// planos_cuidado.js — consulta a coleção importada do Oracle
// =============================================================================
// Roda DEPOIS do planos_cuidado_indices.js e do mongoimport (passo a passo em
// docs/banco/README.md):
//
//   mongosh "mongodb://localhost:27017/petbuddies" docs/banco/mongodb/planos_cuidado.js
//
// Na Azure, troque a URI pela connection string de produção, com /petbuddies
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

print(`\nColeção planos_cuidado: ${planos.countDocuments()} documento(s)`);

print("\nÍndices (criados por planos_cuidado_indices.js antes da importação):");
planos.getIndexes().forEach(i => print(`  ${i.name}  ${JSON.stringify(i.key)}${i.unique ? "  (único)" : ""}`));

// 2.1 O plano de um pet, inteiro, numa leitura só (no Oracle: JOIN de 3 tabelas).
print("\n2.1 Planos do animal 1, com os eventos embutidos:");
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
// ALTER TABLE e sem mexer nos outros documentos. O alvo é o primeiro plano da
// coleção, para o script funcionar com qualquer dataset importado.
const alvo = planos.find({}, { _id: 0, planoId: 1 }).sort({ planoId: 1 }).limit(1).toArray()[0];
if (alvo) {
    print(`\n2.5 Acrescenta 'observacaoClinica' só no plano ${alvo.planoId} e lê de volta:`);
    planos.updateOne({ planoId: alvo.planoId }, { $set: { observacaoClinica: "Revisar dose na próxima consulta." } });
    printjson(planos.findOne({ planoId: alvo.planoId }, { _id: 0, planoId: 1, animalNome: 1, observacaoClinica: 1 }));
    print(`Documentos com o campo: ${planos.countDocuments({ observacaoClinica: { $exists: true } })} de ${planos.countDocuments()}`);
}
