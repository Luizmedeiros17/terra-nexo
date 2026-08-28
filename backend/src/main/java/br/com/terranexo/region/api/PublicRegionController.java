package br.com.terranexo.region.api;

import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Regiões atendidas (seções 6 e 20). São páginas editoriais no MVP completo;
 * aqui expõem nome, slug e a contagem de imóveis publicados para a home.
 */
@RestController
@RequestMapping("/api/v1/public/regions")
@Tag(name = "Regiões", description = "Regiões atendidas pela imobiliária")
public class PublicRegionController {

    private final PropertyRepository repository;

    public PublicRegionController(PropertyRepository repository) {
        this.repository = repository;
    }

    private static final List<Region> CATALOGO = List.of(
            new Region("goias-sudeste", "Goiás — Sudeste goiano", "GO", "RURAL",
                    "Cristalina e Catalão: alta tecnologia, irrigação por pivô e grãos.",
                    "https://images.unsplash.com/photo-1719253860282-cd3f93da6548?auto=format&fit=crop&w=900&q=70", 0),
            new Region("noroeste-mineiro", "Minas Gerais — Noroeste mineiro", "MG", "RURAL",
                    "Unaí, Paracatu e João Pinheiro: grãos, algodão e pecuária leiteira.",
                    "https://images.unsplash.com/photo-1742020972203-8a5b1a83cdd6?auto=format&fit=crop&w=900&q=70", 0),
            new Region("entorno-df", "Goiás — Entorno do Distrito Federal", "GO", "RURAL",
                    "Formosa e Luziânia: grãos e pecuária de corte a poucos km de Brasília.",
                    "https://images.unsplash.com/photo-1715798637010-8a4f27a0950f?auto=format&fit=crop&w=900&q=70", 0));

    @GetMapping
    @Operation(summary = "Listar regiões atendidas com a quantidade de imóveis publicados")
    public List<Region> listar() {
        Map<String, Long> contagem = repository.findAll().stream()
                .filter(Property::visivelNaBusca)
                .filter(p -> p.endereco().regiaoSlug() != null)
                .collect(Collectors.groupingBy(p -> p.endereco().regiaoSlug(), Collectors.counting()));

        return CATALOGO.stream()
                .map(r -> r.comQuantidade(contagem.getOrDefault(r.slug(), 0L)))
                .filter(r -> r.quantidade() > 0)
                .toList();
    }

    public record Region(String slug, String nome, String uf, String categoria,
                         String descricao, String imagem, long quantidade) {

        Region comQuantidade(long qtd) {
            return new Region(slug, nome, uf, categoria, descricao, imagem, qtd);
        }
    }
}
