# -*- coding: utf-8 -*-
"""Carga inicial de Paracatu-MG — mesmos 6 segmentos, mesmo método de Unaí
(diretórios agregadores + confirmação individual via BrasilAPI/Receita Federal)."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    # ---------------- Agropecuária ----------------
    dict(
        municipio="Paracatu", setor="Agropecuária",
        nome_fantasia="Agripar", razao_social="Agripar Comercial LTDA",
        cnpj="06.003.006/0001-07", situacao="ATIVA",
        logradouro="Rodovia MG 188, Km 158", numero="S/N", complemento="Distrito Industrial",
        bairro="Unaí (trecho rural)", cep="38609-899", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE oficial: moagem/fabricação de produtos de origem vegetal; opera como comércio atacadista de alimentos/insumos na praça.",
        socios=[("Jose Carlos Lana Pereira", "***.821.356-**", "Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3672-1458", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Agropecuária",
        nome_fantasia="Agro-Paracatu Comércio e Representação", razao_social="Agro-Paracatu Comércio e Representação LTDA",
        cnpj="25.224.925/0001-47", situacao="ATIVA",
        logradouro="Rodovia Presidente Juscelino Kubitschek", numero="1400", complemento=None,
        bairro="Jardim Serrano", cep="38606-180", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="Comércio atacadista de defensivos agrícolas, adubos e fertilizantes. Razão social inclui 'em recuperação judicial' — situação cadastral está ATIVA, mas é sinal de risco de crédito a avaliar antes de negociar prazos. Possui filial mais recente (2023) na Rua Alirio Herval, 32, mesmo bairro.",
        socios=[("Galba Vieira Cordeiro Junior", "***.582.786-**", "Sócio", 1),
                ("Crystina Novais Cordeiro", "***.112.026-**", "Sócio", 0),
                ("Rafael Augusto Neiva e Lemos", "***.509.846-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(38) 3671-1620", 1)],
    ),
    # ---------------- Imobiliária ----------------
    dict(
        municipio="Paracatu", setor="Imobiliária",
        nome_fantasia="Morado Imóveis", razao_social="Morado e Borges Imóveis LTDA",
        cnpj="48.697.018/0001-10", situacao="ATIVA",
        logradouro="Avenida São João Paulo II", numero="1985", complemento=None,
        bairro="Paracatuzinho", cep="38603-401", site="https://moradoimoveis.com.br",
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CRECI 7842-J.",
        socios=[("Jose Humberto Borges", "***.270.716-**", "Sócio-Administrador", 1),
                ("Daniel Bontempo Albino", "***.492.526-**", "Administrador", 0)],
        contatos=[("telefone_fixo", "(38) 3672-3068", 1), ("telefone_celular", "(34) 99140-4665", 1),
                  ("email", "moradoimoveis@gmail.com", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Imobiliária",
        nome_fantasia="Lene Imobiliária", razao_social="Lene Empreendimentos Imobiliários LTDA",
        cnpj="65.384.422/0001-53", situacao="ATIVA",
        logradouro="Rua Goiás", numero="394", complemento="Sala 01",
        bairro="Centro", cep="38600-001", site="https://leneimobiliaria.com.br",
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CRECI PJ 2305. Empresa fundada em 1991, uma das mais antigas da praça.",
        socios=[("Humberto Euler da Silva Neiva", "***.010.011-**", "Sócio-Administrador", 1),
                ("Lucas Lelis Neiva", "***.116.556-**", "Sócio", 0),
                ("Susane Aparecida Sabino Neiva", "***.405.806-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(38) 3671-5050", 1), ("email", "imobiliaria@lene.com.br", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Imobiliária",
        nome_fantasia="AssIMOB", razao_social="Imobiliária AssIMOB LTDA",
        cnpj="10.866.123/0001-63", situacao="ATIVA",
        logradouro="Rua Deputado Paulo Camilo Pena", numero="109", complemento=None,
        bairro="Centro", cep="38600-220", site="https://www.assimob.com",
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CRECI 3725 PJ.",
        socios=[],
        contatos=[("telefone_fixo", "(38) 3671-3900", 1), ("email", "assimobptu@gmail.com", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Imobiliária",
        nome_fantasia="Área 38 Imobiliária", razao_social="Área 38 Imobiliária LTDA",
        cnpj="22.072.353/0001-30", situacao="ATIVA",
        logradouro="Rua Doutor Sergio Ulhoa", numero="39", complemento=None,
        bairro="Centro", cep="38600-108", site="https://area38.com.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes="Atua também com imóveis rurais e lançamentos, além de locação/venda urbana.",
        socios=[("Felipe Lepesqueur Lamas", "***.277.976-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3671-0038", 1), ("email", "contato@area38.com.br", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Imobiliária",
        nome_fantasia="MM Imobiliária", razao_social=None,
        cnpj=None, situacao=None,
        logradouro="Rua Augusto Ferreira Albernaz", numero="56", complemento="Sala 64",
        bairro=None, cep="38600-000", site=None,
        fonte="Busca web (GuiaImobiliarias)", data_coleta="2026-08-26",
        observacoes="CNPJ e titular não localizados nas fontes públicas consultadas — confirmar em contato direto.",
        socios=[],
        contatos=[],
    ),
    # ---------------- Irrigação ----------------
    dict(
        municipio="Paracatu", setor="Irrigação",
        nome_fantasia="Pivot Equipamentos Agrícolas e Irrigação (Paracatu)",
        razao_social="Pivot Equipamentos Agrícolas e Irrigação S.A.",
        cnpj="33.302.019/0004-94", situacao="ATIVA",
        logradouro="BR-040", numero="1497", complemento="Km 43,5",
        bairro="Nossa Senhora de Fátima", cep="38600-001", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="Estabelecimento fundado em 1996 — mais antigo que a filial de Unaí (2001); mesma diretoria (grupo Pivot).",
        socios=[("Jorge Luiz de Almeida Campos", "***.916.321-**", "Presidente", 1),
                ("Fabio Euclides Borgonhone", "***.895.119-**", "Diretor", 0),
                ("João Carlos Obici", "***.146.449-**", "Diretor", 0)],
        contatos=[("telefone_fixo", "(38) 3671-3155", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Irrigação",
        nome_fantasia="Irrivale Tecnologia de Irrigação", razao_social="Irrivale Irrigação LTDA",
        cnpj="33.510.127/0001-00", situacao="ATIVA",
        logradouro="Rua Marginal", numero="940", complemento=None,
        bairro="Primavera", cep="38606-261", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes=None,
        socios=[("Hermes Bomfim Neto", "***.060.335-**", "Sócio-Administrador", 1),
                ("Sergio Prates Zaggo", "***.480.427-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(34) 3821-4831", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Irrigação",
        nome_fantasia="Meribá Irrigação, Comércio e Representações", razao_social="Meribá Irrigação & Comércio LTDA",
        cnpj="05.520.717/0001-88", situacao="ATIVA",
        logradouro="Rua Luiz Gouveia Damasceno", numero="133", complemento=None,
        bairro="Nossa Senhora de Fátima", cep="38600-001", site="https://meribairrigacao.com.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes="CNAE oficial é comércio varejista genérico — negócio se apresenta comercialmente como especialista em irrigação.",
        socios=[("Bianca Tereza Pires", "***.620.998-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3671-2848", 1)],
    ),
    # ---------------- Leilão de Animais ----------------
    dict(
        municipio="Paracatu", setor="Leilão de Animais",
        nome_fantasia="Paracatu Leilões", razao_social="Paracatu Leilões LTDA",
        cnpj="10.741.807/0001-39", situacao="ATIVA",
        logradouro="Rodovia BR 040, Km 45,5", numero="S/N", complemento=None,
        bairro="Santana", cep="38600-001", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE oficial: organização de feiras/eventos — atua comercialmente como leiloeira tradicional de gado de corte na região.",
        socios=[],
        contatos=[],
    ),
    # ---------------- Laticínios ----------------
    dict(
        municipio="Paracatu", setor="Laticínios",
        nome_fantasia="Coopervap", razao_social="Cooperativa Agro Pecuária do Vale do Paracatu LTDA",
        cnpj="23.153.943/0009-07", situacao="ATIVA",
        logradouro="Rua Alirio Rocha", numero="700", complemento=None,
        bairro="Vila Alvorada", cep="38606-454", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE: fabricação de laticínios. Cooperativa mais antiga do setor em Paracatu (atividade original de 1984).",
        socios=[("Icaro Brochado Botelho", "***.054.646-**", "Diretor", 1),
                ("Carlos Eduardo Borges da Silva", "***.212.596-**", "Diretor", 0),
                ("Henrique Ulhoa Pimentel", "***.660.606-**", "Diretor", 0)],
        contatos=[("telefone_fixo", "(38) 3679-8912", 1)],
    ),
    dict(
        municipio="Paracatu", setor="Laticínios",
        nome_fantasia="Coopernossa", razao_social="Cooperativa de Produtores Rurais de Paracatu LTDA",
        cnpj="22.769.008/0001-50", situacao="ATIVA",
        logradouro="Rua Gastão Lepesqueur", numero="2", complemento=None,
        bairro="Alto do Córrego", cep="38606-004", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE: comércio atacadista de leite e laticínios.",
        socios=[("Jose Maria Andrade Porto", "***.863.746-**", "Presidente", 1),
                ("Donizeti Luiz Moreira", "***.576.766-**", "Administrador", 0),
                ("Jose Joaquim Antunes dos Reis", "***.987.016-**", "Administrador", 0)],
        contatos=[("telefone_fixo", "(38) 9908-4433", 1)],
    ),
]

for emp in REGISTROS:
    inserir_empresa(conn, emp, uf_municipio="MG")

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
print(f"Paracatu lote 1 inserido: {len(REGISTROS)} empresas. Total no banco: {total}.")
conn.close()
