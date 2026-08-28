package br.com.terranexo.partner.infrastructure;

import br.com.terranexo.partner.domain.Partner;
import br.com.terranexo.partner.domain.PartnerRepository;
import org.springframework.stereotype.Repository;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Persistência em memória do protótipo. Ao adotar PostgreSQL, esta classe é
 * substituída por um adaptador JPA sobre o schema {@code prospeccao} — nenhuma
 * outra camada muda.
 */
@Repository
public class InMemoryPartnerRepository implements PartnerRepository {

    private final List<Partner> parceiros = new CopyOnWriteArrayList<>(PartnerSeed.carregar());

    @Override
    public List<Partner> findAll() {
        return List.copyOf(parceiros);
    }

    @Override
    public Optional<Partner> findById(String id) {
        return parceiros.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    @Override
    public List<Partner> findByMunicipio(String municipio) {
        String alvo = normalizar(municipio);
        return parceiros.stream().filter(p -> normalizar(p.municipio()).equals(alvo)).toList();
    }

    @Override
    public Partner save(Partner partner) {
        parceiros.removeIf(p -> p.id().equals(partner.id()));
        parceiros.add(partner);
        return partner;
    }

    @Override
    public void deleteById(String id) {
        parceiros.removeIf(p -> p.id().equals(id));
    }

    @Override
    public String proximoId() {
        int maior = parceiros.stream()
                .map(Partner::id)
                .filter(id -> id.startsWith("prt-"))
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
        return "prt-%03d".formatted(maior + 1);
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }
}
