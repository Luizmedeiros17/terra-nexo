package br.com.terranexo.partner.infrastructure;

import br.com.terranexo.partner.domain.Partner;
import br.com.terranexo.partner.domain.PartnerRepository;
import br.com.terranexo.shared.infrastructure.InMemoryStore;
import br.com.terranexo.shared.text.Textos;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persistência em memória do protótipo. Ao adotar PostgreSQL, esta classe é
 * substituída por um adaptador JPA sobre o schema {@code prospeccao} — nenhuma
 * outra camada muda.
 */
@Repository
public class InMemoryPartnerRepository extends InMemoryStore<Partner> implements PartnerRepository {

    public InMemoryPartnerRepository() {
        super(PartnerSeed.carregar(), Partner::id);
    }

    @Override
    public List<Partner> findByMunicipio(String municipio) {
        String alvo = Textos.normalizar(municipio);
        return itens.stream().filter(p -> Textos.normalizar(p.municipio()).equals(alvo)).toList();
    }

    @Override
    public String proximoId() {
        return proximoIdSequencial("prt");
    }
}
