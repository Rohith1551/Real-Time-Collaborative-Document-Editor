package com.example.Real_Time_Collaborative_Document_Editor.service;

import com.example.Real_Time_Collaborative_Document_Editor.dto.LoginRequest;
import com.example.Real_Time_Collaborative_Document_Editor.entity.User;
import com.example.Real_Time_Collaborative_Document_Editor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository repo;
    @Autowired
    private JwtService jwtService;

    public void register(User user){

        if(repo.existsByEmail(user.getEmail())){
            throw new RuntimeException("This Email is already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        repo.save(user);

    }

    public String login(LoginRequest request){

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email Not Found"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Wrong Password");
        };

        return jwtService.generateToken(user);
    }

}
