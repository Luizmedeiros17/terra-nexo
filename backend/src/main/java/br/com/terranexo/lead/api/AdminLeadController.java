package br.com.terranexo.lead.api;

import br.com.terranexo.lead.api.dto.CreateLeadRequest;
import br.com.terranexo.lead.api.dto.LeadCardResponse;
import br.com.terranexo.lead.api.dto.LeadDetailResponse;
import br.com.terranexo.lead.api.dto.UpdateLeadStageRequest;
import br.com.terranexo.lead.application.LeadService;
import br.com.terranexo.lead.domain.Lead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * CRUD de leads (funil comercial, seção 17) — uso interno da equipe. No
 * protótipo estas rotas ficam abertas, como as demais de {@code /admin}; a
 * autenticação por perfil entra na etapa 1 do plano de implementação.
 */
@RestController
@RequestMapping("/api/v1/admin/leads")
@Tag(name = "Leads", description = "Funil comercial de leads captados pelo portal e cadastrados manualmente")
public class AdminLeadController {

    private final LeadService service;
    private final LeadMapper mapper;

    public AdminLeadController(LeadService service, LeadMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar leads com filtros por etapa, origem e busca livre")
    public List<LeadCardResponse> listar(
            @RequestParam(required = false) String etapa,
            @RequestParam(required = false) String origem,
            @RequestParam(required = false) String q) {
        Lead.Stage etapaEnum = etapa == null || etapa.isBlank() ? null : Lead.Stage.valueOf(etapa.toUpperCase());
        Lead.Origin origemEnum = origem == null || origem.isBlank() ? null : Lead.Origin.valueOf(origem.toUpperCase());
        return service.listar(etapaEnum, origemEnum, q).stream().map(mapper::toCard).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um lead")
    public LeadDetailResponse detalhe(@PathVariable String id) {
        Lead lead = service.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado: " + id));
        return mapper.toDetail(lead);
    }

    @PostMapping
    @Operation(summary = "Cadastrar manualmente um novo lead")
    public ResponseEntity<LeadDetailResponse> criar(@Valid @RequestBody CreateLeadRequest req) {
        Lead criado = service.criarManual(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDetail(criado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados de contato de um lead (não altera etapa nem responsável)")
    public LeadDetailResponse atualizar(@PathVariable String id, @Valid @RequestBody CreateLeadRequest req) {
        Lead atualizado = service.atualizar(id, req);
        return mapper.toDetail(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um lead")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }

    @PatchMapping("/{id}/etapa")
    @Operation(summary = "Atualizar a etapa do funil e/ou o responsável pela distribuição do lead")
    public LeadDetailResponse atualizarEtapa(@PathVariable String id, @Valid @RequestBody UpdateLeadStageRequest req) {
        Lead.Stage novaEtapa = Lead.Stage.valueOf(req.etapa().toUpperCase());
        Lead atualizado = service.atualizarEtapa(id, novaEtapa, req.responsavel());
        return mapper.toDetail(atualizado);
    }

    @GetMapping("/etapas")
    @Operation(summary = "Etapas do funil disponíveis")
    public List<Opcao> etapas() {
        return Arrays.stream(Lead.Stage.values()).map(s -> new Opcao(s.name(), s.rotulo())).toList();
    }

    @GetMapping("/origens")
    @Operation(summary = "Origens de lead disponíveis")
    public List<Opcao> origens() {
        return Arrays.stream(Lead.Origin.values()).map(o -> new Opcao(o.name(), o.rotulo())).toList();
    }

    public record Opcao(String valor, String rotulo) {
    }
}
