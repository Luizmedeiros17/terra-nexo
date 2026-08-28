# -*- coding: utf-8 -*-
"""Carrega o piloto de Unaí (agropecuária, imobiliária, irrigação) na base existente."""
import urllib.parse
import openpyxl

PATH = "Base_Prospeccao_Clientes_Parceiros.xlsx"
DATA_COLETA = "26/08/2026"

def maps_link(endereco):
    return "https://www.google.com/maps/search/?api=1&query=" + urllib.parse.quote(endereco + ", Unaí - MG")

# (Cidade, Setor, Empresa, Titular, Endereço, CNPJ, CPF, TelFixo, TelCel, WhatsApp,
#  Email, Site, Fonte, Observações)
REGISTROS = [
    ("Unaí", "Agropecuária", "União Agropecuária e Transporte LTDA", "Raimundo Sauer (Sócio-Administrador)",
     "Rua Natal Justino da Costa, 805, Centro, Unaí-MG, 38610-044", "19.328.529/0001-67", "***.072.726-**",
     "(38) 3676-2689", "", "",
     "", "",
     "Receita Federal (CNPJ público via BrasilAPI) + Agrolink",
     "CPF mascarado conforme divulgação oficial da Receita Federal (QSA) — não coletado na íntegra."),

    ("Unaí", "Agropecuária", "FJA. Agronegócio Unaí LTDA", "Adenivaldo Oliveira Xavier (Sócio-Administrador)",
     "Rodovia Frei Jorge, 1310, De Lourdes, Unaí-MG, 38616-470", "48.510.635/0001-65", "***.135.716-**",
     "(38) 8832-2555", "", "",
     "", "",
     "Receita Federal (CNPJ público via BrasilAPI) + Agrolink",
     "Comércio atacadista de máquinas e equipamentos agropecuários."),

    ("Unaí", "Agropecuária", "Agrorural Comércio e Representação LTDA", "Afranio Queiroz Neto (Administrador)",
     "Avenida Central, 138, Parque Explanada, Unaí-MG, 38620-115", "49.573.204/0001-00", "***.969.846-**",
     "", "(38) 9970-0717", "",
     "", "",
     "Receita Federal (CNPJ público via BrasilAPI) + Agrolink",
     "Comércio atacadista de defensivos agrícolas, adubos e fertilizantes."),

    ("Unaí", "Agropecuária", "Mundo Agropecuário", "Não identificado (CNPJ não confirmado)",
     "Avenida Governador Valadares, 116, Unaí-MG", "", "",
     "(38) 3677-4331", "", "(38) 99970-6030",
     "", "",
     "Guia Unaí (guiaunai.com.br)",
     "CNPJ e titular não localizados nas fontes públicas consultadas — confirmar em contato direto."),

    ("Unaí", "Imobiliária", "Alfa Nicolau Imóveis LTDA", "Renato Nicolau Ribeiro (Sócio-Administrador)",
     "Rua Djalma Torres, 351, Centro, Unaí-MG, 38610-036 (ponto comercial; endereço fiscal Receita: Av. Princesa Isabel, 531, Jardim)",
     "14.927.825/0001-60", "***.195.336-**",
     "(38) 3676-7636", "", "(38) 98849-2919",
     "", "https://alfanicolau.com.br",
     "Receita Federal (BrasilAPI) + site oficial",
     "CRECI J 4188."),

    ("Unaí", "Imobiliária", "Novo Lar Imobiliária LTDA", "Alisson Morais Carvalho (Sócio-Administrador)",
     "Rua Buritis, 314, Loja 2, Centro, Unaí-MG, 38610-049", "33.625.816/0001-60", "***.758.986-**",
     "(38) 3676-3243", "", "(38) 99879-9441",
     "novolarunai@gmail.com", "https://novolarimobiliariaunai.com.br",
     "Receita Federal (BrasilAPI) + site oficial",
     "CRECI MGJ0006148. Site lista WhatsApp separados por setor (vendas/locação/financeiro)."),

    ("Unaí", "Imobiliária", "Prime Imóveis Unaí LTDA", "José Lindomar Coelho e Ronie José de Carvalho (Sócios-Administradores)",
     "Rua Celina Lisboa Frederico, 69, Sala 01, Centro, Unaí-MG, 38610-041", "14.460.843/0001-85", "***.605.706-**",
     "(38) 3677-6070", "", "(38) 99970-6070",
     "primeimoveisunai@gmail.com", "https://primeimoveisunai.com.br",
     "Receita Federal (BrasilAPI) + site oficial",
     "CRECI MGJ0006090. 4 sócios no QSA; listado o sócio-administrador principal."),

    ("Unaí", "Imobiliária", "Companhia Imobiliária (CIB Companhia de Infraestrutura Brasileira LTDA)",
     "Gualter Moura Filho (Sócio-Administrador)",
     "Rua Djalma Torres, 557, Centro, Unaí-MG, 38610-036", "36.770.097/0001-69", "***.381.461-**",
     "(38) 3676-6772", "", "",
     "cia.imobiliaria@hotmail.com", "",
     "Receita Federal (BrasilAPI) + busca web",
     "E-mail encontrado em diretório de terceiros, não confirmado no site oficial."),

    ("Unaí", "Irrigação", "Irricampo Unaí – Sistemas de Irrigação", "Não identificado (CNPJ não localizado)",
     "Avenida Governador Valadares, 100, Unaí-MG", "", "",
     "(38) 3677-6200", "", "(38) 99913-6200",
     "", "https://www.irricampocristalina.com.br",
     "Guia Unaí (guiaunai.com.br)",
     "Site institucional aponta para domínio de Cristalina — confirmar se é filial/mesma rede."),

    ("Unaí", "Irrigação", "Pivot Equipamentos Agrícolas e Irrigação S.A. (filial Unaí)",
     "Jorge Luiz de Almeida Campos (Presidente)",
     "Rua Porangatu, 314, Kamayurá, Unaí-MG, 38616-222", "33.302.019/0013-85", "***.916.321-**",
     "(62) 3293-0496", "", "",
     "", "",
     "Receita Federal (BrasilAPI) + Econodata",
     "Filial (matriz fora de MG); telefone da Receita tem DDD 62 (central da matriz)."),

    ("Unaí", "Irrigação", "Ideal Irrigação", "Não identificado (endereço/CNPJ não localizados)",
     "Unaí-MG (endereço físico não disponível publicamente)", "", "",
     "(38) 3677-5900", "", "",
     "ideal@idealirrigacao.com.br", "https://www.projetospremium.com.br/idealirrigacao",
     "Site oficial",
     "Atua também em Lagamar, Buritizeiro, Pirapora e Várzea da Palma-MG."),
]

wb = openpyxl.load_workbook(PATH)
ws = wb["Base_Prospeccao"]

start_row = 2
for i, rec in enumerate(REGISTROS):
    (cidade, setor, empresa, titular, endereco, cnpj, cpf, telfixo, telcel, whats,
     email, site, fonte, obs) = rec
    row = start_row + i
    ws.cell(row=row, column=2, value=cidade)
    ws.cell(row=row, column=3, value=setor)
    ws.cell(row=row, column=4, value=empresa)
    ws.cell(row=row, column=5, value=titular)
    ws.cell(row=row, column=6, value=endereco)
    ws.cell(row=row, column=7, value=cnpj)
    ws.cell(row=row, column=8, value=cpf)
    ws.cell(row=row, column=9, value=telfixo)
    ws.cell(row=row, column=10, value=telcel)
    ws.cell(row=row, column=11, value=whats)
    ws.cell(row=row, column=12, value=email)
    ws.cell(row=row, column=13, value=maps_link(endereco))
    ws.cell(row=row, column=14, value=site)
    ws.cell(row=row, column=15, value="Pesquisado")
    ws.cell(row=row, column=16, value=fonte)
    ws.cell(row=row, column=17, value=DATA_COLETA)
    ws.cell(row=row, column=18, value="NA")
    ws.cell(row=row, column=19, value=obs)

wb.save(PATH)
print(f"Carregados {len(REGISTROS)} registros do piloto Unaí.")
