package com.cn.hotelDemo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cn.hotelDemo.model.AuditLog;
import com.cn.hotelDemo.repository.AuditLogRepository;

@Service
public class AuditService {

	private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

	private final AuditLogRepository auditLogRepository;

	public AuditService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	@Transactional
	public AuditLog record(String action, String actor, String resourceType, String resourceId, String status,
			String details) {
		AuditLog auditLog = new AuditLog();
		auditLog.setAction(action);
		auditLog.setActor(normalize(actor, "anonymous"));
		auditLog.setResourceType(normalize(resourceType, "UNKNOWN"));
		auditLog.setResourceId(resourceId);
		auditLog.setStatus(normalize(status, "SUCCESS"));
		auditLog.setDetails(details);
		auditLog.setCreatedAt(LocalDateTime.now());

		AuditLog saved = auditLogRepository.save(auditLog);
		logger.info("AUDIT action={} actor={} resourceType={} resourceId={} status={}",
				saved.getAction(), saved.getActor(), saved.getResourceType(), saved.getResourceId(), saved.getStatus());
		return saved;
	}

	@Transactional(readOnly = true)
	public List<AuditLog> getAllAuditLogs() {
		return auditLogRepository.findAllByOrderByCreatedAtDesc();
	}

	@Transactional(readOnly = true)
	public List<AuditLog> getAuditLogsByAction(String action) {
		return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
	}

	private String normalize(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}
}
