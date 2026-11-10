package br.com.terranexo.broker.application;

import br.com.terranexo.broker.api.dto.CreateBrokerRequest;
import br.com.terranexo.broker.domain.Broker;
import br.com.terranexo.broker.domain.BrokerRepository;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import br.com.terranexo.shared.text.Textos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Cadastro (CRUD) da equipe de corretores exibida no portal e usada no atendimento. */
@Service
public class BrokerService {

    private final BrokerRepository repository;
    private final PropertyRepository propertyRepository;
    private final Clock clock;

    @Autowired
    public BrokerService(BrokerRepository repository, PropertyRepository propertyRepository) {
        this(repository, propertyRepository, Clock.systemDefaultZone());
    }

    BrokerService(BrokerRepository repository, PropertyRepository propertyRepository, Clock clock) {
        this.repository = repository;
        this.propertyRepository = propertyRepository;
        this.clock = clock;
    }

    public List<Broker> listar(Boolean ativo, String q) {
        return repository.findAll().stream()
                .filter(b -> ativo == null || b.ativo() == ativo)
                .filter(b -> q == null || q.isBlank()
                        || Textos.normalizar(b.nome() + " " + b.cargo()).contains(Textos.normalizar(q)))
                .sorted(Comparator.comparing(Broker::nome))
                .toList();
    }

    public Optional<Broker> buscarPorId(String id) {
        return repository.findById(id);
    }

    /** Quantidade de imóveis visíveis na busca sob responsabilidade de cada corretor. */
    public Map<String, Long> carteiraPorSlug() {
        return propertyRepository.findAll().stream()
                .filter(Property::visivelNaBusca)
                .filter(p -> p.corretorSlug() != null)
                .collect(Collectors.groupingBy(Property::corretorSlug, Collectors.counting()));
    }

    public Broker criar(CreateBrokerRequest req) {
        String id = repository.proximoId();
        String slug = slugUnico(req.nome());
        OffsetDateTime agora = OffsetDateTime.now(clock);
        Broker novo = new Broker(id, slug, req.nome(), req.cargo(), req.creci(), req.foto(), req.bio(),
                req.especialidades() == null ? List.of() : req.especialidades(),
                req.regioes() == null ? List.of() : req.regioes(),
                req.email(), req.telefone(), req.ativo() == null || req.ativo(), agora, agora);
        return repository.save(novo);
    }

    public Broker atualizar(String id, CreateBrokerRequest req) {
        Broker existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Corretor não encontrado: " + id));
        Broker atualizado = existente.comAtualizacao(req.nome(), req.cargo(), req.creci(), req.foto(), req.bio(),
                req.especialidades() == null ? List.of() : req.especialidades(),
                req.regioes() == null ? List.of() : req.regioes(),
                req.email(), req.telefone(), req.ativo() == null || req.ativo(), OffsetDateTime.now(clock));
        return repository.save(atualizado);
    }

    public void excluir(String id) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Corretor não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private String slugUnico(String nome) {
        String base = Textos.normalizar(nome).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "corretor";
        }
        String slug = base;
        int sufixo = 2;
        while (repository.findBySlug(slug).isPresent()) {
            slug = base + "-" + sufixo++;
        }
        return slug;
    }
}
