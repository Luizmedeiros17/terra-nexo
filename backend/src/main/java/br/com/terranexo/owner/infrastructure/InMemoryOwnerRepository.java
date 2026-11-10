package br.com.terranexo.owner.infrastructure;

import br.com.terranexo.owner.domain.Owner;
import br.com.terranexo.owner.domain.OwnerRepository;
import br.com.terranexo.shared.infrastructure.InMemoryStore;
import org.springframework.stereotype.Repository;

/** Persistência em memória do protótipo — reinicia com o container (ver {@link InMemoryStore}). */
@Repository
public class InMemoryOwnerRepository extends InMemoryStore<Owner> implements OwnerRepository {

    public InMemoryOwnerRepository() {
        super(OwnerSeed.carregar(), Owner::id);
    }

    @Override
    public String proximoId() {
        return proximoIdSequencial("prop");
    }
}
