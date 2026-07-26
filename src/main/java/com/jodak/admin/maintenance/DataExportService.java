package com.jodak.admin.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jodak.admin.dtos.ExportManifest;
import com.jodak.entities.Athlete;
import com.jodak.entities.Country;
import com.jodak.entities.Discipline;
import com.jodak.entities.Epreuve;
import com.jodak.entities.Resultat;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.CountryRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Construit une archive ZIP contenant toutes les données du domaine (un CSV par table) et un
 * {@code metadata.json} (date, version, administrateur, nombre d'enregistrements et empreintes
 * SHA-256). L'archive complète est construite en mémoire dans une transaction en lecture afin
 * d'initialiser les associations paresseuses (volumes modérés ; pagination/streaming à prévoir
 * pour de très gros volumes).
 */
@Service
@RequiredArgsConstructor
public class DataExportService {

    private final CountryRepository countryRepository;
    private final DisciplineRepository disciplineRepository;
    private final AthleteRepository athleteRepository;
    private final EpreuveRepository epreuveRepository;
    private final ResultatRepository resultatRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ArchiveResult build(String adminEmail) {
        Map<String, Long> records = new LinkedHashMap<>();
        Map<String, String> checksums = new LinkedHashMap<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            records.put("countries", csv(zip, "countries.csv",
                    List.of("code", "name"), countryRepository.findAll(),
                    (Country c) -> List.of(c.getCode(), c.getName()), checksums));

            records.put("disciplines", csv(zip, "disciplines.csv",
                    List.of("name"), disciplineRepository.findAll(),
                    (Discipline d) -> List.of(d.getName()), checksums));

            records.put("athletes", csv(zip, "athletes.csv",
                    List.of("lastName", "firstName", "gender", "birthDate", "countryCode",
                            "discipline", "heightCm", "weightKg"),
                    athleteRepository.findAll(),
                    (Athlete a) -> List.of(a.getLastName(), a.getFirstName(), a.getGender().name(),
                            a.getBirthDate().toString(), a.getCountry().getCode(),
                            a.getDiscipline().getName(), String.valueOf(a.getHeightCm()),
                            String.valueOf(a.getWeightKg())),
                    checksums));

            records.put("epreuves", csv(zip, "epreuves.csv",
                    List.of("label", "discipline", "eventDate"), epreuveRepository.findAll(),
                    (Epreuve e) -> List.of(e.getLabel(), e.getDiscipline().getName(),
                            e.getEventDate().toString()),
                    checksums));

            records.put("resultats", csv(zip, "resultats.csv",
                    List.of("epreuveLabel", "eventDate", "athleteLastName", "athleteFirstName",
                            "rank", "medal"),
                    resultatRepository.findAll(),
                    (Resultat r) -> List.of(r.getEpreuve().getLabel(),
                            r.getEpreuve().getEventDate().toString(),
                            r.getAthlete().getLastName(), r.getAthlete().getFirstName(),
                            String.valueOf(r.getRankPosition()),
                            r.getMedal() == null ? "" : r.getMedal().name()),
                    checksums));

            ExportManifest manifest = new ExportManifest(OffsetDateTime.now().toString(),
                    applicationVersion(), adminEmail, records, checksums);
            byte[] metadata = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest);
            zip.putNextEntry(new ZipEntry("metadata.json"));
            zip.write(metadata);
            zip.closeEntry();
            zip.finish();

            byte[] content = out.toByteArray();
            return new ArchiveResult(content, manifest, sha256(content));
        } catch (IOException ex) {
            throw new IllegalStateException("Échec de la génération de l'archive d'export.", ex);
        }
    }

    private <T> long csv(ZipOutputStream zip, String name, List<String> headers, List<T> rows,
                         Function<T, List<String>> mapper, Map<String, String> checksums)
            throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(buffer, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader(headers.toArray(String[]::new)).build())) {
            for (T row : rows) {
                printer.printRecord(mapper.apply(row));
            }
        }
        byte[] bytes = buffer.toByteArray();
        checksums.put(name, sha256(bytes));
        zip.putNextEntry(new ZipEntry(name));
        zip.write(bytes);
        zip.closeEntry();
        return rows.size();
    }

    private String applicationVersion() {
        return Optional.ofNullable(getClass().getPackage().getImplementationVersion()).orElse("dev");
    }

    static String sha256(byte[] data) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(data);
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 indisponible", ex);
        }
    }

    /** Archive construite en mémoire, avec ses métadonnées et son empreinte globale. */
    public record ArchiveResult(byte[] content, ExportManifest manifest, String checksum) {
    }
}
