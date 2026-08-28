package br.com.terranexo.partner.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de acesso à base de prospecção. A implementação atual é em memória,
 * carregada do mesmo dado normalizado usado em docs/prospeccao/db/prospeccao.db
 * (ver docs/prospeccao/db/V1__create_prospeccao_schema.sql para a versão
 * PostgreSQL, pronta para virar Flyway migration quando o backend adotar
 * persistência — a troca não afeta os pacotes {@code application} e {@code api}).
 */
public interface PartnerRepository {

    List<Partner> findAll();

    Optional<Partner> findById(String id);

    List<Partner> findByMunicipio(String municipio);

    /** Insere ou substitui um parceiro, devolvendo a versão persistida. */
    Partner save(Partner partner);

    /** Remove o parceiro. Não faz nada se o id não existir. */
    void deleteById(String id);

    /** Próximo id sequencial (prt-078, prt-079…). */
    String proximoId();
}
