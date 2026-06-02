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

@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    UserService userService;

    @Autowired
    AuditService auditService;

    @GetMapping("/getUsers")
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/getUsers/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/createUser")
	public void createUser(@RequestBody UserRequest userRequest)
	{
		User createdUser = userService.createUser(userRequest);
		auditService.record("USER_CREATED", userRequest.getUsername(), "USER", String.valueOf(createdUser.getId()),
				"SUCCESS", "Public user registration completed for email=%s".formatted(userRequest.getEmail()));
	}

    @DeleteMapping("/remove/id/{id}")
	public void deleteUserById(@PathVariable Long id)
	{
		userService.deleteUserById(id);
		auditService.record("USER_DELETED", "system", "USER", String.valueOf(id), "SUCCESS",
				"User delete requested for id=%s".formatted(id));
		
	}
}
