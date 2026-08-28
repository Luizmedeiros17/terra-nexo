package br.com.terranexo.property.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Construtor fluente de {@link Property}. O record tem muitos componentes;
 * o builder mantém a carga inicial e os testes legíveis.
 */
public class PropertyBuilder {

    private String id;
    private String codigo;
    private String slug;
    private Category categoria = Category.URBANO;
    private String tipo;
    private Purpose finalidade = Purpose.VENDA;
    private PropertyStatus situacao = PropertyStatus.PUBLICADO;
    private String titulo;
    private String chamada = "";
    private String descricao = "";
    private Property.Money valores;
    private Property.Areas areas;
    private Property.Rooms ambientes = Property.Rooms.nenhum();
    private Property.Address endereco;
    private final Set<String> caracteristicas = new LinkedHashSet<>();
    private final List<Property.Media> midias = new ArrayList<>();
    private Property.Urban urbano;
    private Property.Rural rural;
    private String corretorSlug = "marina-alencar";
    private String corretorNome = "Marina Alencar";
    private boolean destaque;
    private boolean exclusivo;
    private boolean lancamento;
    private boolean oportunidade;
    private boolean aceitaPermuta;
    private boolean aceitaFinanciamento = true;
    private LocalDate publicadoEm = LocalDate.now();
    private LocalDate atualizadoEm = LocalDate.now();

    public static PropertyBuilder novo(String codigo, String slug) {
        PropertyBuilder b = new PropertyBuilder();
        b.codigo = codigo;
        b.slug = slug;
        b.id = java.util.UUID.nameUUIDFromBytes(codigo.getBytes()).toString();
        return b;
    }

    public PropertyBuilder categoria(Category c) {
        this.categoria = c;
        return this;
    }

    public PropertyBuilder tipo(String t) {
        this.tipo = t;
        return this;
    }

    public PropertyBuilder finalidade(Purpose f) {
        this.finalidade = f;
        return this;
    }

    public PropertyBuilder situacao(PropertyStatus s) {
        this.situacao = s;
        return this;
    }

    public PropertyBuilder titulo(String t) {
        this.titulo = t;
        return this;
    }

    public PropertyBuilder chamada(String c) {
        this.chamada = c;
        return this;
    }

    public PropertyBuilder descricao(String d) {
        this.descricao = d;
        return this;
    }

    public PropertyBuilder valores(Property.Money m) {
        this.valores = m;
        return this;
    }

    public PropertyBuilder areas(Property.Areas a) {
        this.areas = a;
        return this;
    }

    public PropertyBuilder ambientes(int dormitorios, int suites, int banheiros, int vagas) {
        this.ambientes = new Property.Rooms(dormitorios, suites, banheiros, vagas);
        return this;
    }

    public PropertyBuilder endereco(Property.Address e) {
        this.endereco = e;
        return this;
    }

    public PropertyBuilder caracteristicas(String... valores) {
        this.caracteristicas.addAll(List.of(valores));
        return this;
    }

    public PropertyBuilder fotos(String alt, String... urls) {
        for (int i = 0; i < urls.length; i++) {
            midias.add(new Property.Media(urls[i], alt + " — foto " + (i + 1), i, i == 0, Property.MediaType.FOTO));
        }
        return this;
    }

    /** Substitui a galeria inteira — usado para preservar mídias já enviadas ao atualizar o cadastro. */
    public PropertyBuilder midias(List<Property.Media> lista) {
        this.midias.clear();
        this.midias.addAll(lista);
        return this;
    }

    public PropertyBuilder urbano(Property.Urban u) {
        this.urbano = u;
        return this;
    }

    public PropertyBuilder rural(Property.Rural r) {
        this.rural = r;
        this.categoria = Category.RURAL;
        return this;
    }

    public PropertyBuilder corretor(String slug, String nome) {
        this.corretorSlug = slug;
        this.corretorNome = nome;
        return this;
    }

    public PropertyBuilder destaque() {
        this.destaque = true;
        return this;
    }

    public PropertyBuilder exclusivo() {
        this.exclusivo = true;
        return this;
    }

    public PropertyBuilder lancamento() {
        this.lancamento = true;
        return this;
    }

    public PropertyBuilder oportunidade() {
        this.oportunidade = true;
        return this;
    }

    public PropertyBuilder permuta() {
        this.aceitaPermuta = true;
        return this;
    }

    public PropertyBuilder semFinanciamento() {
        this.aceitaFinanciamento = false;
        return this;
    }

    public PropertyBuilder datas(LocalDate publicado, LocalDate atualizado) {
        this.publicadoEm = publicado;
        this.atualizadoEm = atualizado;
        return this;
    }

    public Property build() {
        return new Property(id, codigo, slug, categoria, tipo, finalidade, situacao,
                titulo, chamada, descricao, valores, areas, ambientes, endereco,
                Set.copyOf(caracteristicas), List.copyOf(midias), urbano, rural,
                corretorSlug, corretorNome, destaque, exclusivo, lancamento, oportunidade,
                aceitaPermuta, aceitaFinanciamento, publicadoEm, atualizadoEm);
    }
}
