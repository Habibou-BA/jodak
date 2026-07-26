package com.jodak.admin.security;

import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.exceptions.InvalidCredentialsException;
import com.jodak.admin.repositories.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Résout l'administrateur authentifié à partir de l'identifiant porté par le JWT (sujet).
 */
@Service
@RequiredArgsConstructor
public class CurrentAdminService {

    private final AdminUserRepository adminUserRepository;

    public AdminUser require(String principalId) {
        try {
            return adminUserRepository.findById(Long.valueOf(principalId))
                    .orElseThrow(() -> new InvalidCredentialsException("Session invalide."));
        } catch (NumberFormatException ex) {
            throw new InvalidCredentialsException("Session invalide.");
        }
    }
}
