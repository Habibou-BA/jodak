package com.jodak.admin.repositories;

import com.jodak.admin.entities.AdminLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès au journal d'audit d'administration.
 */
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    Page<AdminLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
