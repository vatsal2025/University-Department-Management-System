package com.udiims.controller;

import com.udiims.service.SecretaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/secretary")
public class SecretaryController {

    @Autowired
    private SecretaryService secretaryService;

    // UC-06: Faculty Management
    @GetMapping("/faculty")
    public ResponseEntity<?> getFaculty(@RequestParam String departmentId) {
        try {
            return ResponseEntity.ok(secretaryService.getFaculty(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/faculty")
    public ResponseEntity<?> addFaculty(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.addFaculty(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/faculty/{facultyId}")
    public ResponseEntity<?> updateFaculty(@PathVariable String facultyId, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.updateFaculty(facultyId, body));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/faculty/{facultyId}")
    public ResponseEntity<?> deactivateFaculty(@PathVariable String facultyId) {
        try {
            secretaryService.deactivateFaculty(facultyId);
            return ResponseEntity.ok(Map.of("message", "Faculty deactivated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/faculty/{facultyId}/force")
    public ResponseEntity<?> forceDeactivateFaculty(@PathVariable String facultyId) {
        try {
            secretaryService.updateFaculty(facultyId, Map.of("active_status", false));
            return ResponseEntity.ok(Map.of("message", "Faculty deactivated."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-07: Project Management
    @GetMapping("/projects")
    public ResponseEntity<?> getProjects(@RequestParam String departmentId) {
        try {
            return ResponseEntity.ok(secretaryService.getProjects(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/projects")
    public ResponseEntity<?> addProject(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.addProject(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<?> updateProject(@PathVariable String projectId, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.updateProject(projectId, body));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-08: Inventory Management
    @GetMapping("/inventory")
    public ResponseEntity<?> getInventory(@RequestParam String departmentId) {
        try {
            return ResponseEntity.ok(secretaryService.getInventory(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/inventory")
    public ResponseEntity<?> addInventoryItem(
            @RequestBody Map<String, Object> body,
            @RequestParam String departmentId,
            @RequestParam(defaultValue = "false") boolean isTechnical) {
        try {
            return ResponseEntity.ok(secretaryService.addInventoryItem(body, departmentId, isTechnical));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/inventory/{itemId}")
    public ResponseEntity<?> updateInventoryItem(@PathVariable String itemId, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.updateInventoryItem(itemId, body));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/inventory/{itemId}")
    public ResponseEntity<?> disposeInventoryItem(@PathVariable String itemId) {
        try {
            secretaryService.disposeInventoryItem(itemId);
            return ResponseEntity.ok(Map.of("message", "Item disposed."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-09: Department Accounts
    @GetMapping("/accounts")
    public ResponseEntity<?> getDepartmentAccounts(@RequestParam String departmentId) {
        try {
            return ResponseEntity.ok(secretaryService.getDepartmentAccounts(departmentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-10: Course Offerings
    @GetMapping("/courses")
    public ResponseEntity<?> getCourseOfferings(
            @RequestParam String departmentId,
            @RequestParam(required = false) String semesterTerm) {
        try {
            return ResponseEntity.ok(secretaryService.getCourseOfferings(departmentId, semesterTerm));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<?> addCourseOffering(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.addCourseOffering(body));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/courses/{courseCode}")
    public ResponseEntity<?> updateCourseOffering(
            @PathVariable String courseCode,
            @RequestParam String semesterTerm,
            @RequestParam String departmentId,
            @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(secretaryService.updateCourseOffering(courseCode, semesterTerm, departmentId, body));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/courses/{courseCode}")
    public ResponseEntity<?> removeCourseOffering(
            @PathVariable String courseCode,
            @RequestParam String semesterTerm,
            @RequestParam String departmentId) {
        try {
            secretaryService.removeCourseOffering(courseCode, semesterTerm, departmentId);
            return ResponseEntity.ok(Map.of("message", "Course offering removed."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/departments/{departmentId}")
    public ResponseEntity<?> getDepartment(@PathVariable String departmentId) {
        try {
            Map<String, Object> dept = secretaryService.getDepartment(departmentId);
            if (dept == null) return ResponseEntity.status(404).body(Map.of("error", "Department not found."));
            return ResponseEntity.ok(dept);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
