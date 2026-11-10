package br.com.terranexo.shared.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Base dos repositórios em memória do protótipo. Concentra o CRUD por id e a
 * geração de identificadores sequenciais. Reinicia com o container; ao adotar
 * PostgreSQL, cada repositório é trocado por um adaptador JPA sem afetar as
 * camadas {@code application} e {@code api}.
 */
public abstract class InMemoryStore<T> {

    protected final List<T> itens;
    private final Function<T, String> idDe;
    private final boolean novosNoInicio;

    protected InMemoryStore(Collection<T> seed, Function<T, String> idDe) {
        this(seed, idDe, false);
    }

    /** @param novosNoInicio quando {@code true}, {@link #save} insere na primeira posição. */
    protected InMemoryStore(Collection<T> seed, Function<T, String> idDe, boolean novosNoInicio) {
        this.itens = new CopyOnWriteArrayList<>(seed);
        this.idDe = idDe;
        this.novosNoInicio = novosNoInicio;
    }

    public List<T> findAll() {
        return List.copyOf(itens);
    }

    public Optional<T> findById(String id) {
        return itens.stream().filter(i -> idDe.apply(i).equals(id)).findFirst();
    }

    public T save(T item) {
        String id = idDe.apply(item);
        itens.removeIf(i -> idDe.apply(i).equals(id));
        if (novosNoInicio) {
            itens.add(0, item);
        } else {
            itens.add(item);
        }
        return item;
    }

    public void deleteById(String id) {
        itens.removeIf(i -> idDe.apply(i).equals(id));
    }

    /** Próximo id no formato {@code prefixo-NNN} (3 dígitos), a partir do maior existente. */
    protected String proximoIdSequencial(String prefixo) {
        return proximoSequencial(itens.stream().map(idDe), prefixo, 3, 0);
    }

    /**
     * Próximo identificador {@code prefixo-<numero>} com {@code digitos} dígitos.
     * Valores com sufixo não numérico são ignorados; sem nenhum existente, parte de {@code inicial}.
     */
    protected static String proximoSequencial(Stream<String> existentes, String prefixo, int digitos, int inicial) {
        String comHifen = prefixo + "-";
        int maior = existentes
                .filter(v -> v.startsWith(comHifen))
                .mapToInt(v -> {
                    try {
                        return Integer.parseInt(v.substring(comHifen.length()));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(inicial);
        return ("%s-%0" + digitos + "d").formatted(prefixo, maior + 1);
    }
}
