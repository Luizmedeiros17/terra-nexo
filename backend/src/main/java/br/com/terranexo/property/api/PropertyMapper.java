package br.com.terranexo.property.api;

import br.com.terranexo.property.api.dto.CreatePropertyRequest;
import br.com.terranexo.property.api.dto.PropertyCardResponse;
import br.com.terranexo.property.api.dto.PropertyDetailResponse;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.Purpose;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Converte o domínio em DTOs públicos — entidades nunca são expostas (seção 5). */
@Component
public class PropertyMapper {

    private static final Locale BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PropertyCardResponse toCard(Property p) {
        return new PropertyCardResponse(
                p.id(),
                p.codigo(),
                p.slug(),
                "/imoveis/" + p.slug() + "-" + p.codigo().toLowerCase(),
                p.titulo(),
                p.chamada(),
                p.categoria().name(),
                p.categoria().rotulo(),
                p.tipo(),
                p.finalidade().name(),
                p.finalidade().rotulo(),
                p.situacao().name(),
                p.endereco().resumoPublico(),
                p.endereco().municipio(),
                p.endereco().uf(),
                p.valores().exibirPreco() ? p.valores().preco() : null,
                p.valores().exibirPreco(),
                precoTexto(p),
                precoSufixo(p),
                p.valores().condominio(),
                p.areas().areaTotalM2(),
                areaTexto(p),
                new PropertyCardResponse.Ambientes(
                        p.ambientes().dormitorios(), p.ambientes().suites(),
                        p.ambientes().banheiros(), p.ambientes().vagas()),
                resumoRural(p),
                selos(p),
                new PropertyCardResponse.Imagem(p.capaUrl(), p.capaAlt()),
                new PropertyCardResponse.Corretor(p.corretorSlug(), p.corretorNome()),
                p.atualizadoEm());
    }

    public PropertyDetailResponse toDetail(Property p) {
        // A galeria pública mostra só fotos — vídeos ficam para uma exibição dedicada futura.
        List<PropertyCardResponse.Imagem> galeria = p.midias().stream()
                .filter(m -> m.tipo() == Property.MediaType.FOTO)
                .sorted(Comparator.comparingInt(Property.Media::ordem))
                .map(m -> new PropertyCardResponse.Imagem(m.url(), m.textoAlternativo()))
                .toList();

        return new PropertyDetailResponse(
                toCard(p),
                p.descricao(),
                galeria,
                List.copyOf(p.caracteristicas()),
                fichaTecnica(p),
                p.isRural() ? toRural(p) : null);
    }

    /**
     * Reconstrói o payload de cadastro a partir do domínio — usado para
     * pré-preencher o formulário de edição e como base do corpo do PUT.
     */
    public CreatePropertyRequest toEditRequest(Property p) {
        return new CreatePropertyRequest(
                p.categoria().name(),
                p.tipo(),
                p.finalidade().name(),
                p.situacao().name(),
                p.titulo(),
                p.chamada(),
                p.descricao(),
                new CreatePropertyRequest.Valores(p.valores().preco(), p.valores().condominio(),
                        p.valores().iptu(), p.valores().arrendamentoPorHectare(),
                        p.valores().exibirPreco(), p.valores().textoAlternativo()),
                new CreatePropertyRequest.Areas(p.areas().valorNaUnidadeInformada(),
                        p.areas().unidadeInformada(), p.areas().areaConstruidaM2(), p.areas().areaTerrenoM2()),
                new CreatePropertyRequest.Ambientes(p.ambientes().dormitorios(), p.ambientes().suites(),
                        p.ambientes().banheiros(), p.ambientes().vagas()),
                new CreatePropertyRequest.Endereco(p.endereco().uf(), p.endereco().municipio(),
                        p.endereco().bairro(), p.endereco().logradouro(), p.endereco().numero(),
                        p.endereco().complemento(), p.endereco().cep(), p.endereco().regiaoSlug(),
                        p.endereco().latitude(), p.endereco().longitude(), p.endereco().exposicao().name()),
                List.copyOf(p.caracteristicas()),
                p.corretorSlug(), p.corretorNome(), null,
                p.destaque(), p.exclusivo(), p.lancamento(), p.oportunidade(),
                p.aceitaPermuta(), p.aceitaFinanciamento(),
                p.urbano() == null ? null : new CreatePropertyRequest.Urbano(
                        p.urbano().condominioNome(), p.urbano().andar(), p.urbano().anoConstrucao(),
                        p.urbano().mobiliado(), p.urbano().aceitaAnimais(), p.urbano().transporteProximo()),
                p.rural() == null ? null : toRuralRequest(p.rural()));
    }

    private CreatePropertyRequest.Rural toRuralRequest(Property.Rural r) {
        return new CreatePropertyRequest.Rural(
                r.areaProdutivaHa(), r.areaPastagemHa(), r.areaCultivadaHa(), r.areaMataHa(),
                r.reservaLegalHa(), r.areaIrrigadaHa(),
                List.copyOf(r.terreno()), r.soloPredominante(), r.soloFertilidade(),
                List.copyOf(r.atividades()),
                r.culturas().stream()
                        .map(c -> new CreatePropertyRequest.Rural.Cultura(
                                c.nome(), c.categoria(), c.areaHa(), c.safra(), c.produtividade()))
                        .toList(),
                r.pecuaria() == null ? null : new CreatePropertyRequest.Rural.Pecuaria(
                        r.pecuaria().tipoCriacao(), r.pecuaria().capacidadeCabecas(),
                        r.pecuaria().divisoesPasto(), List.copyOf(r.pecuaria().estruturas())),
                List.copyOf(r.recursosHidricos()),
                r.benfeitorias().stream()
                        .map(b -> new CreatePropertyRequest.Rural.Benfeitoria(
                                b.tipo(), b.quantidade(), b.areaM2(), b.conservacao()))
                        .toList(),
                new CreatePropertyRequest.Rural.Acesso(
                        r.acesso().tipoAcesso(), r.acesso().distanciaAsfaltoKm(), r.acesso().distanciaCidadeKm(),
                        r.acesso().municipioReferencia(), r.acesso().acessoAnoTodo(),
                        r.acesso().energiaTrifasica(), r.acesso().internet(), r.acesso().sinalCelular()),
                new CreatePropertyRequest.Rural.Documentacao(
                        r.documentacao().carInformado(), r.documentacao().ccirInformado(),
                        r.documentacao().sigefInformado(), r.documentacao().matriculaInformada(),
                        r.documentacao().georreferenciado(), r.documentacao().situacaoDeclarada(),
                        r.documentacao().matriculaNumero(), r.documentacao().carReciboNumero(),
                        r.documentacao().carDemonstrativoCompletoInformado(),
                        r.documentacao().carMapaPoligonoInformado(),
                        r.documentacao().sigefSituacaoGeorreferenciamento(),
                        r.documentacao().sigefPlantaMemorialDisponivel(),
                        r.documentacao().sncrIncraSituacaoCadastral(),
                        r.documentacao().cnirCafirCib(), r.documentacao().cnirCafirSituacaoCadastral()),
                r.fonteInformacao(),
                r.dataReferencia() == null ? null : r.dataReferencia().toString());
    }

    // ------------------------------------------------------------------

    private String precoTexto(Property p) {
        if (!p.valores().exibirPreco()) {
            return p.valores().textoAlternativo() == null ? "Consulte-nos" : p.valores().textoAlternativo();
        }
        return moeda(p.valores().preco());
    }

    private String precoSufixo(Property p) {
        return switch (p.finalidade()) {
            case LOCACAO -> "/mês";
            case ARRENDAMENTO -> "/ha por ano";
            default -> null;
        };
    }

    private String moeda(BigDecimal valor) {
        if (valor == null) {
            return "Consulte-nos";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(BR);
        nf.setMaximumFractionDigits(0);
        return nf.format(valor);
    }

    /** Rurais são apresentados na unidade cadastrada (ha/alqueire); urbanos em m². */
    private String areaTexto(Property p) {
        Property.Areas a = p.areas();
        return switch (a.unidadeInformada()) {
            case "ha" -> numero(a.valorNaUnidadeInformada()) + " ha";
            case "alq" -> numero(a.valorNaUnidadeInformada()) + " alq.";
            default -> numero(a.areaTotalM2()) + " m²";
        };
    }

    private String numero(double valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(BR);
        nf.setMaximumFractionDigits(valor < 10 ? 1 : 0);
        return nf.format(valor);
    }

    /** Linha-resumo exibida no cartão de imóveis rurais (seção 13). */
    private String resumoRural(Property p) {
        if (!p.isRural()) {
            return null;
        }
        Property.Rural r = p.rural();
        List<String> partes = new ArrayList<>();
        if (!r.atividades().isEmpty()) {
            partes.add(String.join(" e ", r.atividades()).toLowerCase(BR));
        }
        if (!r.culturas().isEmpty()) {
            partes.add(r.culturas().stream()
                    .map(Property.Rural.Culture::nome)
                    .limit(2)
                    .reduce((a, b) -> a + " e " + b)
                    .orElse(""));
        }
        if (r.pecuaria() != null && r.pecuaria().capacidadeCabecas() != null) {
            partes.add(numero(r.pecuaria().capacidadeCabecas()) + " cabeças");
        }
        if (r.areaIrrigadaHa() != null && r.areaIrrigadaHa() > 0) {
            partes.add(numero(r.areaIrrigadaHa()) + " ha irrigados");
        }
        if (!r.recursosHidricos().isEmpty()) {
            partes.add(String.join(", ", r.recursosHidricos()).toLowerCase(BR));
        }
        partes.add("a " + numero(r.acesso().distanciaAsfaltoKm()) + " km do asfalto");
        return String.join(" · ", partes);
    }

    private List<String> selos(Property p) {
        List<String> selos = new ArrayList<>();
        if (p.exclusivo()) selos.add("Exclusivo");
        if (p.destaque()) selos.add("Destaque");
        if (p.lancamento()) selos.add("Lançamento");
        if (p.oportunidade()) selos.add("Oportunidade");
        if (p.aceitaPermuta()) selos.add("Aceita permuta");
        if (p.isRural() && p.rural().documentacao().georreferenciado()) selos.add("Georreferenciado");
        return selos;
    }

    private Map<String, String> fichaTecnica(Property p) {
        Map<String, String> ficha = new LinkedHashMap<>();
        ficha.put("Código", p.codigo());
        ficha.put("Categoria", p.categoria().rotulo());
        ficha.put("Tipo", p.tipo());
        ficha.put("Finalidade", p.finalidade().rotulo());
        ficha.put("Área", areaTexto(p));
        if (p.areas().areaConstruidaM2() != null) {
            ficha.put("Área construída", numero(p.areas().areaConstruidaM2()) + " m²");
        }
        if (p.valores().condominio() != null && p.valores().condominio().signum() > 0) {
            ficha.put("Condomínio", moeda(p.valores().condominio()));
        }
        if (p.valores().iptu() != null && p.valores().iptu().signum() > 0) {
            ficha.put("IPTU", moeda(p.valores().iptu()));
        }
        if (p.finalidade() == Purpose.ARRENDAMENTO && p.valores().arrendamentoPorHectare() != null) {
            ficha.put("Arrendamento", moeda(p.valores().arrendamentoPorHectare()) + "/ha por ano");
        }
        if (p.urbano() != null && p.urbano().anoConstrucao() != null) {
            ficha.put("Ano de construção", String.valueOf(p.urbano().anoConstrucao()));
        }
        ficha.put("Atualizado em", p.atualizadoEm().format(DATA));
        return ficha;
    }

    private PropertyDetailResponse.RuralDetalhe toRural(Property p) {
        Property.Rural r = p.rural();
        List<String> docs = new ArrayList<>();
        if (r.documentacao().carInformado()) docs.add("CAR");
        if (r.documentacao().ccirInformado()) docs.add("CCIR");
        if (r.documentacao().sigefInformado()) docs.add("SIGEF");
        if (r.documentacao().matriculaInformada()) docs.add("Matrícula");
        if (r.documentacao().georreferenciado()) docs.add("Georreferenciamento");

        return new PropertyDetailResponse.RuralDetalhe(
                p.areaHectares(),
                r.areaProdutivaHa(), r.areaPastagemHa(), r.areaCultivadaHa(), r.areaMataHa(),
                r.reservaLegalHa(), r.areaIrrigadaHa(),
                List.copyOf(r.terreno()), r.soloPredominante(), r.soloFertilidade(),
                List.copyOf(r.atividades()),
                r.culturas().stream()
                        .map(c -> new PropertyDetailResponse.RuralDetalhe.Cultura(
                                c.nome(), c.categoria(), c.areaHa(), c.safra(), c.produtividade()))
                        .toList(),
                r.pecuaria() == null ? null : new PropertyDetailResponse.RuralDetalhe.Pecuaria(
                        r.pecuaria().tipoCriacao(), r.pecuaria().capacidadeCabecas(),
                        r.pecuaria().divisoesPasto(), List.copyOf(r.pecuaria().estruturas())),
                List.copyOf(r.recursosHidricos()),
                r.benfeitorias().stream()
                        .map(b -> new PropertyDetailResponse.RuralDetalhe.Benfeitoria(
                                b.tipo(), b.quantidade(), b.areaM2(), b.conservacao()))
                        .toList(),
                new PropertyDetailResponse.RuralDetalhe.Acesso(
                        r.acesso().tipoAcesso(), r.acesso().distanciaAsfaltoKm(), r.acesso().distanciaCidadeKm(),
                        r.acesso().municipioReferencia(), r.acesso().acessoAnoTodo(),
                        r.acesso().energiaTrifasica(), r.acesso().internet(), r.acesso().sinalCelular()),
                docs,
                r.documentacao().situacaoDeclarada(),
                r.fonteInformacao(),
                r.dataReferencia() == null ? null : r.dataReferencia().format(DATA));
    }
}
