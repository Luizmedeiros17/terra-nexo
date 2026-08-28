-- Base de Prospecção — Clientes e Parceiros (Terra Nexo)
-- Schema normalizado (SQLite). Espelha docs/prospeccao/db/V1__create_prospeccao_schema.sql
-- (versão PostgreSQL, pronta para virar migration Flyway quando o backend adotar persistência).
--
-- Por que normalizado: a planilha original é uma tabela única (achatada) com colunas fixas
-- para telefone fixo/celular/whatsapp e um único titular por empresa. Isso não escala (uma
-- empresa pode ter vários sócios e vários telefones) e não guarda histórico do funil de
-- contato. Aqui cada entidade multivalorada vira tabela própria, referenciada por empresa_id.

PRAGMA foreign_keys = ON;

CREATE TABLE municipio (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nome         TEXT NOT NULL,
    uf           TEXT NOT NULL CHECK (length(uf) = 2),
    codigo_ibge  TEXT,
    UNIQUE (nome, uf)
);

CREATE TABLE setor (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nome      TEXT NOT NULL UNIQUE,
    descricao TEXT
);

CREATE TABLE empresa (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    municipio_id        INTEGER NOT NULL REFERENCES municipio (id),
    setor_id            INTEGER NOT NULL REFERENCES setor (id),
    nome_fantasia       TEXT NOT NULL,
    razao_social        TEXT,
    cnpj                TEXT UNIQUE,               -- formato oficial Receita Federal: NN.NNN.NNN/NNNN-NN
    situacao_cadastral  TEXT,                       -- ATIVA, BAIXADA, EM LIQUIDACAO, ... (Receita Federal)
    logradouro          TEXT,
    numero              TEXT,
    complemento         TEXT,
    bairro              TEXT,
    cep                 TEXT,
    site                TEXT,
    status_prospeccao   TEXT NOT NULL DEFAULT 'Não iniciado'
                         CHECK (status_prospeccao IN (
                             'Não iniciado', 'Pesquisado', 'Contatado', 'Respondeu',
                             'Reunião marcada', 'Proposta enviada', 'Convertido',
                             'Sem interesse', 'Contato inválido'
                         )),
    fonte_informacao    TEXT,
    data_coleta         DATE,
    observacoes         TEXT,
    criado_em           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_empresa_municipio ON empresa (municipio_id);
CREATE INDEX idx_empresa_setor ON empresa (setor_id);
CREATE INDEX idx_empresa_status ON empresa (status_prospeccao);

-- Sócios/titulares — 0..N por empresa (CPF é publicado mascarado pela própria Receita
-- Federal no QSA; nunca gravar aqui o CPF completo de terceiros sem consentimento — LGPD).
CREATE TABLE socio (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    empresa_id     INTEGER NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    nome           TEXT NOT NULL,
    cpf_mascarado  TEXT,                            -- ex.: ***.072.726-**
    qualificacao   TEXT,                             -- Sócio-Administrador, Diretor, Presidente...
    principal      INTEGER NOT NULL DEFAULT 0 CHECK (principal IN (0, 1))
);

CREATE INDEX idx_socio_empresa ON socio (empresa_id);

-- Contatos — 0..N por empresa, por tipo (permite vários telefones/whatsapp/e-mails).
CREATE TABLE contato (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    empresa_id  INTEGER NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    tipo        TEXT NOT NULL CHECK (tipo IN ('telefone_fixo', 'telefone_celular', 'whatsapp', 'email')),
    valor       TEXT NOT NULL,
    principal   INTEGER NOT NULL DEFAULT 0 CHECK (principal IN (0, 1))
);

CREATE INDEX idx_contato_empresa ON contato (empresa_id);
CREATE INDEX idx_contato_tipo ON contato (tipo);

-- Geolocalização — 1:1 com empresa (fica em tabela própria por ser opcional/derivada
-- e para não poluir a tabela principal com colunas quase sempre vazias no início).
CREATE TABLE geolocalizacao (
    empresa_id  INTEGER PRIMARY KEY REFERENCES empresa (id) ON DELETE CASCADE,
    latitude    REAL,
    longitude   REAL,
    maps_url    TEXT
);

-- Histórico do funil de prospecção — cada mudança de status vira uma linha aqui.
-- É o que dá rastreabilidade real de "atualização e manutenção" que uma única coluna
-- de status na planilha não oferece.
CREATE TABLE status_historico (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    empresa_id   INTEGER NOT NULL REFERENCES empresa (id) ON DELETE CASCADE,
    status       TEXT NOT NULL,
    data_status  DATE NOT NULL,
    responsavel  TEXT,
    observacao   TEXT
);

CREATE INDEX idx_status_historico_empresa ON status_historico (empresa_id);

-- View auxiliar: recompõe a visão "achatada" (uma linha por empresa) para exportação
-- e para quem quiser consultar sem fazer JOIN manual.
CREATE VIEW vw_empresa_completa AS
SELECT
    e.id,
    m.nome AS cidade,
    m.uf,
    s.nome AS setor,
    e.nome_fantasia,
    e.razao_social,
    e.cnpj,
    e.situacao_cadastral,
    (SELECT sc.nome FROM socio sc WHERE sc.empresa_id = e.id
        ORDER BY sc.principal DESC, sc.id LIMIT 1) AS titular_principal,
    (SELECT sc.cpf_mascarado FROM socio sc WHERE sc.empresa_id = e.id
        ORDER BY sc.principal DESC, sc.id LIMIT 1) AS cpf_titular_principal,
    trim(coalesce(e.logradouro, '') || ' ' || coalesce(e.numero, '') || ', ' ||
         coalesce(e.bairro, '') || ', ' || m.nome || '-' || m.uf ||
         CASE WHEN e.cep IS NOT NULL THEN ', ' || e.cep ELSE '' END) AS endereco_completo,
    (SELECT c.valor FROM contato c WHERE c.empresa_id = e.id AND c.tipo = 'telefone_fixo'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS telefone_fixo,
    (SELECT c.valor FROM contato c WHERE c.empresa_id = e.id AND c.tipo = 'telefone_celular'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS telefone_celular,
    (SELECT c.valor FROM contato c WHERE c.empresa_id = e.id AND c.tipo = 'whatsapp'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS whatsapp,
    (SELECT c.valor FROM contato c WHERE c.empresa_id = e.id AND c.tipo = 'email'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS email,
    g.maps_url AS geolocalizacao,
    e.site,
    e.status_prospeccao,
    e.fonte_informacao,
    e.data_coleta,
    e.observacoes
FROM empresa e
JOIN municipio m ON m.id = e.municipio_id
JOIN setor s ON s.id = e.setor_id
LEFT JOIN geolocalizacao g ON g.empresa_id = e.id;
