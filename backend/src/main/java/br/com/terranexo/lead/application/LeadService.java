package br.com.terranexo.lead.application;

import br.com.terranexo.lead.api.dto.CreateLeadRequest;
import br.com.terranexo.lead.domain.Lead;
import br.com.terranexo.lead.domain.LeadRepository;
import br.com.terranexo.shared.text.Textos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Captação e gestão de leads (seção 17). No protótipo os leads ficam em
 * memória; a notificação por e-mail entra junto do módulo administrativo.
 */
@Service
public class LeadService {

    private static final Logger log = LoggerFactory.getLogger(LeadService.class);

    private final LeadRepository repository;
    private final Clock clock;

    @Autowired
    public LeadService(LeadRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    LeadService(LeadRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Captação pública (portal): nasce sempre em {@link Lead.Stage#NOVO}, sem responsável. */
    public Lead registrar(Lead parcial) {
        String id = UUID.randomUUID().toString();
        String protocolo = "TN-%d-%04d".formatted(
                OffsetDateTime.now(clock).getYear(), repository.proximaSequencia());
        OffsetDateTime agora = OffsetDateTime.now(clock);

        Lead lead = new Lead(id, protocolo, parcial.nome(), parcial.email(), parcial.telefone(),
                parcial.mensagem(), parcial.melhorHorario(), parcial.origem(), parcial.imovelCodigo(),
                parcial.paginaOrigem(), parcial.utmSource(), parcial.utmMedium(), parcial.utmCampaign(),
                parcial.consentimento(), parcial.consentimentoVersao(),
                Lead.Stage.NOVO, null, agora, agora);

        repository.save(lead);
        // Log sem dados pessoais além do necessário para rastrear o atendimento (seção 26).
        log.info("Lead captado protocolo={} origem={} imovel={} pagina={}",
                protocolo, lead.origem(), lead.imovelCodigo(), lead.paginaOrigem());
        return lead;
    }

    /** Sinaliza possível duplicidade por telefone/e-mail no mesmo imóvel (seção 17). */
    public boolean possivelDuplicidade(String email, String telefone, String imovelCodigo) {
        return repository.existsByEmailOrTelefoneAndImovel(email, telefone, imovelCodigo);
    }

    public List<Lead> listar(Lead.Stage etapa, Lead.Origin origem, String q) {
        return repository.findAll().stream()
                .filter(l -> etapa == null || l.etapa() == etapa)
                .filter(l -> origem == null || l.origem() == origem)
                .filter(l -> q == null || q.isBlank() || Textos.normalizar(
                        l.nome() + " " + l.email() + " " + l.telefone() + " " + l.protocolo())
                        .contains(Textos.normalizar(q)))
                .sorted(Comparator.comparing(Lead::criadoEm).reversed())
                .toList();
    }

    public Optional<Lead> buscarPorId(String id) {
        return repository.findById(id);
    }

    /** Cadastro manual pelo painel administrativo — sempre com origem CADASTRO_MANUAL. */
    public Lead criarManual(CreateLeadRequest req) {
        String id = UUID.randomUUID().toString();
        String protocolo = "TN-%d-%04d".formatted(
                OffsetDateTime.now(clock).getYear(), repository.proximaSequencia());
        OffsetDateTime agora = OffsetDateTime.now(clock);

        Lead lead = new Lead(id, protocolo, req.nome(), req.email(), req.telefone(), req.mensagem(),
                req.melhorHorario(), Lead.Origin.CADASTRO_MANUAL, req.imovelCodigo(), null, null, null, null,
                true, "cadastro-manual-painel", Lead.Stage.NOVO, req.responsavel(), agora, agora);
        return repository.save(lead);
    }

    /** Atualiza os dados de contato de um lead. Não mexe em etapa nem em responsável. */
    public Lead atualizar(String id, CreateLeadRequest req) {
        Lead existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado: " + id));
        Lead atualizado = existente.comEdicao(req.nome(), req.email(), req.telefone(), req.mensagem(),
                req.melhorHorario(), existente.responsavel(), OffsetDateTime.now(clock));
        return repository.save(atualizado);
    }

    /** Atualiza a etapa do funil e/ou o responsável pela distribuição do lead. */
    public Lead atualizarEtapa(String id, Lead.Stage novaEtapa, String responsavel) {
        Lead existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lead não encontrado: " + id));
        Lead atualizado = existente.comEtapa(novaEtapa, responsavel, OffsetDateTime.now(clock));
        return repository.save(atualizado);
    }

    public void excluir(String id) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Lead não encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
