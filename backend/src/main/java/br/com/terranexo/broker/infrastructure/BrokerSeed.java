package br.com.terranexo.broker.infrastructure;

import br.com.terranexo.broker.domain.Broker;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Carga inicial da equipe de corretores. Inclui os 4 corretores fictícios que
 * compunham a vitrine pública original e o corretor real da plataforma
 * (Luiz Medeiros — Corretor Remax Prime), conforme decidido em 28/08/2026.
 */
final class BrokerSeed {

    private BrokerSeed() {
    }

    static List<Broker> carregar() {
        OffsetDateTime agora = OffsetDateTime.now(ZoneOffset.UTC);
        return List.of(
                new Broker("cor-001", "luiz-medeiros", "Luiz Medeiros", "Corretor Remax Prime",
                        "CRECI-MG 45.213-F", null,
                        "Corretor responsável pela plataforma Terra Nexo, atuando nas 7 cidades atendidas "
                                + "com foco em imóveis urbanos e rurais.",
                        List.of("Urbano", "Fazendas", "Avaliação"),
                        List.of("Unaí", "Paracatu", "Formosa", "Luziânia", "Cristalina", "Catalão", "João Pinheiro"),
                        "luiz.medeiros01@gmail.com", "(38) 99999-0001", true, agora, agora),
                new Broker("cor-002", "marina-alencar", "Marina Alencar", "Sócia-fundadora", "CRECI 98.765-F",
                        "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=300&q=70",
                        "Especialista em imóveis urbanos das cidades atendidas pelo Terra Nexo, com foco em residências e avaliação.",
                        List.of("Urbano", "Avaliação"), List.of("Unaí"),
                        null, null, true, agora, agora),
                new Broker("cor-003", "rafael-ortiz", "Rafael Ortiz", "Consultor sênior", "CRECI 87.432-F",
                        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=70",
                        "Atua no entorno do Distrito Federal, com foco em fazendas de pecuária e negócios para investidores rurais.",
                        List.of("Pecuária", "Investimento"), List.of("Luziânia"),
                        null, null, true, agora, agora),
                new Broker("cor-004", "juliana-prado", "Juliana Prado", "Consultora", "CRECI 104.219-F",
                        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=70",
                        "Formosa, João Pinheiro e Cristalina. Conduz negociações de fazendas e sítios de lazer.",
                        List.of("Fazendas", "Sítios de lazer"), List.of("Formosa", "João Pinheiro", "Cristalina"),
                        null, null, true, agora, agora),
                new Broker("cor-005", "eduardo-salles", "Eduardo Salles", "Diretor rural", "CRECI 76.108-F",
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=70",
                        "Responsável pela carteira rural: análise de matrícula, CAR, georreferenciamento e avaliação de terras.",
                        List.of("Fazendas", "Arrendamento", "Georreferenciamento"),
                        List.of("Unaí", "Paracatu", "Catalão", "Cristalina"),
                        null, null, true, agora, agora));
    }
}
