package br.com.terranexo.property.infrastructure;

import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persistência em memória do protótipo. Ao adotar PostgreSQL/PostGIS, esta classe
 * é substituída por um adaptador JPA — nenhuma outra camada muda.
 */
@Repository
public class InMemoryPropertyRepository implements PropertyRepository {

    private static final Pattern CODIGO_NO_FINAL = Pattern.compile("([a-zA-Z]{3}-\\d{6})$");

    private final List<Property> imoveis = new CopyOnWriteArrayList<>(PropertySeed.carregar());

    @Override
    public List<Property> findAll() {
        return List.copyOf(imoveis);
    }

    /**
     * Aceita o código (RUR-000201), o slug puro ou a URL pública completa
     * ({@code slug-codigo}) — o código no final mantém o link estável mesmo
     * que o título mude (seção 7).
     */
    @Override
    public Optional<Property> findByCodeOrSlug(String codeOrSlug) {
        if (codeOrSlug == null || codeOrSlug.isBlank()) {
            return Optional.empty();
        }
        String alvo = codeOrSlug.trim();
        Optional<Property> direto = imoveis.stream()
                .filter(p -> p.slug().equalsIgnoreCase(alvo) || p.codigo().equalsIgnoreCase(alvo))
                .findFirst();
        if (direto.isPresent()) {
            return direto;
        }
        Matcher m = CODIGO_NO_FINAL.matcher(alvo);
        if (m.find()) {
            String codigo = m.group(1);
            return imoveis.stream().filter(p -> p.codigo().equalsIgnoreCase(codigo)).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Property> findById(String id) {
        return imoveis.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    @Override
    public Property save(Property property) {
        imoveis.removeIf(p -> p.id().equals(property.id()));
        imoveis.add(0, property);
        return property;
    }

    @Override
    public void deleteById(String id) {
        imoveis.removeIf(p -> p.id().equals(id));
    }

    @Override
    public String proximoCodigo(Category categoria) {
        String prefixo = categoria == Category.RURAL ? "RUR" : "URB";
        int maior = imoveis.stream()
                .map(Property::codigo)
                .filter(c -> c.startsWith(prefixo + "-"))
                .map(c -> c.substring(prefixo.length() + 1))
                .mapToInt(n -> {
                    try {
                        return Integer.parseInt(n);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(categoria == Category.RURAL ? 200 : 100);
        return "%s-%06d".formatted(prefixo, maior + 1);
    }
}
