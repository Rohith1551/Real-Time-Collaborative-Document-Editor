package com.example.Real_Time_Collaborative_Document_Editor.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Testt {

    @GetMapping("/g")
    public String test(Authentication authentication){
        return authentication.getName() + "-" + authentication.getAuthorities();
    }
}
