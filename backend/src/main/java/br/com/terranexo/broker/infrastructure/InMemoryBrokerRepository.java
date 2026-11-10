package br.com.terranexo.broker.infrastructure;

import br.com.terranexo.broker.domain.Broker;
import br.com.terranexo.broker.domain.BrokerRepository;
import br.com.terranexo.shared.infrastructure.InMemoryStore;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Persistência em memória do protótipo — reinicia com o container (ver {@link InMemoryStore}). */
@Repository
public class InMemoryBrokerRepository extends InMemoryStore<Broker> implements BrokerRepository {

    public InMemoryBrokerRepository() {
        super(BrokerSeed.carregar(), Broker::id);
    }

    @Override
    public Optional<Broker> findBySlug(String slug) {
        return itens.stream().filter(b -> b.slug().equals(slug)).findFirst();
    }

    @Override
    public String proximoId() {
        return proximoIdSequencial("cor");
    }
}
