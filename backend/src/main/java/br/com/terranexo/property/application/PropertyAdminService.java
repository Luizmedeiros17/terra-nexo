package br.com.terranexo.property.application;

import br.com.terranexo.lead.application.LeadService;
import br.com.terranexo.lead.domain.Lead;
import br.com.terranexo.property.api.dto.CreatePropertyRequest;
import br.com.terranexo.property.api.dto.DashboardResponse;
import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyBuilder;
import br.com.terranexo.property.domain.PropertyRepository;
import br.com.terranexo.property.domain.PropertyStatus;
import br.com.terranexo.property.domain.Purpose;
import br.com.terranexo.property.infrastructure.MediaStorageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Operações do ambiente administrativo: indicadores do painel (seção 18) e
 * cadastro de imóveis urbanos e rurais (seções 8 a 10).
 */
@Service
public class PropertyAdminService {

    /** Um anúncio parado há mais de 60 dias precisa de revisão de preço ou fotos. */
    private static final int DIAS_SEM_ATUALIZACAO = 60;

    private final PropertyRepository repository;
    private final LeadService leadService;
    private final MediaStorageService midiaStorage;

    public PropertyAdminService(PropertyRepository repository, LeadService leadService,
                                 MediaStorageService midiaStorage) {
        this.repository = repository;
        this.leadService = leadService;
        this.midiaStorage = midiaStorage;
    }

    // ------------------------------------------------------------------
    // Painel
    // ------------------------------------------------------------------

    public DashboardResponse.Imoveis indicadoresDeImoveis() {
        List<Property> todos = repository.findAll();
        LocalDate limite = LocalDate.now().minusDays(DIAS_SEM_ATUALIZACAO);

        BigDecimal carteira = todos.stream()
                .filter(p -> p.situacao() == PropertyStatus.PUBLICADO)
                .filter(p -> p.finalidade() != Purpose.LOCACAO)
                .map(p -> p.valores().preco())
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse.Imoveis(
                todos.size(),
                contar(todos, p -> p.situacao() == PropertyStatus.PUBLICADO),
                contar(todos, p -> p.situacao() == PropertyStatus.RASCUNHO),
                contar(todos, p -> p.situacao() == PropertyStatus.AGUARDANDO_REVISAO),
                contar(todos, p -> p.categoria() == Category.URBANO),
                contar(todos, p -> p.categoria() == Category.RURAL),
                contar(todos, p -> p.atualizadoEm().isBefore(limite)),
                contar(todos, Property::destaque),
                carteira);
    }

    public DashboardResponse.Leads indicadoresDeLeads() {
        List<Lead> leads = leadService.listar(null, null, null);
        return new DashboardResponse.Leads(
                leads.size(),
                leads.stream().filter(l -> l.etapa() == Lead.Stage.NOVO).count(),
                leads.stream().filter(l -> l.etapa() == Lead.Stage.NOVO
                        || l.etapa() == Lead.Stage.AGUARDANDO_ATENDIMENTO).count(),
                agrupar(leads, l -> l.etapa().name(), this::rotuloEtapa),
                agrupar(leads, l -> l.origem().name(), this::rotuloOrigem));
    }

    public List<DashboardResponse.Contagem> porSituacao() {
        return agrupar(repository.findAll(), p -> p.situacao().name(),
                chave -> PropertyStatus.valueOf(chave).rotulo());
    }

    public List<DashboardResponse.Contagem> porCorretor() {
        return agrupar(repository.findAll(), Property::corretorSlug,
                slug -> repository.findAll().stream()
                        .filter(p -> p.corretorSlug().equals(slug))
                        .map(Property::corretorNome)
                        .findFirst()
                        .orElse(slug));
    }

    public List<DashboardResponse.Contagem> porMunicipio() {
        return agrupar(repository.findAll(), p -> p.endereco().municipio(), chave -> chave)
                .stream().limit(8).toList();
    }

    public List<Property> ultimosCadastros(int limite) {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Property::atualizadoEm).reversed())
                .limit(limite)
                .toList();
    }

    /** Pendências acionáveis do painel (seção 18). */
    public List<DashboardResponse.Alerta> alertas() {
        List<DashboardResponse.Alerta> alertas = new ArrayList<>();
        List<Property> todos = repository.findAll();
        LocalDate limite = LocalDate.now().minusDays(DIAS_SEM_ATUALIZACAO);

        long semAtualizacao = contar(todos, p -> p.atualizadoEm().isBefore(limite));
        if (semAtualizacao > 0) {
            alertas.add(new DashboardResponse.Alerta("atencao",
                    "%d %s sem atualização há mais de %d dias".formatted(
                            semAtualizacao, semAtualizacao == 1 ? "imóvel" : "imóveis", DIAS_SEM_ATUALIZACAO),
                    "Revisar anúncios", "/admin/imoveis?situacao=PUBLICADO"));
        }

        long rascunhos = contar(todos, p -> p.situacao() == PropertyStatus.RASCUNHO);
        if (rascunhos > 0) {
            alertas.add(new DashboardResponse.Alerta("info",
                    "%d %s em rascunho aguardando conclusão".formatted(
                            rascunhos, rascunhos == 1 ? "cadastro" : "cadastros"),
                    "Concluir cadastros", "/admin/imoveis?situacao=RASCUNHO"));
        }

        long semAtendimento = leadService.listar(null, null, null).stream()
                .filter(l -> l.etapa() == Lead.Stage.NOVO)
                .count();
        if (semAtendimento > 0) {
            alertas.add(new DashboardResponse.Alerta("urgente",
                    "%d %s sem atendimento".formatted(
                            semAtendimento, semAtendimento == 1 ? "lead novo" : "leads novos"),
                    "Distribuir leads", "/admin/leads"));
        }

        long semFoto = contar(todos, p -> p.midias().stream().noneMatch(m -> m.tipo() == Property.MediaType.FOTO));
        if (semFoto > 0) {
            alertas.add(new DashboardResponse.Alerta("atencao",
                    "%d %s sem fotografia — não podem ser publicados".formatted(
                            semFoto, semFoto == 1 ? "imóvel" : "imóveis"),
                    "Enviar fotos", "/admin/imoveis"));
        }
        return alertas;
    }

    // ------------------------------------------------------------------
    // Cadastro
    // ------------------------------------------------------------------

    /**
     * Cria o imóvel. Rascunhos aceitam cadastro incompleto; publicar exige os
     * requisitos das regras 24.1 e 24.2.
     */
    public Property cadastrar(CreatePropertyRequest req) {
        Category categoria = enumDe(Category.class, req.categoria(), "categoria");
        PropertyStatus situacao = situacaoDe(req);
        if (situacao == PropertyStatus.PUBLICADO) {
            validarPublicacao(req, categoria, List.of());
        }

        String codigo = repository.proximoCodigo(categoria);
        String slug = gerarSlug(req.titulo(), req.endereco().municipio(), req.endereco().uf());
        PropertyBuilder builder = PropertyBuilder.novo(codigo, slug)
                .datas(LocalDate.now(), LocalDate.now());
        popular(builder, req, categoria, situacao);
        // Fotos e vídeos são enviados depois, pelo gerenciador de mídia (o imóvel precisa existir antes).

        return repository.save(builder.build());
    }

    /**
     * Atualiza um imóvel existente. Mantém id, código e data de publicação;
     * o restante é substituído pelo conteúdo do formulário — a mesma validação
     * de publicação do cadastro (regras 24.1 e 24.2) vale aqui.
     */
    public Property atualizar(String id, CreatePropertyRequest req) {
        Property existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + id));

        Category categoria = enumDe(Category.class, req.categoria(), "categoria");
        PropertyStatus situacao = situacaoDe(req);
        if (situacao == PropertyStatus.PUBLICADO) {
            validarPublicacao(req, categoria, existente.midias());
        }

        String slug = gerarSlug(req.titulo(), req.endereco().municipio(), req.endereco().uf());
        PropertyBuilder builder = PropertyBuilder.novo(existente.codigo(), slug)
                .datas(existente.publicadoEm(), LocalDate.now());
        popular(builder, req, categoria, situacao);
        // A galeria de fotos/vídeos não faz parte do formulário — preserva o que já foi enviado.
        builder.midias(existente.midias());

        return repository.save(builder.build());
    }

    /** Exclui um imóvel e os arquivos de mídia associados. Lança se o id não existir. */
    public void excluir(String id) {
        Property imovel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + id));
        imovel.midias().forEach(m -> midiaStorage.excluir(m.url()));
        repository.deleteById(id);
    }

    private PropertyStatus situacaoDe(CreatePropertyRequest req) {
        return req.situacao() == null || req.situacao().isBlank()
                ? PropertyStatus.RASCUNHO
                : enumDe(PropertyStatus.class, req.situacao(), "situação");
    }

    /** Preenche o builder com os campos do formulário — comum a cadastro e atualização. */
    private void popular(PropertyBuilder builder, CreatePropertyRequest req, Category categoria,
                         PropertyStatus situacao) {
        builder.categoria(categoria)
                .tipo(req.tipo())
                .finalidade(enumDe(Purpose.class, req.finalidade(), "finalidade"))
                .situacao(situacao)
                .titulo(req.titulo())
                .chamada(req.chamada() == null ? "" : req.chamada())
                .descricao(req.descricao() == null ? "" : req.descricao())
                .valores(converterValores(req.valores()))
                .areas(converterAreas(req.areas()))
                .endereco(converterEndereco(req.endereco()));

        if (req.ambientes() != null) {
            builder.ambientes(req.ambientes().dormitorios(), req.ambientes().suites(),
                    req.ambientes().banheiros(), req.ambientes().vagas());
        }
        if (req.caracteristicas() != null && !req.caracteristicas().isEmpty()) {
            builder.caracteristicas(req.caracteristicas().toArray(String[]::new));
        }
        if (req.corretorSlug() != null && !req.corretorSlug().isBlank()) {
            builder.corretor(req.corretorSlug(),
                    req.corretorNome() == null ? req.corretorSlug() : req.corretorNome());
        }
        if (req.destaque()) builder.destaque();
        if (req.exclusivo()) builder.exclusivo();
        if (req.lancamento()) builder.lancamento();
        if (req.oportunidade()) builder.oportunidade();
        if (req.aceitaPermuta()) builder.permuta();
        if (!req.aceitaFinanciamento()) builder.semFinanciamento();

        if (categoria == Category.URBANO && req.urbano() != null) {
            builder.urbano(new Property.Urban(
                    req.urbano().condominioNome(), req.urbano().andar(), req.urbano().anoConstrucao(),
                    req.urbano().mobiliado(), req.urbano().aceitaAnimais(),
                    lista(req.urbano().transporteProximo())));
        }
        if (categoria == Category.RURAL && req.rural() != null) {
            builder.rural(converterRural(req.rural()));
        }
    }

    /** Regras 24.1 e 24.2 — o que impede a publicação. */
    private void validarPublicacao(CreatePropertyRequest req, Category categoria, List<Property.Media> midiasAtuais) {
        List<String> faltando = new ArrayList<>();

        if (vazio(req.descricao())) faltando.add("descrição");
        if (midiasAtuais.stream().noneMatch(m -> m.tipo() == Property.MediaType.FOTO)) {
            faltando.add("ao menos uma fotografia");
        }
        if (vazio(req.corretorSlug())) faltando.add("corretor responsável");

        boolean temPreco = req.valores() != null
                && (req.valores().preco() != null || !vazio(req.valores().textoAlternativo()));
        if (!temPreco) faltando.add("preço ou indicação de consulta");

        if (categoria == Category.RURAL) {
            if (req.areas() == null || req.areas().valor() <= 0) faltando.add("área total");
            if (req.endereco() == null || vazio(req.endereco().municipio())) faltando.add("município");
        }

        if (!faltando.isEmpty()) {
            throw new IllegalArgumentException(
                    "Para publicar, informe: " + String.join(", ", faltando)
                            + ". Salve como rascunho para concluir depois.");
        }
    }

    // ------------------------------------------------------------------
    // Conversões
    // ------------------------------------------------------------------

    private Property.Money converterValores(CreatePropertyRequest.Valores v) {
        if (v == null) {
            return new Property.Money(null, null, null, null, false, "Consulte-nos");
        }
        return new Property.Money(v.preco(), v.condominio(), v.iptu(), v.arrendamentoPorHectare(),
                v.exibirPreco(), v.textoAlternativo());
    }

    /** Converte a unidade informada para metros quadrados, guardando o fator usado. */
    private Property.Areas converterAreas(CreatePropertyRequest.Areas a) {
        double fator = switch (a.unidade() == null ? "m²" : a.unidade().toLowerCase(Locale.ROOT)) {
            case "ha", "hectare", "hectares" -> 10_000d;
            case "alq", "alqueire", "alqueires" -> 24_200d;
            default -> 1d;
        };
        String unidade = fator == 10_000d ? "ha" : fator == 24_200d ? "alq" : "m²";
        return new Property.Areas(a.valor() * fator, a.areaConstruidaM2(), a.areaTerrenoM2(),
                unidade, a.valor());
    }

    private Property.Address converterEndereco(CreatePropertyRequest.Endereco e) {
        Property.Address.Exposure exposicao = enumDe(Property.Address.Exposure.class,
                e.exposicao(), "nível de exposição");
        return new Property.Address(e.uf().toUpperCase(Locale.ROOT), e.municipio(), e.bairro(),
                e.logradouro(), e.numero(), e.complemento(), normalizarCep(e.cep()),
                e.regiaoSlug(), e.latitude(), e.longitude(), exposicao);
    }

    /** Guarda o CEP sempre no formato 00000-000. */
    private String normalizarCep(String cep) {
        if (cep == null || cep.isBlank()) {
            return null;
        }
        String digitos = cep.replaceAll("\\D", "");
        return digitos.length() == 8
                ? digitos.substring(0, 5) + "-" + digitos.substring(5)
                : cep.trim();
    }

    private Property.Rural converterRural(CreatePropertyRequest.Rural r) {
        List<Property.Rural.Culture> culturas = r.culturas() == null ? List.of()
                : r.culturas().stream()
                .map(c -> new Property.Rural.Culture(c.nome(), c.categoria(), c.areaHa(),
                        c.safra(), c.produtividade()))
                .toList();

        List<Property.Rural.Improvement> benfeitorias = r.benfeitorias() == null ? List.of()
                : r.benfeitorias().stream()
                .map(b -> new Property.Rural.Improvement(b.tipo(), b.quantidade(), b.areaM2(),
                        b.conservacao() == null ? "Não informado" : b.conservacao()))
                .toList();

        Property.Rural.Livestock pecuaria = r.pecuaria() == null || vazio(r.pecuaria().tipoCriacao())
                ? null
                : new Property.Rural.Livestock(r.pecuaria().tipoCriacao(),
                r.pecuaria().capacidadeCabecas(), r.pecuaria().divisoesPasto(),
                conjunto(r.pecuaria().estruturas()));

        CreatePropertyRequest.Rural.Acesso a = r.acesso();
        Property.Rural.Access acesso = a == null
                ? new Property.Rural.Access("Não informado", 0, 0, "", true, false, false, false)
                : new Property.Rural.Access(
                a.tipoAcesso() == null ? "Não informado" : a.tipoAcesso(),
                a.distanciaAsfaltoKm(), a.distanciaCidadeKm(),
                a.municipioReferencia() == null ? "" : a.municipioReferencia(),
                a.acessoAnoTodo(), a.energiaTrifasica(), a.internet(), a.sinalCelular());

        CreatePropertyRequest.Rural.Documentacao d = r.documentacao();
        Property.Rural.Documentation documentacao = d == null
                ? new Property.Rural.Documentation(false, false, false, false, false, null)
                : new Property.Rural.Documentation(d.carInformado(), d.ccirInformado(),
                d.sigefInformado(), d.matriculaInformada(), d.georreferenciado(), d.situacaoDeclarada(),
                d.matriculaNumero(), d.carReciboNumero(), d.carDemonstrativoCompletoInformado(),
                d.carMapaPoligonoInformado(), d.sigefSituacaoGeorreferenciamento(),
                d.sigefPlantaMemorialDisponivel(), d.sncrIncraSituacaoCadastral(),
                d.cnirCafirCib(), d.cnirCafirSituacaoCadastral());

        LocalDate referencia = LocalDate.now();
        if (r.dataReferencia() != null && !r.dataReferencia().isBlank()) {
            try {
                referencia = LocalDate.parse(r.dataReferencia());
            } catch (RuntimeException ignorado) {
                // Data inválida não bloqueia o cadastro: assume a data de hoje.
            }
        }

        return new Property.Rural(
                r.areaProdutivaHa(), r.areaPastagemHa(), r.areaCultivadaHa(), r.areaMataHa(),
                r.reservaLegalHa(), r.areaIrrigadaHa(),
                conjunto(r.terreno()), r.soloPredominante(), r.soloFertilidade(),
                conjunto(r.atividades()), culturas, pecuaria, conjunto(r.recursosHidricos()),
                benfeitorias, acesso, documentacao,
                r.fonteInformacao() == null || r.fonteInformacao().isBlank()
                        ? "Declarado pelo proprietário" : r.fonteInformacao(),
                referencia);
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    private <T> long contar(List<T> lista, java.util.function.Predicate<T> filtro) {
        return lista.stream().filter(filtro).count();
    }

    private <T> List<DashboardResponse.Contagem> agrupar(
            List<T> itens,
            java.util.function.Function<T, String> chave,
            java.util.function.Function<String, String> rotulo) {
        Map<String, Long> contagens = itens.stream()
                .collect(Collectors.groupingBy(chave, Collectors.counting()));
        return contagens.entrySet().stream()
                .map(e -> new DashboardResponse.Contagem(e.getKey(), rotulo.apply(e.getKey()), e.getValue()))
                .sorted(Comparator.comparingLong(DashboardResponse.Contagem::quantidade).reversed())
                .toList();
    }

    private String rotuloEtapa(String etapa) {
        return switch (Lead.Stage.valueOf(etapa)) {
            case NOVO -> "Novo";
            case AGUARDANDO_ATENDIMENTO -> "Aguardando atendimento";
            case CONTATO_INICIADO -> "Contato iniciado";
            case SEM_RESPOSTA -> "Sem resposta";
            case QUALIFICADO -> "Qualificado";
            case VISITA_AGENDADA -> "Visita agendada";
            case VISITA_REALIZADA -> "Visita realizada";
            case PROPOSTA -> "Proposta";
            case NEGOCIACAO -> "Negociação";
            case CONVERTIDO -> "Convertido";
            case PERDIDO -> "Perdido";
            case DUPLICADO -> "Duplicado";
            case DESCARTADO -> "Descartado";
        };
    }

    private String rotuloOrigem(String origem) {
        return switch (Lead.Origin.valueOf(origem)) {
            case FORMULARIO_IMOVEL -> "Formulário do imóvel";
            case CONTATO_GERAL -> "Contato geral";
            case WHATSAPP -> "WhatsApp";
            case TELEFONE -> "Telefone";
            case EMAIL -> "E-mail";
            case CADASTRO_MANUAL -> "Cadastro manual";
            case REDES_SOCIAIS -> "Redes sociais";
            case INDICACAO -> "Indicação";
            case CAMPANHA -> "Campanha";
            case OUTRA -> "Outra";
        };
    }

    private Set<String> conjunto(List<String> valores) {
        return valores == null ? Set.of() : Set.copyOf(valores);
    }

    private List<String> lista(List<String> valores) {
        return valores == null ? List.of() : List.copyOf(valores);
    }

    private boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private <E extends Enum<E>> E enumDe(Class<E> tipo, String valor, String campo) {
        for (E e : tipo.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(valor.trim())) {
                return e;
            }
        }
        throw new IllegalArgumentException("Valor inválido para " + campo + ": " + valor);
    }

    /** Slug público: minúsculo, sem acento, separado por hífen (seção 20). */
    private String gerarSlug(String titulo, String municipio, String uf) {
        String base = "%s %s %s".formatted(titulo, municipio, uf);
        String semAcento = Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String slug = semAcento.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.length() > 90 ? slug.substring(0, 90).replaceAll("-$", "") : slug;
    }
}
