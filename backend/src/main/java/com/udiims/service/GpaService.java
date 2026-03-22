package com.udiims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class GpaService {

    @Autowired
    private SupabaseService supabase;

    // Grade point mapping as per docs (all 21 grade values)
    private static final Map<String, Double> GRADE_POINTS = new LinkedHashMap<>() {{
        put("A",  10.0);
        put("A-",  9.0);
        put("B",   8.0);
        put("B-",  7.0);
        put("C",   6.0);
        put("C-",  5.0);
        put("D",   4.0);
        put("E",   3.0);
        put("F",   0.0);
        put("F*",  0.0);
        put("I",   0.0);
        put("R",   0.0);
        put("W",   0.0);
        put("Z",   0.0);
        put("AP",  0.0);
        put("AF",  0.0);
        put("AU",  0.0);
        put("S",   8.0);
        put("U",   0.0);
        put("P",   5.0);
        put("NP",  0.0);
        put("TR", 10.0);
    }};

    private static final Set<String> FAIL_GRADES = Set.of("F", "F*", "U", "NP", "AF");
    private static final Set<String> EXCLUDE_FROM_CALC = Set.of("I", "R", "W", "Z", "AP", "AU");

    // UC-03: Calculate SGPA/CGPA and write back to D1
    public Map<String, Object> calculateAndUpdateGpa(String studentId) throws Exception {
        List<Map<String, Object>> registrations = supabase.getList("course_registrations",
                "student_id=eq." + studentId + "&registration_status=eq.completed");

        if (registrations.isEmpty()) {
            return Map.of("message", "Grades not yet available.", "sgpa", 0.0, "cgpa", 0.0);
        }

        // Group by semester_term
        Map<String, List<Map<String, Object>>> bySemester = new LinkedHashMap<>();
        for (Map<String, Object> reg : registrations) {
            String term = (String) reg.get("semester_term");
            if (term != null) {
                bySemester.computeIfAbsent(term, k -> new ArrayList<>()).add(reg);
            }
        }

        // Update backlog flags for fail grades
        for (Map<String, Object> reg : registrations) {
            String grade = (String) reg.get("grade");
            if (grade != null && FAIL_GRADES.contains(grade)) {
                supabase.patch("course_registrations",
                        "registration_id=eq." + reg.get("registration_id"),
                        Map.of("backlog_flag", true));
            }
        }

        // Calculate per-semester SGPA
        Map<String, Double> semesterSgpa = new LinkedHashMap<>();
        double totalWeightedPoints = 0;
        int totalCredits = 0;
        String latestSemester = null;

        for (Map.Entry<String, List<Map<String, Object>>> entry : bySemester.entrySet()) {
            String term = entry.getKey();
            List<Map<String, Object>> semRegs = entry.getValue();
            double weightedSum = 0;
            int credits = 0;
            boolean hasGrades = false;

            for (Map<String, Object> reg : semRegs) {
                String grade = (String) reg.get("grade");
                if (grade == null || EXCLUDE_FROM_CALC.contains(grade)) continue;
                Integer creditHours = reg.get("credit_hours") instanceof Number n ? n.intValue() : 0;
                Double gradePoint = GRADE_POINTS.getOrDefault(grade, null);
                if (gradePoint == null) continue;

                weightedSum += gradePoint * creditHours;
                credits += creditHours;
                hasGrades = true;
            }

            double sgpa = (credits > 0) ? weightedSum / credits : 0.0;
            semesterSgpa.put(term, Math.round(sgpa * 100.0) / 100.0);
            totalWeightedPoints += weightedSum;
            totalCredits += credits;
            latestSemester = term;
        }

        double cgpa = (totalCredits > 0) ? totalWeightedPoints / totalCredits : 0.0;
        cgpa = Math.round(cgpa * 100.0) / 100.0;
        double currentSgpa = latestSemester != null ? semesterSgpa.get(latestSemester) : 0.0;

        // Write updated GPA to D1
        Map<String, Object> gpaUpdate = new HashMap<>();
        gpaUpdate.put("sgpa", currentSgpa);
        gpaUpdate.put("cgpa", cgpa);
        gpaUpdate.put("gpa_updated_timestamp", Instant.now().toString());

        // Update backlog count
        long backlogCount = registrations.stream()
                .filter(r -> {
                    String g = (String) r.get("grade");
                    return g != null && FAIL_GRADES.contains(g);
                }).count();
        gpaUpdate.put("backlog_count", (int) backlogCount);

        try {
            supabase.patch("students", "student_id=eq." + studentId, gpaUpdate);
        } catch (Exception e) {
            // Log error but preserve existing GPA values (as per UC-03 spec)
            System.err.println("GPA write to D1 failed: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sgpa", currentSgpa);
        result.put("cgpa", cgpa);
        result.put("semester_sgpa", semesterSgpa);
        result.put("backlog_count", (int) backlogCount);
        return result;
    }
}
