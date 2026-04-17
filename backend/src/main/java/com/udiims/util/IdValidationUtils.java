package com.udiims.util;

import com.udiims.exception.InvalidInputException;

import java.util.Map;
import java.util.regex.Pattern;

public final class IdValidationUtils {

    private static final Pattern FACULTY_ID_PATTERN = Pattern.compile("^F\\d+$");
    private static final Pattern PROJECT_ID_PATTERN = Pattern.compile("^PRJ\\d+$");
    private static final Pattern INVENTORY_ID_PATTERN = Pattern.compile("^INV\\d+$");
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^S\\d+$");

    private static final Map<String, String> MESSAGES = Map.of(
            "faculty_id", "Faculty ID must start with F followed by numbers only.",
            "project_id", "Project ID must start with PRJ followed by numbers only.",
            "item_id", "Inventory ID must start with INV followed by numbers only.",
            "student_id", "Student ID must start with S followed by numbers only."
    );

    private IdValidationUtils() {
    }

    public static void validateFacultyId(String facultyId) {
        validateRequired("faculty_id", facultyId, FACULTY_ID_PATTERN);
    }

    public static void validateProjectId(String projectId) {
        validateRequired("project_id", projectId, PROJECT_ID_PATTERN);
    }

    public static void validateInventoryId(String itemId) {
        validateRequired("item_id", itemId, INVENTORY_ID_PATTERN);
    }

    public static void validateStudentId(String studentId) {
        validateRequired("student_id", studentId, STUDENT_ID_PATTERN);
    }

    public static void validateOptionalFacultyId(Object facultyId) {
        validateOptional("faculty_id", facultyId, FACULTY_ID_PATTERN);
    }

    public static void validateOptionalProjectId(Object projectId) {
        validateOptional("project_id", projectId, PROJECT_ID_PATTERN);
    }

    public static void validateOptionalInventoryId(Object itemId) {
        validateOptional("item_id", itemId, INVENTORY_ID_PATTERN);
    }

    private static void validateRequired(String key, String value, Pattern pattern) {
        if (value == null || value.isBlank() || !pattern.matcher(value).matches()) {
            throw new InvalidInputException(MESSAGES.get(key));
        }
    }

    private static void validateOptional(String key, Object value, Pattern pattern) {
        if (value == null) return;
        if (!(value instanceof String stringValue) || stringValue.isBlank() || !pattern.matcher(stringValue).matches()) {
            throw new InvalidInputException(MESSAGES.get(key));
        }
    }

    public static void validateCourseCode(String courseCode, String departmentId) {
        if ("CSE".equalsIgnoreCase(departmentId)) {
            Pattern csCoursePattern = Pattern.compile("^CS\\d+$");
            if (courseCode == null || courseCode.isBlank() || !csCoursePattern.matcher(courseCode).matches()) {
                throw new InvalidInputException("Course Code for CSE department must start with CS followed by numbers.");
            }
        }
    }
}
