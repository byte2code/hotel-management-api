package com.cn.hotelDemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cn.hotelDemo.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

	List<AuditLog> findAllByOrderByCreatedAtDesc();

	List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
}
