package br.com.terranexo.property.domain;

import java.util.List;
import java.util.Optional;

/**
 * Porta de acesso aos imóveis. A implementação atual é em memória; a troca por
 * JPA/PostGIS não afeta os pacotes {@code application} e {@code api}.
 */
public interface PropertyRepository {

    List<Property> findAll();

    Optional<Property> findByCodeOrSlug(String codeOrSlug);

    Optional<Property> findById(String id);

    /** Insere ou substitui um imóvel, devolvendo a versão persistida. */
    Property save(Property property);

    /** Remove o imóvel. Não faz nada se o id não existir. */
    void deleteById(String id);

    /** Próximo código sequencial da categoria (URB-000113, RUR-000212…). */
    String proximoCodigo(Category categoria);
}
