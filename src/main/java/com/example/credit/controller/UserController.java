package com.example.credit.controller;

import com.example.credit.entity.CreditApplication;
import com.example.credit.entity.User;
import com.example.credit.repository.CreditApplicationRepository;
import com.example.credit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private CreditApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<?> getHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }

        List<CreditApplication> history = applicationRepository.findByUserId(user.getId());
        return ResponseEntity.ok(history);
    }
}
