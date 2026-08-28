# -*- coding: utf-8 -*-
"""Carga inicial de Cristalina-GO."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    dict(municipio="Cristalina", setor="Agropecuária",
         nome_fantasia="Agropecuária Figueiredo", razao_social="Agropecuária Figueiredo LTDA",
         cnpj="18.075.720/0001-81", situacao="ATIVA",
         logradouro="Avenida José de Alencar", numero="S/N", complemento="Quadra 09, Lote 01-B, Sala 03",
         bairro="Jardim Planalto", cep="73850-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CNAE oficial é cultivo de soja (grupo com 7 estabelecimentos em 3 estados); "
                      "aparece em diretórios locais como agropecuária — confirmar mix de negócio em contato.",
         socios=[], contatos=[("telefone_fixo", "(61) 3612-1480", 1)]),
    dict(municipio="Cristalina", setor="Agropecuária",
         nome_fantasia="Agroquima Cristalina", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Rua da Saudade", numero=None, complemento="Quadra 44, Lotes 07 a 12",
         bairro="Setor Noroeste", cep="73850-000", site="https://www.agroquima.com.br/lojas/cristalina-go/",
         fonte="Site oficial + busca web", data_coleta="2026-08-26",
         observacoes="Filial da rede regional Agroquima (mesma rede já cadastrada em Formosa-GO); "
                      "CNPJ específico da filial de Cristalina não localizado nas fontes públicas consultadas.",
         socios=[], contatos=[("telefone_fixo", "(61) 3142-0096", 1)]),

    dict(municipio="Cristalina", setor="Imobiliária",
         nome_fantasia="Essencial Imobiliária", razao_social="Luciano Severino Botelho (Essencial Imobiliária)",
         cnpj="09.392.658/0001-60", situacao="ATIVA",
         logradouro=None, numero=None, complemento=None,
         bairro="Cristalina Velha", cep="73850-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Empresário individual (CNAE: corretagem no aluguel de imóveis).",
         socios=[("Luciano Severino Botelho", None, "Titular (Empresário Individual)", 1)],
         contatos=[]),
    dict(municipio="Cristalina", setor="Imobiliária",
         nome_fantasia="Diego Gomes Imóveis", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Rua João Aguiar", numero="815", complemento=None,
         bairro="Centro", cep="73850-000", site="https://www.diegogomesimoveis.com.br",
         fonte="Site oficial + busca web", data_coleta="2026-08-26",
         observacoes="CRECI 41.919. CNPJ não localizado nas fontes públicas consultadas.",
         socios=[], contatos=[("telefone_celular", "(61) 99183-0427", 1)]),

    dict(municipio="Cristalina", setor="Irrigação",
         nome_fantasia="Irricampo Cristalina Sistemas de Irrigação", razao_social="Irricampo Sistemas de Irrigação LTDA",
         cnpj="13.933.332/0001-70", situacao="ATIVA",
         logradouro="Rua 3", numero="S/N", complemento="Quadra 3, Lote 12-B",
         bairro="Setor Noroeste", cep="73850-000", site="https://www.irricampocristalina.com.br",
         fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
         observacoes="Esta é a sede/matriz da rede Irricampo (a unidade cadastrada em Unaí-MG aponta o site "
                      "para este mesmo domínio — confirmado como a mesma empresa/grupo).",
         socios=[("Carlos Alberto Garcia", "***.129.871-**", "Sócio-Administrador", 1)],
         contatos=[("telefone_fixo", "(61) 3612-7086", 1)]),
    dict(municipio="Cristalina", setor="Irrigação",
         nome_fantasia="Central Irrigação", razao_social="Central Irrigação LTDA",
         cnpj="04.784.935/0005-90", situacao="ATIVA",
         logradouro="Avenida José de Alencar", numero="S/N", complemento=None,
         bairro="Jardim Planalto", cep="73850-000", site="https://www.centralirrigacao.com.br",
         fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
         observacoes="Atua em GO, MG e TO; esta é a filial de Cristalina.",
         socios=[("Hugo Luiz Vocurca", "***.758.408-**", "Sócio-Administrador", 1),
                 ("Erik Neves Oliveira", "***.824.281-**", "Sócio", 0)],
         contatos=[]),
    dict(municipio="Cristalina", setor="Irrigação",
         nome_fantasia="PC Pivô Irrigação", razao_social="Paulo Cesar Silveira Pessoa - PC Pivô Irrigação",
         cnpj="08.906.692/0001-43", situacao="ATIVA",
         logradouro="Rua Araguaia", numero=None, complemento=None,
         bairro="Setor Noroeste", cep="73850-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Empresário individual.",
         socios=[("Paulo Cesar Silveira Pessoa", None, "Titular (Empresário Individual)", 1)],
         contatos=[]),

    dict(municipio="Cristalina", setor="Silos/Armazéns",
         nome_fantasia="COOPA-DF (unidade Cristalina)", razao_social="Cooperativa Agropecuária da Região do Distrito Federal LTDA",
         cnpj="00.518.969/0005-82", situacao="ATIVA",
         logradouro="Rua 21 de Abril", numero="361", complemento=None,
         bairro="Centro", cep="73850-000", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Mesma cooperativa já cadastrada em Formosa-GO (matriz em Paranoá-DF); CNAE oficial "
                      "'Armazéns gerais - emissão de warrant'.",
         socios=[("Jose Guilherme Brenner", "***.850.001-**", "Presidente", 1),
                 ("Leandro Luis Maldaner", "***.655.541-**", "Diretor", 0)],
         contatos=[]),
]

for reg in REGISTROS:
    inserir_empresa(conn, reg)

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
n = conn.execute(
    "SELECT COUNT(*) FROM empresa WHERE municipio_id = (SELECT id FROM municipio WHERE nome='Cristalina')"
).fetchone()[0]
print(f"Cristalina: {n} empresas inseridas nesta carga. Total no banco: {total}.")
print("Segmentos sem registro em Cristalina: Leilão de Animais (só achamos leiloeira sediada em Rio Verde-GO "
      "que atua na região, não uma empresa sediada em Cristalina) e Laticínios (Econodata indica ~7 pequenos "
      "produtores individuais registrados sob o CNAE de laticínios na cidade, mas não foi possível confirmar "
      "CNPJ/contato específico nas fontes gratuitas consultadas).")
conn.close()
