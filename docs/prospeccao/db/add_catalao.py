# -*- coding: utf-8 -*-
"""Carga inicial de Catalão-GO."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    dict(municipio="Catalão", setor="Agropecuária",
         nome_fantasia="Agropecuária Rampelotti", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Avenida 20 de Agosto", numero="2095", complemento=None,
         bairro="Setor Central", cep="75701-010", site=None,
         fonte="Diretório local + busca web", data_coleta="2026-08-26",
         observacoes="Ligada ao Grupo Rampelotti (produtor de soja/milho/feijão/trigo na região). "
                      "CNPJ específico da loja agropecuária não confirmado — 'Silva & Rampelotti LTDA' "
                      "(mesma família) é loja de vestuário, entidade diferente, não incluída aqui.",
         socios=[], contatos=[("telefone_fixo", "(64) 3441-3353", 1)]),
    dict(municipio="Catalão", setor="Agropecuária",
         nome_fantasia="Agrotelas Produtos Agropecuários", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Avenida José Marcelino", numero=None, complemento=None,
         bairro="Setor Central", cep=None, site=None,
         fonte="Diretório local + busca web", data_coleta="2026-08-26",
         observacoes="CNPJ não localizado nas fontes públicas consultadas — confirmar em contato direto.",
         socios=[], contatos=[("telefone_fixo", "(64) 3441-4279", 1)]),

    dict(municipio="Catalão", setor="Imobiliária",
         nome_fantasia="Parati Imóveis", razao_social="Imobiliária Parati LTDA",
         cnpj="01.047.147/0001-08", situacao="ATIVA",
         logradouro="Avenida José Marcelino", numero="384", complemento=None,
         bairro="Setor Central", cep="75701-430", site="https://www.paratiimoveis.com.br",
         fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
         observacoes="Mais de 3 décadas de atuação no mercado imobiliário de Catalão.",
         socios=[("Genair Patrocinio da Silva", "***.773.601-**", "Sócio-Administrador", 1),
                 ("Simone Rosa de Oliveira", "***.935.771-**", "Sócio", 0)],
         contatos=[("telefone_fixo", "(64) 3441-3112", 1), ("email", "paratiimoveis@hotmail.com", 1)]),
    dict(municipio="Catalão", setor="Imobiliária",
         nome_fantasia="Ribeiro e Bones Soluções Imobiliárias (Attitude Imóveis)", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Rua Americano do Brasil", numero="111", complemento=None,
         bairro="Centro", cep=None, site="https://www.imoveisattitude.com.br",
         fonte="Diretório local + busca web", data_coleta="2026-08-26",
         observacoes="CRECI 38.330. Opera com a marca 'Attitude Imóveis'. CNPJ não localizado nas fontes "
                      "públicas consultadas.",
         socios=[], contatos=[("whatsapp", "(64) 99609-6040", 1), ("telefone_fixo", "(64) 3040-6040", 1)]),

    dict(municipio="Catalão", setor="Irrigação",
         nome_fantasia="Pivot Equipamentos Agrícolas e Irrigação (filial Catalão)",
         razao_social="Pivot Equipamentos Agrícolas e Irrigação S.A.",
         cnpj="33.302.019/0025-19", situacao="ATIVA",
         logradouro="Avenida Doutor Lamartine Pinto de Avelar", numero="3405", complemento=None,
         bairro="Loteamento Copacabana", cep="75705-291", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Mesmo grupo já cadastrado em Unaí, Formosa e Cristalina; concessionária Case IH e "
                      "sistemas Lindsay de irrigação.",
         socios=[("Fabio Euclides Borgonhone", "***.895.119-**", "Diretor", 1),
                 ("João Carlos Obici", "***.146.449-**", "Diretor", 0)],
         contatos=[("telefone_fixo", "(62) 3018-3039", 1)]),

    dict(municipio="Catalão", setor="Laticínios",
         nome_fantasia="COACAL", razao_social="Cooperativa Agropecuária de Catalão - COACAL",
         cnpj="01.320.951/0001-00", situacao="ATIVA",
         logradouro="Rua Moisés Santana", numero="394", complemento=None,
         bairro="São João", cep="75703-060", site="http://www.coacal.com.br",
         fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
         observacoes="Fundada em 1964 originalmente como 'Laticínios Vereda'. CNAE oficial 'Preparação do "
                      "leite'. Produz leite pasteurizado, queijo, manteiga, iogurte e doce de leite. Também "
                      "opera loja agropecuária e posto de combustível sob o mesmo grupo. A unidade de "
                      "armazém/silo da cooperativa (CNPJ 01.320.951/0007-04) está com situação SUSPENSA — "
                      "não incluída no segmento Silos/Armazéns.",
         socios=[("Wender Antonio Ferreira", "***.100.841-**", "Presidente", 1)],
         contatos=[("telefone_fixo", "(64) 3441-2288", 1)]),
]

for reg in REGISTROS:
    inserir_empresa(conn, reg)

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
n = conn.execute(
    "SELECT COUNT(*) FROM empresa WHERE municipio_id = (SELECT id FROM municipio WHERE nome='Catalão')"
).fetchone()[0]
print(f"Catalão: {n} empresas inseridas nesta carga. Total no banco: {total}.")
print("Segmentos sem registro em Catalão: Silos/Armazéns (única unidade de armazém cooperativo "
      "identificada, da COACAL, está com situação SUSPENSA) e Leilão de Animais (leilões de gado só "
      "aparecem como parte da feira sazonal Expo Catalão, organizada pelo Sindicato Rural, sem empresa "
      "leiloeira dedicada e sediada na cidade confirmada).")
conn.close()
