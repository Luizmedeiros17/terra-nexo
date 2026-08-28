package br.com.terranexo.partner.application;

import br.com.terranexo.partner.domain.Partner;
import br.com.terranexo.partner.domain.PartnerRepository;
import br.com.terranexo.partner.domain.ProspectStatus;
import br.com.terranexo.partner.domain.Segment;
import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Cruza o catálogo de imóveis com a base de prospecção: sugere parceiros
 * (agropecuária, irrigação, silos/armazéns, leilão de animais, laticínios —
 * ver {@link Segment#relevanteParaRural}) do mesmo município de um imóvel
 * rural, para uso interno da equipe comercial.
 *
 * <p>Limitação conhecida: só compara município exato (sem noção de distância
 * entre cidades vizinhas) — documentado, não é um TODO escondido.</p>
 */
@Service
public class PartnerSuggestionService {

    private final PartnerRepository partnerRepository;
    private final PropertyRepository propertyRepository;

    public PartnerSuggestionService(PartnerRepository partnerRepository, PropertyRepository propertyRepository) {
        this.partnerRepository = partnerRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<Partner> sugerirParaImovel(String imovelId, int limite) {
        Property imovel = propertyRepository.findById(imovelId)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + imovelId));

        boolean rural = imovel.categoria() == Category.RURAL;
        String municipio = imovel.endereco().municipio();

        return partnerRepository.findByMunicipio(municipio).stream()
                .filter(p -> !rural || Segment.relevanteParaRural(p.setor()))
                .sorted(Comparator
                        .comparing((Partner p) -> prioridadeStatus(p.status()))
                        .thenComparing(Partner::nomeFantasia))
                .limit(Math.max(1, limite))
                .toList();
    }

    /** Parceiros com relacionamento já mais avançado aparecem primeiro na sugestão. */
    private int prioridadeStatus(ProspectStatus status) {
        return switch (status) {
            case CONVERTIDO -> 0;
            case REUNIAO_MARCADA, PROPOSTA_ENVIADA -> 1;
            case RESPONDEU, CONTATADO -> 2;
            case PESQUISADO, NAO_INICIADO -> 3;
            case SEM_INTERESSE, CONTATO_INVALIDO -> 4;
        };
    }
}
