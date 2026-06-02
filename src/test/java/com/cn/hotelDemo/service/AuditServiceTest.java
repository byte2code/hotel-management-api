package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cn.hotelDemo.model.AuditLog;
import com.cn.hotelDemo.repository.AuditLogRepository;

class AuditServiceTest {

	private AuditLogRepository auditLogRepository;
	private AuditService auditService;

	@BeforeEach
	void setUp() {
		auditLogRepository = mock(AuditLogRepository.class);
		auditService = new AuditService(auditLogRepository);
	}

	@Test
	void recordPersistsAuditDetails() {
		when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AuditLog saved = auditService.record("BOOKING_CREATED", "alice", "BOOKING", "10", "SUCCESS",
				"Booking confirmed successfully");

		assertEquals("BOOKING_CREATED", saved.getAction());
		assertEquals("alice", saved.getActor());
		assertEquals("BOOKING", saved.getResourceType());
		assertEquals("10", saved.getResourceId());
		assertEquals("SUCCESS", saved.getStatus());
		assertNotNull(saved.getCreatedAt());
		verify(auditLogRepository).save(any(AuditLog.class));
	}

	@Test
	void getAllAuditLogsUsesDescendingOrder() {
		when(auditLogRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

		assertTrue(auditService.getAllAuditLogs().isEmpty());
		verify(auditLogRepository).findAllByOrderByCreatedAtDesc();
	}
}
