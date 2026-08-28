# -*- coding: utf-8 -*-
"""Carga inicial de João Pinheiro-MG."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    dict(municipio="João Pinheiro", setor="Agropecuária",
         nome_fantasia="Agrominas Agropecuária Minas", razao_social="Agrominas Agropecuária Minas LTDA",
         cnpj="71.492.664/0001-63", situacao="ATIVA",
         logradouro="Rua Aparício Saraiva", numero="182", complemento=None,
         bairro="Centro", cep="38770-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CNAE oficial: comércio varejista de medicamentos veterinários (agropecuária/veterinária).",
         socios=[("Celuta Batista Horta", "***.371.366-**", "Sócio-Administrador", 1),
                 ("Pricila Batista Horta", "***.557.726-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_fixo", "(38) 3561-1387", 1)]),
    dict(municipio="João Pinheiro", setor="Agropecuária",
         nome_fantasia="Coopatos - Loja Agropecuária João Pinheiro",
         razao_social="Cooperativa Mista Agropecuária de Patos de Minas LTDA (filial João Pinheiro)",
         cnpj="23.338.189/0004-75", situacao="ATIVA",
         logradouro="Rua Aparício Saraiva", numero="119", complemento=None,
         bairro="Centro", cep="38770-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Filial em João Pinheiro de cooperativa sediada em Patos de Minas-MG.",
         socios=[("Celio Humberto Rodrigues", "***.799.746-**", "Presidente", 1)],
         contatos=[("telefone_fixo", "(34) 3818-8000", 1)]),

    dict(municipio="João Pinheiro", setor="Imobiliária",
         nome_fantasia="Imperial Imóveis", razao_social="Imperial Imobiliária LTDA",
         cnpj="08.228.676/0001-49", situacao="ATIVA",
         logradouro="Rua Antônio Carlos", numero="383", complemento=None,
         bairro="Centro", cep="38770-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CRECI ME-PJMG0005662.",
         socios=[("Luiz Tomas Almeida", "***.222.076-**", "Sócio-Administrador", 1),
                 ("Valdemir Antonio da Silva", "***.250.394-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_fixo", "(38) 3561-3866", 1), ("telefone_celular", "(38) 99972-2262", 1)]),
    dict(municipio="João Pinheiro", setor="Imobiliária",
         nome_fantasia="Caldeira Corretor de Imóveis", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Rua Geraldo Rios", numero="163", complemento=None,
         bairro=None, cep="38770-000", site=None,
         fonte="Diretório local (guiaimobiliarias.com)", data_coleta="2026-08-26",
         observacoes="CNPJ não localizado nas fontes públicas consultadas — confirmar em contato direto.",
         socios=[], contatos=[]),
    dict(municipio="João Pinheiro", setor="Imobiliária",
         nome_fantasia="Edson Araújo Corretor", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Rua Adelmo Xavier", numero="203", complemento=None,
         bairro=None, cep=None, site="https://www.edsonaraujocorretor.com.br",
         fonte="Site oficial + busca web", data_coleta="2026-08-26",
         observacoes="CRECI-MG 59138 (corretor autônomo, não é sociedade com CNPJ próprio confirmado).",
         socios=[("Edson Araújo", None, "Corretor (CRECI-MG 59138)", 1)],
         contatos=[("telefone_celular", "(38) 99755-1010", 1)]),

    dict(municipio="João Pinheiro", setor="Irrigação",
         nome_fantasia="Agrimaq - Equipamentos Irrigação", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Avenida José Rabelo de Souza", numero="688", complemento=None,
         bairro=None, cep="38770-000", site=None,
         fonte="Diretório local + busca web", data_coleta="2026-08-26",
         observacoes="CNPJ não localizado nas fontes públicas consultadas — confirmar em contato direto. "
                      "Ideal Irrigação (já cadastrada em Unaí) mantém apenas ponto de atendimento em João "
                      "Pinheiro, sem CNPJ próprio na cidade — não duplicado aqui.",
         socios=[], contatos=[("telefone_fixo", "(38) 3561-1640", 1)]),

    dict(municipio="João Pinheiro", setor="Silos/Armazéns",
         nome_fantasia="Progresso Armazenamento e Comércio de Cereais",
         razao_social="Progresso Armazenamento e Comércio de Cereais LTDA",
         cnpj="47.198.029/0001-93", situacao="ATIVA",
         logradouro="Rodovia MG-181, Km 172", numero="S/N", complemento="Fazenda Anjo da Nascente",
         bairro="Zona Rural", cep="38770-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Armazém de grãos de maior capacidade/tecnologia da região, inaugurado em 2022 por "
                      "3 produtores rurais. CNAE oficial: 'Armazéns gerais - emissão de warrant' — ótimo fit.",
         socios=[("Rafael Furtado da Silveira", "***.961.476-**", "Sócio-Administrador", 1),
                 ("Francisco Glycerio de Freitas", "***.798.558-**", "Sócio-Administrador", 0),
                 ("Jaqueline Oliveira Lemos", "***.199.036-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_celular", "(38) 9947-3058", 1), ("email", "progressocereais@gmail.com", 1)]),

    dict(municipio="João Pinheiro", setor="Laticínios",
         nome_fantasia="Laticínios Noroeste", razao_social="Laticínios Noroeste LTDA",
         cnpj="65.377.244/0001-33", situacao="ATIVA",
         logradouro="Rua Wilson de Oliveira", numero="37-A", complemento=None,
         bairro="Centro", cep="38770-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Fundada em 1991. CNAE oficial: fabricação de laticínios.",
         socios=[("Joao Carlos de Moura Couto", "***.829.996-**", "Sócio-Administrador", 1),
                 ("Joao Gabriel de Moura Couto", "***.232.386-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_fixo", "(38) 3561-1224", 1)]),
]

for reg in REGISTROS:
    inserir_empresa(conn, reg)

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
n = conn.execute(
    "SELECT COUNT(*) FROM empresa WHERE municipio_id = (SELECT id FROM municipio WHERE nome='João Pinheiro')"
).fetchone()[0]
print(f"João Pinheiro: {n} empresas inseridas nesta carga. Total no banco: {total}.")
print("Segmento sem registro em João Pinheiro: Leilão de Animais (as duas leiloeiras locais encontradas, "
      "João Pinheiro Leilões & Promoções e J.P. Leilões, estão INAPTA/BAIXADA na Receita; os leilões de gado "
      "atuais do Sindicato Rural são realizados via plataforma MCN Leilões, sem empresa leiloeira local ativa "
      "confirmada).")
conn.close()
