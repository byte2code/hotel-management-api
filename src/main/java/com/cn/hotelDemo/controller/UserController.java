package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.annotation.AuditLogged;
import com.cn.hotelDemo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUsers")
    @Operation(summary = "Get all users with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    public org.springframework.http.ResponseEntity<org.springframework.data.domain.Page<User>> getUsers(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "asc") String direction) {
        org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
                ? org.springframework.data.domain.Sort.by(sortBy).descending()
                : org.springframework.data.domain.Sort.by(sortBy).ascending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        return org.springframework.http.ResponseEntity.ok(userService.getUsers(pageable));
    }

    @GetMapping("/getUsers/{id}")
    @Operation(summary = "Get user by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

	@PostMapping("/createUser")
    @Operation(summary = "Register a new user (Public)")
    @AuditLogged(action = "USER_CREATED", resourceType = "USER", resourceIdSpel = "#result.id")
	public User createUser(@Valid @RequestBody UserRequest userRequest)
	{
		return userService.createUser(userRequest);
	}

    @DeleteMapping("/remove/id/{id}")
    @Operation(summary = "Delete a user by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @AuditLogged(action = "USER_DELETED", resourceType = "USER", resourceIdSpel = "#id")
	public void deleteUserById(@PathVariable Long id)
	{
		userService.deleteUserById(id);
	}
}
