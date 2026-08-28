# -*- coding: utf-8 -*-
"""
Gera Base_Prospeccao_Clientes_Parceiros.xlsx a partir do banco normalizado
prospeccao.db. O Excel passa a ser uma VIEW/relatório derivado do banco — não
edite os dados diretamente nele para não perder a normalização; use
add_empresa.py / atualizar_status() (ou SQL direto) e rode este script de novo.
"""
import os
import sqlite3

import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.worksheet.datavalidation import DataValidation
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.table import Table, TableStyleInfo

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "prospeccao.db")
OUT_PATH = os.path.join(os.path.dirname(BASE_DIR), "Base_Prospeccao_Clientes_Parceiros.xlsx")

NAVY = "1F3B57"
GREEN = "2E7D32"
LIGHT = "EAF1F8"
WHITE = "FFFFFF"
thin = Side(style="thin", color="C9D6E3")
border = Border(left=thin, right=thin, top=thin, bottom=thin)

COLS = [
    ("ID", 6), ("Cidade", 14), ("Setor", 16),
    ("Nome da Empresa (Razão Social / Fantasia)", 32), ("Nome do Titular", 26),
    ("Endereço", 36), ("CNPJ", 18), ("CPF", 16), ("Telefone Fixo", 16),
    ("Telefone Celular", 16), ("WhatsApp", 16), ("E-mail", 26),
    ("Geolocalização (link Maps)", 30), ("Site", 26), ("Status do Contato", 18),
    ("Fonte da Informação", 26), ("Data da Coleta", 14),
    ("Consentimento LGPD (S/N/NA)", 14), ("Observações", 40),
]

STATUS = [
    "Não iniciado", "Pesquisado", "Contatado", "Respondeu",
    "Reunião marcada", "Proposta enviada", "Convertido", "Sem interesse", "Contato inválido",
]


def buscar_dados(conn):
    conn.row_factory = sqlite3.Row
    cidades = [r[0] for r in conn.execute("SELECT nome FROM municipio ORDER BY nome").fetchall()]
    setores = [r[0] for r in conn.execute("SELECT nome FROM setor ORDER BY nome").fetchall()]
    empresas = conn.execute("SELECT * FROM vw_empresa_completa ORDER BY cidade, setor, nome_fantasia").fetchall()
    return cidades, setores, empresas


def build_instrucoes(ws):
    ws.sheet_view.showGridLines = False
    ws.column_dimensions["A"].width = 3
    ws.column_dimensions["B"].width = 100
    h1 = Font(name="Calibri", size=16, bold=True, color=NAVY)
    h2 = Font(name="Calibri", size=12, bold=True, color=NAVY)
    body = Font(name="Calibri", size=11, color="333333")
    wrap = Alignment(wrap_text=True, vertical="top")

    ws["B2"] = "Base de Prospecção — Clientes e Parceiros (Terra Nexo)"
    ws["B2"].font = h1

    blocos = [
        ("OBJETIVO",
         "Consolidar contatos de empresas de agropecuária, imobiliárias, irrigação, silos/armazéns, "
         "leilão de animais e laticínios nas cidades de Unaí, Paracatu, Formosa, Luziânia, Cristalina, "
         "Catalão e João Pinheiro, para prospecção comercial e de parcerias do Terra Nexo."),
        ("ARQUITETURA DA BASE (a partir desta versão)",
         "Os dados deixaram de viver só nesta planilha. A fonte de verdade agora é um banco "
         "relacional normalizado em docs/prospeccao/db/prospeccao.db (SQLite), com tabelas: "
         "municipio, setor, empresa, socio (1 empresa pode ter vários sócios), contato (1 empresa "
         "pode ter vários telefones/whatsapp/e-mails), geolocalizacao e status_historico (registra "
         "cada mudança de status ao longo do tempo, não só o status atual). Esta planilha é gerada "
         "a partir do banco pelo script export_excel.py — é um relatório/relatório de trabalho, "
         "não a fonte primária."),
        ("COMO ATUALIZAR A BASE",
         "1. Nunca edite esta planilha manualmente esperando que persista — ela é sobrescrita a "
         "cada exportação.\n"
         "2. Para adicionar uma empresa nova: use a função inserir_empresa() de "
         "docs/prospeccao/db/add_empresa.py (não rode load_data.py de novo — ele apaga e recria "
         "o banco do zero, perdendo o histórico de status já registrado).\n"
         "3. Para mudar o status de um contato (ex.: 'Contatado' → 'Reunião marcada'): use "
         "atualizar_status() do mesmo arquivo — isso atualiza o status atual E grava uma linha em "
         "status_historico, preservando o funil completo.\n"
         "4. Depois de qualquer alteração no banco, rode: python docs/prospeccao/db/export_excel.py "
         "— isso regenera esta planilha (abas Instruções, Base_Prospeccao e Painel) com os dados "
         "atuais.\n"
         "5. Para inspecionar/editar o banco visualmente sem escrever SQL, use o app gratuito "
         "'DB Browser for SQLite' (sqlitebrowser.org) apontando para prospeccao.db."),
        ("INTEGRAÇÃO COM O PROJETO TERRA NEXO",
         "O schema também existe em versão PostgreSQL, pronta para virar migration Flyway: "
         "docs/prospeccao/db/V1__create_prospeccao_schema.sql. Ela cria um schema Postgres "
         "separado ('prospeccao'), isolado das tabelas do catálogo de imóveis (property/lead/"
         "broker/region), para não acoplar prospecção comercial com o domínio público do portal. "
         "Quando o backend adotar Flyway (ver README do projeto, seção 'Próximos passos' — hoje a "
         "persistência ainda é em memória), basta copiar esse arquivo para "
         "backend/src/main/resources/db/migration/ ajustando o número de versão para não colidir "
         "com as migrations que já existirem. A partir daí um módulo Spring (ex.: 'partner' ou "
         "'prospect', seguindo o mesmo padrão domain/application/api/infrastructure dos módulos "
         "existentes) pode ler direto dessas tabelas — por exemplo para um painel interno de "
         "prospecção dentro do /admin, ou para sugerir parceiros por região ao lado de um imóvel "
         "rural publicado."),
        ("FONTES LEGÍTIMAS USADAS E RECOMENDADAS PARA A COLETA",
         "• CNPJ, razão social, endereço, situação cadastral e sócios/administradores (QSA): "
         "consulta pública da Receita Federal — usamos a BrasilAPI (brasilapi.com.br), que "
         "espelha o CNPJ público oficial. É dado público por lei (transparência de registro "
         "empresarial).\n"
         "• Telefone, site, geolocalização e horário: Google Maps / site institucional da própria "
         "empresa.\n"
         "• Imobiliárias: CRECI da regional (GO/MG conforme a cidade).\n"
         "• Agropecuária/silos/armazéns: Agrolink, sindicatos rurais municipais, Emater, "
         "cooperativas locais, e o sistema de armazenagem da CONAB (dados abertos) para mapear "
         "armazéns registrados por município.\n"
         "• Leilão de animais: sites das próprias leiloeiras e diretórios locais (ex. Guia Unaí).\n"
         "• Laticínios: cooperativas regionais (ex. Capul) e Emater/Secretaria de Agricultura "
         "municipal."),
        ("ATENÇÃO — LGPD (Lei 13.709/2018)",
         "CNPJ, razão social, endereço comercial e o nome do sócio-administrador são dado público "
         "(a Receita Federal os divulga oficialmente, inclusive o CPF do sócio já mascarado — é "
         "assim que gravamos aqui, nunca o número completo). Telefone celular, WhatsApp e e-mail "
         "pessoal do titular já são dados pessoais: mesmo em contexto B2B, o uso para contato "
         "comercial deve se apoiar em base legal do Art. 7º da LGPD (em geral 'legítimo interesse', "
         "Art. 7º IX), e a pessoa tem direito a se opor e pedir exclusão a qualquer momento. "
         "Prefira sempre o canal comercial/institucional da empresa; registre a fonte de cada dado "
         "(coluna Fonte da Informação); ofereça opt-out no primeiro contato; não compartilhe esta "
         "base fora da equipe comercial."),
        ("LIMITAÇÕES CONHECIDAS DESTA CARGA",
         "Unaí está com cobertura inicial nos 6 segmentos (14 empresas), priorizando fontes "
         "públicas verificáveis — não é exaustivo. Alguns registros ficaram sem CNPJ/titular "
         "porque não foram encontrados nas fontes públicas consultadas (ver coluna Observações "
         "de cada linha). O segmento Silos/Armazéns está com cobertura fraca em Unaí: CASEMG e "
         "Coanor apareceram com registro BAIXADA/em liquidação na Receita e não foram incluídas. "
         "As outras 6 cidades (Paracatu, Formosa, Luziânia, Cristalina, Catalão, João Pinheiro) "
         "ainda não têm pesquisa — os municípios e setores já estão cadastrados no banco, prontos "
         "para receber empresas."),
    ]

    r = 4
    for titulo, texto in blocos:
        ws.merge_cells(f"B{r}:B{r}")
        c = ws[f"B{r}"]
        c.value = titulo
        c.font = h2
        r += 1
        ws.merge_cells(f"B{r}:B{r}")
        c = ws[f"B{r}"]
        c.value = texto
        c.font = body
        c.alignment = wrap
        linhas_estimadas = max(1, len(texto) // 95 + texto.count("\n") + 1)
        ws.row_dimensions[r].height = 15 * linhas_estimadas
        r += 2


def build_base(wb, cidades, setores, empresas):
    ws = wb.create_sheet("Base_Prospeccao")
    ws.sheet_view.showGridLines = False
    header_fill = PatternFill("solid", fgColor=NAVY)
    header_font = Font(name="Calibri", size=11, bold=True, color=WHITE)

    for idx, (name, width) in enumerate(COLS, start=1):
        col_letter = get_column_letter(idx)
        ws.column_dimensions[col_letter].width = width
        cell = ws.cell(row=1, column=idx, value=name)
        cell.font = header_font
        cell.fill = header_fill
        cell.alignment = Alignment(wrap_text=True, vertical="center", horizontal="center")
        cell.border = border

    ws.freeze_panes = "A2"
    ws.row_dimensions[1].height = 34

    n_data_rows = len(empresas)
    n_rows = max(n_data_rows, 50) + 50  # linhas extras em branco, já formatadas, para novas cargas manuais

    for i in range(n_rows):
        row = i + 2
        emp = empresas[i] if i < n_data_rows else None
        values = [
            emp["id"] if emp else None,
            emp["cidade"] if emp else None,
            emp["setor"] if emp else None,
            emp["nome_fantasia"] if emp else None,
            emp["titular_principal"] if emp else None,
            emp["endereco_completo"] if emp else None,
            emp["cnpj"] if emp else None,
            emp["cpf_titular_principal"] if emp else None,
            emp["telefone_fixo"] if emp else None,
            emp["telefone_celular"] if emp else None,
            emp["whatsapp"] if emp else None,
            emp["email"] if emp else None,
            emp["geolocalizacao"] if emp else None,
            emp["site"] if emp else None,
            emp["status_prospeccao"] if emp else None,
            emp["fonte_informacao"] if emp else None,
            emp["data_coleta"] if emp else None,
            "NA" if emp else None,
            emp["observacoes"] if emp else None,
        ]
        for col, val in enumerate(values, start=1):
            c = ws.cell(row=row, column=col, value=val)
            c.border = border
            if row % 2 == 0:
                c.fill = PatternFill("solid", fgColor=LIGHT)

    last_col_letter = get_column_letter(len(COLS))
    tabela = Table(displayName="BaseProspeccao", ref=f"A1:{last_col_letter}{n_rows + 1}")
    tabela.tableStyleInfo = TableStyleInfo(
        name="TableStyleMedium2", showFirstColumn=False,
        showLastColumn=False, showRowStripes=False, showColumnStripes=False,
    )
    ws.add_table(tabela)

    dv_cidade = DataValidation(type="list", formula1=f'"{",".join(cidades)}"', allow_blank=True)
    dv_setor = DataValidation(type="list", formula1=f'"{",".join(setores)}"', allow_blank=True)
    dv_status = DataValidation(type="list", formula1=f'"{",".join(STATUS)}"', allow_blank=True)
    dv_consent = DataValidation(type="list", formula1='"S,N,NA"', allow_blank=True)
    for dv in (dv_cidade, dv_setor, dv_status, dv_consent):
        ws.add_data_validation(dv)
    dv_cidade.add(f"B2:B{n_rows + 1}")
    dv_setor.add(f"C2:C{n_rows + 1}")
    dv_status.add(f"O2:O{n_rows + 1}")
    dv_consent.add(f"R2:R{n_rows + 1}")

    return ws, n_rows


def build_painel(wb, cidades, setores, n_rows):
    ws = wb.create_sheet("Painel")
    ws.sheet_view.showGridLines = False
    ws.column_dimensions["A"].width = 20
    for col in "BCDEFGH":
        ws.column_dimensions[col].width = 15

    ws.merge_cells("A1:H1")
    ws["A1"] = "Painel — Volume por Cidade x Setor"
    ws["A1"].font = Font(size=14, bold=True, color=NAVY)

    ws.cell(row=3, column=1, value="Cidade").font = Font(bold=True, color=WHITE)
    ws.cell(row=3, column=1).fill = PatternFill("solid", fgColor=NAVY)
    for j, setor in enumerate(setores, start=2):
        c = ws.cell(row=3, column=j, value=setor)
        c.font = Font(bold=True, color=WHITE)
        c.fill = PatternFill("solid", fgColor=NAVY)
        c.alignment = Alignment(horizontal="center", wrap_text=True)

    data_range = f"Base_Prospeccao!$B$2:$B${n_rows + 1}"
    setor_range = f"Base_Prospeccao!$C$2:$C${n_rows + 1}"
    row = 4
    for cidade in cidades:
        ws.cell(row=row, column=1, value=cidade).font = Font(bold=True)
        for j, setor in enumerate(setores, start=2):
            col_letter = get_column_letter(j)
            formula = f'=COUNTIFS({data_range},$A{row},{setor_range},{col_letter}$3)'
            ws.cell(row=row, column=j, value=formula).alignment = Alignment(horizontal="center")
        row += 1

    ws.cell(row=row, column=1, value="TOTAL").font = Font(bold=True)
    for j in range(2, len(setores) + 2):
        col_letter = get_column_letter(j)
        ws.cell(row=row, column=j, value=f"=SUM({col_letter}4:{col_letter}{row - 1})").font = Font(bold=True)

    col_status = len(setores) + 3
    letter_status = get_column_letter(col_status)
    letter_qtd = get_column_letter(col_status + 1)
    ws.column_dimensions[letter_status].width = 20
    ws.column_dimensions[letter_qtd].width = 10
    ws[f"{letter_status}3"] = "Status do Contato"
    ws[f"{letter_qtd}3"] = "Qtde"
    for cell in (f"{letter_status}3", f"{letter_qtd}3"):
        ws[cell].font = Font(bold=True, color=WHITE)
        ws[cell].fill = PatternFill("solid", fgColor=NAVY)

    status_range = f"Base_Prospeccao!$O$2:$O${n_rows + 1}"
    r2 = 4
    for s in STATUS:
        ws.cell(row=r2, column=col_status, value=s)
        formula = f'=COUNTIF({status_range},{letter_status}{r2})'
        ws.cell(row=r2, column=col_status + 1, value=formula).alignment = Alignment(horizontal="center")
        r2 += 1


def main():
    conn = sqlite3.connect(DB_PATH)
    cidades, setores, empresas = buscar_dados(conn)
    conn.close()

    wb = openpyxl.Workbook()
    ws_i = wb.active
    ws_i.title = "Instruções"
    build_instrucoes(ws_i)

    ws_base, n_rows = build_base(wb, cidades, setores, empresas)
    build_painel(wb, cidades, setores, n_rows)

    wb.save(OUT_PATH)
    print(f"Planilha gerada em {OUT_PATH} a partir de {len(empresas)} empresas no banco.")


if __name__ == "__main__":
    main()
