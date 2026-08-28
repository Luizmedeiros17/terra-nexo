# -*- coding: utf-8 -*-
"""Rodada 2 de fechamento de lacunas — Cristalina/Leilão, Catalão/Leilão fechados;
bônus Armazém do Criador (Luziânia, Agropecuária/insumos, não é silo de grãos)."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

inserir_empresa(conn, dict(
    municipio="Cristalina", setor="Leilão de Animais",
    nome_fantasia="Mundial Leilões", razao_social="Mundial Leilões LTDA",
    cnpj="06.049.015/0002-01", situacao="ATIVA",
    logradouro="Rodovia BR 050", numero="S/N", complemento=None,
    bairro="Setor Sudoeste", cep="73850-000", site="https://www.mundialleiloes.com.br",
    fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
    observacoes="Filial do mesmo grupo que opera em Catalão-GO (mesma matriz/sócios); atua com leilões de gado de corte na região.",
    socios=[("Rubens Divino Francisco de Almeida", "***.629.101-**", "Sócio-Administrador", 1),
            ("Thais Veronica Ferreira de Almeida", "***.941.981-**", "Sócio-Administrador", 0)],
    contatos=[("telefone_fixo", "(64) 8107-0486", 1)],
))

inserir_empresa(conn, dict(
    municipio="Catalão", setor="Leilão de Animais",
    nome_fantasia="Mundial Leilões", razao_social="Mundial Leilões LTDA",
    cnpj="06.049.015/0001-20", situacao="ATIVA",
    logradouro="GO 506", numero="S/N", complemento=None,
    bairro="Zona Rural", cep="75701-010", site="https://www.mundialleiloes.com.br",
    fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
    observacoes="Matriz/unidade em Catalão do mesmo grupo que opera a filial de Cristalina-GO.",
    socios=[("Rubens Divino Francisco de Almeida", "***.629.101-**", "Sócio-Administrador", 1),
            ("Thais Veronica Ferreira de Almeida", "***.941.981-**", "Sócio-Administrador", 0)],
    contatos=[("telefone_fixo", "(64) 8105-0521", 1)],
))

# Bônus: não fecha o gap de Silos/Armazéns (CNAE real é fabricação de ração animal,
# não armazenagem de grãos), mas é um lead real de Agropecuária em Luziânia.
inserir_empresa(conn, dict(
    municipio="Luziânia", setor="Agropecuária",
    nome_fantasia="Armazém do Criador", razao_social="A.C Comercial de Produtos Agropecuários LTDA",
    cnpj="04.526.987/0001-33", situacao="ATIVA",
    logradouro="Rua Doutor Neylo Rolim", numero="S/N", complemento=None,
    bairro="Parque Alvorada I", cep="72836-330", site=None,
    fonte="Receita Federal (BrasilAPI) + busca web", data_coleta="2026-08-26",
    observacoes=("Apesar do nome 'Armazém', CNAE oficial é fabricação de alimentos para animais "
                 "(ração) — classificado como Agropecuária, não Silos/Armazéns (que continua sem "
                 "player ativo confirmado em Luziânia)."),
    socios=[("Ana Carla Rodrigues de Sousa Lima", "***.893.281-**", "Sócio-Administrador", 1)],
    contatos=[("telefone_fixo", "(61) 3620-0767", 1)],
))

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
print(f"Rodada 2 de gaps inserida. Total de empresas no banco: {total}.")
conn.close()
