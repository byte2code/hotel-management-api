package com.cn.hotelDemo.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.cn.hotelDemo.annotation.AuditLogged;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Login Controller", description = "Endpoints for authentication UI")
public class LoginController {

	public LoginController() {
	}

	@GetMapping("/login")
	@Operation(summary = "Serve the login page")
	@AuditLogged(action = "LOGIN_PAGE_VIEWED", resourceType = "AUTH", resourceIdSpel = "#request.getRemoteAddr()")
	public String login(HttpServletRequest request) {
		return "login";
	}
}
