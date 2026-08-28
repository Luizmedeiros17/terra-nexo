package br.com.terranexo.owner.application;

import br.com.terranexo.owner.api.dto.CreateOwnerRequest;
import br.com.terranexo.owner.domain.Owner;
import br.com.terranexo.owner.domain.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Cadastro (CRUD) de proprietários de imóveis. */
@Service
public class OwnerService {

    private final OwnerRepository repository;
    private final Clock clock;

    @Autowired
    public OwnerService(OwnerRepository repository) {
        this(repository, Clock.systemDefaultZone());
    }

    OwnerService(OwnerRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public List<Owner> listar(String cidade, String q) {
        return repository.findAll().stream()
                .filter(o -> cidade == null || cidade.isBlank()
                        || normalizar(o.endereco().municipio()).equals(normalizar(cidade)))
                .filter(o -> q == null || q.isBlank() || normalizar(
                        o.nome() + " " + Optional.ofNullable(o.documento()).orElse("")
                                + " " + Optional.ofNullable(o.email()).orElse(""))
                        .contains(normalizar(q)))
                .sorted(Comparator.comparing(Owner::nome))
                .toList();
    }

    public Optional<Owner> buscarPorId(String id) {
        return repository.findById(id);
    }

    public Owner criar(CreateOwnerRequest req) {
        String id = repository.proximoId();
        OffsetDateTime agora = OffsetDateTime.now(clock);
        Owner novo = new Owner(id, tipoPessoa(req.tipoPessoa()), req.nome(), req.documento(), req.email(),
                req.telefone(), req.telefoneAlternativo(), enderecoDe(req),
                req.imoveisVinculados() == null ? List.of() : req.imoveisVinculados(),
                req.observacoes(), agora, agora);
        return repository.save(novo);
    }

    public Owner atualizar(String id, CreateOwnerRequest req) {
        Owner existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proprietário não encontrado: " + id));
        Owner atualizado = existente.comAtualizacao(tipoPessoa(req.tipoPessoa()), req.nome(), req.documento(),
                req.email(), req.telefone(), req.telefoneAlternativo(), enderecoDe(req),
                req.imoveisVinculados() == null ? List.of() : req.imoveisVinculados(),
                req.observacoes(), OffsetDateTime.now(clock));
        return repository.save(atualizado);
    }

    public void excluir(String id) {
        if (repository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("Proprietário não encontrado: " + id);
        }
        repository.deleteById(id);
    }

    private Owner.Address enderecoDe(CreateOwnerRequest req) {
        return new Owner.Address(req.municipio(), req.uf().toUpperCase(), req.logradouro(), req.numero(),
                req.complemento(), req.bairro(), req.cep());
    }

    private Owner.TipoPessoa tipoPessoa(String valor) {
        try {
            return Owner.TipoPessoa.valueOf(valor.toUpperCase());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Tipo de pessoa inválido: " + valor);
        }
    }

    private String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }
}
