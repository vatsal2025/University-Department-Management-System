package com.udiims.service;

import com.udiims.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private SupabaseService supabase;

    public Map<String, Object> loginStudent(String studentId, String password) throws Exception {
        Map<String, Object> studentRecord = supabase.getSingle("students",
                "student_id=eq." + studentId + "&select=student_id,student_name,program,semester,sgpa,cgpa,backlog_count,department_id,password");
        if (studentRecord == null) {
            throw new InvalidCredentialsException("Invalid ID");
        }
        if (!String.valueOf(studentRecord.get("password")).equals(password)) {
            throw new InvalidCredentialsException("Invalid password");
        }

        // Set dashboard_access = true
        Map<String, Object> update = new HashMap<>();
        update.put("dashboard_access", true);
        supabase.patch("students", "student_id=eq." + studentId, update);

        Map<String, Object> result = new HashMap<>(studentRecord);
        result.remove("password");
        result.put("role", "student");
        result.put("dashboard_access", true);
        return result;
    }

    public Map<String, Object> loginSecretary(String secretaryId, String password) throws Exception {
        Map<String, Object> secretary = supabase.getSingle("department_secretaries",
                "secretary_id=eq." + secretaryId);
        if (secretary == null) {
            throw new InvalidCredentialsException("Invalid ID");
        }
        if (!String.valueOf(secretary.get("password")).equals(password)) {
            throw new InvalidCredentialsException("Invalid password");
        }

        Map<String, Object> result = new HashMap<>(secretary);
        result.remove("password");
        result.put("role", "secretary");
        return result;
    }

    public Map<String, Object> loginFinanceOfficer(String officerId, String password) throws Exception {
        Map<String, Object> officer = supabase.getSingle("finance_officers",
                "officer_id=eq." + officerId);
        if (officer == null) {
            throw new InvalidCredentialsException("Invalid ID");
        }
        if (!String.valueOf(officer.get("password")).equals(password)) {
            throw new InvalidCredentialsException("Invalid password");
        }

        Map<String, Object> result = new HashMap<>(officer);
        result.remove("password");
        result.put("role", "finance_officer");
        return result;
    }
}
