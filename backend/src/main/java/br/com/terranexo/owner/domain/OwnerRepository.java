package br.com.terranexo.owner.domain;

import java.util.List;
import java.util.Optional;

/** Porta de acesso à base de proprietários. Implementação atual é em memória. */
public interface OwnerRepository {

    List<Owner> findAll();

    Optional<Owner> findById(String id);

    /** Insere ou substitui um proprietário, devolvendo a versão persistida. */
    Owner save(Owner owner);

    /** Remove o proprietário. Não faz nada se o id não existir. */
    void deleteById(String id);

    /** Próximo id sequencial (prop-001, prop-002…). */
    String proximoId();
}
