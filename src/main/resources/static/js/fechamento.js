// Etapas do fechar atendimento. O estado inicial vem do servidor (window.FECHAMENTO), inclusive
// depois de um erro de validação; os campos de texto continuam sendo do formulário.
document.addEventListener('alpine:init', function () {
    Alpine.data('fechamento', function () {
        var inicial = window.FECHAMENTO;
        var NOMES_ETAPA = { 2: 'procedimento', 3: 'prescrição', 4: 'revisar' };

        return {
            etapa: inicial.etapa,
            houveProcedimento: inicial.houveProcedimento,
            prescrever: inicial.prescrever,
            tipo: inicial.tipo,
            itemPlanoId: inicial.itemPlanoId,
            nome: inicial.nome,
            itensAbertos: inicial.itensAbertos,
            condicoes: inicial.condicoes,
            regras: inicial.regras,
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

            novaRegra: function () {
                this.regras.push({ condicaoClinicaId: '', operador: '', limite: '', acaoDose: '' });
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
                var min = this.valor('prescricao.doseMin');
                var max = this.valor('prescricao.doseMax');
                var un = this.valor('prescricao.unidade');
                var inicio = this.valor('prescricao.dataInicio');

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
                    prescricao: this.prescrever
                        ? this.valor('prescricao.medicamento') + ' ' + (min === max ? min : min + '–' + max) + ' ' + un
                        : 'Sem prescrição',
                    prescricaoLinha: this.prescrever
                        ? this.valor('prescricao.frequenciaDia') + '×/dia · ' + this.valor('prescricao.duracaoDias')
                            + ' dias · a partir de ' + (inicio ? this.dia(inicio) : '?')
                        : '',
                    duracao: this.valor('prescricao.duracaoDias'),
                    regras: this.prescrever ? this.regras.map(function (r) { return self.previa(r); }) : []
                };
            }
        };
    });
});
