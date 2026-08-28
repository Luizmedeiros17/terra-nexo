package br.com.terranexo.broker.api;

import br.com.terranexo.broker.api.dto.BrokerCardResponse;
import br.com.terranexo.broker.api.dto.BrokerDetailResponse;
import br.com.terranexo.broker.domain.Broker;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BrokerMapper {

    public BrokerCardResponse toCard(Broker b, Map<String, Long> carteira) {
        return new BrokerCardResponse(b.id(), b.slug(), b.nome(), b.cargo(), b.creci(), b.foto(),
                b.especialidades(), b.regioes(), b.ativo(), carteira.getOrDefault(b.slug(), 0L));
    }

    public BrokerDetailResponse toDetail(Broker b, long imoveis) {
        return new BrokerDetailResponse(b.id(), b.slug(), b.nome(), b.cargo(), b.creci(), b.foto(), b.bio(),
                b.especialidades(), b.regioes(), b.email(), b.telefone(), b.ativo(), imoveis,
                b.criadoEm(), b.atualizadoEm());
    }
}
