package com.udiims.service;

import com.udiims.util.IdValidationUtils;
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
        IdValidationUtils.validateFacultyId(facultyId);
        Map<String, Object> existing = supabase.getSingle("faculty", "faculty_id=eq." + facultyId);
        if (existing != null) throw new RuntimeException("Faculty ID already exists.");

        body.putIfAbsent("active_status", true);
        List<Map<String, Object>> result = supabase.post("faculty", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateFaculty(String facultyId, Map<String, Object> body) throws Exception {
        IdValidationUtils.validateOptionalFacultyId(body.get("faculty_id"));
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

    public void deleteFaculty(String facultyId) throws Exception {
        Map<String, Object> existing = supabase.getSingle("faculty", "faculty_id=eq." + facultyId);
        if (existing == null) throw new RuntimeException("Faculty record not found.");
        supabase.delete("faculty", "faculty_id=eq." + facultyId);
    }

    // UC-07: Project Management

    public List<Map<String, Object>> getProjects(String departmentId) throws Exception {
        return supabase.getList("projects", "department_id=eq." + departmentId + "&order=project_title.asc");
    }

    public Map<String, Object> addProject(Map<String, Object> body) throws Exception {
        String projectId = (String) body.get("project_id");
        IdValidationUtils.validateProjectId(projectId);
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
        IdValidationUtils.validateOptionalProjectId(body.get("project_id"));
        List<Map<String, Object>> result = supabase.patch("projects", "project_id=eq." + projectId, body);
        return result.isEmpty() ? body : result.get(0);
    }

    // UC-08: Inventory Management

    public List<Map<String, Object>> getInventory(String departmentId) throws Exception {
        List<Map<String, Object>> items = supabase.getList("inventory",
                "assigned_department=eq." + departmentId + "&order=item_name.asc");
        return enrichInventoryWithLabIncharge(items);
    }

    public Map<String, Object> addInventoryItem(Map<String, Object> body, String departmentId, boolean isTechnical) throws Exception {
        Boolean isLabItem = (Boolean) body.getOrDefault("is_lab_item", false);
        if (Boolean.TRUE.equals(isLabItem) && !isTechnical) {
            throw new RuntimeException("Lab Inventory not applicable for this department.");
        }

        String itemId = (String) body.get("item_id");
        IdValidationUtils.validateInventoryId(itemId);
        Map<String, Object> existing = supabase.getSingle("inventory", "item_id=eq." + itemId);
        if (existing != null) throw new RuntimeException("Item ID already exists.");

        // Validate lab_incharge_id if provided
        String labInchargeId = (String) body.get("lab_incharge_id");
        if (labInchargeId != null && !labInchargeId.isBlank()) {
            if (!Boolean.TRUE.equals(isLabItem)) {
                throw new RuntimeException("lab_incharge_id is only applicable for lab items (is_lab_item must be true).");
            }
            Map<String, Object> faculty = supabase.getSingle("faculty",
                    "faculty_id=eq." + labInchargeId + "&active_status=eq.true");
            if (faculty == null) throw new RuntimeException("Lab incharge faculty not found or inactive: " + labInchargeId);
        }

        body.put("assigned_department", departmentId);
        body.putIfAbsent("condition", "new");
        List<Map<String, Object>> result = supabase.post("inventory", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateInventoryItem(String itemId, Map<String, Object> body) throws Exception {
        IdValidationUtils.validateOptionalInventoryId(body.get("item_id"));
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
        List<Map<String, Object>> courses = supabase.getList("courses", query + "&order=course_code.asc");
        return enrichCoursesWithFaculty(courses);
    }

    public Map<String, Object> addCourseOffering(Map<String, Object> body) throws Exception {
        String courseCode = (String) body.get("course_code");
        String semesterTerm = (String) body.get("semester_term");
        String deptId = (String) body.get("department_id");

        IdValidationUtils.validateCourseCode(courseCode, deptId);

        // Check duplicate
        Map<String, Object> existing = supabase.getSingle("courses",
                "course_code=eq." + courseCode + "&semester_term=eq." + semesterTerm + "&department_id=eq." + deptId);
        if (existing != null) throw new RuntimeException("Course already offered this term.");

        Object creditHours = body.get("credit_hours");
        if (creditHours instanceof Number n && n.intValue() <= 0) {
            throw new RuntimeException("Credit hours must be positive.");
        }

        // Validate faculty_id if provided
        String facultyId = (String) body.get("faculty_id");
        if (facultyId != null && !facultyId.isBlank()) {
            Map<String, Object> faculty = supabase.getSingle("faculty",
                    "faculty_id=eq." + facultyId + "&active_status=eq.true");
            if (faculty == null) throw new RuntimeException("Faculty not found or inactive: " + facultyId);
        }

        List<Map<String, Object>> result = supabase.post("courses", body);
        return result.isEmpty() ? body : result.get(0);
    }

    public Map<String, Object> updateCourseOffering(String courseCode, String semesterTerm, String deptId, Map<String, Object> body) throws Exception {
        if (body.containsKey("course_code")) {
            IdValidationUtils.validateCourseCode((String) body.get("course_code"), deptId);
        }
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

    // Helper: attach faculty name/department to each course
    private List<Map<String, Object>> enrichCoursesWithFaculty(List<Map<String, Object>> courses) throws Exception {
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

    // Helper: attach lab incharge faculty info for lab items
    private List<Map<String, Object>> enrichInventoryWithLabIncharge(List<Map<String, Object>> items) throws Exception {
        for (Map<String, Object> item : items) {
            Boolean isLabItem = (Boolean) item.getOrDefault("is_lab_item", false);
            String inchargeId = (String) item.get("lab_incharge_id");
            if (Boolean.TRUE.equals(isLabItem) && inchargeId != null && !inchargeId.isBlank()) {
                Map<String, Object> faculty = supabase.getSingle("faculty",
                        "faculty_id=eq." + inchargeId + "&select=faculty_id,faculty_name,designation,department_name");
                if (faculty != null) {
                    item.put("lab_incharge_info", faculty);
                }
            }
        }
        return items;
    }
}
