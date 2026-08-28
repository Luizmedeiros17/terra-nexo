package br.com.terranexo.broker.infrastructure;

import br.com.terranexo.broker.domain.Broker;
import br.com.terranexo.broker.domain.BrokerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Persistência em memória do protótipo — reinicia com o container (ver PropertyRepository). */
@Repository
public class InMemoryBrokerRepository implements BrokerRepository {

    private final List<Broker> corretores = new CopyOnWriteArrayList<>(BrokerSeed.carregar());

    @Override
    public List<Broker> findAll() {
        return List.copyOf(corretores);
    }

    @Override
    public Optional<Broker> findById(String id) {
        return corretores.stream().filter(b -> b.id().equals(id)).findFirst();
    }

    @Override
    public Optional<Broker> findBySlug(String slug) {
        return corretores.stream().filter(b -> b.slug().equals(slug)).findFirst();
    }

    @Override
    public Broker save(Broker broker) {
        corretores.removeIf(b -> b.id().equals(broker.id()));
        corretores.add(broker);
        return broker;
    }

    @Override
    public void deleteById(String id) {
        corretores.removeIf(b -> b.id().equals(id));
    }

    @Override
    public String proximoId() {
        int maior = corretores.stream()
                .map(Broker::id)
                .filter(id -> id.startsWith("cor-"))
                .map(id -> id.substring(4))
                .mapToInt(n -> {
                    try {
                        return Integer.parseInt(n);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return "cor-%03d".formatted(maior + 1);
    }
}
