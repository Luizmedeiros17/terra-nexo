package br.com.terranexo.broker.domain;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Corretor da equipe Terra Nexo (seções 6 e 16). Exibido publicamente quando
 * {@code ativo}; o cadastro completo (CRUD) é feito no ambiente administrativo.
 */
public record Broker(
        String id,
        String slug,
        String nome,
        String cargo,
        String creci,
        String foto,
        String bio,
        List<String> especialidades,
        List<String> regioes,
        String email,
        String telefone,
        boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    public Broker comAtualizacao(String nome, String cargo, String creci, String foto, String bio,
                                  List<String> especialidades, List<String> regioes, String email,
                                  String telefone, boolean ativo, OffsetDateTime agora) {
        return new Broker(id, slug, nome, cargo, creci, foto, bio, especialidades, regioes,
                email, telefone, ativo, criadoEm, agora);
    }
}
