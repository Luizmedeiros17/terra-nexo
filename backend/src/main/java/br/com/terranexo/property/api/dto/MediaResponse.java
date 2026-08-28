package br.com.terranexo.property.api.dto;

/** Um item da galeria de fotos/vídeos de um imóvel, no ambiente administrativo. */
public record MediaResponse(String url, String textoAlternativo, int ordem, boolean capa, String tipo) {
}
