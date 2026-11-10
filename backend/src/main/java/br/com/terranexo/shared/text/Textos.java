package br.com.terranexo.shared.text;

import java.text.Normalizer;
import java.util.regex.Pattern;

/** Utilitários de texto compartilhados entre os módulos. */
public final class Textos {

    private static final Pattern DIACRITICOS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private Textos() {
    }

    /** Remove acentos preservando a caixa original. {@code null} vira {@code ""}. */
    public static String semAcento(String valor) {
        if (valor == null) {
            return "";
        }
        return DIACRITICOS.matcher(Normalizer.normalize(valor, Normalizer.Form.NFD)).replaceAll("");
    }

    /** Minúsculas, sem acento e sem espaços nas pontas — usado em buscas e comparações. */
    public static String normalizar(String valor) {
        return semAcento(valor).toLowerCase().trim();
    }
}
