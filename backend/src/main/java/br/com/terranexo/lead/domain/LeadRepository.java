package br.com.terranexo.lead.domain;

import java.util.List;
import java.util.Optional;

/** Porta de acesso aos leads captados pelo portal e cadastrados manualmente. */
public interface LeadRepository {

    List<Lead> findAll();

    Optional<Lead> findById(String id);

    boolean existsByEmailOrTelefoneAndImovel(String email, String telefone, String imovelCodigo);

    /** Insere ou substitui um lead, devolvendo a versão persistida. */
    Lead save(Lead lead);

    /** Remove o lead. Não faz nada se o id não existir. */
    void deleteById(String id);

    /** Próximo número sequencial do protocolo (TN-2026-0001, TN-2026-0002…). */
    int proximaSequencia();
}
