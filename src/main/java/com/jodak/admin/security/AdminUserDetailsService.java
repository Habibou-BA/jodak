package com.jodak.admin.security;

import com.jodak.admin.entities.AdminUser;
import com.jodak.admin.repositories.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Charge un administrateur par email pour Spring Security. Sa présence supprime aussi le compte
 * utilisateur par défaut généré par Spring Boot.
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        AdminUser admin = adminUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Administrateur introuvable."));
        return User.withUsername(admin.getEmail())
                .password(admin.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(admin.getRole().name())))
                .disabled(!admin.isEnabled())
                .accountLocked(admin.isLocked(OffsetDateTime.now()))
                .build();
    }
}
