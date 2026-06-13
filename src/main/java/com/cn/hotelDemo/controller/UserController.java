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

import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.service.AuditService;
import com.cn.hotelDemo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/user")
@Tag(name = "User Controller", description = "Endpoints for managing users")
public class UserController {
    
    @Autowired
    UserService userService;

    @Autowired
    AuditService auditService;

    @GetMapping("/getUsers")
    @Operation(summary = "Get all users")
    @SecurityRequirement(name = "Bearer Authentication")
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/getUsers/{id}")
    @Operation(summary = "Get user by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/createUser")
    @Operation(summary = "Register a new user (Public)")
	public void createUser(@RequestBody UserRequest userRequest)
	{
		User createdUser = userService.createUser(userRequest);
		auditService.record("USER_CREATED", userRequest.getUsername(), "USER", String.valueOf(createdUser.getId()),
				"SUCCESS", "Public user registration completed for email=%s".formatted(userRequest.getEmail()));
	}

    @DeleteMapping("/remove/id/{id}")
    @Operation(summary = "Delete a user by ID")
    @SecurityRequirement(name = "Bearer Authentication")
	public void deleteUserById(@PathVariable Long id)
	{
		userService.deleteUserById(id);
		auditService.record("USER_DELETED", "system", "USER", String.valueOf(id), "SUCCESS",
				"User delete requested for id=%s".formatted(id));
		
	}
}
