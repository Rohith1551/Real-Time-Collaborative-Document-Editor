package com.example.Real_Time_Collaborative_Document_Editor.controller;

import com.example.Real_Time_Collaborative_Document_Editor.config.SecurityConfig;
import com.example.Real_Time_Collaborative_Document_Editor.dto.LoginRequest;
import com.example.Real_Time_Collaborative_Document_Editor.entity.User;
import com.example.Real_Time_Collaborative_Document_Editor.repository.UserRepository;
import com.example.Real_Time_Collaborative_Document_Editor.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService service;
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user){

        service.register(user);
        return ResponseEntity.ok("Email Registered Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request){

        String Token = service.login(request);
        return ResponseEntity.ok(Token);

    }
}
