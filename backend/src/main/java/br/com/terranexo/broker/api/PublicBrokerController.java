package br.com.terranexo.broker.api;

import br.com.terranexo.broker.application.BrokerService;
import br.com.terranexo.broker.domain.Broker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Corretores exibidos publicamente (seções 6 e 16) — somente os cadastrados como ativos. */
@RestController
@RequestMapping("/api/v1/public/brokers")
@Tag(name = "Corretores", description = "Equipe exibida no portal público")
public class PublicBrokerController {

    private final BrokerService service;
    private final BrokerMapper mapper;

    public PublicBrokerController(BrokerService service, BrokerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar corretores públicos com a quantidade de imóveis sob responsabilidade")
    public List<PublicBroker> listar() {
        Map<String, Long> carteira = service.carteiraPorSlug();
        return service.listar(true, null).stream()
                .map(b -> toPublico(b, carteira.getOrDefault(b.slug(), 0L)))
                .toList();
    }

    private PublicBroker toPublico(Broker b, long imoveis) {
        return new PublicBroker(b.slug(), b.nome(), b.cargo(), b.creci(), b.foto(), b.bio(),
                b.especialidades(), b.regioes(), imoveis);
    }

    /** Forma pública, sem dados internos (email/telefone/ativo/id) — mesmo contrato de antes. */
    public record PublicBroker(String slug, String nome, String cargo, String creci, String foto,
                                String bio, List<String> especialidades, List<String> regioes, long imoveis) {
    }
}
