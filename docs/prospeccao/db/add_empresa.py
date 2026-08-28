# -*- coding: utf-8 -*-
"""
Inserção incremental de uma empresa na base normalizada — usar isto (e não
load_data.py) para adicionar prospects depois da carga inicial, porque
load_data.py apaga e recria o banco do zero (perderia status/histórico já
atualizados pelo time comercial).

Uso como biblioteca:

    from add_empresa import inserir_empresa, conectar

    conn = conectar()
    inserir_empresa(conn, dict(
        municipio="Paracatu", setor="Agropecuária",
        nome_fantasia="...", razao_social="...", cnpj="...", situacao="ATIVA",
        logradouro="...", numero="...", complemento=None, bairro="...", cep="...",
        site=None, fonte="Receita Federal (BrasilAPI) + ...", data_coleta="2026-08-27",
        observacoes=None,
        socios=[("Nome", "***.999.999-**", "Sócio-Administrador", 1)],
        contatos=[("telefone_fixo", "(38) 0000-0000", 1)],
    ))
    conn.commit()
    conn.close()

Uso como CLI: só valida e mostra o schema esperado (não há input interativo
neste ambiente) — prefira editar um script chamador como load_data.py fez.
"""
import os
import sqlite3
import urllib.parse

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "prospeccao.db")


def conectar():
    # timeout alto + busy_timeout: evita "database is locked" quando mais de um
    # processo/agente grava no mesmo prospeccao.db ao mesmo tempo (ex.: pesquisa
    # de duas cidades rodando em paralelo em background).
    conn = sqlite3.connect(DB_PATH, timeout=30)
    conn.execute("PRAGMA foreign_keys = ON")
    conn.execute("PRAGMA busy_timeout = 30000")
    return conn


def maps_link(logradouro, numero, bairro, municipio):
    partes = [logradouro, numero, bairro, municipio]
    endereco = ", ".join(p for p in partes if p)
    return "https://www.google.com/maps/search/?api=1&query=" + urllib.parse.quote(endereco)


def _get_or_create_municipio(conn, nome, uf=None):
    row = conn.execute("SELECT id, uf FROM municipio WHERE nome = ?", (nome,)).fetchone()
    if row:
        return row[0]
    if not uf:
        raise ValueError(f"Município '{nome}' não existe ainda — informe uf= para criá-lo.")
    cur = conn.execute("INSERT INTO municipio (nome, uf) VALUES (?, ?)", (nome, uf))
    return cur.lastrowid


def _get_setor_id(conn, nome):
    row = conn.execute("SELECT id FROM setor WHERE nome = ?", (nome,)).fetchone()
    if not row:
        opcoes = [r[0] for r in conn.execute("SELECT nome FROM setor").fetchall()]
        raise ValueError(f"Setor '{nome}' não cadastrado. Opções: {opcoes} "
                          f"(criar um setor novo é decisão de negócio — inserir manualmente na tabela 'setor').")
    return row[0]


def inserir_empresa(conn, emp, uf_municipio=None):
    """Insere uma empresa (+ sócios, contatos, geolocalização, 1º status do histórico).
    Não commita — chame conn.commit() depois (permite inserir várias em lote)."""
    municipio_id = _get_or_create_municipio(conn, emp["municipio"], uf_municipio)
    setor_id = _get_setor_id(conn, emp["setor"])

    cur = conn.execute(
        """INSERT INTO empresa
           (municipio_id, setor_id, nome_fantasia, razao_social, cnpj, situacao_cadastral,
            logradouro, numero, complemento, bairro, cep, site, status_prospeccao,
            fonte_informacao, data_coleta, observacoes)
           VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pesquisado', ?, ?, ?)""",
        (municipio_id, setor_id, emp["nome_fantasia"], emp.get("razao_social"), emp.get("cnpj"),
         emp.get("situacao"), emp.get("logradouro"), emp.get("numero"), emp.get("complemento"),
         emp.get("bairro"), emp.get("cep"), emp.get("site"),
         emp.get("fonte"), emp.get("data_coleta"), emp.get("observacoes")),
    )
    empresa_id = cur.lastrowid

    for nome, cpf, qualificacao, principal in emp.get("socios", []):
        conn.execute(
            "INSERT INTO socio (empresa_id, nome, cpf_mascarado, qualificacao, principal) "
            "VALUES (?, ?, ?, ?, ?)",
            (empresa_id, nome, cpf, qualificacao, principal),
        )

    for tipo, valor, principal in emp.get("contatos", []):
        conn.execute(
            "INSERT INTO contato (empresa_id, tipo, valor, principal) VALUES (?, ?, ?, ?)",
            (empresa_id, tipo, valor, principal),
        )

    conn.execute(
        "INSERT INTO geolocalizacao (empresa_id, maps_url) VALUES (?, ?)",
        (empresa_id, maps_link(emp.get("logradouro"), emp.get("numero"), emp.get("bairro"), emp["municipio"])),
    )

    conn.execute(
        "INSERT INTO status_historico (empresa_id, status, data_status, responsavel, observacao) "
        "VALUES (?, 'Pesquisado', ?, 'pesquisa automatizada', 'Registro inicial carregado na base.')",
        (empresa_id, emp.get("data_coleta")),
    )

    return empresa_id


def atualizar_status(conn, empresa_id, novo_status, responsavel, observacao, data_status):
    """Atualiza o status corrente e registra a mudança no histórico (auditoria do funil)."""
    conn.execute("UPDATE empresa SET status_prospeccao = ? WHERE id = ?", (novo_status, empresa_id))
    conn.execute(
        "INSERT INTO status_historico (empresa_id, status, data_status, responsavel, observacao) "
        "VALUES (?, ?, ?, ?, ?)",
        (empresa_id, novo_status, data_status, responsavel, observacao),
    )


if __name__ == "__main__":
    print(__doc__)
