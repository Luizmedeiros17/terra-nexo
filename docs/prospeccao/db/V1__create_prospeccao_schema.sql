-- Base de Prospecção — Clientes e Parceiros (Terra Nexo)
-- Versão PostgreSQL do schema em docs/prospeccao/db/schema_sqlite.sql.
--
-- Nomeada V1__create_prospeccao_schema.sql de propósito: quando o backend adotar
-- Flyway (ver README > Próximos passos > "Substituir o repositório em memória por
-- PostgreSQL/PostGIS com Flyway"), este arquivo pode ser copiado para
-- backend/src/main/resources/db/migration/ ajustando apenas o número de versão
-- para não colidir com as migrations que já existirem no módulo `property`/`lead`
-- naquele momento. Schema isolado (`prospeccao`) para não acoplar com as tabelas
-- do catálogo de imóveis.

CREATE SCHEMA IF NOT EXISTS prospeccao;

CREATE TABLE prospeccao.municipio (
    id           BIGSERIAL PRIMARY KEY,
    nome         VARCHAR(120) NOT NULL,
    uf           CHAR(2) NOT NULL,
    codigo_ibge  VARCHAR(7),
    UNIQUE (nome, uf)
);

CREATE TABLE prospeccao.setor (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(80) NOT NULL UNIQUE,
    descricao TEXT
);

CREATE TABLE prospeccao.empresa (
    id                  BIGSERIAL PRIMARY KEY,
    municipio_id        BIGINT NOT NULL REFERENCES prospeccao.municipio (id),
    setor_id            BIGINT NOT NULL REFERENCES prospeccao.setor (id),
    nome_fantasia       VARCHAR(200) NOT NULL,
    razao_social        VARCHAR(200),
    cnpj                VARCHAR(18) UNIQUE,
    situacao_cadastral  VARCHAR(30),
    logradouro          VARCHAR(200),
    numero              VARCHAR(20),
    complemento         VARCHAR(120),
    bairro              VARCHAR(120),
    cep                 VARCHAR(9),
    site                VARCHAR(255),
    status_prospeccao   VARCHAR(30) NOT NULL DEFAULT 'Não iniciado'
                         CHECK (status_prospeccao IN (
                             'Não iniciado', 'Pesquisado', 'Contatado', 'Respondeu',
                             'Reunião marcada', 'Proposta enviada', 'Convertido',
                             'Sem interesse', 'Contato inválido'
                         )),
    fonte_informacao    VARCHAR(255),
    data_coleta         DATE,
    observacoes         TEXT,
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_empresa_municipio ON prospeccao.empresa (municipio_id);
CREATE INDEX idx_empresa_setor ON prospeccao.empresa (setor_id);
CREATE INDEX idx_empresa_status ON prospeccao.empresa (status_prospeccao);

CREATE TABLE prospeccao.socio (
    id             BIGSERIAL PRIMARY KEY,
    empresa_id     BIGINT NOT NULL REFERENCES prospeccao.empresa (id) ON DELETE CASCADE,
    nome           VARCHAR(200) NOT NULL,
    cpf_mascarado  VARCHAR(20),
    qualificacao   VARCHAR(80),
    principal      BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_socio_empresa ON prospeccao.socio (empresa_id);

CREATE TABLE prospeccao.contato (
    id          BIGSERIAL PRIMARY KEY,
    empresa_id  BIGINT NOT NULL REFERENCES prospeccao.empresa (id) ON DELETE CASCADE,
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('telefone_fixo', 'telefone_celular', 'whatsapp', 'email')),
    valor       VARCHAR(255) NOT NULL,
    principal   BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_contato_empresa ON prospeccao.contato (empresa_id);
CREATE INDEX idx_contato_tipo ON prospeccao.contato (tipo);

CREATE TABLE prospeccao.geolocalizacao (
    empresa_id  BIGINT PRIMARY KEY REFERENCES prospeccao.empresa (id) ON DELETE CASCADE,
    latitude    DOUBLE PRECISION,
    longitude   DOUBLE PRECISION,
    maps_url    VARCHAR(500)
    -- Se/quando o backend ligar PostGIS (já usado em `property`), trocar lat/long
    -- por uma coluna geography(Point, 4326) e reindexar com GiST, mantendo maps_url
    -- como fallback legível.
);

CREATE TABLE prospeccao.status_historico (
    id           BIGSERIAL PRIMARY KEY,
    empresa_id   BIGINT NOT NULL REFERENCES prospeccao.empresa (id) ON DELETE CASCADE,
    status       VARCHAR(30) NOT NULL,
    data_status  DATE NOT NULL,
    responsavel  VARCHAR(120),
    observacao   TEXT
);

CREATE INDEX idx_status_historico_empresa ON prospeccao.status_historico (empresa_id);

CREATE OR REPLACE FUNCTION prospeccao.touch_atualizado_em()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_empresa_atualizado_em
    BEFORE UPDATE ON prospeccao.empresa
    FOR EACH ROW EXECUTE FUNCTION prospeccao.touch_atualizado_em();

CREATE VIEW prospeccao.vw_empresa_completa AS
SELECT
    e.id,
    m.nome AS cidade,
    m.uf,
    s.nome AS setor,
    e.nome_fantasia,
    e.razao_social,
    e.cnpj,
    e.situacao_cadastral,
    (SELECT sc.nome FROM prospeccao.socio sc WHERE sc.empresa_id = e.id
        ORDER BY sc.principal DESC, sc.id LIMIT 1) AS titular_principal,
    (SELECT sc.cpf_mascarado FROM prospeccao.socio sc WHERE sc.empresa_id = e.id
        ORDER BY sc.principal DESC, sc.id LIMIT 1) AS cpf_titular_principal,
    trim(concat_ws(' ', e.logradouro, e.numero)) || ', ' || coalesce(e.bairro, '') || ', ' ||
        m.nome || '-' || m.uf || coalesce(', ' || e.cep, '') AS endereco_completo,
    (SELECT c.valor FROM prospeccao.contato c WHERE c.empresa_id = e.id AND c.tipo = 'telefone_fixo'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS telefone_fixo,
    (SELECT c.valor FROM prospeccao.contato c WHERE c.empresa_id = e.id AND c.tipo = 'telefone_celular'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS telefone_celular,
    (SELECT c.valor FROM prospeccao.contato c WHERE c.empresa_id = e.id AND c.tipo = 'whatsapp'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS whatsapp,
    (SELECT c.valor FROM prospeccao.contato c WHERE c.empresa_id = e.id AND c.tipo = 'email'
        ORDER BY c.principal DESC, c.id LIMIT 1) AS email,
    g.maps_url AS geolocalizacao,
    e.site,
    e.status_prospeccao,
    e.fonte_informacao,
    e.data_coleta,
    e.observacoes
FROM prospeccao.empresa e
JOIN prospeccao.municipio m ON m.id = e.municipio_id
JOIN prospeccao.setor s ON s.id = e.setor_id
LEFT JOIN prospeccao.geolocalizacao g ON g.empresa_id = e.id;

COMMENT ON SCHEMA prospeccao IS 'Base de prospecção de clientes e parceiros (comercial) — independente do catálogo público de imóveis.';
COMMENT ON TABLE prospeccao.empresa IS 'Uma linha por empresa/organização prospectada. CNPJ e situação cadastral vêm da Receita Federal (dado público).';
COMMENT ON TABLE prospeccao.socio IS 'Sócios/titulares por empresa. cpf_mascarado replica o formato de divulgação oficial da Receita Federal (QSA) — nunca gravar CPF completo de terceiros sem base legal (LGPD).';
