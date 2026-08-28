package br.com.terranexo.property.application;

import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import br.com.terranexo.property.infrastructure.MediaStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Upload, remoção, reordenação e escolha de capa da galeria de fotos/vídeos de um imóvel. */
@Service
public class PropertyMediaService {

    private final PropertyRepository repository;
    private final MediaStorageService storage;

    public PropertyMediaService(PropertyRepository repository, MediaStorageService storage) {
        this.repository = repository;
        this.storage = storage;
    }

    public Property adicionar(String propertyId, MultipartFile arquivo, String textoAlternativo, String tipoInformado) {
        Property imovel = buscar(propertyId);

        Property.MediaType tipo = tipoInformado == null || tipoInformado.isBlank()
                ? storage.inferirTipo(arquivo.getContentType())
                : Property.MediaType.valueOf(tipoInformado.toUpperCase());
        String url = storage.salvar(arquivo, tipo);

        List<Property.Media> atuais = new ArrayList<>(imovel.midias());
        int ordem = atuais.size();
        boolean semCapaAinda = atuais.stream()
                .noneMatch(m -> m.tipo() == Property.MediaType.FOTO && m.capa());
        boolean capa = tipo == Property.MediaType.FOTO && semCapaAinda;
        String alt = textoAlternativo == null || textoAlternativo.isBlank() ? imovel.titulo() : textoAlternativo;

        atuais.add(new Property.Media(url, alt, ordem, capa, tipo));
        return salvar(imovel, atuais);
    }

    public Property remover(String propertyId, int ordem) {
        Property imovel = buscar(propertyId);
        List<Property.Media> atuais = new ArrayList<>(imovel.midias());
        atuais.sort(Comparator.comparingInt(Property.Media::ordem));

        Property.Media alvo = atuais.stream().filter(m -> m.ordem() == ordem).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mídia não encontrada: ordem " + ordem));
        atuais.remove(alvo);
        storage.excluir(alvo.url());

        return salvar(imovel, renumerar(atuais));
    }

    public Property definirCapa(String propertyId, int ordem) {
        Property imovel = buscar(propertyId);
        List<Property.Media> atuais = imovel.midias();

        Property.Media alvo = atuais.stream().filter(m -> m.ordem() == ordem).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mídia não encontrada: ordem " + ordem));
        if (alvo.tipo() != Property.MediaType.FOTO) {
            throw new IllegalArgumentException("Somente uma fotografia pode ser a capa do imóvel.");
        }

        List<Property.Media> atualizadas = atuais.stream()
                .map(m -> new Property.Media(m.url(), m.textoAlternativo(), m.ordem(),
                        m.ordem() == ordem, m.tipo()))
                .toList();
        return salvar(imovel, atualizadas);
    }

    /** Troca a posição da mídia com a vizinha imediatamente anterior ou seguinte. */
    public Property mover(String propertyId, int ordem, boolean paraCima) {
        Property imovel = buscar(propertyId);
        List<Property.Media> atuais = new ArrayList<>(imovel.midias());
        atuais.sort(Comparator.comparingInt(Property.Media::ordem));

        int indice = -1;
        for (int i = 0; i < atuais.size(); i++) {
            if (atuais.get(i).ordem() == ordem) {
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            throw new IllegalArgumentException("Mídia não encontrada: ordem " + ordem);
        }
        int alvo = paraCima ? indice - 1 : indice + 1;
        if (alvo < 0 || alvo >= atuais.size()) {
            return imovel; // já está na ponta — nada a fazer
        }
        java.util.Collections.swap(atuais, indice, alvo);

        return salvar(imovel, renumerar(atuais));
    }

    /** Renumera a partir da ORDEM ATUAL DA LISTA (posição) — não reordena por {@code ordem()},
     * para não desfazer uma troca de posição feita por {@link #mover}. */
    private List<Property.Media> renumerar(List<Property.Media> lista) {
        boolean temCapa = lista.stream().anyMatch(Property.Media::capa);
        List<Property.Media> renumeradas = new ArrayList<>();
        for (int i = 0; i < lista.size(); i++) {
            Property.Media m = lista.get(i);
            boolean capa = m.capa() || (!temCapa && m.tipo() == Property.MediaType.FOTO
                    && renumeradas.stream().noneMatch(Property.Media::capa));
            renumeradas.add(new Property.Media(m.url(), m.textoAlternativo(), i, capa, m.tipo()));
        }
        return renumeradas;
    }

    private Property salvar(Property imovel, List<Property.Media> midias) {
        Property atualizado = imovel.comMidias(List.copyOf(midias));
        return repository.save(atualizado);
    }

    private Property buscar(String propertyId) {
        return repository.findById(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + propertyId));
    }
}
