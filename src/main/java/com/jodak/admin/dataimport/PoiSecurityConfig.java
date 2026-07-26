package com.jodak.admin.dataimport;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.IOUtils;
import org.springframework.context.annotation.Configuration;

/**
 * Durcit Apache POI contre les archives XLSX malveillantes (un {@code .xlsx} est une archive ZIP).
 *
 * <ul>
 *   <li><b>Zip bomb</b> : {@link ZipSecureFile#setMinInflateRatio(double)} rejette une entrée dont
 *       le ratio de décompression est trop élevé, et {@link ZipSecureFile#setMaxEntrySize(long)} /
 *       {@link IOUtils#setByteArrayMaxOverride(int)} bornent la taille décompressée.</li>
 *   <li><b>XXE</b> : POI configure par défaut ses fabriques XML en traitement sécurisé (entités
 *       externes et DOCTYPE désactivés) ; aucune entité externe n'est donc résolue à la lecture.</li>
 *   <li><b>Zip slip</b> : POI lit les entrées de l'archive en mémoire sans les extraire sur le
 *       disque ; aucune écriture de chemin issu de l'archive n'a lieu.</li>
 * </ul>
 *
 * <p>Ces réglages POI sont statiques (globaux à la JVM), d'où leur application au démarrage.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PoiSecurityConfig {

    private final ImportProperties properties;

    @PostConstruct
    void hardenPoi() {
        if (properties.minInflateRatio() > 0) {
            ZipSecureFile.setMinInflateRatio(properties.minInflateRatio());
        }
        long maxBytes = properties.maxUncompressedBytes();
        if (maxBytes > 0) {
            ZipSecureFile.setMaxEntrySize(maxBytes);
            ZipSecureFile.setMaxTextSize(maxBytes);
            IOUtils.setByteArrayMaxOverride((int) Math.min(maxBytes, Integer.MAX_VALUE));
        }
        log.info("Durcissement POI actif (ratio min={}, taille décompressée max={} octets).",
                properties.minInflateRatio(), maxBytes);
    }
}
