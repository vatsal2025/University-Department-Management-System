package com.udiims.controller;

import com.udiims.service.DepartmentFundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/departments")
public class DepartmentFundController {

    @Autowired
    private DepartmentFundService departmentFundService;

    // ── Fund Sources (incoming money) ─────────────────────────────────────────

    @GetMapping("/{departmentId}/fund-sources")
    public ResponseEntity<?> getFundSources(@PathVariable String departmentId) {
        try {
            return ResponseEntity.ok(departmentFundService.getFundSources(departmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{departmentId}/fund-sources")
    public ResponseEntity<?> addFundSource(
            @PathVariable String departmentId,
            @RequestBody Map<String, Object> body) {
        try {
            body.put("department_id", departmentId);
            return ResponseEntity.ok(departmentFundService.addFundSource(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Fund Usage (expenditures) ─────────────────────────────────────────────

    @GetMapping("/{departmentId}/fund-usage")
    public ResponseEntity<?> getFundUsage(@PathVariable String departmentId) {
        try {
            return ResponseEntity.ok(departmentFundService.getFundUsage(departmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{departmentId}/fund-usage")
    public ResponseEntity<?> addFundUsage(
            @PathVariable String departmentId,
            @RequestBody Map<String, Object> body) {
        try {
            body.put("department_id", departmentId);
            return ResponseEntity.ok(departmentFundService.addFundUsage(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Summary ───────────────────────────────────────────────────────────────

    @GetMapping("/{departmentId}/fund-summary")
    public ResponseEntity<?> getDepartmentFundSummary(@PathVariable String departmentId) {
        try {
            return ResponseEntity.ok(departmentFundService.getDepartmentFundSummary(departmentId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
