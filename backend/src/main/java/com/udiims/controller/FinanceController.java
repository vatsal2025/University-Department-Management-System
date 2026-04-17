package com.udiims.controller;

import com.udiims.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    // UC-11: Grant Management
    @GetMapping("/grants")
    public ResponseEntity<?> getGrants(@RequestParam(required = false) String departmentId) {
        try {
            return ResponseEntity.ok(financeService.getGrants(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/grants")
    public ResponseEntity<?> recordGrant(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(financeService.recordGrant(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-12: Consultancy Fund Management
    @GetMapping("/consultancy")
    public ResponseEntity<?> getConsultancy(@RequestParam(required = false) String departmentId) {
        try {
            return ResponseEntity.ok(financeService.getConsultancy(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/consultancy")
    public ResponseEntity<?> recordConsultancy(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(financeService.recordConsultancy(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-13: Fee Collection & Modification
    @GetMapping("/fees/{studentId}")
    public ResponseEntity<?> getStudentFees(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(financeService.getStudentFees(studentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/fees/{studentId}")
    public ResponseEntity<?> updateFeeStatus(
            @PathVariable String studentId,
            @RequestBody Map<String, String> body) {
        try {
            String semesterTerm = body.get("semester_term");
            String feeStatus = body.get("fee_status");
            if (feeStatus == null || feeStatus.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "fee_status is required."));
            }
            return ResponseEntity.ok(financeService.updateFeeStatus(studentId, semesterTerm, feeStatus));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/fees")
    public ResponseEntity<?> createFeeRecord(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(financeService.createFeeRecord(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-13 (NEW): Partial fee payment endpoints

    @PostMapping("/fees/{studentId}/payments")
    public ResponseEntity<?> recordFeePayment(
            @PathVariable String studentId,
            @RequestBody Map<String, Object> body) {
        try {
            body.put("student_id", studentId);
            return ResponseEntity.ok(financeService.recordFeePayment(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/fees/{studentId}/payments")
    public ResponseEntity<?> getFeePayments(
            @PathVariable String studentId,
            @RequestParam(required = false) String semesterTerm) {
        try {
            return ResponseEntity.ok(financeService.getFeePayments(studentId, semesterTerm));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-14: Project Financial Management
    @GetMapping("/projects")
    public ResponseEntity<?> getProjects(@RequestParam(required = false) String departmentId) {
        try {
            return ResponseEntity.ok(financeService.getProjects(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/project-finance")
    public ResponseEntity<?> getProjectFinance(@RequestParam(required = false) String departmentId) {
        try {
            return ResponseEntity.ok(financeService.getProjectFinance(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/project-finance")
    public ResponseEntity<?> recordProjectFinance(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(financeService.recordProjectFinance(body));
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.startsWith("BUDGET_WARNING:")) {
                return ResponseEntity.status(409).body(Map.of("error", msg, "requires_confirmation", true));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
