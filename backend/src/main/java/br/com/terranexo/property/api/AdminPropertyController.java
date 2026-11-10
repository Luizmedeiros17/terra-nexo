package br.com.terranexo.property.api;

import br.com.terranexo.property.api.dto.CreatePropertyRequest;
import br.com.terranexo.property.api.dto.DashboardResponse;
import br.com.terranexo.property.api.dto.PropertyCardResponse;
import br.com.terranexo.property.application.PropertyAdminService;
import br.com.terranexo.property.domain.Property;
import br.com.terranexo.property.domain.PropertyRepository;
import br.com.terranexo.property.domain.PropertyStatus;
import br.com.terranexo.shared.text.Textos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * Endpoints do ambiente administrativo (seções 18 e 23).
 *
 * <p>No MVP completo estas rotas exigem autenticação e perfil (seção 21). No
 * protótipo elas ficam abertas para permitir a demonstração das telas; a camada
 * de segurança entra na etapa 1 do plano de implementação.</p>
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administração", description = "Painel e cadastro de imóveis")
public class AdminPropertyController {

    private final PropertyAdminService service;
    private final PropertyRepository repository;
    private final PropertyMapper mapper;

    public AdminPropertyController(PropertyAdminService service, PropertyRepository repository,
                                   PropertyMapper mapper) {
        this.service = service;
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Indicadores, alertas e últimos cadastros do painel")
    public DashboardResponse dashboard() {
        return new DashboardResponse(
                service.indicadoresDeImoveis(),
                service.indicadoresDeLeads(),
                service.porSituacao(),
                service.porCorretor(),
                service.porMunicipio(),
                service.ultimosCadastros(6).stream().map(mapper::toCard).toList(),
                service.alertas());
    }

    @GetMapping("/properties")
    @Operation(summary = "Listar imóveis com os filtros administrativos (seção 18)")
    public List<PropertyCardResponse> listar(
            @RequestParam(required = false) String situacao,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String corretor,
            @RequestParam(required = false) String municipio,
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "30") int limite) {

        return repository.findAll().stream()
                .filter(p -> situacao == null || p.situacao().name().equalsIgnoreCase(situacao))
                .filter(p -> categoria == null || p.categoria().name().equalsIgnoreCase(categoria))
                .filter(p -> corretor == null || p.corretorSlug().equalsIgnoreCase(corretor))
                .filter(p -> municipio == null || Textos.normalizar(p.endereco().municipio()).equals(Textos.normalizar(municipio)))
                .filter(p -> q == null || q.isBlank()
                        || Textos.normalizar(p.titulo() + " " + p.codigo()).contains(Textos.normalizar(q)))
                .sorted(Comparator.comparing(Property::atualizadoEm).reversed())
                .limit(Math.max(1, limite))
                .map(mapper::toCard)
                .toList();
    }

    @PostMapping("/properties")
    @Operation(summary = "Cadastrar imóvel urbano ou rural")
    public ResponseEntity<PropertyCardResponse> cadastrar(@Valid @RequestBody CreatePropertyRequest req) {
        Property criado = service.cadastrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCard(criado));
    }

    @GetMapping("/properties/{id}")
    @Operation(summary = "Dados brutos de um imóvel, no formato do formulário de cadastro/edição")
    public CreatePropertyRequest detalhe(@PathVariable String id) {
        Property imovel = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel não encontrado: " + id));
        return mapper.toEditRequest(imovel);
    }

    @PutMapping("/properties/{id}")
    @Operation(summary = "Atualizar um imóvel existente")
    public PropertyCardResponse atualizar(@PathVariable String id, @Valid @RequestBody CreatePropertyRequest req) {
        Property atualizado = service.atualizar(id, req);
        return mapper.toCard(atualizado);
    }

    @DeleteMapping("/properties/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um imóvel")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }

    @GetMapping("/situacoes")
    @Operation(summary = "Situações administrativas disponíveis")
    public List<Opcao> situacoes() {
        return java.util.Arrays.stream(PropertyStatus.values())
                .map(s -> new Opcao(s.name(), s.rotulo()))
                .toList();
    }

    public record Opcao(String valor, String rotulo) {
    }
}
