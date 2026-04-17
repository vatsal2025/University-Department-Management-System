package com.udiims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StudentService {

    @Autowired
    private SupabaseService supabase;

    @Autowired
    private GpaService gpaService;

    // UC-01: Get student profile
    public Map<String, Object> getStudent(String studentId) throws Exception {
        return supabase.getSingle("students",
                "student_id=eq." + studentId + "&select=student_id,student_name,program,semester,sgpa,cgpa,backlog_count,department_id,gpa_updated_timestamp");
    }

    // UC-02: Course Registration — get available courses for semester with faculty info
    public List<Map<String, Object>> getAvailableCourses(String semesterTerm, String departmentId) throws Exception {
        String query = "semester_term=eq." + semesterTerm;
        if (departmentId != null && !departmentId.isEmpty()) {
            query += "&department_id=eq." + departmentId;
        }
        List<Map<String, Object>> courses = supabase.getList("courses", query);
        // Attach faculty info so students can see who teaches each course
        for (Map<String, Object> course : courses) {
            String facultyId = (String) course.get("faculty_id");
            if (facultyId != null && !facultyId.isBlank()) {
                Map<String, Object> faculty = supabase.getSingle("faculty",
                        "faculty_id=eq." + facultyId + "&select=faculty_id,faculty_name,designation,department_name");
                if (faculty != null) {
                    course.put("faculty_info", faculty);
                }
            }
        }
        return courses;
    }

    // UC-02: Get student's current registrations
    public List<Map<String, Object>> getRegistrations(String studentId, String semesterTerm) throws Exception {
        String query = "student_id=eq." + studentId;
        if (semesterTerm != null && !semesterTerm.isEmpty()) {
            query += "&semester_term=eq." + semesterTerm;
        }
        return supabase.getList("course_registrations", query + "&order=semester_term.desc");
    }

    // UC-02: Register for courses
    public Map<String, Object> registerCourses(String studentId, String semesterTerm, List<String> courseCodes) throws Exception {
        List<String> registered = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String code : courseCodes) {
            // Validate course exists
            Map<String, Object> course = supabase.getSingle("courses",
                    "course_code=eq." + code + "&semester_term=eq." + semesterTerm);
            if (course == null) {
                failed.add(code + " (not found)");
                continue;
            }

            // Check not already completed
            Map<String, Object> existing = supabase.getSingle("course_registrations",
                    "student_id=eq." + studentId + "&course_code=eq." + code + "&registration_status=eq.completed");
            if (existing != null) {
                failed.add(code + " (already completed)");
                continue;
            }

            // Check not already active this term
            Map<String, Object> activeReg = supabase.getSingle("course_registrations",
                    "student_id=eq." + studentId + "&course_code=eq." + code + "&semester_term=eq." + semesterTerm + "&registration_status=eq.active");
            if (activeReg != null) {
                failed.add(code + " (already registered)");
                continue;
            }

            // Check if there is an existing dropped registration to reactivate
            Map<String, Object> droppedReg = supabase.getSingle("course_registrations",
                    "student_id=eq." + studentId + "&course_code=eq." + code + "&semester_term=eq." + semesterTerm + "&registration_status=eq.dropped");
            if (droppedReg != null) {
                try {
                    supabase.patch("course_registrations",
                            "student_id=eq." + studentId + "&course_code=eq." + code + "&semester_term=eq." + semesterTerm + "&registration_status=eq.dropped",
                            Map.of("registration_status", "active"));
                    registered.add(code);
                } catch (Exception e) {
                    failed.add(code + " (DB error)");
                }
                continue;
            }

            String regId = "REG-" + studentId + "-" + code + "-" + semesterTerm.replace(" ", "");
            Map<String, Object> reg = new HashMap<>();
            reg.put("registration_id", regId);
            reg.put("student_id", studentId);
            reg.put("course_code", code);
            reg.put("course_name", course.get("course_name"));
            reg.put("semester_term", semesterTerm);
            reg.put("credit_hours", course.get("credit_hours"));
            reg.put("registration_status", "active");
            reg.put("backlog_flag", false);

            try {
                supabase.post("course_registrations", reg);
                registered.add(code);
            } catch (Exception e) {
                failed.add(code + " (DB error)");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("registered", registered);
        result.put("failed", failed);
        return result;
    }

    // UC-02: Drop a course
    public void dropCourse(String studentId, String courseCode, String semesterTerm) throws Exception {
        supabase.patch("course_registrations",
                "student_id=eq." + studentId + "&course_code=eq." + courseCode + "&semester_term=eq." + semesterTerm + "&registration_status=eq.active",
                Map.of("registration_status", "dropped"));
    }

    // UC-03: Calculate and update GPA
    public Map<String, Object> calculateAndUpdateGpa(String studentId) throws Exception {
        return gpaService.calculateAndUpdateGpa(studentId);
    }

    // UC-04: Get backlog tracking
    public Map<String, Object> getBacklogTracking(String studentId) throws Exception {
        List<Map<String, Object>> all = supabase.getList("course_registrations",
                "student_id=eq." + studentId);

        if (all.isEmpty()) {
            return Map.of("message", "No academic records available.", "completed", List.of(), "backlogs", List.of(), "credits_earned", 0, "credits_required", 180);
        }

        List<Map<String, Object>> completed = new ArrayList<>();
        List<Map<String, Object>> backlogs = new ArrayList<>();
        int creditsEarned = 0;

        for (Map<String, Object> reg : all) {
            String status = (String) reg.get("registration_status");
            Boolean backlogFlag = (Boolean) reg.getOrDefault("backlog_flag", false);
            Integer credits = reg.get("credit_hours") instanceof Number n ? n.intValue() : 0;

            if ("completed".equals(status)) {
                completed.add(reg);
                if (backlogFlag == null || !backlogFlag) {
                    creditsEarned += credits;
                }
            }
            if (Boolean.TRUE.equals(backlogFlag)) {
                backlogs.add(reg);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("completed", completed);
        result.put("backlogs", backlogs);
        result.put("credits_earned", creditsEarned);
        result.put("credits_required", 180);
        if (backlogs.isEmpty()) result.put("backlog_message", "No pending backlogs.");
        return result;
    }

    // UC-05: Get fee status (read-only)
    public List<Map<String, Object>> getFeeStatus(String studentId) throws Exception {
        List<Map<String, Object>> fees = supabase.getList("financial_records",
                "student_id=eq." + studentId + "&record_type=eq.student-fee&order=semester_term.asc");
        if (fees.isEmpty()) throw new RuntimeException("No fee records available.");
        return fees;
    }
}
