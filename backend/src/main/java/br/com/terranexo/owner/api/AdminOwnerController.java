package br.com.terranexo.owner.api;

import br.com.terranexo.owner.api.dto.CreateOwnerRequest;
import br.com.terranexo.owner.api.dto.OwnerCardResponse;
import br.com.terranexo.owner.api.dto.OwnerDetailResponse;
import br.com.terranexo.owner.application.OwnerService;
import br.com.terranexo.owner.domain.Owner;
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

import java.util.List;

/**
 * CRUD de proprietários de imóveis — cadastro de uso interno da equipe
 * comercial. No protótipo estas rotas ficam abertas, como as demais de
 * {@code /admin}; a autenticação por perfil entra na etapa 1 do plano de
 * implementação.
 */
@RestController
@RequestMapping("/api/v1/admin/owners")
@Tag(name = "Proprietários", description = "Cadastro de proprietários de imóveis")
public class AdminOwnerController {

    private final OwnerService service;
    private final OwnerMapper mapper;

    public AdminOwnerController(OwnerService service, OwnerMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @Operation(summary = "Listar proprietários com filtro por cidade e busca livre")
    public List<OwnerCardResponse> listar(
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String q) {
        return service.listar(cidade, q).stream().map(mapper::toCard).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe de um proprietário")
    public OwnerDetailResponse detalhe(@PathVariable String id) {
        Owner owner = service.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Proprietário não encontrado: " + id));
        return mapper.toDetail(owner);
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo proprietário")
    public ResponseEntity<OwnerDetailResponse> criar(@Valid @RequestBody CreateOwnerRequest req) {
        Owner criado = service.criar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDetail(criado));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados de um proprietário")
    public OwnerDetailResponse atualizar(@PathVariable String id, @Valid @RequestBody CreateOwnerRequest req) {
        Owner atualizado = service.atualizar(id, req);
        return mapper.toDetail(atualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir um proprietário")
    public void excluir(@PathVariable String id) {
        service.excluir(id);
    }
}
