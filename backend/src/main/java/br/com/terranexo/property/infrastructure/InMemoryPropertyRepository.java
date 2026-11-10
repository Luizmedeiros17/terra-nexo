package br.com.terranexo.property.infrastructure;

import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import br.com.terranexo.shared.infrastructure.InMemoryStore;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Persistência em memória do protótipo. Ao adotar PostgreSQL/PostGIS, esta classe
 * é substituída por um adaptador JPA — nenhuma outra camada muda. Imóveis salvos
 * vão para o início da lista (mais recentes primeiro).
 */
@Repository
public class InMemoryPropertyRepository extends InMemoryStore<Property> implements PropertyRepository {

    private static final Pattern CODIGO_NO_FINAL = Pattern.compile("([a-zA-Z]{3}-\\d{6})$");

    public InMemoryPropertyRepository() {
        super(PropertySeed.carregar(), Property::id, true);
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
        Optional<Property> direto = itens.stream()
                .filter(p -> p.slug().equalsIgnoreCase(alvo) || p.codigo().equalsIgnoreCase(alvo))
                .findFirst();
        if (direto.isPresent()) {
            return direto;
        }
        Matcher m = CODIGO_NO_FINAL.matcher(alvo);
        if (m.find()) {
            String codigo = m.group(1);
            return itens.stream().filter(p -> p.codigo().equalsIgnoreCase(codigo)).findFirst();
        }
        return Optional.empty();
    }

    @Override
    public String proximoCodigo(Category categoria) {
        boolean rural = categoria == Category.RURAL;
        return proximoSequencial(itens.stream().map(Property::codigo), rural ? "RUR" : "URB", 6, rural ? 200 : 100);
    }
}
