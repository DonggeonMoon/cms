package com.dgmoonlabs.cms.domain.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String showHome() {
        return "home";
    }

    @PostMapping("/test2")
    public ResponseEntity<Void> test() {
        return ResponseEntity.ok().build();
    }
}
