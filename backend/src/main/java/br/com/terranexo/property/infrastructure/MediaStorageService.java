package br.com.terranexo.property.infrastructure;

import br.com.terranexo.property.domain.Property;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Armazenamento dos arquivos de fotos e vídeos enviados pelo painel administrativo.
 *
 * <p>Assim como as demais persistências do protótipo, os arquivos ficam em disco
 * local do container ({@code /tmp}, gravável sem privilégios extras) e são
 * perdidos ao recriar o container — mesma regra do restante dos dados (ver
 * {@link br.com.terranexo.property.infrastructure.InMemoryPropertyRepository}).
 * Servidos de volta via {@code /api/v1/midias/**} (ver WebConfig).</p>
 */
@Service
public class MediaStorageService {

    private static final Map<String, String> EXTENSAO_POR_TIPO = Map.ofEntries(
            Map.entry("image/jpeg", ".jpg"),
            Map.entry("image/png", ".png"),
            Map.entry("image/webp", ".webp"),
            Map.entry("image/gif", ".gif"),
            Map.entry("video/mp4", ".mp4"),
            Map.entry("video/webm", ".webm"),
            Map.entry("video/quicktime", ".mov"));

    private static final Set<String> CONTENT_TYPES_FOTO = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> CONTENT_TYPES_VIDEO = Set.of(
            "video/mp4", "video/webm", "video/quicktime");

    private static final long MAX_FOTO_BYTES = 12L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 80L * 1024 * 1024;

    private final Path diretorio;

    public MediaStorageService(@Value("${terranexo.midias.diretorio:/tmp/terranexo-midias}") String diretorio) {
        this.diretorio = Path.of(diretorio);
        try {
            Files.createDirectories(this.diretorio);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar o diretório de mídias: " + diretorio, e);
        }
    }

    /** Deduz o tipo (foto/vídeo) a partir do content-type enviado pelo navegador. */
    public Property.MediaType inferirTipo(String contentType) {
        String ct = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (CONTENT_TYPES_FOTO.contains(ct)) {
            return Property.MediaType.FOTO;
        }
        if (CONTENT_TYPES_VIDEO.contains(ct)) {
            return Property.MediaType.VIDEO;
        }
        throw new IllegalArgumentException(
                "Tipo de arquivo não suportado: " + contentType
                        + ". Envie uma imagem (JPEG/PNG/WEBP/GIF) ou um vídeo (MP4/WEBM/MOV).");
    }

    /** Valida, grava em disco e devolve a URL pública do arquivo salvo. */
    public String salvar(MultipartFile arquivo, Property.MediaType tipo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie um arquivo.");
        }
        long limite = tipo == Property.MediaType.VIDEO ? MAX_VIDEO_BYTES : MAX_FOTO_BYTES;
        if (arquivo.getSize() > limite) {
            throw new IllegalArgumentException(
                    "Arquivo muito grande — o limite é %dMB para %s.".formatted(
                            limite / (1024 * 1024), tipo == Property.MediaType.VIDEO ? "vídeos" : "fotos"));
        }

        String extensao = EXTENSAO_POR_TIPO.getOrDefault(
                arquivo.getContentType() == null ? "" : arquivo.getContentType().toLowerCase(Locale.ROOT), "");
        String nomeArquivo = UUID.randomUUID() + extensao;

        try {
            arquivo.transferTo(diretorio.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar o arquivo de mídia", e);
        }
        return "/api/v1/midias/" + nomeArquivo;
    }

    /** Remove o arquivo do disco. Não faz nada se a URL não corresponder a um arquivo local conhecido. */
    public void excluir(String url) {
        if (url == null || !url.startsWith("/api/v1/midias/")) {
            return;
        }
        String nomeArquivo = url.substring("/api/v1/midias/".length());
        if (nomeArquivo.contains("/") || nomeArquivo.contains("\\") || nomeArquivo.contains("..")) {
            return;
        }
        try {
            Files.deleteIfExists(diretorio.resolve(nomeArquivo));
        } catch (IOException ignorado) {
            // Arquivo já ausente ou inacessível — não impede a remoção do registro.
        }
    }
}
