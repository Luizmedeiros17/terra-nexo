package br.com.terranexo.broker.domain;

import java.util.List;
import java.util.Optional;

/** Porta de acesso à base de corretores. Implementação atual é em memória. */
public interface BrokerRepository {

    List<Broker> findAll();

    Optional<Broker> findById(String id);

    Optional<Broker> findBySlug(String slug);

    /** Insere ou substitui um corretor, devolvendo a versão persistida. */
    Broker save(Broker broker);

    /** Remove o corretor. Não faz nada se o id não existir. */
    void deleteById(String id);

    /** Próximo id sequencial (cor-001, cor-002…). */
    String proximoId();
}
