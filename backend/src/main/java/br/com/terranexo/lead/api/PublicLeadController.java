package br.com.terranexo.lead.api;

import br.com.terranexo.lead.application.LeadService;
import br.com.terranexo.lead.domain.Lead;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Captação pública de leads (seção 23: {@code POST /public/leads}). */
@RestController
@RequestMapping("/api/v1/public/leads")
@Tag(name = "Leads", description = "Contatos enviados pelo portal público")
public class PublicLeadController {

    private final LeadService service;

    public PublicLeadController(LeadService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar um contato do portal")
    public ResponseEntity<LeadResponse> captar(@Valid @RequestBody LeadRequest req) {
        boolean duplicado = service.possivelDuplicidade(req.email(), req.telefone(), req.imovelCodigo());

        Lead lead = service.registrar(new Lead(null, null, req.nome(), req.email(), req.telefone(),
                req.mensagem(), req.melhorHorario(),
                req.imovelCodigo() == null || req.imovelCodigo().isBlank()
                        ? Lead.Origin.CONTATO_GERAL : Lead.Origin.FORMULARIO_IMOVEL,
                req.imovelCodigo(), req.paginaOrigem(), req.utmSource(), req.utmMedium(),
                req.utmCampaign(), req.consentimento(), "politica-privacidade-v1",
                null, null, null, null));

        return ResponseEntity.status(HttpStatus.CREATED).body(new LeadResponse(
                lead.protocolo(),
                "Recebemos seu contato. Um consultor responde em até 2 horas úteis.",
                duplicado));
    }

    /** Campos do formulário de interesse (seção 14). */
    public record LeadRequest(
            @NotBlank(message = "Informe seu nome") @Size(max = 120) String nome,
            @NotBlank(message = "Informe um e-mail") @Email(message = "E-mail inválido") String email,
            @NotBlank(message = "Informe um telefone") @Size(min = 10, max = 20) String telefone,
            @Size(max = 2000) String mensagem,
            @Size(max = 60) String melhorHorario,
            String imovelCodigo,
            String paginaOrigem,
            String utmSource,
            String utmMedium,
            String utmCampaign,
            @AssertTrue(message = "É necessário aceitar a política de privacidade") boolean consentimento) {
    }

    public record LeadResponse(String protocolo, String mensagem, boolean possivelDuplicidade) {
    }
}
