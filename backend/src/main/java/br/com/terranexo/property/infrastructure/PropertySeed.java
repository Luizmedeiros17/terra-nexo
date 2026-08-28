package br.com.terranexo.property.infrastructure;

import br.com.terranexo.property.domain.Category;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.Property.Address.Exposure;
import br.com.terranexo.property.domain.PropertyBuilder;
import br.com.terranexo.property.domain.PropertyStatus;
import br.com.terranexo.property.domain.Purpose;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Carga inicial do protótipo: imóveis urbanos e rurais publicados, com
 * profundidade suficiente para exercitar todos os filtros da seção 13.
 *
 * <p>Restrita às sete cidades cobertas pela base de prospecção de clientes e
 * parceiros (docs/prospeccao/db): Unaí, Paracatu, Formosa, Luziânia,
 * Cristalina, Catalão e João Pinheiro — permite exercitar
 * PartnerSuggestionService de ponta a ponta.</p>
 *
 * <p>Substituível por Flyway + tabela {@code property} sem alterar as demais camadas.</p>
 */
final class PropertySeed {

    private PropertySeed() {
    }

    private static String u(String id, int w) {
        return "https://images.unsplash.com/photo-" + id + "?auto=format&fit=crop&w=" + w + "&q=70";
    }

    private static String foto(String id) {
        return u(id, 900);
    }

    private static Property.Address urbano(String uf, String municipio, String bairro, String logradouro,
                                           String cep, String regiao, double lat, double lng) {
        return new Property.Address(uf, municipio, bairro, logradouro, null, null, cep, regiao,
                lat, lng, Exposure.BAIRRO_MUNICIPIO);
    }

    /** Imóveis rurais expõem apenas município e região; coordenadas exatas são privadas (regra 24.5). */
    private static Property.Address rural(String uf, String municipio, String regiao,
                                          double latAprox, double lngAprox) {
        return new Property.Address(uf, municipio, null, null, null, null, null, regiao,
                latAprox, lngAprox, Exposure.SOMENTE_MUNICIPIO);
    }

    static List<Property> carregar() {
        return List.of(
                // ============================ URBANOS ============================

                PropertyBuilder.novo("URB-000101", "casa-centro-unai-mg")
                        .tipo("Casa").finalidade(Purpose.VENDA)
                        .titulo("Casa térrea reformada no Centro de Unaí")
                        .chamada("Perto do comércio e dos serviços, com quintal e área gourmet.")
                        .descricao("Casa térrea com três dormitórios, totalmente reformada, em rua tranquila e "
                                + "arborizada no Centro de Unaí, a poucos minutos do comércio, bancos e escolas.")
                        .valores(Property.Money.venda(650_000, 0, 380))
                        .areas(new Property.Areas(180, 180d, 300d, "m²", 180))
                        .ambientes(3, 1, 2, 2)
                        .endereco(urbano("MG", "Unaí", "Centro", "Rua Goiás", "38610-000", null, -16.3567, -46.9075))
                        .caracteristicas("Quintal", "Área gourmet", "Portão eletrônico")
                        .urbano(new Property.Urban(null, null, 2010, false, true, List.of()))
                        .fotos("Casa no Centro de Unaí", foto("1507918923745-5d2118eb516b"))
                        .corretor("marina-alencar", "Marina Alencar")
                        .datas(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 7, 18))
                        .build(),

                // ============================= RURAIS =============================

                PropertyBuilder.novo("RUR-000210", "fazenda-soja-cristalina-go")
                        .tipo("Fazenda").finalidade(Purpose.VENDA)
                        .titulo("Fazenda de soja de 1.200 hectares em Cristalina")
                        .chamada("Alta tecnologia, pivôs centrais e armazenagem própria no cerrado goiano.")
                        .descricao("Doze pivôs centrais, armazém para 90 mil sacas e sede administrativa. "
                                + "Área com histórico de correção e produtividade acima da média regional.")
                        .valores(Property.Money.venda(198_000_000, 0, 0))
                        .areas(Property.Areas.hectares(1200))
                        .ambientes(6, 3, 8, 12)
                        .endereco(rural("GO", "Cristalina", "goias-sudeste", -16.7680, -47.6130))
                        .caracteristicas("Pivôs centrais", "Armazém próprio", "Energia trifásica")
                        .rural(new Property.Rural(
                                1080, 0, 1080, 120, 240.0, 720.0,
                                Set.of("Plano"),
                                "Latossolo vermelho", "Alta",
                                Set.of("Agricultura"),
                                List.of(new Property.Rural.Culture("Soja", "Cultura anual", 1080, "2025/2026", "72 sacas/ha"),
                                        new Property.Rural.Culture("Milho", "Cultura anual", 900, "2025/2026", "180 sacas/ha"),
                                        new Property.Rural.Culture("Feijão irrigado", "Cultura anual", 320, "2025/2026", "58 sacas/ha")),
                                null,
                                Set.of("Rio", "Represa", "Poço artesiano", "Irrigação"),
                                List.of(new Property.Rural.Improvement("Casa sede", 1, 420d, "Ótimo"),
                                        new Property.Rural.Improvement("Armazém", 1, 2600d, "Ótimo"),
                                        new Property.Rural.Improvement("Silo", 4, null, "Bom"),
                                        new Property.Rural.Improvement("Oficina", 1, 600d, "Bom"),
                                        new Property.Rural.Improvement("Alojamentos", 2, 180d, "Bom"),
                                        new Property.Rural.Improvement("Balança", 1, null, "Bom")),
                                new Property.Rural.Access("Asfalto", 3.0, 34, "Cristalina", true, true, true, true),
                                new Property.Rural.Documentation(true, true, true, true, true, "Georreferenciada e certificada"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 6, 5)))
                        .fotos("Fazenda de soja em Cristalina", foto("1742020972203-8a5b1a83cdd6"), foto("1719253860282-cd3f93da6548"))
                        .corretor("eduardo-salles", "Eduardo Salles")
                        .destaque()
                        .datas(LocalDate.of(2026, 6, 8), LocalDate.of(2026, 7, 26))
                        .build(),

                // Suspenso: comprova que a busca pública só devolve imóveis publicados (regra 24.8).
                PropertyBuilder.novo("RUR-000218", "sitio-suspenso-cristalina-go")
                        .tipo("Sítio").finalidade(Purpose.VENDA)
                        .situacao(PropertyStatus.SUSPENSO)
                        .titulo("Sítio em Cristalina (anúncio suspenso)")
                        .chamada("Registro usado para validar o filtro de situação.")
                        .valores(Property.Money.venda(680_000, 0, 0))
                        .areas(Property.Areas.hectares(2.0))
                        .ambientes(2, 1, 2, 3)
                        .endereco(rural("GO", "Cristalina", "goias-sudeste", -16.7680, -47.6130))
                        .rural(new Property.Rural(1.4, 0.6, 0.6, 0.4, null, 0d,
                                Set.of("Ondulado"), "Cambissolo", "Média", Set.of("Lazer"),
                                List.of(), null, Set.of("Nascente"),
                                List.of(new Property.Rural.Improvement("Casa sede", 1, 150d, "Regular")),
                                new Property.Rural.Access("Estrada de terra", 5.0, 10, "Cristalina", true, false, false, true),
                                new Property.Rural.Documentation(false, false, false, true, false, "Em regularização"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 4, 5)))
                        .fotos("Sítio em Cristalina", foto("1573611236714-1fe596de1fd3"))
                        .corretor("juliana-prado", "Juliana Prado")
                        .datas(LocalDate.of(2026, 4, 10), LocalDate.of(2026, 5, 2))
                        .build(),

                PropertyBuilder.novo("RUR-000212", "fazenda-soja-leite-unai-mg")
                        .tipo("Fazenda").finalidade(Purpose.VENDA)
                        .titulo("Fazenda de soja e pecuária leiteira de 650 hectares em Unaí")
                        .chamada("Lavoura irrigada por pivô central e curral para ordenha mecanizada, a 12 km da sede.")
                        .descricao("Área plana a levemente ondulada, solo corrigido, com histórico de soja e milho "
                                + "safrinha e rebanho leiteiro em curral de ordenha mecanizada.")
                        .valores(Property.Money.venda(85_000_000, 0, 0))
                        .areas(Property.Areas.hectares(650))
                        .ambientes(4, 2, 4, 6)
                        .endereco(rural("MG", "Unaí", "noroeste-mineiro", -16.3577, -46.9061))
                        .caracteristicas("Pivô central", "Curral coberto", "Energia trifásica")
                        .rural(new Property.Rural(
                                520, 240, 280, 130, 130.0, 180.0,
                                Set.of("Plano", "Levemente ondulado"),
                                "Latossolo vermelho-amarelo", "Alta",
                                Set.of("Agricultura", "Pecuária de leite"),
                                List.of(new Property.Rural.Culture("Soja", "Cultura anual", 280, "2025/2026", "64 sacas/ha"),
                                        new Property.Rural.Culture("Milho safrinha", "Cultura anual", 180, "2025/2026", "150 sacas/ha")),
                                new Property.Rural.Livestock("Leiteira", 220, 8, Set.of("Curral de ordenha", "Sala de espera")),
                                Set.of("Rio", "Poço artesiano", "Irrigação"),
                                List.of(new Property.Rural.Improvement("Casa sede", 1, 260d, "Bom"),
                                        new Property.Rural.Improvement("Curral de ordenha", 1, 450d, "Ótimo"),
                                        new Property.Rural.Improvement("Silo", 2, null, "Bom"),
                                        new Property.Rural.Improvement("Galpão", 1, 500d, "Bom")),
                                new Property.Rural.Access("Asfalto", 8.0, 12, "Unaí", true, true, true, true),
                                new Property.Rural.Documentation(true, true, false, true, false,
                                        "CAR e CCIR regularizados; SIGEF em andamento"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 5, 20)))
                        .fotos("Fazenda de soja e leite em Unaí", foto("1719253860282-cd3f93da6548"), foto("1573611236714-1fe596de1fd3"))
                        .corretor("eduardo-salles", "Eduardo Salles")
                        .datas(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 7, 15))
                        .build(),

                PropertyBuilder.novo("RUR-000213", "area-agricola-arrendamento-paracatu-mg")
                        .tipo("Propriedade agrícola").finalidade(Purpose.ARRENDAMENTO)
                        .titulo("Área agrícola de 480 hectares para arrendamento em Paracatu")
                        .chamada("Solo corrigido e histórico de soja e milho, contrato mínimo de quatro safras.")
                        .descricao("Área 100% mecanizável em região consolidada de grãos, valor por hectare/ano.")
                        .valores(new Property.Money(java.math.BigDecimal.valueOf(4_200), null, null,
                                java.math.BigDecimal.valueOf(4_200), true, null))
                        .areas(Property.Areas.hectares(480))
                        .ambientes(0, 0, 1, 2)
                        .endereco(rural("MG", "Paracatu", "noroeste-mineiro", -17.2222, -46.8748))
                        .caracteristicas("Energia trifásica", "Solo corrigido")
                        .rural(new Property.Rural(
                                460, 0, 460, 20, 96.0, 0d,
                                Set.of("Plano"),
                                "Latossolo vermelho", "Alta",
                                Set.of("Agricultura"),
                                List.of(new Property.Rural.Culture("Soja", "Cultura anual", 460, "2025/2026", "68 sacas/ha"),
                                        new Property.Rural.Culture("Milho safrinha", "Cultura anual", 420, "2025/2026", "165 sacas/ha")),
                                null,
                                Set.of("Poço artesiano"),
                                List.of(new Property.Rural.Improvement("Galpão", 1, 380d, "Bom"),
                                        new Property.Rural.Improvement("Estradas internas", 1, null, "Bom")),
                                new Property.Rural.Access("Asfalto", 5.0, 22, "Paracatu", true, true, true, true),
                                new Property.Rural.Documentation(true, true, false, true, true,
                                        "CAR ativo, CCIR regularizado"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 6, 12)))
                        .fotos("Área agrícola em Paracatu", foto("1742020972203-8a5b1a83cdd6"), foto("1720334540915-335eaf0eb1d2"))
                        .corretor("eduardo-salles", "Eduardo Salles")
                        .semFinanciamento()
                        .datas(LocalDate.of(2026, 6, 18), LocalDate.of(2026, 7, 20))
                        .build(),

                PropertyBuilder.novo("RUR-000214", "fazenda-graos-pecuaria-formosa-go")
                        .tipo("Fazenda").finalidade(Purpose.VENDA)
                        .titulo("Fazenda de grãos e pecuária de 1.050 hectares em Formosa")
                        .chamada("Lavoura de soja e milho com armazenagem própria e pastagem formada para recria.")
                        .descricao("Propriedade mista no entorno do Distrito Federal, com armazém próprio para "
                                + "grãos e curral estruturado para recria de gado de corte.")
                        .valores(Property.Money.venda(72_000_000, 0, 0))
                        .areas(Property.Areas.hectares(1050))
                        .ambientes(5, 2, 5, 10)
                        .endereco(rural("GO", "Formosa", "entorno-df", -15.5372, -47.3346))
                        .caracteristicas("Armazém próprio", "Energia trifásica", "Curral")
                        .rural(new Property.Rural(
                                930, 430, 500, 120, 210.0, 0d,
                                Set.of("Plano", "Levemente ondulado"),
                                "Latossolo vermelho", "Média",
                                Set.of("Agricultura", "Pecuária de corte"),
                                List.of(new Property.Rural.Culture("Soja", "Cultura anual", 500, "2025/2026", "62 sacas/ha"),
                                        new Property.Rural.Culture("Milho", "Cultura anual", 300, "2025/2026", "145 sacas/ha")),
                                new Property.Rural.Livestock("Corte — recria", 680, 6, Set.of("Curral", "Balança")),
                                Set.of("Rio", "Poço artesiano"),
                                List.of(new Property.Rural.Improvement("Casa sede", 1, 220d, "Bom"),
                                        new Property.Rural.Improvement("Armazém", 1, 1800d, "Bom"),
                                        new Property.Rural.Improvement("Curral", 1, 300d, "Bom")),
                                new Property.Rural.Access("Asfalto", 6.0, 15, "Formosa", true, true, true, true),
                                new Property.Rural.Documentation(true, true, true, true, true, "Georreferenciada"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 5, 28)))
                        .fotos("Fazenda em Formosa", foto("1720334540915-335eaf0eb1d2"), foto("1715798637010-8a4f27a0950f"))
                        .corretor("juliana-prado", "Juliana Prado")
                        .datas(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 10))
                        .build(),

                PropertyBuilder.novo("RUR-000215", "fazenda-pecuaria-luziania-go")
                        .tipo("Fazenda").finalidade(Purpose.VENDA)
                        .titulo("Fazenda de pecuária de corte de 720 hectares em Luziânia")
                        .chamada("Pastagem formada e dividida, a 45 km de Brasília, ideal para recria e engorda.")
                        .descricao("Propriedade voltada à pecuária, com cercas divisórias, curral e nascente "
                                + "própria — logística facilitada pela proximidade com o Distrito Federal.")
                        .valores(Property.Money.venda(38_000_000, 0, 0))
                        .areas(Property.Areas.hectares(720))
                        .ambientes(3, 1, 3, 5)
                        .endereco(rural("GO", "Luziânia", "entorno-df", -16.2528, -47.9500))
                        .caracteristicas("Curral", "Divisão de pasto", "Energia trifásica")
                        .rural(new Property.Rural(
                                620, 620, 0, 100, 144.0, 0d,
                                Set.of("Ondulado"),
                                "Cambissolo", "Média",
                                Set.of("Pecuária de corte"),
                                List.of(),
                                new Property.Rural.Livestock("Corte — recria e engorda", 900, 14, Set.of("Curral", "Balança", "Cocho")),
                                Set.of("Rio", "Nascente"),
                                List.of(new Property.Rural.Improvement("Casa sede", 1, 180d, "Regular"),
                                        new Property.Rural.Improvement("Curral", 1, 260d, "Bom"),
                                        new Property.Rural.Improvement("Cercas divisórias", 14, null, "Bom")),
                                new Property.Rural.Access("Estrada de terra", 4.0, 45, "Luziânia", true, true, false, true),
                                new Property.Rural.Documentation(true, false, false, true, false,
                                        "CAR ativo; demais documentos em regularização"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 4, 25)))
                        .fotos("Fazenda de pecuária em Luziânia", foto("1715798637010-8a4f27a0950f"), foto("1573611236714-1fe596de1fd3"))
                        .corretor("rafael-ortiz", "Rafael Ortiz")
                        .datas(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 6, 18))
                        .build(),

                PropertyBuilder.novo("RUR-000216", "fazenda-soja-milho-catalao-go")
                        .tipo("Fazenda").finalidade(Purpose.VENDA)
                        .titulo("Fazenda de soja e milho de 980 hectares em Catalão")
                        .chamada("Lavoura mecanizada em região consolidada de agronegócio no sudeste goiano.")
                        .descricao("Área plana, solo corrigido, 100% mecanizável, com histórico de produtividade "
                                + "acima da média regional em soja e milho safrinha.")
                        .valores(Property.Money.venda(88_000_000, 0, 0))
                        .areas(Property.Areas.hectares(980))
                        .ambientes(4, 2, 4, 8)
                        .endereco(rural("GO", "Catalão", "goias-sudeste", -18.1662, -47.9457))
                        .caracteristicas("Energia trifásica", "Solo corrigido")
                        .rural(new Property.Rural(
                                780, 0, 780, 200, 196.0, 0d,
                                Set.of("Plano"),
                                "Latossolo vermelho-escuro", "Alta",
                                Set.of("Agricultura"),
                                List.of(new Property.Rural.Culture("Soja", "Cultura anual", 780, "2025/2026", "66 sacas/ha"),
                                        new Property.Rural.Culture("Milho safrinha", "Cultura anual", 700, "2025/2026", "158 sacas/ha")),
                                null,
                                Set.of("Rio", "Poço artesiano"),
                                List.of(new Property.Rural.Improvement("Galpão", 1, 600d, "Bom"),
                                        new Property.Rural.Improvement("Casa sede", 1, 200d, "Bom")),
                                new Property.Rural.Access("Asfalto", 7.0, 20, "Catalão", true, true, true, true),
                                new Property.Rural.Documentation(true, true, true, true, true,
                                        "Georreferenciada e certificada"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 7, 3)))
                        .fotos("Fazenda em Catalão", foto("1719253860282-cd3f93da6548"), foto("1720334540915-335eaf0eb1d2"))
                        .corretor("eduardo-salles", "Eduardo Salles")
                        .datas(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 28))
                        .build(),

                PropertyBuilder.novo("RUR-000217", "fazenda-algodao-pecuaria-joao-pinheiro-mg")
                        .tipo("Fazenda").finalidade(Purpose.VENDA)
                        .titulo("Fazenda de algodão e pecuária de 1.400 hectares em João Pinheiro")
                        .chamada("Lavoura de algodão irrigado e área de pastagem, com armazém próprio no noroeste de Minas.")
                        .descricao("Propriedade de grande porte com pivôs centrais para algodão e soja, "
                                + "armazém próprio e área de pecuária de corte para diversificação.")
                        .valores(Property.Money.venda(105_000_000, 0, 0))
                        .areas(Property.Areas.hectares(1400))
                        .ambientes(5, 3, 6, 10)
                        .endereco(rural("MG", "João Pinheiro", "noroeste-mineiro", -17.7433, -46.1750))
                        .caracteristicas("Pivô central", "Armazém próprio", "Energia trifásica")
                        .rural(new Property.Rural(
                                1120, 500, 620, 280, 280.0, 240.0,
                                Set.of("Plano", "Levemente ondulado"),
                                "Latossolo vermelho-amarelo", "Alta",
                                Set.of("Agricultura", "Pecuária de corte"),
                                List.of(new Property.Rural.Culture("Algodão", "Cultura anual", 380, "2025/2026", "280 @/ha"),
                                        new Property.Rural.Culture("Soja", "Cultura anual", 240, "2025/2026", "64 sacas/ha")),
                                new Property.Rural.Livestock("Corte", 600, 8, Set.of("Curral", "Balança")),
                                Set.of("Rio", "Poço artesiano", "Irrigação"),
                                List.of(new Property.Rural.Improvement("Casa sede", 1, 300d, "Ótimo"),
                                        new Property.Rural.Improvement("Armazém", 1, 2200d, "Bom"),
                                        new Property.Rural.Improvement("Galpão de máquinas", 1, 700d, "Bom"),
                                        new Property.Rural.Improvement("Curral", 1, 320d, "Bom")),
                                new Property.Rural.Access("Asfalto", 10.0, 18, "João Pinheiro", true, true, true, true),
                                new Property.Rural.Documentation(true, true, true, true, true,
                                        "Georreferenciada, matrícula única"),
                                "Declarado pelo proprietário", LocalDate.of(2026, 6, 20)))
                        .fotos("Fazenda em João Pinheiro", foto("1761069183787-0272d2739ae6"), foto("1761189464066-4975b3eaeb0b"))
                        .corretor("juliana-prado", "Juliana Prado")
                        .destaque()
                        .datas(LocalDate.of(2026, 6, 25), LocalDate.of(2026, 7, 30))
                        .build()
        );
    }

    /** Categorias usadas na home para separar destaques urbanos e rurais (seção 6). */
    static Category[] categorias() {
        return Category.values();
    }
}
