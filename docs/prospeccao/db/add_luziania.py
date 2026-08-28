# -*- coding: utf-8 -*-
"""Carga inicial de Luziânia-GO."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    dict(municipio="Luziânia", setor="Agropecuária",
         nome_fantasia="Sertaneja Agropecuária", razao_social="L.R & J.E Produtos Agropecuários LTDA",
         cnpj="10.925.057/0001-55", situacao="ATIVA",
         logradouro="Avenida Santa Maria", numero="S/N", complemento=None,
         bairro="Setor Aeroporto", cep="72801-020", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26", observacoes=None,
         socios=[("Aluisio Matias Pereira", "***.115.341-**", "Sócio-Administrador", 1),
                 ("Jose Edimilson Cristovao Pinto", "***.654.141-**", "Sócio", 0)],
         contatos=[("telefone_fixo", "(61) 3621-2519", 1)]),
    dict(municipio="Luziânia", setor="Agropecuária",
         nome_fantasia="Agroluz Agropecuária", razao_social="Agroluz Agropecuária Luziânia LTDA",
         cnpj="18.532.645/0001-30", situacao="ATIVA",
         logradouro="Rua Plácido de Paiva", numero="S/N", complemento="Quadra 133, Lote 12, Loja 01",
         bairro="Setor Fumal", cep="72801-670", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CNAE oficial é comércio de animais vivos/pet — loja agropecuária de linha mais ampla "
                      "segundo diretórios locais; confirmar mix de produtos em contato direto.",
         socios=[("Miriam Rizia Pereira", "***.605.151-**", "Sócio-Administrador", 1)],
         contatos=[("telefone_fixo", "(61) 3622-2970", 1)]),

    dict(municipio="Luziânia", setor="Imobiliária",
         nome_fantasia="Braz Imóveis", razao_social="Braz Imóveis LTDA",
         cnpj="12.440.175/0001-07", situacao="ATIVA",
         logradouro="Rua José Franco Pimentel", numero=None, complemento=None,
         bairro="Centro", cep="72800-500", site="https://www.brazimoveisluziania.com",
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CNAE oficial é construção de edifícios; opera publicamente como imobiliária "
                      "(consultoria/venda/locação) segundo site próprio.",
         socios=[], contatos=[("telefone_fixo", "(61) 3622-3656", 1)]),
    dict(municipio="Luziânia", setor="Imobiliária",
         nome_fantasia="JL Imobiliária", razao_social="JL Empreendimentos Imobiliários LTDA",
         cnpj="19.512.769/0001-17", situacao="ATIVA",
         logradouro="Rua Benedito José de Morais", numero="70/74", complemento=None,
         bairro="Diogo Machado de Araújo", cep="72810-010", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26", observacoes=None,
         socios=[("Ana da Aparecida Roriz Carneiro", "***.349.391-**", "Sócio-Administrador", 1),
                 ("Cleonea Guimarães Roriz", "***.292.681-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_fixo", "(61) 3601-1350", 1)]),
    dict(municipio="Luziânia", setor="Imobiliária",
         nome_fantasia="Guimarães Imóveis", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Rua Dr. João Teixeira", numero="1", complemento=None,
         bairro="Centro", cep="72800-440", site=None,
         fonte="Diretório local (guiaimobiliarias.com)", data_coleta="2026-08-26",
         observacoes="CNPJ não localizado nas fontes públicas consultadas — confirmar em contato direto.",
         socios=[], contatos=[]),

    dict(municipio="Luziânia", setor="Leilão de Animais",
         nome_fantasia="M.A Braz - Leilões Rurais", razao_social="M.A Braz - Leilões Rurais",
         cnpj="34.057.072/0001-97", situacao="ATIVA",
         logradouro="Rodovia Braluz - Dical", numero=None, complemento="Parque de Exposições de Luziânia",
         bairro=None, cep="72801-360", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Também conhecida como 'Santa Clara Leilões'. Outra leiloeira do mesmo endereço "
                      "(Tropical Leilões LTDA, CNPJ 10.145.769/0001-51) está com situação INAPTA — não incluída.",
         socios=[], contatos=[]),

    dict(municipio="Luziânia", setor="Laticínios",
         nome_fantasia="Mourão Laticínios", razao_social=None,
         cnpj=None, situacao=None,
         logradouro=None, numero=None, complemento="Loja de laticínios (queijos, coalhada, requeijão)",
         bairro=None, cep=None, site="https://mourao-laticinios.ueniweb.com",
         fonte="Site oficial + busca web", data_coleta="2026-08-26",
         observacoes="CNPJ não localizado nas fontes públicas consultadas — parece ser loja/varejo, não "
                      "indústria. Nenhuma indústria de laticínios de porte com sede em Luziânia foi confirmada.",
         socios=[], contatos=[("telefone_celular", "(61) 99665-9989", 1)]),
]

for reg in REGISTROS:
    inserir_empresa(conn, reg)

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
n = conn.execute(
    "SELECT COUNT(*) FROM empresa WHERE municipio_id = (SELECT id FROM municipio WHERE nome='Luziânia')"
).fetchone()[0]
print(f"Luziânia: {n} empresas inseridas nesta carga. Total no banco: {total}.")
print("Segmentos sem registro em Luziânia: Irrigação (única empresa dedicada encontrada, Valesto Santos "
      "Irrigação, está BAIXADA na Receita) e Silos/Armazéns (nenhuma cooperativa/armazém confirmado na cidade; "
      "COOPA-DF, que atua na região, tem sede em Paranoá-DF, não em Luziânia).")
conn.close()
