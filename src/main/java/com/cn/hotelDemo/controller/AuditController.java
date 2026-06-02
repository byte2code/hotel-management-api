package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.hotelDemo.model.AuditLog;
import com.cn.hotelDemo.service.AuditService;

@RestController
@RequestMapping("/audit")
public class AuditController {

	private final AuditService auditService;

	public AuditController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
		return ResponseEntity.ok(auditService.getAllAuditLogs());
	}
}
