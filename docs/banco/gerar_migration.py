"""Gera as migrations Flyway do Java a partir dos blocos desta pasta.

O que vai para o professor (docs/banco) e o que o Java instala no banco
precisam ser o mesmo código. Mudou um bloco aqui? Rode de novo:

    python docs/banco/gerar_migration.py

V9 (tipos, pacotes, trigger e T_PB_AUDITORIA) JÁ RODOU em produção e está
CONGELADA: regerar mudaria o checksum, e o Flyway recusaria a subida do Java.
Por isso este script não a toca mais.

V10 recompila só o corpo do PKG_PB_JSON, tirado do 05_pacotes.sql: a correção
do número inteiro no JSON (4 saía "4."). Ela também ainda pode ser regerada
enquanto não tiver rodado em nenhum banco; depois disso, mudança nova vira V11.
"""
from pathlib import Path

PASTA = Path(__file__).resolve().parent
MIGRATIONS = PASTA.parents[1] / "src/main/resources/db/migration"
DESTINO_V10 = MIGRATIONS / "V10__corrige_numero_inteiro_no_json.sql"

INICIO_CORPO_JSON = "CREATE OR REPLACE PACKAGE BODY PKG_PB_JSON AS"
FIM_CORPO_JSON = "END PKG_PB_JSON;\n/"

CABECALHO_V10 = """\
-- =============================================================================
-- V10 — número inteiro no JSON sem o ponto sobrando
--
-- GERADO por docs/banco/gerar_migration.py a partir do corpo do PKG_PB_JSON em
-- docs/banco/05_pacotes.sql. Não edite aqui: edite lá e gere de novo.
--
-- A Função 1 (MONTA_JSON) formatava número com TO_CHAR(..., 'FM...0.999'), que
-- deixa o ponto decimal em número inteiro: "planoId":4. — JSON inválido, que o
-- mongoimport recusa. O RTRIM tira esse ponto; decimal (3.5) não muda.
--
-- Só o corpo do pacote é recompilado: a especificação, os tipos e a trigger da
-- V9 ficam como estão, e quem usa o pacote não precisa recompilar.
-- =============================================================================

"""


def corpo_do_pkg_pb_json() -> str:
    texto = (PASTA / "05_pacotes.sql").read_text(encoding="utf-8").replace("\r\n", "\n")
    inicio = texto.index(INICIO_CORPO_JSON)
    fim = texto.index(FIM_CORPO_JSON, inicio) + len(FIM_CORPO_JSON)
    return texto[inicio:fim]


def main() -> None:
    DESTINO_V10.write_text(CABECALHO_V10 + corpo_do_pkg_pb_json() + "\n", encoding="utf-8", newline="\n")
    print(f"gerado: {DESTINO_V10}")


if __name__ == "__main__":
    main()
