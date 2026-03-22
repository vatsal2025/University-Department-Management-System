package com.udiims.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecretaryService {

    @Autowired
    private SupabaseService supabase;

    // UC-06: Faculty Management

    public List<Map<String, Object>> getFaculty(String departmentId) throws Exception {
        return supabase.getList("faculty", "department_id=eq." + departmentId + "&order=faculty_name.asc");
    }

    public Map<String, Object> addFaculty(Map<String, Object> body) throws Exception {
        String facultyId = (String) body.get("faculty_id");
        Map<String, Object> existing = supabase.getSingle("faculty", "faculty_id=eq." + facultyId);
        if (existing != null) throw new RuntimeException("Faculty ID already exists.");

        body.putIfAbsent("active_status", true);
        List<Map<String, Object>> result = supabase.post("faculty", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateFaculty(String facultyId, Map<String, Object> body) throws Exception {
        List<Map<String, Object>> result = supabase.patch("faculty", "faculty_id=eq." + facultyId, body);
        return result.isEmpty() ? body : result.get(0);
    }

    public void deactivateFaculty(String facultyId) throws Exception {
        // Check for active projects
        List<Map<String, Object>> activeProjects = supabase.getList("projects",
                "faculty_id=eq." + facultyId + "&project_status=eq.active");
        if (!activeProjects.isEmpty()) {
            throw new RuntimeException("Faculty has active project associations. Confirm removal?");
        }
        supabase.patch("faculty", "faculty_id=eq." + facultyId, Map.of("active_status", false));
    }

    // UC-07: Project Management

    public List<Map<String, Object>> getProjects(String departmentId) throws Exception {
        return supabase.getList("projects", "department_id=eq." + departmentId + "&order=project_title.asc");
    }

    public Map<String, Object> addProject(Map<String, Object> body) throws Exception {
        String projectId = (String) body.get("project_id");
        Map<String, Object> existing = supabase.getSingle("projects", "project_id=eq." + projectId);
        if (existing != null) throw new RuntimeException("Project ID already exists.");

        String facultyId = (String) body.get("faculty_id");
        if (facultyId != null) {
            Map<String, Object> faculty = supabase.getSingle("faculty", "faculty_id=eq." + facultyId + "&active_status=eq.true");
            if (faculty == null) throw new RuntimeException("Invalid Faculty ID.");
        }

        body.putIfAbsent("project_status", "active");
        List<Map<String, Object>> result = supabase.post("projects", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateProject(String projectId, Map<String, Object> body) throws Exception {
        List<Map<String, Object>> result = supabase.patch("projects", "project_id=eq." + projectId, body);
        return result.isEmpty() ? body : result.get(0);
    }

    // UC-08: Inventory Management

    public List<Map<String, Object>> getInventory(String departmentId) throws Exception {
        return supabase.getList("inventory", "assigned_department=eq." + departmentId + "&order=item_name.asc");
    }

    public Map<String, Object> addInventoryItem(Map<String, Object> body, String departmentId, boolean isTechnical) throws Exception {
        Boolean isLabItem = (Boolean) body.getOrDefault("is_lab_item", false);
        if (Boolean.TRUE.equals(isLabItem) && !isTechnical) {
            throw new RuntimeException("Lab Inventory not applicable for this department.");
        }

        String itemId = (String) body.get("item_id");
        Map<String, Object> existing = supabase.getSingle("inventory", "item_id=eq." + itemId);
        if (existing != null) throw new RuntimeException("Item ID already exists.");

        body.put("assigned_department", departmentId);
        body.putIfAbsent("condition", "new");
        List<Map<String, Object>> result = supabase.post("inventory", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateInventoryItem(String itemId, Map<String, Object> body) throws Exception {
        List<Map<String, Object>> result = supabase.patch("inventory", "item_id=eq." + itemId, body);
        return result.isEmpty() ? body : result.get(0);
    }

    public void disposeInventoryItem(String itemId) throws Exception {
        supabase.patch("inventory", "item_id=eq." + itemId, Map.of("condition", "disposed"));
    }

    // UC-09: Department Accounts (read-only view)
    public Map<String, Object> getDepartmentAccounts(String departmentId) throws Exception {
        List<Map<String, Object>> records = supabase.getList("financial_records",
                "department_id=eq." + departmentId + "&order=transaction_date.desc");

        if (records.isEmpty()) {
            return Map.of("message", "No financial data available for this department.",
                    "budget_allocation", 0.0, "total_expenses", 0.0, "remaining_balance", 0.0);
        }

        double opening = 0, grants = 0, income = 0, expenses = 0;
        for (Map<String, Object> r : records) {
            String type = (String) r.get("record_type");
            double amount = r.get("amount") instanceof Number n ? n.doubleValue() : 0;
            switch (type) {
                case "grant" -> grants += amount;
                case "consultancy" -> income += amount;
                case "expense" -> expenses += amount;
                case "project-budget" -> expenses += amount;
            }
        }

        double remaining = (opening + grants + income) - expenses;
        Map<String, Object> result = new HashMap<>();
        result.put("department_id", departmentId);
        result.put("budget_allocation", grants);
        result.put("total_income", income);
        result.put("total_expenses", expenses);
        result.put("remaining_balance", remaining);
        result.put("records", records);
        if (remaining < 0) result.put("warning", "Warning: Funds Remaining is negative. Budget review required.");
        return result;
    }

    // UC-10: Course Offering Management

    public List<Map<String, Object>> getCourseOfferings(String departmentId, String semesterTerm) throws Exception {
        String query = "department_id=eq." + departmentId;
        if (semesterTerm != null && !semesterTerm.isEmpty()) {
            query += "&semester_term=eq." + semesterTerm;
        }
        return supabase.getList("courses", query + "&order=course_code.asc");
    }

    public Map<String, Object> addCourseOffering(Map<String, Object> body) throws Exception {
        String courseCode = (String) body.get("course_code");
        String semesterTerm = (String) body.get("semester_term");
        String deptId = (String) body.get("department_id");

        // Check duplicate
        Map<String, Object> existing = supabase.getSingle("courses",
                "course_code=eq." + courseCode + "&semester_term=eq." + semesterTerm + "&department_id=eq." + deptId);
        if (existing != null) throw new RuntimeException("Course already offered this term.");

        Object creditHours = body.get("credit_hours");
        if (creditHours instanceof Number n && n.intValue() <= 0) {
            throw new RuntimeException("Credit hours must be positive.");
        }

        List<Map<String, Object>> result = supabase.post("courses", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateCourseOffering(String courseCode, String semesterTerm, String deptId, Map<String, Object> body) throws Exception {
        String filter = "course_code=eq." + courseCode + "&semester_term=eq." + semesterTerm + "&department_id=eq." + deptId;
        List<Map<String, Object>> result = supabase.patch("courses", filter, body);
        return result.isEmpty() ? body : result.get(0);
    }

    public void removeCourseOffering(String courseCode, String semesterTerm, String deptId) throws Exception {
        // Check if students enrolled
        List<Map<String, Object>> enrolled = supabase.getList("course_registrations",
                "course_code=eq." + courseCode + "&semester_term=eq." + semesterTerm + "&registration_status=eq.active");
        if (!enrolled.isEmpty()) {
            throw new RuntimeException("Students enrolled in this course. Confirm removal?");
        }
        supabase.delete("courses", "course_code=eq." + courseCode + "&semester_term=eq." + semesterTerm + "&department_id=eq." + deptId);
    }

    // Get department info
    public Map<String, Object> getDepartment(String departmentId) throws Exception {
        return supabase.getSingle("departments", "department_id=eq." + departmentId);
    }
}
