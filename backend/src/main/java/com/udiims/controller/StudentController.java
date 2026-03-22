package com.udiims.controller;

import com.udiims.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // UC-01: Get student profile
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudent(@PathVariable String studentId) {
        try {
            Map<String, Object> student = studentService.getStudent(studentId);
            if (student == null) return ResponseEntity.status(404).body(Map.of("error", "Invalid Roll Number"));
            return ResponseEntity.ok(student);
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of("error", "Service Unavailable. Please try again later."));
        }
    }

    // UC-02: Get available courses for registration
    @GetMapping("/courses/available")
    public ResponseEntity<?> getAvailableCourses(
            @RequestParam String semesterTerm,
            @RequestParam(required = false) String departmentId) {
        try {
            List<Map<String, Object>> courses = studentService.getAvailableCourses(semesterTerm, departmentId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-02: Get student's course registrations
    @GetMapping("/{studentId}/registrations")
    public ResponseEntity<?> getRegistrations(
            @PathVariable String studentId,
            @RequestParam(required = false) String semesterTerm) {
        try {
            return ResponseEntity.ok(studentService.getRegistrations(studentId, semesterTerm));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-02: Register for courses
    @PostMapping("/{studentId}/registrations")
    public ResponseEntity<?> registerCourses(@PathVariable String studentId, @RequestBody Map<String, Object> body) {
        try {
            String semesterTerm = (String) body.get("semester_term");
            @SuppressWarnings("unchecked")
            List<String> courseCodes = (List<String>) body.get("course_codes");
            if (courseCodes == null || courseCodes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No course codes provided."));
            }
            return ResponseEntity.ok(studentService.registerCourses(studentId, semesterTerm, courseCodes));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-02: Drop a course
    @DeleteMapping("/{studentId}/registrations/{courseCode}")
    public ResponseEntity<?> dropCourse(
            @PathVariable String studentId,
            @PathVariable String courseCode,
            @RequestParam String semesterTerm) {
        try {
            studentService.dropCourse(studentId, courseCode, semesterTerm);
            return ResponseEntity.ok(Map.of("message", "Course dropped successfully."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-03: Calculate and get GPA
    @PostMapping("/{studentId}/gpa/calculate")
    public ResponseEntity<?> calculateGpa(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(studentService.calculateAndUpdateGpa(studentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{studentId}/gpa")
    public ResponseEntity<?> getGpa(@PathVariable String studentId) {
        try {
            Map<String, Object> student = studentService.getStudent(studentId);
            if (student == null) return ResponseEntity.status(404).body(Map.of("error", "Student not found."));
            return ResponseEntity.ok(Map.of(
                    "sgpa", student.getOrDefault("sgpa", 0.0),
                    "cgpa", student.getOrDefault("cgpa", 0.0),
                    "student_name", student.getOrDefault("student_name", ""),
                    "program", student.getOrDefault("program", ""),
                    "semester", student.getOrDefault("semester", 0)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-04: Program and backlog tracking
    @GetMapping("/{studentId}/backlogs")
    public ResponseEntity<?> getBacklogs(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(studentService.getBacklogTracking(studentId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // UC-05: Fee status (read-only)
    @GetMapping("/{studentId}/fees")
    public ResponseEntity<?> getFees(@PathVariable String studentId) {
        try {
            return ResponseEntity.ok(studentService.getFeeStatus(studentId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
