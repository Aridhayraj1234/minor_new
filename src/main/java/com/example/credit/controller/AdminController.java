package com.example.credit.controller;

import com.example.credit.entity.CreditApplication;
import com.example.credit.entity.User;
import com.example.credit.repository.CreditApplicationRepository;
import com.example.credit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditApplicationRepository applicationRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream().map(user -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", user.getId());
            dto.put("name", user.getName());
            dto.put("email", user.getEmail());
            dto.put("role", user.getRole());
            dto.put("createdAt", user.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<CreditApplication>> getAllApplications() {
        return ResponseEntity.ok(applicationRepository.findAll());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String roleValue = payload.get("role");
        if (roleValue == null) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        User user = optionalUser.get();
        try {
            user.setRole(User.Role.valueOf(roleValue.toUpperCase()));
            userRepository.save(user);
            return ResponseEntity.ok("User role updated");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Invalid role. Allowed: ADMIN, USER");
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getUsageSummary() {
        List<CreditApplication> applications = applicationRepository.findAll();
        long totalPredictions = applications.size();
        long highRisk = applications.stream().filter(a -> "HIGH".equalsIgnoreCase(a.getPredictionResult())).count();
        long mediumRisk = applications.stream().filter(a -> "MEDIUM".equalsIgnoreCase(a.getPredictionResult())).count();
        long lowRisk = applications.stream().filter(a -> "LOW".equalsIgnoreCase(a.getPredictionResult())).count();

        Map<String, Object> usage = new HashMap<>();
        usage.put("totalUsers", userRepository.count());
        usage.put("totalPredictions", totalPredictions);
        usage.put("highRiskCount", highRisk);
        usage.put("mediumRiskCount", mediumRisk);
        usage.put("lowRiskCount", lowRisk);
        return ResponseEntity.ok(usage);
    }
}
