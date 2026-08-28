# -*- coding: utf-8 -*-
"""
Cria (do zero) o banco normalizado docs/prospeccao/db/prospeccao.db a partir de
schema_sqlite.sql e carrega os dados coletados até agora.

Reexecutável: apaga o .db anterior e recria — a fonte de verdade dos REGISTROS
é este arquivo. Para adicionar prospects novos, adicione um item em REGISTROS
(ou, no dia a dia, insira direto no banco / no Excel exportado e depois rode
import_from_excel.py) e rode este script de novo.
"""
import os
import sqlite3

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "prospeccao.db")
SCHEMA_PATH = os.path.join(BASE_DIR, "schema_sqlite.sql")

MUNICIPIOS = [
    ("Unaí", "MG"), ("Paracatu", "MG"), ("Formosa", "GO"), ("Luziânia", "GO"),
    ("Cristalina", "GO"), ("Catalão", "GO"), ("João Pinheiro", "MG"),
]

SETORES = [
    ("Agropecuária", "Comércio/serviços agropecuários, insumos, defensivos, maquinário"),
    ("Imobiliária", "Corretagem, administração e intermediação de imóveis"),
    ("Irrigação", "Projetos, equipamentos e serviços de irrigação (pivô central, aspersão etc.)"),
    ("Silos/Armazéns", "Armazenagem, silos graneleiros e comércio/corretagem de grãos"),
    ("Leilão de Animais", "Leiloeiras e parques de leilão de gado/animais"),
    ("Laticínios", "Cooperativas e indústrias de laticínios/processamento de leite"),
]

# empresa: (municipio, setor, nome_fantasia, razao_social, cnpj, situacao, logradouro, numero,
#           complemento, bairro, cep, site, status, fonte, data_coleta, observacoes)
# socios: lista de (nome, cpf_mascarado, qualificacao, principal)
# contatos: lista de (tipo, valor, principal)
EMPRESAS = [
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="União Agropecuária e Transporte", razao_social="União Agropecuária e Transporte LTDA",
        cnpj="19.328.529/0001-67", situacao="ATIVA",
        logradouro="Rua Natal Justino da Costa", numero="805", complemento=None,
        bairro="Centro", cep="38610-044", site=None,
        fonte="Receita Federal (CNPJ público via BrasilAPI) + Agrolink", data_coleta="2026-08-26",
        observacoes=None,
        socios=[("Raimundo Sauer", "***.072.726-**", "Sócio-Administrador", 1),
                ("Alice Lopes Souto Tomaz", "***.149.626-**", "Sócio", 0)],
        contatos=[("telefone_fixo", "(38) 3676-2689", 1)],
    ),
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="FJA. Agronegócio Unaí", razao_social="FJA. Agronegócio Unaí LTDA",
        cnpj="48.510.635/0001-65", situacao="ATIVA",
        logradouro="Rodovia Frei Jorge", numero="1310", complemento="Sala 01",
        bairro="De Lourdes", cep="38616-470", site=None,
        fonte="Receita Federal (CNPJ público via BrasilAPI) + Agrolink", data_coleta="2026-08-26",
        observacoes="Comércio atacadista de máquinas e equipamentos agropecuários.",
        socios=[("Adenivaldo Oliveira Xavier", "***.135.716-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 8832-2555", 1)],
    ),
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="Agrorural", razao_social="Agrorural Comércio e Representação LTDA",
        cnpj="49.573.204/0001-00", situacao="ATIVA",
        logradouro="Avenida Central", numero="138", complemento=None,
        bairro="Parque Explanada", cep="38620-115", site=None,
        fonte="Receita Federal (CNPJ público via BrasilAPI) + Agrolink", data_coleta="2026-08-26",
        observacoes="Comércio atacadista de defensivos agrícolas, adubos e fertilizantes.",
        socios=[("Afranio Queiroz Neto", "***.969.846-**", "Administrador", 1)],
        contatos=[("telefone_celular", "(38) 9970-0717", 1)],
    ),
    dict(
        municipio="Unaí", setor="Agropecuária",
        nome_fantasia="Mundo Agropecuário", razao_social=None,
        cnpj=None, situacao=None,
        logradouro="Avenida Governador Valadares", numero="116", complemento=None,
        bairro=None, cep=None, site=None,
        fonte="Guia Unaí (guiaunai.com.br)", data_coleta="2026-08-26",
        observacoes="CNPJ e titular não localizados nas fontes públicas consultadas — confirmar em contato direto.",
        socios=[],
        contatos=[("telefone_fixo", "(38) 3677-4331", 1), ("whatsapp", "(38) 99970-6030", 1)],
    ),
    dict(
        municipio="Unaí", setor="Imobiliária",
        nome_fantasia="Alfa Nicolau Imóveis", razao_social="Alfa Nicolau Imóveis LTDA",
        cnpj="14.927.825/0001-60", situacao="ATIVA",
        logradouro="Rua Djalma Torres", numero="351", complemento="ponto comercial (fiscal: Av. Princesa Isabel, 531, Jardim)",
        bairro="Centro", cep="38610-036", site="https://alfanicolau.com.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes="CRECI J 4188.",
        socios=[("Renato Nicolau Ribeiro", "***.195.336-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3676-7636", 1), ("whatsapp", "(38) 98849-2919", 1)],
    ),
    dict(
        municipio="Unaí", setor="Imobiliária",
        nome_fantasia="Novo Lar Imobiliária", razao_social="Novo Lar Imobiliária LTDA",
        cnpj="33.625.816/0001-60", situacao="ATIVA",
        logradouro="Rua Buritis", numero="314", complemento="Loja 2",
        bairro="Centro", cep="38610-049", site="https://novolarimobiliariaunai.com.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes="CRECI MGJ0006148. Site lista WhatsApp separados por setor (vendas/locação/financeiro).",
        socios=[("Alisson Morais Carvalho", "***.758.986-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3676-3243", 1), ("whatsapp", "(38) 99879-9441", 1),
                  ("email", "novolarunai@gmail.com", 1)],
    ),
    dict(
        municipio="Unaí", setor="Imobiliária",
        nome_fantasia="Prime Imóveis Unaí", razao_social="Prime Imóveis Unaí LTDA",
        cnpj="14.460.843/0001-85", situacao="ATIVA",
        logradouro="Rua Celina Lisboa Frederico", numero="69", complemento="Sala 01",
        bairro="Centro", cep="38610-041", site="https://primeimoveisunai.com.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes="CRECI MGJ0006090. 4 sócios no QSA; listados os dois sócios-administradores.",
        socios=[("José Lindomar Coelho", "***.605.706-**", "Sócio-Administrador", 1),
                ("Ronie José de Carvalho", "***.207.816-**", "Sócio-Administrador", 0)],
        contatos=[("telefone_fixo", "(38) 3677-6070", 1), ("whatsapp", "(38) 99970-6070", 1),
                  ("email", "primeimoveisunai@gmail.com", 1)],
    ),
    dict(
        municipio="Unaí", setor="Imobiliária",
        nome_fantasia="Companhia Imobiliária", razao_social="CIB Companhia de Infraestrutura Brasileira LTDA",
        cnpj="36.770.097/0001-69", situacao="ATIVA",
        logradouro="Rua Djalma Torres", numero="557", complemento=None,
        bairro="Centro", cep="38610-036", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes="E-mail encontrado em diretório de terceiros, não confirmado no site oficial.",
        socios=[("Gualter Moura Filho", "***.381.461-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 3676-6772", 1), ("email", "cia.imobiliaria@hotmail.com", 1)],
    ),
    dict(
        municipio="Unaí", setor="Irrigação",
        nome_fantasia="Irricampo Unaí – Sistemas de Irrigação", razao_social=None,
        cnpj=None, situacao=None,
        logradouro="Avenida Governador Valadares", numero="100", complemento=None,
        bairro=None, cep=None, site="https://www.irricampocristalina.com.br",
        fonte="Guia Unaí (guiaunai.com.br)", data_coleta="2026-08-26",
        observacoes="Site institucional aponta para domínio de Cristalina — confirmar se é filial/mesma rede.",
        socios=[],
        contatos=[("telefone_fixo", "(38) 3677-6200", 1), ("whatsapp", "(38) 99913-6200", 1)],
    ),
    dict(
        municipio="Unaí", setor="Irrigação",
        nome_fantasia="Pivot Equipamentos Agrícolas e Irrigação (filial Unaí)",
        razao_social="Pivot Equipamentos Agrícolas e Irrigação S.A.",
        cnpj="33.302.019/0013-85", situacao="ATIVA",
        logradouro="Rua Porangatu", numero="314", complemento=None,
        bairro="Kamayurá", cep="38616-222", site=None,
        fonte="Receita Federal (BrasilAPI) + Econodata", data_coleta="2026-08-26",
        observacoes="Filial; matriz fora de MG. Telefone da Receita tem DDD 62 (central da matriz).",
        socios=[("Jorge Luiz de Almeida Campos", "***.916.321-**", "Presidente", 1),
                ("Fabio Euclides Borgonhone", "***.895.119-**", "Diretor", 0),
                ("João Carlos Obici", "***.146.449-**", "Diretor", 0),
                ("Marcos Antonio Trintinalha", "***.251.209-**", "Diretor", 0)],
        contatos=[("telefone_fixo", "(62) 3293-0496", 1)],
    ),
    dict(
        municipio="Unaí", setor="Irrigação",
        nome_fantasia="Ideal Irrigação", razao_social=None,
        cnpj=None, situacao=None,
        logradouro=None, numero=None, complemento="Endereço físico não disponível publicamente",
        bairro=None, cep=None, site="https://www.projetospremium.com.br/idealirrigacao",
        fonte="Site oficial", data_coleta="2026-08-26",
        observacoes="Atua também em Lagamar, Buritizeiro, Pirapora e Várzea da Palma-MG.",
        socios=[],
        contatos=[("telefone_fixo", "(38) 3677-5900", 1), ("email", "ideal@idealirrigacao.com.br", 1)],
    ),
    dict(
        municipio="Unaí", setor="Silos/Armazéns",
        nome_fantasia="Corretora e Comércio de Grãos Unaí", razao_social="Corretora e Comércio de Grãos Unaí LTDA",
        cnpj="43.737.346/0001-80", situacao="ATIVA",
        logradouro="Rua Juvêncio Correio", numero="469", complemento=None,
        bairro="Capim Branco", cep="38610-104", site=None,
        fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
        observacoes=("CNAE oficial na Receita é 'Cultivo de soja' — opera como corretora/"
                     "comercializadora de grãos; confirmar em contato direto se possui silo "
                     "próprio. Segmento com poucas empresas confirmadas publicamente em Unaí: "
                     "CASEMG (armazém estadual) e Coanor (cooperativa) estão com registro "
                     "BAIXADA/em liquidação na Receita — não incluídas. Fonte oficial recomendada "
                     "para completar este setor: sistema de armazenagem da CONAB (dados abertos)."),
        socios=[("Walter da Silva Campos", "***.002.966-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_celular", "(38) 9961-3829", 1)],
    ),
    dict(
        municipio="Unaí", setor="Leilão de Animais",
        nome_fantasia="Unaí Leilões", razao_social="Unaí Leilões LTDA",
        cnpj="45.702.181/0002-17", situacao="ATIVA",
        logradouro="Rodovia Frei Jorge", numero="4289", complemento="parque de leilões (escritório: Av. Belo Horizonte, 335)",
        bairro="Capim Branco II", cep="38623-899", site=None,
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes=("Parque com capacidade para ~4.000 animais e 300 pessoas; tradicional desde "
                     "1993 (entidade predecessora com o mesmo grupo familiar está BAIXADA na Receita)."),
        socios=[("Leonardo Campos Dias", "***.932.816-**", "Sócio-Administrador", 1),
                ("Magna Oliveira Campos", "***.927.706-**", "Sócio-Administrador", 0),
                ("Paulo Sergio Campos Dias", "***.944.666-**", "Sócio-Administrador", 0)],
        contatos=[("telefone_fixo", "(61) 3364-4694", 1), ("whatsapp", "(38) 99911-2043", 1),
                  ("email", "unaileiloes@hotmail.com", 1)],
    ),
    dict(
        municipio="Unaí", setor="Laticínios",
        nome_fantasia="Capul – Cooperativa Agropecuária Unaí", razao_social="Cooperativa Agropecuária Unaí LTDA",
        cnpj="25.834.847/0001-00", situacao="ATIVA",
        logradouro="Rua Prefeito João Costa", numero="1455", complemento="Sede administrativa",
        bairro="Parque Industrial", cep="38610-000", site="https://capul.coop.br",
        fonte="Receita Federal (BrasilAPI) + site oficial", data_coleta="2026-08-26",
        observacoes=("Cooperativa com +3.000 associados e 34 filiais (várias atividades: leite, "
                     "ração, insumos, veterinária). Unidade industrial dedicada 'Laticínios Unaí' "
                     "fica na Rua Prefeito João Costa, 1375, Bairro Planalto, CNPJ da unidade "
                     "25.834.847/0003-64, tel. (38) 2102-5100 / SAC (38) 2102-5179."),
        socios=[("Adailson Alves de Almeida", None, "Administrador", 1),
                ("Adair Lemes de Mendonça", None, "Diretor", 0),
                ("Cleuton Domingos da Silva", None, "Administrador", 0)],
        contatos=[("telefone_fixo", "(38) 2102-5100", 1), ("email", "comunicacao@capul.coop.br", 1)],
    ),
]


def maps_link(empresa):
    partes = [empresa.get("logradouro"), empresa.get("numero"), empresa.get("bairro"),
              empresa["municipio"] + " - MG/GO"]
    endereco = ", ".join(p for p in partes if p)
    import urllib.parse
    return "https://www.google.com/maps/search/?api=1&query=" + urllib.parse.quote(endereco)


def main():
    if os.path.exists(DB_PATH):
        os.remove(DB_PATH)

    conn = sqlite3.connect(DB_PATH)
    conn.executescript(open(SCHEMA_PATH, encoding="utf-8").read())

    mun_id = {}
    for nome, uf in MUNICIPIOS:
        cur = conn.execute("INSERT INTO municipio (nome, uf) VALUES (?, ?)", (nome, uf))
        mun_id[nome] = cur.lastrowid

    setor_id = {}
    for nome, desc in SETORES:
        cur = conn.execute("INSERT INTO setor (nome, descricao) VALUES (?, ?)", (nome, desc))
        setor_id[nome] = cur.lastrowid

    for emp in EMPRESAS:
        cur = conn.execute(
            """INSERT INTO empresa
               (municipio_id, setor_id, nome_fantasia, razao_social, cnpj, situacao_cadastral,
                logradouro, numero, complemento, bairro, cep, site, status_prospeccao,
                fonte_informacao, data_coleta, observacoes)
               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pesquisado', ?, ?, ?)""",
            (mun_id[emp["municipio"]], setor_id[emp["setor"]], emp["nome_fantasia"],
             emp["razao_social"], emp["cnpj"], emp["situacao"], emp["logradouro"], emp["numero"],
             emp["complemento"], emp["bairro"], emp["cep"], emp["site"],
             emp["fonte"], emp["data_coleta"], emp["observacoes"]),
        )
        empresa_id = cur.lastrowid

        for nome, cpf, qualificacao, principal in emp["socios"]:
            conn.execute(
                "INSERT INTO socio (empresa_id, nome, cpf_mascarado, qualificacao, principal) "
                "VALUES (?, ?, ?, ?, ?)",
                (empresa_id, nome, cpf, qualificacao, principal),
            )

        for tipo, valor, principal in emp["contatos"]:
            conn.execute(
                "INSERT INTO contato (empresa_id, tipo, valor, principal) VALUES (?, ?, ?, ?)",
                (empresa_id, tipo, valor, principal),
            )

        conn.execute(
            "INSERT INTO geolocalizacao (empresa_id, maps_url) VALUES (?, ?)",
            (empresa_id, maps_link(emp)),
        )

        conn.execute(
            "INSERT INTO status_historico (empresa_id, status, data_status, responsavel, observacao) "
            "VALUES (?, 'Pesquisado', ?, 'pesquisa automatizada', 'Registro inicial carregado na base.')",
            (empresa_id, emp["data_coleta"]),
        )

    conn.commit()
    total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
    print(f"Banco criado em {DB_PATH} com {total} empresas.")
    conn.close()


if __name__ == "__main__":
    main()
