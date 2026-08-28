# -*- coding: utf-8 -*-
"""Carga incremental: Agropaulista (Unaí), encontrada após checagem do usuário."""
from add_empresa import conectar, inserir_empresa

conn = conectar()

inserir_empresa(conn, dict(
    municipio="Unaí", setor="Agropecuária",
    nome_fantasia="Agropaulista", razao_social="Agropaulista Com e Representação Prod. Agrop. Unaí LTDA",
    cnpj="01.103.004/0001-68", situacao="ATIVA",
    logradouro="Rua Doutor Joaquim Brochado", numero="91", complemento=None,
    bairro="Capim Branco", cep="38610-108", site=None,
    fonte="Receita Federal (CNPJ público via BrasilAPI) + busca web", data_coleta="2026-08-26",
    observacoes="Comércio atacadista de defensivos agrícolas, adubos, fertilizantes e corretivos do solo.",
    socios=[("Neivaldo Cappellesso", "***.028.920-**", "Sócio-Administrador", 1),
            ("Jackeline Cappellesso Viana", "***.036.566-**", "Sócio", 0),
            ("Luiz Felipe Viana Cappellesso", "***.013.076-**", "Sócio", 0)],
    contatos=[("telefone_fixo", "(38) 3676-6063", 1), ("email", "agropaulista@agropaulistaunai.com.br", 1)],
))

conn.commit()
total = conn.execute("SELECT COUNT(*) FROM empresa").fetchone()[0]
print(f"Agropaulista inserida. Total de empresas no banco: {total}.")
conn.close()
