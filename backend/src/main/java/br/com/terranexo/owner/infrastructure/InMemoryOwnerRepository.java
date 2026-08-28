package br.com.terranexo.owner.infrastructure;

import br.com.terranexo.owner.domain.Owner;
import br.com.terranexo.owner.domain.OwnerRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** Persistência em memória do protótipo — reinicia com o container (ver PropertyRepository). */
@Repository
public class InMemoryOwnerRepository implements OwnerRepository {

    private final List<Owner> proprietarios = new CopyOnWriteArrayList<>(OwnerSeed.carregar());

    @Override
    public List<Owner> findAll() {
        return List.copyOf(proprietarios);
    }

    @Override
    public Optional<Owner> findById(String id) {
        return proprietarios.stream().filter(o -> o.id().equals(id)).findFirst();
    }

    @Override
    public Owner save(Owner owner) {
        proprietarios.removeIf(o -> o.id().equals(owner.id()));
        proprietarios.add(owner);
        return owner;
    }

    @Override
    public void deleteById(String id) {
        proprietarios.removeIf(o -> o.id().equals(id));
    }

    @Override
    public String proximoId() {
        int maior = proprietarios.stream()
                .map(Owner::id)
                .filter(id -> id.startsWith("prop-"))
                .map(id -> id.substring(5))
                .mapToInt(n -> {
                    try {
                        return Integer.parseInt(n);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return "prop-%03d".formatted(maior + 1);
    }
}
