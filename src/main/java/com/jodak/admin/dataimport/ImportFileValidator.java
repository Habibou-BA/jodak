package com.jodak.admin.dataimport;

import com.jodak.admin.enums.ImportFormat;
import com.jodak.admin.exceptions.ImportValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/**
 * Validation d'un fichier téléversé avant stockage : présence, taille bornée et cohérence de
 * l'extension avec le format déclaré (défense en profondeur, en complément des limites multipart
 * et du durcissement POI ; cf. {@link PoiSecurityConfig}).
 */
@Component
@RequiredArgsConstructor
public class ImportFileValidator {

    private final ImportProperties properties;

    public void validate(MultipartFile file, ImportFormat format) {
        if (file == null || file.isEmpty()) {
            throw new ImportValidationException("Le fichier est vide.");
        }
        long max = properties.maxFileSizeBytes();
        if (max > 0 && file.getSize() > max) {
            throw new ImportValidationException(
                    "Le fichier dépasse la taille maximale autorisée (%d Mio).".formatted(max / (1024 * 1024)));
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String expected = switch (format) {
            case CSV -> ".csv";
            case XLSX -> ".xlsx";
        };
        if (!name.endsWith(expected)) {
            throw new ImportValidationException(
                    "L'extension du fichier ne correspond pas au format %s (attendu « %s »)."
                            .formatted(format, expected));
        }
    }
}
