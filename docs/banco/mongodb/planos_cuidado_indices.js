// =============================================================================
// planos_cuidado_indices.js — cria a coleção e os índices ANTES da importação
// =============================================================================
// Ordem (passo a passo em docs/banco/README.md):
//
//   1. mongosh "<URI>/petbuddies" docs/banco/mongodb/planos_cuidado_indices.js
//   2. mongoimport ... --collection planos_cuidado --jsonArray --file planos_cuidado.json
//   3. mongosh "<URI>/petbuddies" docs/banco/mongodb/planos_cuidado.js
//
// Por que antes: no Azure Cosmos DB for MongoDB, índice ÚNICO só pode ser
// criado com a coleção vazia. Num MongoDB comum tanto faz a ordem, então esta
// ordem serve para os dois.
//
// Rodar de novo não duplica nada: createIndex com a mesma definição não faz nada.
// =============================================================================

const planos = db.getCollection("planos_cuidado");

print(`\nColeção planos_cuidado: ${planos.countDocuments()} documento(s) antes da importação`);

// Um índice por pergunta que o app faz, nenhum "por via das dúvidas".

// planoId é a chave que vem do Oracle: único, para reimportar o mesmo arquivo
// não duplicar plano.
planos.createIndex({ planoId: 1 }, { unique: true, name: "ux_plano" });

// "os planos ativos deste pet" — a tela do tutor.
planos.createIndex({ animalId: 1, status: 1 }, { name: "ix_animal_status" });

// "o que vence nos próximos dias" — índice multichave: o MongoDB indexa cada
// elemento do array de eventos, então dá para buscar DENTRO do documento.
// O Cosmos DB pode recusar índice composto sobre campos de array; as consultas
// funcionam do mesmo jeito sem ele, só sem o atalho.
try {
    planos.createIndex({ "eventos.status": 1, "eventos.dataAlvo": 1 }, { name: "ix_eventos_status_data" });
} catch (erro) {
    print(`Aviso: ix_eventos_status_data não foi criado (${erro.message}).`);
}

print("\nÍndices:");
planos.getIndexes().forEach(i => print(`  ${i.name}  ${JSON.stringify(i.key)}${i.unique ? "  (único)" : ""}`));
