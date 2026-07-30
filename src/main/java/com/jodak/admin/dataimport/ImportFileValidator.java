package com.jodak.admin.dataimport;

import com.jodak.admin.exceptions.ImportValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

/**
 * Validation du fichier d'initialisation téléversé avant stockage : présence, taille bornée et
 * extension {@code .xlsx} (défense en profondeur, en complément des limites multipart et du
 * durcissement POI ; cf. {@link PoiSecurityConfig}).
 */
@Component
@RequiredArgsConstructor
public class ImportFileValidator {

    private final ImportProperties properties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImportValidationException("Le fichier est vide.");
        }
        long max = properties.maxFileSizeBytes();
        if (max > 0 && file.getSize() > max) {
            throw new ImportValidationException(
                    "Le fichier dépasse la taille maximale autorisée (%d Mio).".formatted(max / (1024 * 1024)));
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".xlsx")) {
            throw new ImportValidationException(
                    "Le fichier d'initialisation doit être un classeur Excel « .xlsx ».");
        }
    }
}
