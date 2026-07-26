package com.jodak.admin.dataimport;

import com.jodak.admin.exceptions.ImportValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Stocke un fichier importé hors du dépôt et hors web root, et calcule son empreinte SHA-256
 * (idempotence). La lecture se fait en flux (pas de chargement complet en mémoire).
 */
@Component
@RequiredArgsConstructor
public class ImportFileStorage {

    private final ImportProperties properties;

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportValidationException("Le fichier est vide.");
        }
        try {
            Path directory = Path.of(properties.storageDir());
            Files.createDirectories(directory);
            String safeName = UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
            Path target = directory.resolve(safeName);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream();
                 DigestInputStream digestStream = new DigestInputStream(in, digest);
                 OutputStream out = Files.newOutputStream(target)) {
                digestStream.transferTo(out);
            }
            String hash = HexFormat.of().formatHex(digest.digest());
            return new StoredFile(target, Files.size(target), hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Échec du stockage du fichier importé.", ex);
        }
    }

    private String sanitize(String original) {
        if (original == null || original.isBlank()) {
            return "import";
        }
        return Path.of(original).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public record StoredFile(Path path, long size, String hash) {
    }
}
