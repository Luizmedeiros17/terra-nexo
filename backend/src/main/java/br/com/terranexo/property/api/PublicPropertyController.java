package br.com.terranexo.property.api;

import br.com.terranexo.property.api.dto.PropertyCardResponse;
import br.com.terranexo.property.api.dto.PropertyDetailResponse;
import br.com.terranexo.property.api.dto.SearchResponse;
import br.com.terranexo.property.application.PropertyQuery;
import br.com.terranexo.property.application.PropertySearchService;
import br.com.terranexo.property.application.SearchResult;
import br.com.terranexo.property.application.SortOption;
import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.Purpose;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/** Endpoints públicos do portal (seção 23). */
@RestController
@RequestMapping("/api/v1/public/properties")
@Tag(name = "Imóveis públicos", description = "Pesquisa e consulta de imóveis publicados")
public class PublicPropertyController {

    private final PropertySearchService service;
    private final PropertyMapper mapper;

    public PublicPropertyController(PropertySearchService service, PropertyMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Pesquisar imóveis publicados com filtros, ordenação, paginação e facetas")
    public ResponseEntity<SearchResponse> pesquisar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String finalidade,
            @RequestParam(required = false) List<String> tipo,

            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String regiao,

            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Double areaMin,
            @RequestParam(required = false) Double areaMax,

            @RequestParam(required = false) Integer dormitorios,
            @RequestParam(required = false) Integer suites,
            @RequestParam(required = false) Integer banheiros,
            @RequestParam(required = false) Integer vagas,

            @RequestParam(required = false) List<String> caracteristica,
            @RequestParam(required = false) String corretor,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Boolean permuta,
            @RequestParam(required = false) Boolean financiamento,

            @RequestParam(required = false) Double areaHaMin,
            @RequestParam(required = false) Double areaHaMax,
            @RequestParam(required = false) List<String> terreno,
            @RequestParam(required = false) List<String> atividade,
            @RequestParam(required = false) List<String> cultura,
            @RequestParam(required = false) Boolean pecuaria,
            @RequestParam(required = false) List<String> agua,
            @RequestParam(required = false) Boolean irrigacao,
            @RequestParam(required = false) Boolean energiaTrifasica,
            @RequestParam(required = false) Double distanciaAsfaltoMax,
            @RequestParam(required = false) List<String> benfeitoria,
            @RequestParam(required = false) List<String> documentacao,

            @RequestParam(required = false, defaultValue = "relevancia") String ordenar,
            @RequestParam(required = false, defaultValue = "1") int pagina,
            @RequestParam(required = false, defaultValue = "12") int tamanho) {

        PropertyQuery query = new PropertyQuery(
                q,
                enumOuNulo(Category.class, categoria),
                enumOuNulo(Purpose.class, finalidade),
                PropertyQuery.conjunto(tipo),
                uf, municipio, bairro, regiao,
                precoMin, precoMax, areaMin, areaMax,
                dormitorios, suites, banheiros, vagas,
                PropertyQuery.conjunto(caracteristica), corretor, codigo, permuta, financiamento,
                areaHaMin, areaHaMax,
                PropertyQuery.conjunto(terreno), PropertyQuery.conjunto(atividade),
                PropertyQuery.conjunto(cultura), pecuaria, PropertyQuery.conjunto(agua),
                irrigacao, energiaTrifasica, distanciaAsfaltoMax,
                PropertyQuery.conjunto(benfeitoria), PropertyQuery.conjunto(documentacao),
                SortOption.porCodigo(ordenar), pagina, tamanho);

        SearchResult resultado = service.buscar(query);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(toResponse(resultado, query));
    }

    @GetMapping("/featured")
    @Operation(summary = "Imóveis em destaque, opcionalmente por categoria (urbano ou rural)")
    public List<PropertyCardResponse> destaques(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "6") int limite) {
        return service.destaques(limite, enumOuNulo(Category.class, categoria)).stream()
                .map(mapper::toCard)
                .toList();
    }

    @GetMapping("/recent")
    @Operation(summary = "Imóveis publicados recentemente")
    public List<PropertyCardResponse> recentes(@RequestParam(required = false, defaultValue = "6") int limite) {
        return service.recentes(limite).stream().map(mapper::toCard).toList();
    }

    @GetMapping("/similar/{id}")
    @Operation(summary = "Imóveis semelhantes ao informado")
    public List<PropertyCardResponse> semelhantes(@PathVariable String id,
                                                  @RequestParam(required = false, defaultValue = "3") int limite) {
        return service.semelhantes(id, limite).stream().map(mapper::toCard).toList();
    }

    @GetMapping("/{codeOrSlug}")
    @Operation(summary = "Consultar um imóvel pelo código, slug ou URL pública")
    public ResponseEntity<PropertyDetailResponse> porCodigoOuSlug(@PathVariable String codeOrSlug) {
        return service.porCodigoOuSlug(codeOrSlug)
                .filter(Property::visivelNaBusca)
                .map(mapper::toDetail)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ------------------------------------------------------------------

    private SearchResponse toResponse(SearchResult r, PropertyQuery q) {
        List<SearchResponse.Faceta> facetas = r.facetas().stream()
                .map(g -> new SearchResponse.Faceta(g.chave(), g.rotulo(),
                        g.valores().stream()
                                .map(v -> new SearchResponse.FacetaValor(
                                        v.valor(), v.rotulo(), v.quantidade(), v.selecionado()))
                                .toList()))
                .toList();

        List<SearchResponse.OrdenacaoOpcao> ordenacoes = Arrays.stream(SortOption.values())
                .map(s -> new SearchResponse.OrdenacaoOpcao(s.codigo(), s.rotulo()))
                .toList();

        String tituloBusca = q.tituloDaBusca();
        String seoTitulo = "%s | Terra Nexo".formatted(tituloBusca);
        String seoDescricao = "%s: %d %s na Terra Nexo. Filtre por tipo, região, preço, área e características urbanas ou rurais."
                .formatted(tituloBusca, r.total(),
                        r.total() == 1 ? "imóvel disponível" : "imóveis disponíveis");

        return new SearchResponse(
                r.itens().stream().map(mapper::toCard).toList(),
                new SearchResponse.Paginacao(r.pagina(), r.tamanho(), r.total(), r.totalPaginas(),
                        r.primeira(), r.ultima()),
                r.ordenacao().codigo(),
                ordenacoes,
                facetas,
                new SearchResponse.FaixaPreco(r.faixaPreco().min(), r.faixaPreco().max()),
                new SearchResponse.Seo(seoTitulo, tituloBusca, seoDescricao, r.indexavel()));
    }

    private <E extends Enum<E>> E enumOuNulo(Class<E> tipo, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        for (E e : tipo.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(valor.trim())) {
                return e;
            }
        }
        return null;
    }
}
