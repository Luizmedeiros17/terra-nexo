package br.com.terranexo.broker.api;

import br.com.terranexo.broker.api.dto.BrokerCardResponse;
import br.com.terranexo.broker.api.dto.BrokerDetailResponse;
import br.com.terranexo.broker.api.dto.CreateBrokerRequest;
import br.com.terranexo.broker.application.BrokerService;
import br.com.terranexo.broker.domain.Broker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CRUD da equipe de corretores. No protótipo estas rotas ficam abertas, como
 * as demais de {@code /admin} — a autenticação por perfil entra na etapa 1 do
 * plano de implementação.
 */
@RestController
@RequestMapping("/api/v1/admin/brokers")
@Tag(name = "Corretores", description = "Cadastro da equipe de corretores")
public class AdminBrokerController {

    private final BrokerService service;
    private final BrokerMapper mapper;

    public AdminBrokerController(BrokerService service, BrokerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar corretores com a carteira de imóveis sob responsabilidade")
    public List<BrokerCardResponse> listar(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String q) {
        Map<String, Long> carteira = service.carteiraPorSlug();
        return service.listar(ativo, q).stream().map(b -> mapper.toCard(b, carteira)).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um corretor")
    public BrokerDetailResponse detalhe(@PathVariable String id) {
        Broker broker = service.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Corretor não encontrado: " + id));
        long imoveis = service.carteiraPorSlug().getOrDefault(broker.slug(), 0L);
        return mapper.toDetail(broker, imoveis);
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo corretor")
    public ResponseEntity<BrokerDetailResponse> criar(@Valid @RequestBody CreateBrokerRequest req) {
        Broker criado = service.criar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDetail(criado, 0L));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados de um corretor")
    public BrokerDetailResponse atualizar(@PathVariable String id, @Valid @RequestBody CreateBrokerRequest req) {
        Broker atualizado = service.atualizar(id, req);
        long imoveis = service.carteiraPorSlug().getOrDefault(atualizado.slug(), 0L);
        return mapper.toDetail(atualizado, imoveis);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um corretor")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
