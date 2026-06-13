package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.hotelDemo.model.AuditLog;
import com.cn.hotelDemo.service.AuditService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/audit")
@Tag(name = "Audit Controller", description = "Endpoints for viewing audit logs")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

	private final AuditService auditService;

	public AuditController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Get all audit logs (Admin only)")
	public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
		return ResponseEntity.ok(auditService.getAllAuditLogs());
	}
}
