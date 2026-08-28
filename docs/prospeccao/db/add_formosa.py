# -*- coding: utf-8 -*-
"""Carga inicial de Formosa-GO (pesquisa exaustiva por diretório sistemático + BrasilAPI)."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

REGISTROS = [
    dict(municipio="Formosa", setor="Agropecuária",
         nome_fantasia="Cultivar Formosa", razao_social="Cultivar Comercial Agrícola Formosa LTDA.",
         cnpj="04.350.312/0001-86", situacao="ATIVA",
         logradouro="Avenida Brasília", numero="1710", complemento=None,
         bairro="Formosinha", cep="73813-010", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Rede com 4 filiais (Planaltina-DF, Uruaçu-GO e outras).",
         socios=[("Alvaro Luiz de Martini", "***.436.589-**", "Sócio-Administrador", 1),
                 ("Marcelo Pereira Franco", "***.548.926-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_fixo", "(61) 3642-1777", 1)]),
    dict(municipio="Formosa", setor="Agropecuária",
         nome_fantasia="Nativa Agrícola", razao_social="Nativa Agrícola LTDA",
         cnpj="07.634.396/0001-78", situacao="ATIVA",
         logradouro="Avenida Contorno", numero="6", complemento="Quadra 114, Lote 6",
         bairro="Parque Laguna II", cep="73814-001", site="https://nativaagricola.com.br",
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="12 filiais; mais de 20 anos de atuação.",
         socios=[("Jonatan Amorim da Silva", "***.549.821-**", "Sócio-Administrador", 1),
                 ("Jose Emidio Martins Junior", "***.602.669-**", "Sócio-Administrador", 0)],
         contatos=[("telefone_fixo", "(61) 3642-1206", 1)]),
    dict(municipio="Formosa", setor="Agropecuária",
         nome_fantasia="Agroquima Formosa", razao_social="Agroquima Produtos Agropecuários LTDA (filial Formosa)",
         cnpj="01.626.951/0030-78", situacao="ATIVA",
         logradouro="Avenida Maestro João Luiz do Espírito Santo", numero="6", complemento=None,
         bairro="Parque Laguna II", cep="73814-005", site="https://www.agroquima.com.br/lojas/formosa-go/",
         fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
         observacoes="Rede regional de lojas agropecuárias; este é o CNPJ da filial em Formosa.",
         socios=[], contatos=[("telefone_fixo", "(61) 3642-1141", 1)]),

    dict(municipio="Formosa", setor="Imobiliária",
         nome_fantasia="Vitória Imóveis Formosa", razao_social="Vitória Imóveis Formosa LTDA",
         cnpj="23.477.949/0001-82", situacao="ATIVA",
         logradouro="Rua São Pedro", numero="458", complemento=None,
         bairro="Formosinha", cep="73813-190", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26", observacoes=None,
         socios=[("Pablhiny de Faria Lamonier", "***.378.201-**", "Sócio-Administrador", 1),
                 ("Felipe Ribeiro de Oliveira Emerenciano", "***.135.801-**", "Sócio", 0)],
         contatos=[("telefone_celular", "(61) 9931-4515", 1)]),
    dict(municipio="Formosa", setor="Imobiliária",
         nome_fantasia="Mara Imobiliária", razao_social="Mara Imobiliária LTDA",
         cnpj="28.781.560/0001-40", situacao="ATIVA",
         logradouro="Rua Ângelo Chaves", numero="44", complemento=None,
         bairro="Centro", cep="73801-290", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26", observacoes=None,
         socios=[("Edimaria Rodrigues Araújo", "***.734.891-**", "Sócio-Administrador", 1)],
         contatos=[("telefone_fixo", "(61) 3631-0005", 1)]),
    dict(municipio="Formosa", setor="Imobiliária",
         nome_fantasia="Trato Imóveis", razao_social=None,
         cnpj=None, situacao=None,
         logradouro="Avenida Lagoa Feia", numero="377", complemento="Salas 01/03",
         bairro="Formosinha", cep="73813-370", site="https://www.tratoimoveis.com.br",
         fonte="Site oficial + busca web", data_coleta="2026-08-26",
         observacoes="CRECI-J 28433. CNPJ não localizado nas fontes públicas consultadas — confirmar em contato direto.",
         socios=[],
         contatos=[("telefone_fixo", "(61) 3642-1632", 1), ("whatsapp", "(61) 99642-1632", 1),
                   ("email", "atendimento@tratoimoveis.com.br", 1)]),

    dict(municipio="Formosa", setor="Irrigação",
         nome_fantasia="IrrigaMais Solução em Irrigação", razao_social="Irrigamais Solução em Irrigação e Serviços LTDA",
         cnpj="29.364.052/0001-29", situacao="ATIVA",
         logradouro="Avenida Lagoa Feia", numero="485", complemento=None,
         bairro="Formosinha", cep="73813-370", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CNAE oficial é manutenção de máquinas agrícolas; atua também com projetos de irrigação. "
                      "Há uma segunda empresa do mesmo sócio (ME mais nova, CNPJ 53.328.709/0001-87) no mesmo grupo.",
         socios=[("Alderico Alves Ribeiro", "***.275.126-**", "Sócio-Administrador", 1)],
         contatos=[("telefone_celular", "(61) 99862-9426", 1), ("email", "irrigamaisformosa@gmail.com", 1)]),
    dict(municipio="Formosa", setor="Irrigação",
         nome_fantasia="Pivot Equipamentos Agrícolas e Irrigação (filial Formosa)",
         razao_social="Pivot Equipamentos Agrícolas e Irrigação S.A.",
         cnpj="33.302.019/0015-47", situacao="ATIVA",
         logradouro="Avenida Brasília", numero="2449", complemento=None,
         bairro="Setor Industrial", cep="73813-550", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="Filial; matriz fora de GO (mesmo grupo já cadastrado em Unaí).",
         socios=[("Jorge Luiz de Almeida Campos", "***.916.321-**", "Presidente", 1)],
         contatos=[("telefone_fixo", "(62) 3018-3060", 1)]),

    dict(municipio="Formosa", setor="Silos/Armazéns",
         nome_fantasia="COOPA-DF", razao_social="Cooperativa Agropecuária da Região do Distrito Federal LTDA",
         cnpj="00.518.969/0002-30", situacao="ATIVA",
         logradouro="Avenida Circular", numero="220", complemento="Quadra 1",
         bairro="Setor Industrial", cep="73808-850", site=None,
         fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
         observacoes="CNAE oficial: 'Armazéns gerais - emissão de warrant' — atividade de armazenamento/"
                      "silos de grãos, câmaras frigoríficas por conta de terceiros. Bom fit para o segmento.",
         socios=[("Jose Guilherme Brenner", "***.850.001-**", "Presidente", 1),
                 ("Leandro Luis Maldaner", "***.655.541-**", "Diretor", 0)],
         contatos=[]),

    dict(municipio="Formosa", setor="Laticínios",
         nome_fantasia="Queijaria Pé do Morro", razao_social=None,
         cnpj=None, situacao=None,
         logradouro=None, numero=None, complemento="Produtor direto da fazenda, venda apenas on-line",
         bairro=None, cep=None, site=None,
         fonte="Instagram/redes sociais (@queijariapedomorro)", data_coleta="2026-08-26",
         observacoes="Pequeno produtor artesanal, sem CNPJ localizado — confirmar formalização em contato direto. "
                      "Nenhuma indústria/cooperativa de laticínios de porte foi confirmada com sede em Formosa-GO "
                      "(a 'Laticínios Formosa' de mesmo nome tem sede em Itauçu-GO/Lagoa Formosa-MG, coincidência "
                      "de nome, não incluída aqui).",
         socios=[], contatos=[("whatsapp", "(61) 99999-0205", 1)]),
]

for reg in REGISTROS:
    inserir_empresa(conn, reg)

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
n_formosa = conn.execute(
    "SELECT COUNT(*) FROM empresa WHERE municipio_id = (SELECT id FROM municipio WHERE nome='Formosa')"
).fetchone()[0]
print(f"Formosa: {n_formosa} empresas inseridas nesta carga. Total no banco: {total}.")
print("Segmentos sem registro em Formosa: Leilão de Animais (nenhuma leiloeira de animais confirmada; "
      "só achamos leiloeiros judiciais de bens gerais, fora de escopo).")
conn.close()
