# -*- coding: utf-8 -*-
"""
Gera a planilha de prospecção de clientes/parceiros (Terra Nexo).
Setores: Agropecuária, Imobiliárias, Irrigação.
Cidades: Unaí, Paracatu, Formosa, Luziânia, Cristalina, Catalão, João Pinheiro.
"""
import openpyxl
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.worksheet.datavalidation import DataValidation
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.table import Table, TableStyleInfo

OUT_PATH = "Base_Prospeccao_Clientes_Parceiros.xlsx"

CIDADES = [
    "Unaí", "Paracatu", "Formosa", "Luziânia", "Cristalina", "Catalão", "João Pinheiro",
]
SETORES = ["Agropecuária", "Imobiliária", "Irrigação"]
STATUS = [
    "Não iniciado", "Pesquisado", "Contatado", "Respondeu",
    "Reunião marcada", "Proposta enviada", "Convertido", "Sem interesse", "Contato inválido",
]

COLS = [
    ("ID", 6),
    ("Cidade", 14),
    ("Setor", 14),
    ("Nome da Empresa (Razão Social / Fantasia)", 30),
    ("Nome do Titular", 24),
    ("Endereço", 34),
    ("CNPJ", 18),
    ("CPF", 16),
    ("Telefone Fixo", 16),
    ("Telefone Celular", 16),
    ("WhatsApp", 16),
    ("E-mail", 26),
    ("Geolocalização (lat, long ou link Maps)", 30),
    ("Site", 26),
    ("Status do Contato", 18),
    ("Fonte da Informação", 24),
    ("Data da Coleta", 14),
    ("Consentimento LGPD (S/N/NA)", 14),
    ("Observações", 34),
]

NAVY = "1F3B57"
GREEN = "2E7D32"
LIGHT = "EAF1F8"
WHITE = "FFFFFF"

thin = Side(style="thin", color="C9D6E3")
border = Border(left=thin, right=thin, top=thin, bottom=thin)

wb = openpyxl.Workbook()

# ---------------------------------------------------------------- Instruções
ws_i = wb.active
ws_i.title = "Instruções"
ws_i.sheet_view.showGridLines = False
ws_i.column_dimensions["A"].width = 3
ws_i.column_dimensions["B"].width = 100

title_font = Font(name="Calibri", size=16, bold=True, color=WHITE)
h2_font = Font(name="Calibri", size=12, bold=True, color=NAVY)
body_font = Font(name="Calibri", size=11, color="333333")
body_wrap = Alignment(wrap_text=True, vertical="top")

ws_i.merge_cells("B2:B2")
ws_i["B2"] = "Base de Prospecção — Clientes e Parceiros (Terra Nexo)"
ws_i["B2"].font = Font(name="Calibri", size=16, bold=True, color=NAVY)

linhas = [
    "",
    "OBJETIVO",
    "Consolidar contatos de empresas de agropecuária, imobiliárias e empresas de "
    "irrigação nas cidades de Unaí, Paracatu, Formosa, Luziânia, Cristalina, Catalão "
    "e João Pinheiro, para prospecção comercial e de parcerias do Terra Nexo.",
    "",
    "COMO USAR",
    "1. Preencha os registros na aba 'Base_Prospeccao' — uma linha por empresa/contato.",
    "2. Use os menus suspensos (Cidade, Setor, Status do Contato) para manter os dados padronizados.",
    "3. Preencha 'Fonte da Informação' e 'Data da Coleta' sempre — isso dá rastreabilidade e é "
    "exigido para justificar a base legal de tratamento dos dados (LGPD).",
    "4. Acompanhe o volume e o funil de contato na aba 'Painel' (atualiza automaticamente por fórmula).",
    "",
    "FONTES LEGÍTIMAS SUGERIDAS PARA A COLETA",
    "• CNPJ, razão social, endereço e situação cadastral: Consulta pública CNPJ da Receita Federal "
    "(gov.br/receitafederal) ou os dados abertos do CNPJ (dados.gov.br) — informação pública oficial.",
    "• Telefone, site, geolocalização e horário: Google Maps / Google Meu Negócio da própria empresa.",
    "• Imobiliárias: registro de corretores e imobiliárias no CRECI da regional (GO/MG conforme a cidade).",
    "• Agropecuária: sindicatos rurais municipais, CAR/Emater, cooperativas e associações locais "
    "(ex.: cooperativas de produtores, feiras agropecuárias).",
    "• Irrigação: associações do setor (ex. ABID — Associação Brasileira de Irrigação e Drenagem), "
    "distribuidores de equipamentos e distritos de irrigação regionais.",
    "• Site institucional da própria empresa (contato/fale conosco) — fonte preferencial para e-mail e WhatsApp.",
    "",
    "ATENÇÃO — LGPD (Lei 13.709/2018)",
    "CNPJ, razão social e endereço comercial são dados públicos e de baixo risco. Já CPF, telefone "
    "celular pessoal, WhatsApp pessoal e e-mail pessoal do titular são dados pessoais — mesmo em "
    "contexto B2B a coleta e o uso para contato comercial devem se apoiar em uma base legal do "
    "Art. 7º da LGPD (em geral 'legítimo interesse', Art. 7º IX), e a pessoa tem direito a se opor "
    "e a pedir a exclusão dos dados a qualquer momento.",
    "Recomendações práticas: (1) prefira sempre que possível o canal comercial/institucional da "
    "empresa em vez do celular/e-mail pessoal do titular; (2) registre a fonte de cada dado coletado "
    "(coluna 'Fonte da Informação'); (3) ofereça e respeite o opt-out no primeiro contato "
    "(ex.: 'responda SAIR para não receber mais contato'); (4) não compartilhe esta planilha fora da "
    "equipe comercial; (5) evite armazenar CPF sempre que o CNPJ ou o vínculo com a empresa já for "
    "suficiente para a prospecção.",
    "",
    "PRÓXIMOS PASSOS",
    "Estrutura pronta para carga. A pesquisa efetiva de contatos (empresa por empresa, cidade por "
    "cidade) é a próxima etapa — pode ser feita manualmente com as fontes acima ou em lotes "
    "(ex.: começando por uma cidade/setor piloto para validar o formato antes de escalar).",
]

r = 4
for txt in linhas:
    if txt in (
        "OBJETIVO", "COMO USAR", "FONTES LEGÍTIMAS SUGERIDAS PARA A COLETA",
        "ATENÇÃO — LGPD (Lei 13.709/2018)", "PRÓXIMOS PASSOS",
    ):
        ws_i.merge_cells(f"B{r}:B{r}")
        cell = ws_i[f"B{r}"]
        cell.value = txt
        cell.font = h2_font
        r += 1
    else:
        ws_i.merge_cells(f"B{r}:B{r}")
        cell = ws_i[f"B{r}"]
        cell.value = txt
        cell.font = body_font
        cell.alignment = body_wrap
        if txt:
            ws_i.row_dimensions[r].height = 15 * max(1, (len(txt) // 95 + 1))
        r += 1

# ---------------------------------------------------------------- Base_Prospeccao
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

N_ROWS = 300  # linhas pré-formatadas prontas para carga
last_col_letter = get_column_letter(len(COLS))

for row in range(2, N_ROWS + 2):
    for col in range(1, len(COLS) + 1):
        c = ws.cell(row=row, column=col)
        c.border = border
        if row % 2 == 0:
            c.fill = PatternFill("solid", fgColor=LIGHT)
    ws.cell(row=row, column=1, value=f"=IF(D{row}=\"\",\"\",ROW()-1)")

# Tabela do Excel (facilita filtro/ordenação)
tab_ref = f"A1:{last_col_letter}{N_ROWS + 1}"
tabela = Table(displayName="BaseProspeccao", ref=tab_ref)
tabela.tableStyleInfo = TableStyleInfo(
    name="TableStyleMedium2", showFirstColumn=False,
    showLastColumn=False, showRowStripes=False, showColumnStripes=False,
)
ws.add_table(tabela)

# Validações (dropdowns)
dv_cidade = DataValidation(type="list", formula1=f'"{",".join(CIDADES)}"', allow_blank=True)
dv_setor = DataValidation(type="list", formula1=f'"{",".join(SETORES)}"', allow_blank=True)
dv_status = DataValidation(type="list", formula1=f'"{",".join(STATUS)}"', allow_blank=True)
dv_consent = DataValidation(type="list", formula1='"S,N,NA"', allow_blank=True)

for dv in (dv_cidade, dv_setor, dv_status, dv_consent):
    ws.add_data_validation(dv)

dv_cidade.add(f"B2:B{N_ROWS + 1}")
dv_setor.add(f"C2:C{N_ROWS + 1}")
dv_status.add(f"O2:O{N_ROWS + 1}")
dv_consent.add(f"R2:R{N_ROWS + 1}")

# ---------------------------------------------------------------- Painel
ws_p = wb.create_sheet("Painel")
ws_p.sheet_view.showGridLines = False
ws_p.column_dimensions["A"].width = 20
ws_p.column_dimensions["B"].width = 16
ws_p.column_dimensions["C"].width = 16
ws_p.column_dimensions["D"].width = 16
ws_p.column_dimensions["E"].width = 3
ws_p.column_dimensions["F"].width = 20
ws_p.column_dimensions["G"].width = 14

ws_p.merge_cells("A1:D1")
ws_p["A1"] = "Painel — Volume por Cidade x Setor"
ws_p["A1"].font = Font(size=14, bold=True, color=NAVY)

ws_p["A3"] = "Cidade"
ws_p["B3"] = "Agropecuária"
ws_p["C3"] = "Imobiliária"
ws_p["D3"] = "Irrigação"
for col in ("A3", "B3", "C3", "D3"):
    ws_p[col].font = Font(bold=True, color=WHITE)
    ws_p[col].fill = PatternFill("solid", fgColor=NAVY)
    ws_p[col].alignment = Alignment(horizontal="center")

data_range = f"Base_Prospeccao!$B$2:$B${N_ROWS + 1}"
setor_range = f"Base_Prospeccao!$C$2:$C${N_ROWS + 1}"

row = 4
for cidade in CIDADES:
    ws_p.cell(row=row, column=1, value=cidade).font = Font(bold=True)
    for j, setor in enumerate(SETORES, start=2):
        col_letter = get_column_letter(j)
        formula = f'=COUNTIFS({data_range},$A{row},{setor_range},{col_letter}$3)'
        ws_p.cell(row=row, column=j, value=formula).alignment = Alignment(horizontal="center")
    row += 1

ws_p.cell(row=row, column=1, value="TOTAL").font = Font(bold=True)
for j in range(2, 5):
    col_letter = get_column_letter(j)
    ws_p.cell(row=row, column=j, value=f"=SUM({col_letter}4:{col_letter}{row-1})").font = Font(bold=True)

ws_p["F3"] = "Status do Contato"
ws_p["G3"] = "Qtde"
ws_p["F3"].font = Font(bold=True, color=WHITE)
ws_p["G3"].font = Font(bold=True, color=WHITE)
ws_p["F3"].fill = PatternFill("solid", fgColor=NAVY)
ws_p["G3"].fill = PatternFill("solid", fgColor=NAVY)

status_range = f"Base_Prospeccao!$O$2:$O${N_ROWS + 1}"
r2 = 4
for s in STATUS:
    ws_p.cell(row=r2, column=6, value=s)
    ws_p.cell(row=r2, column=7, value=f'=COUNTIF({status_range},F{r2})').alignment = Alignment(horizontal="center")
    r2 += 1

wb.save(OUT_PATH)
print("Gerado:", OUT_PATH)
