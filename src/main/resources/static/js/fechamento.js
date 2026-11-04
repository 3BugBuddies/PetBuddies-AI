// Etapas do fechar atendimento. O estado inicial vem do servidor (window.FECHAMENTO), inclusive
// depois de um erro de validação; os campos de texto continuam sendo do formulário.
document.addEventListener('alpine:init', function () {
    Alpine.data('fechamento', function () {
        var inicial = window.FECHAMENTO;
        var NOMES_ETAPA = { 2: 'procedimento', 3: 'prescrição', 4: 'revisar' };

        var proximaChave = 0;
        // A chave mantém cada bloco no seu lugar quando um do meio é removido e os índices dos names mudam.
        function novoMedicamento(base) {
            return Object.assign({ chave: proximaChave++, regrasIgnoradas: 0, erros: [] }, base);
        }

        return {
            etapa: inicial.etapa,
            houveProcedimento: inicial.houveProcedimento,
            prescrever: inicial.prescrever,
            tipo: inicial.tipo,
            itemPlanoId: inicial.itemPlanoId,
            nome: inicial.nome,
            itensAbertos: inicial.itensAbertos,
            condicoes: inicial.condicoes,
            medicamentos: inicial.medicamentos.map(function (m) { return novoMedicamento(m); }),
            atual: 0,
            receitasModelo: inicial.receitasModelo,
            modeloEscolhido: '',
            resumo: { regras: [] },

            get comItemNoPlano() {
                return this.tipo === 'VACINACAO' || this.tipo === 'VERMIFUGACAO';
            },
            get baixaItem() {
                return this.houveProcedimento && this.comItemNoPlano && this.itemPlanoId !== '';
            },
            get itensDoTipo() {
                return this.itensAbertos.filter(function (i) { return i.tipo === this.tipo; }, this);
            },

            proxima: function () {
                return NOMES_ETAPA[this.etapa + 1] || '';
            },
            ir: function (n) {
                this.etapa = n;
                if (n === 4) {
                    this.montarResumo();
                }
                window.scrollTo({ top: 0 });
            },

            trocouTipo: function () {
                this.itemPlanoId = '';
            },
            escolherItem: function () {
                var id = this.itemPlanoId;
                var item = this.itensAbertos.find(function (i) { return i.id === id; });
                if (item) {
                    this.nome = item.nome;
                }
            },

            novaRegra: function (med) {
                med.regras.push({ condicaoClinicaId: '', operador: '', limite: '', acaoDose: '' });
            },
            adicionarMedicamento: function () {
                var primeiro = this.medicamentos[0];
                this.medicamentos.push(novoMedicamento({
                    medicamento: '', doseMin: '', doseMax: '', unidade: '', frequenciaDia: '', duracaoDias: '',
                    dataInicio: primeiro ? primeiro.dataInicio : '', orientacao: '',
                    receitaModeloId: '', receitaModeloVersao: '', regras: []
                }));
                this.atual = this.medicamentos.length - 1;
            },
            removerMedicamento: function (m) {
                this.medicamentos.splice(m, 1);
                this.atual = Math.min(this.atual, this.medicamentos.length - 1);
            },
            // Copia o molde para o medicamento em edição e guarda de onde veio; depois disso a receita é da consulta.
            aplicarReceitaModelo: function () {
                var id = this.modeloEscolhido;
                var modelo = this.receitasModelo.find(function (m) { return m.id === id; });
                var med = this.medicamentos[this.atual];
                if (!modelo || !med) {
                    return;
                }
                med.medicamento = modelo.medicamento;
                med.doseMin = modelo.doseMin;
                med.doseMax = modelo.doseMax;
                med.unidade = modelo.unidade;
                med.frequenciaDia = modelo.frequenciaDia;
                med.duracaoDias = modelo.duracaoDias;
                med.orientacao = modelo.orientacao;
                var ativas = this.condicoes.map(function (c) { return c.id; });
                var cabem = modelo.regras.filter(function (r) { return ativas.indexOf(r.condicaoClinicaId) >= 0; });
                med.regrasIgnoradas = modelo.regras.length - cabem.length;
                med.regras = cabem.map(function (r) {
                    return { condicaoClinicaId: r.condicaoClinicaId, operador: r.operador, limite: r.limite, acaoDose: r.acaoDose };
                });
                med.receitaModeloId = modelo.id;
                med.receitaModeloVersao = modelo.versao;
            },
            condicao: function (regra) {
                return this.condicoes.find(function (c) { return c.id === String(regra.condicaoClinicaId); });
            },
            numerica: function (regra) {
                var c = this.condicao(regra);
                return !!c && c.tipo === 'NUMERICO';
            },
            unidadeDe: function (regra) {
                var c = this.condicao(regra);
                return c && c.unidade ? c.unidade : '';
            },
            previa: function (regra) {
                var c = this.condicao(regra);
                if (!c) {
                    return 'escolha uma condição';
                }
                var texto = c.rotulo.toLowerCase();
                if (c.tipo === 'NUMERICO') {
                    texto += ' ' + (inicial.operadores[regra.operador] || '?') + ' ' + (regra.limite || '?')
                        + (c.unidade ? ' ' + c.unidade : '');
                }
                return texto + ' → ' + (inicial.acoes[regra.acaoDose] || '?');
            },

            valor: function (nome) {
                var campo = this.$refs.form.elements[nome];
                return campo && campo.value ? campo.value.trim() : '';
            },
            dia: function (iso) {
                var p = iso.substring(0, 10).split('-');
                return p.length === 3 ? p[2] + '/' + p[1] + '/' + p[0] : iso;
            },
            montarResumo: function () {
                var self = this;
                var peso = this.valor('registro.peso');
                var retorno = this.valor('registro.proximoRetorno');
                var item = this.itensAbertos.find(function (i) { return i.id === self.itemPlanoId; });
                var lote = this.valor('procedimento.lote');
                var data = this.valor('procedimento.data');
                var dias = this.medicamentos.map(function (m) { return parseInt(m.duracaoDias, 10) || 0; });

                this.resumo = {
                    anamnese: this.valor('registro.anamnese'),
                    registroLinha: (peso ? 'Peso ' + peso.replace('.', ',') + ' kg' : 'Sem peso')
                        + ' · ' + (retorno ? 'retorno em ' + this.dia(retorno) : 'sem retorno marcado'),
                    procedimento: this.houveProcedimento
                        ? (inicial.tipos[this.tipo] || '') + (this.nome ? ' · ' + this.nome : '')
                        : 'Nenhum procedimento',
                    procedimentoLinha: this.houveProcedimento
                        ? (lote ? 'Lote ' + lote + ' · ' : '') + (data ? this.dia(data) : '')
                            + (item ? ' · baixa "' + item.nome + '" do plano' : '')
                        : '',
                    medicamentos: this.prescrever ? this.medicamentos.map(function (m) {
                        return {
                            titulo: m.medicamento + ' ' + (m.doseMin === m.doseMax ? m.doseMin : m.doseMin + '–' + m.doseMax) + ' ' + m.unidade,
                            linha: m.frequenciaDia + '×/dia · ' + m.duracaoDias + ' dias · a partir de '
                                + (m.dataInicio ? self.dia(m.dataInicio) : '?'),
                            regras: m.regras.map(function (r) { return self.previa(r); })
                        };
                    }) : [],
                    duracao: Math.max.apply(null, dias)
                };
            }
        };
    });
});
