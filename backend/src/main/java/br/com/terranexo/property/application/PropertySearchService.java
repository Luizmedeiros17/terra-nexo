package br.com.terranexo.property.application;

import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import br.com.terranexo.property.domain.Purpose;
import br.com.terranexo.shared.text.Textos;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Pesquisa pública de imóveis (seção 13): filtros simples, avançados e rurais,
 * ordenação, paginação e facetas com contagem.
 *
 * <p>Implementação em memória adequada ao volume do MVP. Ao migrar para
 * PostgreSQL, esta classe passa a montar Specifications/consultas nativas —
 * o contrato de entrada ({@link PropertyQuery}) e de saída ({@link SearchResult})
 * permanece o mesmo.</p>
 */
@Service
public class PropertySearchService {

    private final PropertyRepository repository;

    public PropertySearchService(PropertyRepository repository) {
        this.repository = repository;
    }

    public SearchResult buscar(PropertyQuery query) {
        List<Property> publicados = repository.findAll().stream()
                .filter(Property::visivelNaBusca)
                .toList();

        List<Property> filtrados = publicados.stream()
                .filter(p -> corresponde(p, query))
                .sorted(query.ordenacao().comparator())
                .toList();

        int total = filtrados.size();
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) query.tamanho()));
        int paginaAtual = Math.min(query.pagina(), totalPaginas);
        int inicio = (paginaAtual - 1) * query.tamanho();
        List<Property> pagina = inicio >= total
                ? List.of()
                : filtrados.subList(inicio, Math.min(inicio + query.tamanho(), total));

        return new SearchResult(
                pagina,
                total,
                paginaAtual,
                query.tamanho(),
                totalPaginas,
                query.ordenacao(),
                facetas(publicados, query),
                faixaDePreco(filtrados),
                query.elegivelParaIndexacao());
    }

    public List<Property> destaques(int limite, Category categoria) {
        return repository.findAll().stream()
                .filter(Property::visivelNaBusca)
                .filter(Property::destaque)
                .filter(p -> categoria == null || p.categoria() == categoria)
                .sorted(SortOption.RELEVANCIA.comparator())
                .limit(limite)
                .toList();
    }

    public List<Property> recentes(int limite) {
        return repository.findAll().stream()
                .filter(Property::visivelNaBusca)
                .sorted(SortOption.RECENTES.comparator())
                .limit(limite)
                .toList();
    }

    public Optional<Property> porCodigoOuSlug(String codeOrSlug) {
        return repository.findByCodeOrSlug(codeOrSlug);
    }

    /** Semelhantes: mesma categoria, priorizando mesmo município e tipo (backlog "desejável"). */
    public List<Property> semelhantes(String id, int limite) {
        Optional<Property> base = repository.findById(id).or(() -> repository.findByCodeOrSlug(id));
        if (base.isEmpty()) {
            return List.of();
        }
        Property ref = base.get();
        return repository.findAll().stream()
                .filter(Property::visivelNaBusca)
                .filter(p -> !p.id().equals(ref.id()))
                .filter(p -> p.categoria() == ref.categoria())
                .sorted(Comparator.comparingInt((Property p) -> pontuacaoSemelhanca(p, ref)).reversed())
                .limit(limite)
                .toList();
    }

    private int pontuacaoSemelhanca(Property candidato, Property ref) {
        int pontos = 0;
        if (candidato.endereco().municipio().equals(ref.endereco().municipio())) pontos += 3;
        if (candidato.tipo().equals(ref.tipo())) pontos += 3;
        if (candidato.finalidade() == ref.finalidade()) pontos += 2;
        if (candidato.endereco().bairro() != null
                && candidato.endereco().bairro().equals(ref.endereco().bairro())) pontos += 2;
        if (ref.valores().preco() != null && candidato.valores().preco() != null) {
            double razao = candidato.valores().preco().doubleValue() / ref.valores().preco().doubleValue();
            if (razao > 0.6 && razao < 1.6) pontos += 2;
        }
        return pontos;
    }

    // ------------------------------------------------------------------
    // Filtros
    // ------------------------------------------------------------------

    private boolean corresponde(Property p, PropertyQuery q) {
        if (q.categoria() != null && p.categoria() != q.categoria()) return false;
        if (q.finalidade() != null && !p.finalidade().atende(q.finalidade())) return false;
        if (naoContem(q.tipos(), p.tipo())) return false;

        if (texto(q.uf()) && !p.endereco().uf().equalsIgnoreCase(q.uf())) return false;
        if (texto(q.municipio()) && !igualNormalizado(p.endereco().municipio(), q.municipio())) return false;
        if (texto(q.bairro()) && !igualNormalizado(p.endereco().bairro(), q.bairro())) return false;
        if (texto(q.regiao()) && !q.regiao().equalsIgnoreCase(p.endereco().regiaoSlug())) return false;

        BigDecimal preco = p.valores().preco();
        if (q.precoMin() != null && (preco == null || preco.compareTo(q.precoMin()) < 0)) return false;
        if (q.precoMax() != null && (preco == null || preco.compareTo(q.precoMax()) > 0)) return false;

        double area = p.areas().areaTotalM2();
        if (q.areaMinM2() != null && area < q.areaMinM2()) return false;
        if (q.areaMaxM2() != null && area > q.areaMaxM2()) return false;

        if (q.dormitorios() != null && p.ambientes().dormitorios() < q.dormitorios()) return false;
        if (q.suites() != null && p.ambientes().suites() < q.suites()) return false;
        if (q.banheiros() != null && p.ambientes().banheiros() < q.banheiros()) return false;
        if (q.vagas() != null && p.ambientes().vagas() < q.vagas()) return false;

        if (!contemTodas(q.caracteristicas(), p.caracteristicas())) return false;
        if (texto(q.corretor()) && !q.corretor().equalsIgnoreCase(p.corretorSlug())) return false;
        if (texto(q.codigo()) && !p.codigo().equalsIgnoreCase(q.codigo().trim())) return false;
        if (Boolean.TRUE.equals(q.permuta()) && !p.aceitaPermuta()) return false;
        if (Boolean.TRUE.equals(q.financiamento()) && !p.aceitaFinanciamento()) return false;

        if (!correspondeRural(p, q)) return false;

        return correspondeTextoLivre(p, q.q());
    }

    /** Filtros rurais adicionais — descartam automaticamente imóveis urbanos quando usados. */
    private boolean correspondeRural(Property p, PropertyQuery q) {
        boolean usaFiltroRural = q.areaHaMin() != null || q.areaHaMax() != null
                || naoVazio(q.terreno()) || naoVazio(q.atividades()) || naoVazio(q.culturas())
                || naoVazio(q.agua()) || naoVazio(q.benfeitorias()) || naoVazio(q.documentacao())
                || q.pecuaria() != null || q.irrigacao() != null || q.energiaTrifasica() != null
                || q.distanciaAsfaltoMaxKm() != null;

        if (!usaFiltroRural) {
            return true;
        }
        if (!p.isRural()) {
            return false;
        }
        Property.Rural r = p.rural();

        if (q.areaHaMin() != null && p.areaHectares() < q.areaHaMin()) return false;
        if (q.areaHaMax() != null && p.areaHectares() > q.areaHaMax()) return false;
        if (!contemTodas(q.terreno(), r.terreno())) return false;
        if (!contemTodas(q.atividades(), r.atividades())) return false;
        if (!contemTodas(q.agua(), r.recursosHidricos())) return false;

        if (naoVazio(q.culturas())) {
            Set<String> nomes = r.culturas().stream()
                    .map(c -> Textos.normalizar(c.nome()))
                    .collect(Collectors.toSet());
            for (String desejada : q.culturas()) {
                if (!nomes.contains(Textos.normalizar(desejada))) return false;
            }
        }
        if (naoVazio(q.benfeitorias())) {
            Set<String> tipos = r.benfeitorias().stream()
                    .map(b -> Textos.normalizar(b.tipo()))
                    .collect(Collectors.toSet());
            for (String desejada : q.benfeitorias()) {
                if (!tipos.contains(Textos.normalizar(desejada))) return false;
            }
        }
        if (naoVazio(q.documentacao())) {
            for (String doc : q.documentacao()) {
                boolean informado = switch (Textos.normalizar(doc)) {
                    case "car" -> r.documentacao().carInformado();
                    case "ccir" -> r.documentacao().ccirInformado();
                    case "sigef" -> r.documentacao().sigefInformado();
                    case "matricula" -> r.documentacao().matriculaInformada();
                    case "georreferenciado" -> r.documentacao().georreferenciado();
                    default -> false;
                };
                if (!informado) return false;
            }
        }
        if (Boolean.TRUE.equals(q.pecuaria()) && r.pecuaria() == null) return false;
        if (Boolean.TRUE.equals(q.irrigacao())
                && (r.areaIrrigadaHa() == null || r.areaIrrigadaHa() <= 0)) return false;
        if (Boolean.TRUE.equals(q.energiaTrifasica()) && !r.acesso().energiaTrifasica()) return false;
        if (q.distanciaAsfaltoMaxKm() != null
                && r.acesso().distanciaAsfaltoKm() > q.distanciaAsfaltoMaxKm()) return false;

        return true;
    }

    private boolean correspondeTextoLivre(Property p, String termo) {
        if (!texto(termo)) {
            return true;
        }
        String alvo = Textos.normalizar(String.join(" ",
                p.titulo(), p.chamada(), p.tipo(), p.codigo(),
                p.endereco().municipio(), nullSafe(p.endereco().bairro()), p.endereco().uf(),
                String.join(" ", p.caracteristicas()),
                p.isRural() ? resumoRuralParaBusca(p.rural()) : ""));
        // Todas as palavras do termo precisam aparecer — busca conjuntiva.
        for (String palavra : Textos.normalizar(termo).split("\\s+")) {
            if (!palavra.isBlank() && !alvo.contains(palavra)) {
                return false;
            }
        }
        return true;
    }

    private String resumoRuralParaBusca(Property.Rural r) {
        return String.join(" ",
                String.join(" ", r.terreno()),
                String.join(" ", r.atividades()),
                String.join(" ", r.recursosHidricos()),
                r.culturas().stream().map(Property.Rural.Culture::nome).collect(Collectors.joining(" ")),
                r.benfeitorias().stream().map(Property.Rural.Improvement::tipo).collect(Collectors.joining(" ")),
                nullSafe(r.soloPredominante()));
    }

    // ------------------------------------------------------------------
    // Facetas
    // ------------------------------------------------------------------

    /**
     * Cada faceta é contada sobre o conjunto filtrado pelas <em>demais</em> dimensões,
     * para que selecionar um valor não zere as outras opções do mesmo grupo.
     */
    private List<SearchResult.FacetGroup> facetas(List<Property> base, PropertyQuery q) {
        List<SearchResult.FacetGroup> grupos = new ArrayList<>();

        grupos.add(grupo("categoria", "Categoria",
                contar(base, q.semCategoria(), p -> p.categoria().name()),
                valor -> Category.valueOf(valor).rotulo(),
                q.categoria() == null ? Set.of() : Set.of(q.categoria().name())));

        grupos.add(grupo("finalidade", "Finalidade",
                contar(base, q.semFinalidade(), p -> p.finalidade().name()),
                valor -> Purpose.valueOf(valor).rotulo(),
                q.finalidade() == null ? Set.of() : Set.of(q.finalidade().name())));

        grupos.add(grupo("tipo", "Tipo de imóvel",
                contar(base, q.semTipos(), Property::tipo),
                Function.identity(),
                q.tipos() == null ? Set.of() : q.tipos()));

        grupos.add(grupo("municipio", "Município",
                contar(base, q.semMunicipio(), p -> p.endereco().municipio()),
                Function.identity(),
                q.municipio() == null ? Set.of() : Set.of(q.municipio())));

        return grupos;
    }

    private Map<String, Long> contar(List<Property> base, PropertyQuery queryReduzida,
                                     Function<Property, String> chave) {
        return base.stream()
                .filter(p -> corresponde(p, queryReduzida))
                .collect(Collectors.groupingBy(chave, LinkedHashMap::new, Collectors.counting()));
    }

    private SearchResult.FacetGroup grupo(String chave, String rotulo, Map<String, Long> contagens,
                                          Function<String, String> rotuloDoValor, Set<String> selecionados) {
        Set<String> selecionadosNormalizados = selecionados.stream()
                .map(Textos::normalizar).collect(Collectors.toSet());
        List<SearchResult.FacetValue> valores = contagens.entrySet().stream()
                .map(e -> new SearchResult.FacetValue(
                        e.getKey(),
                        rotuloDoValor.apply(e.getKey()),
                        e.getValue(),
                        selecionadosNormalizados.contains(Textos.normalizar(e.getKey()))))
                .sorted(Comparator.comparingLong(SearchResult.FacetValue::quantidade).reversed()
                        .thenComparing(SearchResult.FacetValue::rotulo))
                .toList();
        return new SearchResult.FacetGroup(chave, rotulo, valores);
    }

    private SearchResult.PriceRange faixaDePreco(List<Property> filtrados) {
        List<BigDecimal> precos = filtrados.stream()
                .filter(p -> p.valores().exibirPreco() && p.valores().preco() != null)
                .map(p -> p.valores().preco())
                .sorted()
                .toList();
        if (precos.isEmpty()) {
            return new SearchResult.PriceRange(BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return new SearchResult.PriceRange(precos.get(0), precos.get(precos.size() - 1));
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    private boolean texto(String s) {
        return s != null && !s.isBlank();
    }

    private boolean naoVazio(Set<String> s) {
        return s != null && !s.isEmpty();
    }

    private boolean naoContem(Set<String> filtro, String valor) {
        if (filtro == null || filtro.isEmpty()) {
            return false;
        }
        return filtro.stream().noneMatch(f -> igualNormalizado(f, valor));
    }

    private boolean contemTodas(Set<String> exigidas, Set<String> disponiveis) {
        if (exigidas == null || exigidas.isEmpty()) {
            return true;
        }
        Set<String> normalizadas = disponiveis.stream().map(Textos::normalizar).collect(Collectors.toSet());
        return exigidas.stream().allMatch(e -> normalizadas.contains(Textos.normalizar(e)));
    }

    private boolean igualNormalizado(String a, String b) {
        return a != null && b != null && Textos.normalizar(a).equals(Textos.normalizar(b));
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
