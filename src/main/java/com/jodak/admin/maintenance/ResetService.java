package com.jodak.admin.maintenance;

import com.jodak.admin.dtos.BackupResponse;
import com.jodak.admin.dtos.ResetResponse;
import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.exceptions.ImportValidationException;
import com.jodak.admin.exceptions.InvalidCredentialsException;
import com.jodak.admin.repositories.AdminUserRepository;
import com.jodak.admin.services.AdminLogService;
import com.jodak.exceptions.BusinessRuleException;
import com.jodak.repositories.AthleteRepository;
import com.jodak.repositories.DisciplineRepository;
import com.jodak.repositories.EpreuveRepository;
import com.jodak.repositories.ResultatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Réinitialisation sécurisée : vérifie l'activation et la double confirmation, crée une sauvegarde
 * vérifiée, puis supprime les données de compétition dans l'ordre des clés étrangères. Le
 * référentiel des nations et les données d'administration/audit sont préservés. En cas d'échec,
 * la transaction est annulée (données intactes) et la sauvegarde reste disponible.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetService {

    private final ResetProperties resetProperties;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final BackupService backupService;
    private final AdminLogService adminLogService;
    private final ResultatRepository resultatRepository;
    private final AthleteRepository athleteRepository;
    private final EpreuveRepository epreuveRepository;
    private final DisciplineRepository disciplineRepository;

    @Transactional
    public ResetResponse reset(Long adminId, String password, String phrase, String ip, String userAgent) {
        if (!resetProperties.enabled()) {
            throw new BusinessRuleException("La réinitialisation est désactivée sur cet environnement.");
        }
        AdminUser admin = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new InvalidCredentialsException("Session invalide."));
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            adminLogService.record(adminId, "RESET", false, "Mot de passe incorrect", ip, userAgent);
            throw new InvalidCredentialsException("Mot de passe incorrect.");
        }
        if (!resetProperties.confirmationPhrase().equals(phrase)) {
            adminLogService.record(adminId, "RESET", false, "Phrase incorrecte", ip, userAgent);
            throw new ImportValidationException("Phrase de confirmation incorrecte.");
        }

        BackupResponse backup = backupService.create(admin.getEmail());

        Map<String, Long> deleted = new LinkedHashMap<>();
        deleted.put("resultats", resultatRepository.count());
        deleted.put("athletes", athleteRepository.count());
        deleted.put("epreuves", epreuveRepository.count());
        deleted.put("disciplines", disciplineRepository.count());

        resultatRepository.deleteAllInBatch();
        athleteRepository.deleteAllInBatch();
        epreuveRepository.deleteAllInBatch();
        disciplineRepository.deleteAllInBatch();

        adminLogService.record(adminId, "RESET", true, "Sauvegarde " + backup.fileName(), ip, userAgent);
        log.warn("Plateforme réinitialisée par l'administrateur {} — sauvegarde {}", adminId, backup.fileName());
        return new ResetResponse(backup.fileName(), backup.checksum(), deleted);
    }
}
