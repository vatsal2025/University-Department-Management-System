package com.udiims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class FinanceService {

    @Autowired
    private SupabaseService supabase;

    // UC-11: Grant Management

    public List<Map<String, Object>> getGrants(String departmentId) throws Exception {
        String q = "record_type=eq.grant&order=transaction_date.desc";
        if (departmentId != null && !departmentId.isEmpty()) q += "&department_id=eq." + departmentId;
        return supabase.getList("financial_records", q);
    }

    public Map<String, Object> recordGrant(Map<String, Object> body) throws Exception {
        String grantId = (String) body.get("grant_id");
        String departmentId = (String) body.get("department_id");

        if (grantId == null || grantId.isBlank()) throw new RuntimeException("Grant ID is required.");

        // Check duplicate grant_id
        Map<String, Object> existing = supabase.getSingle("financial_records",
                "grant_id=eq." + grantId + "&record_type=eq.grant");
        if (existing != null) throw new RuntimeException("Grant ID already exists.");

        // Validate department
        Map<String, Object> dept = supabase.getSingle("departments", "department_id=eq." + departmentId);
        if (dept == null) throw new RuntimeException("Department not found.");

        Map<String, Object> record = new HashMap<>();
        record.put("record_id", "GR-" + grantId + "-" + Instant.now().toEpochMilli());
        record.put("record_type", "grant");
        record.put("amount", body.get("amount"));
        record.put("transaction_date", body.getOrDefault("transaction_date", Instant.now().toString()));
        record.put("department_id", departmentId);
        record.put("grant_id", grantId);

        List<Map<String, Object>> result = supabase.post("financial_records", record);
        return result.isEmpty() ? record : result.get(0);
    }

    // UC-12: Consultancy Fund Management

    public List<Map<String, Object>> getConsultancy(String departmentId) throws Exception {
        String q = "record_type=eq.consultancy&order=transaction_date.desc";
        if (departmentId != null && !departmentId.isEmpty()) q += "&department_id=eq." + departmentId;
        return supabase.getList("financial_records", q);
    }

    public Map<String, Object> recordConsultancy(Map<String, Object> body) throws Exception {
        String consultancyId = (String) body.get("consultancy_id");
        if (consultancyId == null || consultancyId.isBlank()) throw new RuntimeException("Consultancy ID is required.");

        Map<String, Object> existing = supabase.getSingle("financial_records",
                "consultancy_id=eq." + consultancyId + "&record_type=eq.consultancy");
        if (existing != null) throw new RuntimeException("Consultancy ID already exists.");

        Object amount = body.get("amount");
        if (amount instanceof Number n && n.doubleValue() <= 0) {
            throw new RuntimeException("Amount must be a positive value.");
        }

        Map<String, Object> record = new HashMap<>();
        record.put("record_id", "CON-" + consultancyId + "-" + Instant.now().toEpochMilli());
        record.put("record_type", "consultancy");
        record.put("amount", body.get("amount"));
        record.put("transaction_date", body.getOrDefault("transaction_date", Instant.now().toString()));
        record.put("department_id", body.get("department_id"));
        record.put("consultancy_id", consultancyId);
        record.put("description", body.getOrDefault("description", ""));

        List<Map<String, Object>> result = supabase.post("financial_records", record);
        return result.isEmpty() ? record : result.get(0);
    }

    // UC-13: Fee Collection & Modification

    public Map<String, Object> getStudentFees(String studentId) throws Exception {
        Map<String, Object> student = supabase.getSingle("students", "student_id=eq." + studentId + "&select=student_id,student_name");
        if (student == null) throw new RuntimeException("Student record not found.");

        List<Map<String, Object>> fees = supabase.getList("financial_records",
                "student_id=eq." + studentId + "&record_type=eq.student-fee&order=semester_term.asc");

        Map<String, Object> result = new HashMap<>(student);
        result.put("fees", fees);
        return result;
    }

    public Map<String, Object> updateFeeStatus(String studentId, String semesterTerm, String feeStatus) throws Exception {
        List<Map<String, Object>> fees = supabase.getList("financial_records",
                "student_id=eq." + studentId + "&record_type=eq.student-fee&semester_term=eq." + semesterTerm);

        if (fees.isEmpty()) throw new RuntimeException("No fee record found.");

        Map<String, Object> update = new HashMap<>();
        update.put("fee_status", feeStatus);
        update.put("fee_updated_timestamp", Instant.now().toString());

        String filter = "student_id=eq." + studentId + "&record_type=eq.student-fee&semester_term=eq." + semesterTerm;
        List<Map<String, Object>> result = supabase.patch("financial_records", filter, update);
        return result.isEmpty() ? update : result.get(0);
    }

    public Map<String, Object> createFeeRecord(Map<String, Object> body) throws Exception {
        String studentId = (String) body.get("student_id");
        String semesterTerm = (String) body.get("semester_term");

        Map<String, Object> existing = supabase.getSingle("financial_records",
                "student_id=eq." + studentId + "&record_type=eq.student-fee&semester_term=eq." + semesterTerm);
        if (existing != null) throw new RuntimeException("Fee record for this semester already exists.");

        Map<String, Object> record = new HashMap<>();
        record.put("record_id", "FEE-" + studentId + "-" + semesterTerm.replace(" ", "") + "-" + Instant.now().toEpochMilli());
        record.put("record_type", "student-fee");
        record.put("amount", body.get("amount"));
        record.put("transaction_date", Instant.now().toString());
        record.put("student_id", studentId);
        record.put("semester_term", semesterTerm);
        record.put("fee_status", body.getOrDefault("fee_status", "pending"));
        record.put("fee_updated_timestamp", Instant.now().toString());
        record.put("department_id", body.get("department_id"));

        List<Map<String, Object>> result = supabase.post("financial_records", record);
        return result.isEmpty() ? record : result.get(0);
    }

    // UC-14: Project Financial Management

    public List<Map<String, Object>> getProjectFinance(String departmentId) throws Exception {
        String q = "record_type=in.(project-budget,expense)&order=transaction_date.desc";
        if (departmentId != null && !departmentId.isEmpty()) q += "&department_id=eq." + departmentId;
        return supabase.getList("financial_records", q);
    }

    public Map<String, Object> recordProjectFinance(Map<String, Object> body) throws Exception {
        String projectId = (String) body.get("project_id");
        if (projectId == null || projectId.isBlank()) throw new RuntimeException("Project ID is required.");

        Map<String, Object> project = supabase.getSingle("projects", "project_id=eq." + projectId);
        if (project == null) throw new RuntimeException("Project not found.");

        String recordType = (String) body.getOrDefault("record_type", "expense");

        // Check budget exhaustion warning
        if ("expense".equals(recordType)) {
            double projectBudget = project.get("project_budget") instanceof Number n ? n.doubleValue() : 0;
            List<Map<String, Object>> existingExpenses = supabase.getList("financial_records",
                    "project_id=eq." + projectId + "&record_type=eq.expense");
            double totalSpent = existingExpenses.stream()
                    .mapToDouble(r -> r.get("amount") instanceof Number n ? n.doubleValue() : 0)
                    .sum();
            double newAmount = body.get("amount") instanceof Number n ? n.doubleValue() : 0;

            if ((totalSpent + newAmount) > projectBudget) {
                boolean confirmed = Boolean.TRUE.equals(body.get("confirmed"));
                if (!confirmed) {
                    throw new RuntimeException("BUDGET_WARNING: Project budget will be exhausted. Confirm entry?");
                }
            }
        }

        Map<String, Object> record = new HashMap<>();
        record.put("record_id", recordType.toUpperCase().substring(0, 3) + "-" + projectId + "-" + Instant.now().toEpochMilli());
        record.put("record_type", recordType);
        record.put("amount", body.get("amount"));
        record.put("transaction_date", body.getOrDefault("transaction_date", Instant.now().toString()));
        record.put("department_id", body.get("department_id"));
        record.put("project_id", projectId);
        record.put("description", body.getOrDefault("description", ""));

        try {
            List<Map<String, Object>> result = supabase.post("financial_records", record);
            return result.isEmpty() ? record : result.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Database write failed. Transaction rolled back.");
        }
    }

    public List<Map<String, Object>> getProjects(String departmentId) throws Exception {
        String q = (departmentId != null && !departmentId.isEmpty())
                ? "department_id=eq." + departmentId + "&order=project_title.asc"
                : "order=project_title.asc";
        return supabase.getList("projects", q);
    }
}
