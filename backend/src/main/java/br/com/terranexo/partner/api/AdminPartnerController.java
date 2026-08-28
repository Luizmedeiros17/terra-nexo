package br.com.terranexo.partner.api;

import br.com.terranexo.partner.api.dto.CreatePartnerRequest;
import br.com.terranexo.partner.api.dto.PartnerCardResponse;
import br.com.terranexo.partner.api.dto.PartnerDetailResponse;
import br.com.terranexo.partner.api.dto.PartnerResumoResponse;
import br.com.terranexo.partner.api.dto.UpdateStatusRequest;
import br.com.terranexo.partner.application.PartnerService;
import br.com.terranexo.partner.application.PartnerSuggestionService;
import br.com.terranexo.partner.domain.Partner;
import br.com.terranexo.partner.domain.ProspectStatus;
import br.com.terranexo.partner.domain.Segment;
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
 * Base de prospecção de clientes e parceiros (agropecuária, imobiliárias,
 * irrigação, silos/armazéns, leilão de animais, laticínios) — uso interno da
 * equipe comercial, independente do catálogo público de imóveis.
 *
 * <p>No protótipo estas rotas ficam abertas, como as demais de {@code /admin}
 * (ver {@link br.com.terranexo.property.api.AdminPropertyController}); a
 * autenticação por perfil entra na etapa 1 do plano de implementação.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/partners")
@Tag(name = "Prospecção", description = "Base de clientes e parceiros comerciais")
public class AdminPartnerController {

    private final PartnerService service;
    private final PartnerSuggestionService suggestionService;
    private final PartnerMapper mapper;

    public AdminPartnerController(PartnerService service, PartnerSuggestionService suggestionService,
                                  PartnerMapper mapper) {
        this.service = service;
        this.suggestionService = suggestionService;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar parceiros/prospects com filtros por cidade, setor e status")
    public List<PartnerCardResponse> listar(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String setor,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        Segment setorEnum = setor == null || setor.isBlank() ? null : Segment.valueOf(setor.toUpperCase());
        ProspectStatus statusEnum = status == null || status.isBlank()
                ? null : ProspectStatus.valueOf(status.toUpperCase());
        return service.listar(cidade, setorEnum, statusEnum, q).stream().map(mapper::toCard).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um parceiro/prospect, incluindo sócios, contatos e histórico de status")
    public PartnerDetailResponse detalhe(@PathVariable String id) {
        Partner partner = service.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Parceiro não encontrado: " + id));
        return mapper.toDetail(partner);
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo parceiro/prospect")
    public ResponseEntity<PartnerDetailResponse> criar(@Valid @RequestBody CreatePartnerRequest req) {
        Partner criado = service.criar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDetail(criado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados cadastrais de um parceiro (não altera o status do funil)")
    public PartnerDetailResponse atualizar(@PathVariable String id, @Valid @RequestBody CreatePartnerRequest req) {
        Partner atualizado = service.atualizar(id, req);
        return mapper.toDetail(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um parceiro/prospect")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar o status do funil de prospecção (grava também no histórico)")
    public PartnerDetailResponse atualizarStatus(@PathVariable String id,
                                                 @Valid @RequestBody UpdateStatusRequest req) {
        ProspectStatus novoStatus = ProspectStatus.valueOf(req.status().toUpperCase());
        Partner atualizado = service.atualizarStatus(id, novoStatus, req.responsavel(), req.observacao());
        return mapper.toDetail(atualizado);
    }

    @GetMapping("/sugestoes")
    @Operation(summary = "Sugerir parceiros do mesmo município de um imóvel (uso interno da equipe comercial)")
    public List<PartnerCardResponse> sugestoes(
            @RequestParam String imovelId,
            @RequestParam(required = false, defaultValue = "6") int limite) {
        return suggestionService.sugerirParaImovel(imovelId, limite).stream().map(mapper::toCard).toList();
    }

    @GetMapping("/setores")
    @Operation(summary = "Segmentos disponíveis")
    public List<Opcao> setores() {
        return Arrays.stream(Segment.values()).map(s -> new Opcao(s.name(), s.rotulo())).toList();
    }

    @GetMapping("/status")
    @Operation(summary = "Status do funil de prospecção disponíveis")
    public List<Opcao> status() {
        return Arrays.stream(ProspectStatus.values()).map(s -> new Opcao(s.name(), s.rotulo())).toList();
    }

    @GetMapping("/resumo")
    @Operation(summary = "Contagens por cidade, setor e status — visão do painel")
    public PartnerResumoResponse resumo() {
        PartnerService.Resumo r = service.resumo();
        List<PartnerResumoResponse.Contagem> porCidade = r.porCidade().entrySet().stream()
                .map(e -> new PartnerResumoResponse.Contagem(e.getKey(), e.getKey(), e.getValue()))
                .toList();
        List<PartnerResumoResponse.Contagem> porSetor = r.porSetor().entrySet().stream()
                .map(e -> new PartnerResumoResponse.Contagem(e.getKey(), Segment.valueOf(e.getKey()).rotulo(), e.getValue()))
                .toList();
        List<PartnerResumoResponse.Contagem> porStatus = r.porStatus().entrySet().stream()
                .map(e -> new PartnerResumoResponse.Contagem(e.getKey(), ProspectStatus.valueOf(e.getKey()).rotulo(), e.getValue()))
                .toList();
        return new PartnerResumoResponse(r.total(), porCidade, porSetor, porStatus);
    }

    public record Opcao(String valor, String rotulo) {
    }
}
