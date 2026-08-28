package br.com.terranexo.property.api;

import br.com.terranexo.property.api.dto.MediaResponse;
import br.com.terranexo.property.application.PropertyMediaService;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

/**
 * Upload e gerenciamento da galeria de fotos e vídeos de um imóvel — processo
 * separado do formulário de cadastro (o imóvel precisa já existir), conforme a
 * seção 12. No protótipo estas rotas ficam abertas, como as demais de
 * {@code /admin}; a autenticação por perfil entra na etapa 1 do plano de
 * implementação.
 */
@RestController
@RequestMapping("/api/v1/admin/properties/{id}/midias")
@Tag(name = "Mídia dos imóveis", description = "Upload e gerenciamento de fotos e vídeos")
public class AdminPropertyMediaController {

    private final PropertyMediaService service;
    private final PropertyRepository repository;

    public AdminPropertyMediaController(PropertyMediaService service, PropertyRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "Listar a galeria de fotos e vídeos de um imóvel")
    public List<MediaResponse> listar(@PathVariable String id) {
        Property imovel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + id));
        return paraResposta(imovel);
    }

    @PostMapping
    @Operation(summary = "Enviar uma foto ou vídeo para a galeria do imóvel")
    public ResponseEntity<List<MediaResponse>> enviar(
            @PathVariable String id,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(required = false) String textoAlternativo,
            @RequestParam(required = false) String tipo) {
        Property atualizado = service.adicionar(id, arquivo, textoAlternativo, tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResposta(atualizado));
    }

    @DeleteMapping("/{ordem}")
    @Operation(summary = "Remover uma foto ou vídeo da galeria")
    public List<MediaResponse> remover(@PathVariable String id, @PathVariable int ordem) {
        return paraResposta(service.remover(id, ordem));
    }

    @PatchMapping("/{ordem}/capa")
    @Operation(summary = "Definir uma fotografia como capa do imóvel")
    public List<MediaResponse> definirCapa(@PathVariable String id, @PathVariable int ordem) {
        return paraResposta(service.definirCapa(id, ordem));
    }

    @PatchMapping("/{ordem}/mover")
    @Operation(summary = "Mover uma mídia uma posição para cima ou para baixo na galeria")
    public List<MediaResponse> mover(@PathVariable String id, @PathVariable int ordem,
                                      @RequestParam String direcao) {
        boolean paraCima = "cima".equalsIgnoreCase(direcao);
        return paraResposta(service.mover(id, ordem, paraCima));
    }

    private List<MediaResponse> paraResposta(Property imovel) {
        return imovel.midias().stream()
                .sorted(Comparator.comparingInt(Property.Media::ordem))
                .map(m -> new MediaResponse(m.url(), m.textoAlternativo(), m.ordem(), m.capa(), m.tipo().name()))
                .toList();
    }
}
