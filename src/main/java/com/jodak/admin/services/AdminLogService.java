package com.jodak.admin.services;

import com.jodak.admin.entities.AdminLog;
import com.jodak.admin.repositories.AdminLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Journalisation des opérations d'administration. Enregistré en {@code REQUIRES_NEW} afin que
 * la trace d'audit persiste même si la transaction appelante échoue (ex. connexion refusée).
 */
@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository adminLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long adminId, String action, boolean success, String message,
                       String ip, String userAgent) {
        adminLogRepository.save(AdminLog.builder()
                .adminId(adminId)
                .action(action)
                .success(success)
                .message(message)
                .ip(ip)
                .userAgent(truncate(userAgent, 300))
                .build());
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
