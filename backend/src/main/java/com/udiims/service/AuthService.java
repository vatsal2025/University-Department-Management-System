package com.udiims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private SupabaseService supabase;

    public Map<String, Object> loginStudent(String studentId, String password) throws Exception {
        Map<String, Object> student = supabase.getSingle("students",
                "student_id=eq." + studentId + "&password=eq." + password + "&select=student_id,student_name,program,semester,sgpa,cgpa,backlog_count,department_id");
        if (student == null) return null;

        // Set dashboard_access = true
        Map<String, Object> update = new HashMap<>();
        update.put("dashboard_access", true);
        supabase.patch("students", "student_id=eq." + studentId, update);

        Map<String, Object> result = new HashMap<>(student);
        result.put("role", "student");
        result.put("dashboard_access", true);
        return result;
    }

    public Map<String, Object> loginSecretary(String secretaryId, String password) throws Exception {
        Map<String, Object> secretary = supabase.getSingle("department_secretaries",
                "secretary_id=eq." + secretaryId + "&password=eq." + password);
        if (secretary == null) return null;

        Map<String, Object> result = new HashMap<>(secretary);
        result.put("role", "secretary");
        return result;
    }

    public Map<String, Object> loginFinanceOfficer(String officerId, String password) throws Exception {
        Map<String, Object> officer = supabase.getSingle("finance_officers",
                "officer_id=eq." + officerId + "&password=eq." + password);
        if (officer == null) return null;

        Map<String, Object> result = new HashMap<>(officer);
        result.put("role", "finance_officer");
        return result;
    }
}
