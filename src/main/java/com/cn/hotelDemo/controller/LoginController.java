package com.cn.hotelDemo.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.cn.hotelDemo.service.AuditService;

@Controller
public class LoginController {

	private final AuditService auditService;

	public LoginController(AuditService auditService) {
		this.auditService = auditService;
	}

	@GetMapping("/login")
	public String login(HttpServletRequest request) {
		auditService.record("LOGIN_PAGE_VIEWED", "anonymous", "AUTH", "/login", "SUCCESS",
				"Login page rendered from %s".formatted(request.getRemoteAddr()));
		return "login";
	}
}
