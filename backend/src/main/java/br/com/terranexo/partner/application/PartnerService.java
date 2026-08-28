package br.com.terranexo.partner.application;

import br.com.terranexo.partner.api.dto.CreatePartnerRequest;
import br.com.terranexo.partner.domain.Partner;
import br.com.terranexo.partner.domain.PartnerRepository;
import br.com.terranexo.partner.domain.ProspectStatus;
import br.com.terranexo.partner.domain.Segment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Consulta e manutenção (atualização de status) da base de prospecção comercial. */
@Service
public class PartnerService {

    private final PartnerRepository repository;
    private final Clock clock;

    @Autowired
    public PartnerService(PartnerRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    PartnerService(PartnerRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Partner> listar(String cidade, Segment setor, ProspectStatus status, String q) {
        return repository.findAll().stream()
                .filter(p -> cidade == null || normalizar(p.municipio()).equals(normalizar(cidade)))
                .filter(p -> setor == null || p.setor() == setor)
                .filter(p -> status == null || p.status() == status)
                .filter(p -> q == null || q.isBlank() || normalizar(
                        p.nomeFantasia() + " " + Optional.ofNullable(p.razaoSocial()).orElse("")
                                + " " + Optional.ofNullable(p.cnpj()).orElse(""))
                        .contains(normalizar(q)))
                .sorted(Comparator.comparing(Partner::municipio).thenComparing(Partner::nomeFantasia))
                .toList();
    }

    public Optional<Partner> buscarPorId(String id) {
        return repository.findById(id);
    }

    /** Atualiza o status corrente e acrescenta o evento ao histórico (auditoria do funil). */
    public Partner atualizarStatus(String id, ProspectStatus novoStatus, String responsavel, String observacao) {
        Partner atual = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado: " + id));
        OffsetDateTime agora = OffsetDateTime.now(clock);
        Partner atualizado = atual.comNovoStatus(novoStatus, responsavel, observacao, LocalDate.now(clock), agora);
        return repository.save(atualizado);
    }

    /** Cadastra um novo parceiro/prospect. Começa sempre em NAO_INICIADO. */
    public Partner criar(CreatePartnerRequest req) {
        String id = repository.proximoId();
        LocalDate dataColeta = dataOuHoje(req.dataColeta());
        OffsetDateTime agora = OffsetDateTime.now(clock);

        Partner novo = construir(req, id, ProspectStatus.NAO_INICIADO, dataColeta, agora, agora,
                List.of(new Partner.StatusEvent(ProspectStatus.NAO_INICIADO, dataColeta,
                        "cadastro manual", "Registro criado no painel administrativo.")));

        return repository.save(novo);
    }

    /** Atualiza os dados cadastrais de um parceiro. Não mexe em status nem no histórico do funil. */
    public Partner atualizar(String id, CreatePartnerRequest req) {
        Partner existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado: " + id));
        LocalDate dataColeta = dataOuHoje(req.dataColeta());

        Partner atualizado = construir(req, existente.id(), existente.status(), dataColeta,
                existente.criadoEm(), OffsetDateTime.now(clock), existente.historicoStatus());

        return repository.save(atualizado);
    }

    /** Monta o registro a partir do formulário — comum a cadastro e atualização. */
    private Partner construir(CreatePartnerRequest req, String id, ProspectStatus status, LocalDate dataColeta,
                              OffsetDateTime criadoEm, OffsetDateTime atualizadoEm,
                              List<Partner.StatusEvent> historicoStatus) {
        Segment setor = Segment.valueOf(req.setor().toUpperCase());
        return new Partner(id, req.municipio(), req.uf().toUpperCase(), setor,
                req.nomeFantasia(), req.razaoSocial(), req.cnpj(), req.situacaoCadastral(),
                new Partner.Address(req.logradouro(), req.numero(), req.complemento(), req.bairro(), req.cep()),
                req.site(), geolocalizacao(req.logradouro(), req.numero(), req.bairro(), req.municipio()),
                titularesDe(req.titulares()), contatosDe(req.contatos()),
                status, req.fonteInformacao(), dataColeta, req.observacoes(),
                criadoEm, atualizadoEm, historicoStatus);
    }

    /** Exclui um parceiro. Lança se o id não existir, para diferenciar de "nada a fazer". */
    public void excluir(String id) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Parceiro não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private List<Partner.Titular> titularesDe(List<CreatePartnerRequest.Titular> titulares) {
        if (titulares == null) {
            return List.of();
        }
        return titulares.stream()
                .map(t -> new Partner.Titular(t.nome(), t.cpfMascarado(), t.qualificacao(), t.principal()))
                .toList();
    }

    private List<Partner.Contato> contatosDe(List<CreatePartnerRequest.Contato> contatos) {
        if (contatos == null) {
            return List.of();
        }
        return contatos.stream()
                .map(c -> new Partner.Contato(
                        Partner.ContatoTipo.valueOf(c.tipo().toUpperCase()), c.valor(), c.principal()))
                .toList();
    }

    private LocalDate dataOuHoje(String data) {
        if (data == null || data.isBlank()) {
            return LocalDate.now(clock);
        }
        try {
            return LocalDate.parse(data);
        } catch (RuntimeException ignorado) {
            return LocalDate.now(clock);
        }
    }

    /** Mesmo esquema de link usado na carga inicial (docs/prospeccao/db) — busca no Google Maps pelo endereço. */
    private Partner.Geo geolocalizacao(String logradouro, String numero, String bairro, String municipio) {
        String endereco = Stream.of(logradouro, numero, bairro, municipio)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
        String url = "https://www.google.com/maps/search/?api=1&query="
                + URLEncoder.encode(endereco, StandardCharsets.UTF_8);
        return new Partner.Geo(url, null, null);
    }

    /** Contagem por cidade x setor e por status — mesma visão da aba "Painel" da planilha de trabalho. */
    public Resumo resumo() {
        List<Partner> todos = repository.findAll();

        Map<String, Long> porCidade = new LinkedHashMap<>();
        todos.stream().sorted(Comparator.comparing(Partner::municipio))
                .forEach(p -> porCidade.merge(p.municipio(), 1L, Long::sum));

        Map<String, Long> porSetor = new LinkedHashMap<>();
        for (Segment s : Segment.values()) {
            long qtd = todos.stream().filter(p -> p.setor() == s).count();
            porSetor.put(s.name(), qtd);
        }

        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (ProspectStatus s : ProspectStatus.values()) {
            long qtd = todos.stream().filter(p -> p.status() == s).count();
            porStatus.put(s.name(), qtd);
        }

        return new Resumo(todos.size(), porCidade, porSetor, porStatus);
    }

    public record Resumo(int total, Map<String, Long> porCidade, Map<String, Long> porSetor,
                          Map<String, Long> porStatus) {
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
