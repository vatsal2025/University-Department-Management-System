package com.udiims.controller;

import com.udiims.exception.InvalidCredentialsException;
import com.udiims.exception.InvalidInputException;
import com.udiims.service.AuthService;
import com.udiims.util.IdValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String role = body.get("role");
        String id = body.get("id");
        String password = body.get("password");

        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID is required."));
        }

        try {
            if ("student".equals(role)) {
                IdValidationUtils.validateStudentId(id);
            }
            Map<String, Object> result = switch (role) {
                case "student" -> authService.loginStudent(id, password);
                case "secretary" -> authService.loginSecretary(id, password);
                case "finance_officer" -> authService.loginFinanceOfficer(id, password);
                default -> null;
            };

            if (result == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid ID"));
            }
            return ResponseEntity.ok(result);
        } catch (InvalidInputException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("error", "Service Unavailable. Please try again later."));
        }
    }
}
