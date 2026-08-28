# -*- coding: utf-8 -*-
"""Carga incremental (lote 2) — cobertura mais exaustiva de Unaí, via diretórios
(Agrolink regional, buscas por CNAE) além da varredura inicial por palavra-chave."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="Sementes Mineirão", razao_social="Sementes Mineirão LTDA",
        cnpj="23.369.801/0003-96", situacao="ATIVA",
        logradouro="Rua Sabará", numero="153", complemento=None,
        bairro="Cruzeiro", cep="38616-018", site="https://www.sementesmineirao.com.br",
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="Maior área de produção de sementes de forrageiras tropicais do mundo (~18.000 ha); parceria histórica com a Embrapa Cerrados.",
        socios=[("Orlando Carlos Martins", "***.519.099-**", "Sócio-Administrador", 1),
                ("Orlando Pulici Martins", "***.970.979-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(38) 3676-0101", 1)],
    ),
    dict(
        municipio="Unaí", setor="Silos/Armazéns",
        nome_fantasia="Coagril", razao_social="Cooperativa Agrícola de Unaí LTDA",
        cnpj="20.499.489/0001-03", situacao="ATIVA",
        logradouro="Rodovia Frei Jorge", numero="8000", complemento=None,
        bairro="Tamboril", cep="38620-900", site="https://www.coagril.coop.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes="Cooperativa fundada em 1985 por 54 produtores para viabilizar armazenagem e comercialização de grãos na região de Unaí — uma das maiores cooperativas agrícolas de MG.",
        socios=[("Danilo Julio Gatto", "***.513.276-**", "Presidente", 1),
                ("Bruno Brunner Santos", "***.160.916-**", "Diretor", 0),
                ("Egon Otto Rehn Junior", "***.121.206-**", "Diretor", 0)],
        contatos=[("telefone_fixo", "(38) 2102-2249", 1), ("whatsapp", "(38) 99970-6030", 1)],
    ),
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="Charrua (Taruma Agrocomercial)", razao_social="Taruma Agrocomercial LTDA",
        cnpj="03.734.155/0001-40", situacao="ATIVA",
        logradouro="Rua José do Patrocínio", numero="211", complemento=None,
        bairro="Centro", cep="38610-081", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="Comércio atacadista de defensivos agrícolas, adubos e fertilizantes; possui filial em Formosa-GO (mesmo grupo, útil para prospecção cruzada).",
        socios=[("Guilherme Telles Piva", "***.603.388-**", "Sócio-Administrador", 1),
                ("Joel Andre Pes", "***.348.000-**", "Sócio-Administrador", 0),
                ("Sergio Edilberto Zimmermann", "***.652.820-**", "Sócio-Administrador", 0)],
        contatos=[("telefone_fixo", "(61) 3631-3814", 1)],
    ),
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="Vilas Boas Agropecuária (Grupo Vilas Boas)", razao_social="Vilas Boas Agropecuária LTDA",
        cnpj="58.323.840/0001-75", situacao="ATIVA",
        logradouro="Rua Afonso Pena", numero="500", complemento=None,
        bairro="Centro", cep="38610-074", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE oficial na Receita é 'Aluguel de imóveis próprios' (entidade patrimonial do grupo) — o grupo opera como agronegócio (Agro Vilas Boas, cultivo de café) sob outro CNPJ do mesmo grupo; confirmar em contato direto qual CNPJ fatura os insumos/serviços agropecuários.",
        socios=[("Carlos Eduardo Vilas Boas", "***.472.628-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3677-1011", 1), ("email", "financeiro@grupovilasboas.com.br", 1)],
    ),
    dict(
        municipio="Unaí", setor="Imobiliária",
        nome_fantasia="ST5A Imobiliária", razao_social="ST5A Imobiliária LTDA",
        cnpj="44.859.570/0001-07", situacao="ATIVA",
        logradouro="Rua Afonso Pena", numero="500", complemento=None,
        bairro="Centro", cep="38610-074", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE oficial: loteamento de imóveis próprios. Mesmo endereço fiscal da Vilas Boas Agropecuária (500, Rua Afonso Pena) — indícios de ligação entre os dois grupos econômicos.",
        socios=[("Adriana Terezinha Gonçalves Matos", "***.478.941-**", "Sócio", 1),
                ("Alecia Maria Gonçalves Matos", "***.505.561-**", "Sócio", 0),
                ("Aline Gonçalves Matos Nunes Maciel", "***.508.671-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(38) 3676-3956", 1)],
    ),
    dict(
        municipio="Unaí", setor="Leilão de Animais",
        nome_fantasia="Noroeste Leilões", razao_social="Noroeste Leilões LTDA",
        cnpj="42.854.976/0001-71", situacao="ATIVA",
        logradouro="Rodovia Frei Jorge", numero="4230", complemento=None,
        bairro="Vale do Amanhecer", cep="38618-001", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE oficial: produção de espetáculos de rodeio/vaquejada — atua comercialmente também como leiloeira de gado de corte na região (canal tradicional de leilões em Unaí).",
        socios=[("Jose Deodato de Andrade", "***.952.366-**", "Sócio-Administrador", 1),
                ("Rosimari Rocha Aguiar Andrade", "***.612.746-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(38) 3676-3500", 1)],
    ),
    dict(
        municipio="Unaí", setor="Irrigação",
        nome_fantasia="Só Pivô", razao_social="Kadox Irrigação LTDA",
        cnpj="49.273.692/0001-30", situacao="ATIVA",
        logradouro="Rua Ezinho Martins", numero="673", complemento=None,
        bairro="Sagrada Família", cep="38616-782", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="CNAE oficial: promoção de vendas — empresa recente (2023), atua no mercado de irrigação por pivô local (nome fantasia 'Só Pivô').",
        socios=[("Valenir Antonio de Lima", "***.353.236-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 9904-3610", 1)],
    ),
]

for emp in REGISTROS:
    inserir_empresa(conn, emp)

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
print(f"Lote 2 de Unaí inserido: {len(REGISTROS)} empresas. Total no banco: {total}.")
conn.close()
