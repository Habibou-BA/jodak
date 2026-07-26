package com.jodak.admin.repositories;

import com.jodak.admin.entities.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès au journal d'audit d'administration.
 */
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {
}
