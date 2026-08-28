# -*- coding: utf-8 -*-
"""
Gera backend/src/main/resources/partners-seed.json a partir de prospeccao.db —
a carga inicial do módulo `partner` do backend Spring Boot (repositório em
memória, mesmo padrão de property/infrastructure/PropertySeed.java).

Rode de novo sempre que o banco normalizado mudar (nova cidade, novos
registros, status atualizado) e depois rebuilde o backend.
"""
import json
import os
import sqlite3

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "prospeccao.db")
OUT_PATH = os.path.join(BASE_DIR, "..", "..", "..", "backend", "src", "main", "resources", "partners-seed.json")

SETOR_ENUM = {
    "Agropecuária": "AGROPECUARIA",
    "Imobiliária": "IMOBILIARIA",
    "Irrigação": "IRRIGACAO",
    "Silos/Armazéns": "SILOS_ARMAZENS",
    "Leilão de Animais": "LEILAO_ANIMAIS",
    "Laticínios": "LATICINIOS",
}

STATUS_ENUM = {
    "Não iniciado": "NAO_INICIADO",
    "Pesquisado": "PESQUISADO",
    "Contatado": "CONTATADO",
    "Respondeu": "RESPONDEU",
    "Reunião marcada": "REUNIAO_MARCADA",
    "Proposta enviada": "PROPOSTA_ENVIADA",
    "Convertido": "CONVERTIDO",
    "Sem interesse": "SEM_INTERESSE",
    "Contato inválido": "CONTATO_INVALIDO",
}

CONTATO_TIPO_ENUM = {
    "telefone_fixo": "TELEFONE_FIXO",
    "telefone_celular": "TELEFONE_CELULAR",
    "whatsapp": "WHATSAPP",
    "email": "EMAIL",
}


def iso_datetime(data_str):
    return f"{data_str}T00:00:00-03:00" if data_str else None


def main():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row

    empresas = conn.execute("""
        SELECT e.*, m.nome AS municipio_nome, m.uf AS uf, s.nome AS setor_nome
        FROM empresa e
        JOIN municipio m ON m.id = e.municipio_id
        JOIN setor s ON s.id = e.setor_id
        ORDER BY e.id
    """).fetchall()

    partners = []
    for e in empresas:
        eid = e["id"]
        socios = conn.execute(
            "SELECT nome, cpf_mascarado, qualificacao, principal FROM socio "
            "WHERE empresa_id = ? ORDER BY principal DESC, id", (eid,)
        ).fetchall()
        contatos = conn.execute(
            "SELECT tipo, valor, principal FROM contato WHERE empresa_id = ? ORDER BY principal DESC, id", (eid,)
        ).fetchall()
        geo = conn.execute("SELECT maps_url FROM geolocalizacao WHERE empresa_id = ?", (eid,)).fetchone()
        historico = conn.execute(
            "SELECT status, data_status, responsavel, observacao FROM status_historico "
            "WHERE empresa_id = ? ORDER BY id", (eid,)
        ).fetchall()

        criado_em = iso_datetime(e["data_coleta"])

        partners.append({
            "id": f"prt-{eid:03d}",
            "municipio": e["municipio_nome"],
            "uf": e["uf"],
            "setor": SETOR_ENUM[e["setor_nome"]],
            "nomeFantasia": e["nome_fantasia"],
            "razaoSocial": e["razao_social"],
            "cnpj": e["cnpj"],
            "situacaoCadastral": e["situacao_cadastral"],
            "endereco": {
                "logradouro": e["logradouro"],
                "numero": e["numero"],
                "complemento": e["complemento"],
                "bairro": e["bairro"],
                "cep": e["cep"],
            },
            "site": e["site"],
            "geolocalizacao": {
                "mapsUrl": geo["maps_url"] if geo else None,
                "latitude": None,
                "longitude": None,
            },
            "titulares": [
                {
                    "nome": s["nome"],
                    "cpfMascarado": s["cpf_mascarado"],
                    "qualificacao": s["qualificacao"],
                    "principal": bool(s["principal"]),
                } for s in socios
            ],
            "contatos": [
                {
                    "tipo": CONTATO_TIPO_ENUM[c["tipo"]],
                    "valor": c["valor"],
                    "principal": bool(c["principal"]),
                } for c in contatos
            ],
            "status": STATUS_ENUM[e["status_prospeccao"]],
            "fonteInformacao": e["fonte_informacao"],
            "dataColeta": e["data_coleta"],
            "observacoes": e["observacoes"],
            "criadoEm": criado_em,
            "atualizadoEm": criado_em,
            "historicoStatus": [
                {
                    "status": STATUS_ENUM[h["status"]],
                    "data": h["data_status"],
                    "responsavel": h["responsavel"],
                    "observacao": h["observacao"],
                } for h in historico
            ],
        })

    os.makedirs(os.path.dirname(OUT_PATH), exist_ok=True)
    with open(OUT_PATH, "w", encoding="utf-8") as f:
        json.dump(partners, f, ensure_ascii=False, indent=2)

    print(f"Gerado {OUT_PATH} com {len(partners)} parceiros.")
    conn.close()


if __name__ == "__main__":
    main()
