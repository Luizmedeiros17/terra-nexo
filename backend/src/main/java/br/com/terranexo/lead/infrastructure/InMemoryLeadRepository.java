package br.com.terranexo.lead.infrastructure;

import br.com.terranexo.lead.domain.Lead;
import br.com.terranexo.lead.domain.LeadRepository;
import br.com.terranexo.shared.infrastructure.InMemoryStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Persistência em memória do protótipo. No MVP os leads nascem só da captação
 * do portal e do cadastro manual no painel — não há carga inicial de seed.
 */
@Repository
public class InMemoryLeadRepository extends InMemoryStore<Lead> implements LeadRepository {

    private final AtomicInteger sequencia = new AtomicInteger(1);

    public InMemoryLeadRepository() {
        super(List.of(), Lead::id);
    }

    @Override
    public boolean existsByEmailOrTelefoneAndImovel(String email, String telefone, String imovelCodigo) {
        return itens.stream().anyMatch(l ->
                Objects.equals(l.imovelCodigo(), imovelCodigo)
                        && ((email != null && email.equalsIgnoreCase(l.email()))
                        || (telefone != null && telefone.equals(l.telefone()))));
    }

    @Override
    public int proximaSequencia() {
        return sequencia.getAndIncrement();
    }
}
