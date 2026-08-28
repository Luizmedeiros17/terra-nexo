package br.com.terranexo.lead.infrastructure;

import br.com.terranexo.lead.domain.Lead;
import br.com.terranexo.lead.domain.LeadRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Persistência em memória do protótipo. No MVP os leads nascem só da captação
 * do portal e do cadastro manual no painel — não há carga inicial de seed.
 */
@Repository
public class InMemoryLeadRepository implements LeadRepository {

    private final List<Lead> leads = new CopyOnWriteArrayList<>();
    private final AtomicInteger sequencia = new AtomicInteger(1);

    @Override
    public List<Lead> findAll() {
        return List.copyOf(leads);
    }

    @Override
    public Optional<Lead> findById(String id) {
        return leads.stream().filter(l -> l.id().equals(id)).findFirst();
    }

    @Override
    public boolean existsByEmailOrTelefoneAndImovel(String email, String telefone, String imovelCodigo) {
        return leads.stream().anyMatch(l ->
                Objects.equals(l.imovelCodigo(), imovelCodigo)
                        && ((email != null && email.equalsIgnoreCase(l.email()))
                        || (telefone != null && telefone.equals(l.telefone()))));
    }

    @Override
    public Lead save(Lead lead) {
        leads.removeIf(l -> l.id().equals(lead.id()));
        leads.add(lead);
        return lead;
    }

    @Override
    public void deleteById(String id) {
        leads.removeIf(l -> l.id().equals(id));
    }

    @Override
    public int proximaSequencia() {
        return sequencia.getAndIncrement();
    }
}
