package com.udiims.tests;

import com.udiims.service.SecretaryService;
import com.udiims.service.StudentService;
import com.udiims.service.SupabaseService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for Course module — covers UC-02 (available courses / offerings)
 * and UC-10 (secretary course offering management).
 * Tests both StudentService.getAvailableCourses and SecretaryService.getCourseOfferings.
 */
@DisplayName("Course Controller & Service Tests")
class CourseControllerTest {

    // ─────────────────────────────────────────────────────────────────────────
    // UNIT TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("StudentService — available courses (Unit)")
    class StudentCourseUnitTests {

        @Mock  SupabaseService supabase;
        @Mock  com.udiims.service.GpaService gpaService;
        @InjectMocks StudentService studentService;

        @Test
        @DisplayName("Should return courses for a given semester term")
        void shouldReturnCoursesForSemesterTerm() throws Exception {
            System.out.println("[TEST START] CourseControllerTest::shouldReturnCoursesForSemesterTerm");
            List<Map<String, Object>> mockCourses = List.of(
                    Map.of("course_code", "CS101", "course_name", "Data Structures", "credit_hours", 4,
                           "semester_term", "Sem-1-2025", "department_id", "CSE"),
                    Map.of("course_code", "CS102", "course_name", "Algorithms",       "credit_hours", 4,
                           "semester_term", "Sem-1-2025", "department_id", "CSE")
            );
            when(supabase.getList(eq("courses"), contains("Sem-1-2025")))
                    .thenReturn(mockCourses);

            List<Map<String, Object>> result = studentService.getAvailableCourses("Sem-1-2025", null);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("CS101", result.get(0).get("course_code"));
            System.out.println("[TEST PASS] CourseControllerTest::shouldReturnCoursesForSemesterTerm");
        }

        @Test
        @DisplayName("Should filter courses by department ID")
        void shouldFilterCoursesByDepartment() throws Exception {
            System.out.println("[TEST START] CourseControllerTest::shouldFilterCoursesByDepartment");
            List<Map<String, Object>> cseCourses = List.of(
                    Map.of("course_code", "CS101", "course_name", "Data Structures",
                           "semester_term", "Sem-1-2025", "department_id", "CSE", "credit_hours", 4));
            when(supabase.getList(eq("courses"), and(contains("Sem-1-2025"), contains("CSE"))))
                    .thenReturn(cseCourses);

            List<Map<String, Object>> result = studentService.getAvailableCourses("Sem-1-2025", "CSE");

            assertEquals(1, result.size());
            assertEquals("CSE", result.get(0).get("department_id"));
            System.out.println("[TEST PASS] CourseControllerTest::shouldFilterCoursesByDepartment");
        }

        @Test
        @DisplayName("Should attach faculty_info when faculty is linked to course")
        void shouldAttachFacultyInfoToCourse() throws Exception {
            System.out.println("[TEST START] CourseControllerTest::shouldAttachFacultyInfoToCourse");
            Map<String, Object> course = new HashMap<>(Map.of(
                    "course_code", "CS101", "course_name", "Data Structures",
                    "semester_term", "Sem-1-2025", "department_id", "CSE",
                    "credit_hours", 4, "faculty_id", "F001"));
            Map<String, Object> faculty = Map.of(
                    "faculty_id", "F001", "faculty_name", "Dr. Ramesh Kumar",
                    "designation", "Professor", "department_name", "Computer Science & Engineering");

            when(supabase.getList(eq("courses"), anyString())).thenReturn(List.of(course));
            when(supabase.getSingle(eq("faculty"), contains("F001"))).thenReturn(faculty);

            List<Map<String, Object>> result = studentService.getAvailableCourses("Sem-1-2025", "CSE");

            assertTrue(result.get(0).containsKey("faculty_info"),
                    "Faculty info should be attached to course");
            @SuppressWarnings("unchecked")
            Map<String, Object> info = (Map<String, Object>) result.get(0).get("faculty_info");
            assertEquals("Dr. Ramesh Kumar", info.get("faculty_name"));
            System.out.println("[TEST PASS] CourseControllerTest::shouldAttachFacultyInfoToCourse");
        }

        @Test
        @DisplayName("Should return empty list when no courses exist for semester")
        void shouldReturnEmptyListForUnknownSemester() throws Exception {
            System.out.println("[TEST START] CourseControllerTest::shouldReturnEmptyListForUnknownSemester");
            when(supabase.getList(eq("courses"), contains("Sem-9-2099")))
                    .thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = studentService.getAvailableCourses("Sem-9-2099", null);

            assertNotNull(result);
            assertTrue(result.isEmpty(), "Result should be empty for non-existent semester");
            System.out.println("[TEST PASS] CourseControllerTest::shouldReturnEmptyListForUnknownSemester");
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("SecretaryService — course offerings (Unit)")
    class SecretaryCourseUnitTests {

        @Mock  SupabaseService supabase;
        @InjectMocks SecretaryService secretaryService;

        @Test
        @DisplayName("Should throw error for duplicate course offering")
        void shouldRejectDuplicateCourseOffering() throws Exception {
            System.out.println("[TEST START] CourseControllerTest::shouldRejectDuplicateCourseOffering");
            Map<String, Object> existing = Map.of("course_code", "CS101");
            when(supabase.getSingle(eq("courses"), anyString())).thenReturn(existing);

            Map<String, Object> body = new HashMap<>();
            body.put("course_code", "CS101");
            body.put("semester_term", "Sem-1-2025");
            body.put("department_id", "CSE");
            body.put("course_name", "Data Structures");
            body.put("credit_hours", 4);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> secretaryService.addCourseOffering(body));
            assertEquals("Course already offered this term.", ex.getMessage());
            System.out.println("[TEST PASS] CourseControllerTest::shouldRejectDuplicateCourseOffering");
        }

        @Test
        @DisplayName("Should throw error when credit hours is zero or negative")
        void shouldRejectInvalidCreditHours() throws Exception {
            System.out.println("[TEST START] CourseControllerTest::shouldRejectInvalidCreditHours");
            when(supabase.getSingle(eq("courses"), anyString())).thenReturn(null);

            Map<String, Object> body = new HashMap<>();
            body.put("course_code", "CS999");
            body.put("semester_term", "Sem-1-2025");
            body.put("department_id", "CSE");
            body.put("course_name", "Invalid Course");
            body.put("credit_hours", 0);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> secretaryService.addCourseOffering(body));
            assertEquals("Credit hours must be positive.", ex.getMessage());
            System.out.println("[TEST PASS] CourseControllerTest::shouldRejectInvalidCreditHours");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTEGRATION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired MockMvc mockMvc;
        @MockBean  SupabaseService supabase;

        @BeforeEach
        void setUp() throws Exception {
            List<Map<String, Object>> courses = List.of(
                    Map.of("course_code", "CS101", "course_name", "Data Structures",
                           "credit_hours", 4, "semester_term", "Sem-1-2025", "department_id", "CSE"));
            when(supabase.getList(eq("courses"), anyString())).thenReturn(courses);
            when(supabase.getSingle(eq("faculty"), anyString())).thenReturn(null);
        }

        @Test
        @DisplayName("GET /api/students/courses/available returns 200 with course list")
        void shouldReturnCoursesViaHttp() throws Exception {
            System.out.println("[TEST START] CourseControllerTest.IntegrationTests::shouldReturnCoursesViaHttp");
            mockMvc.perform(get("/api/students/courses/available")
                            .param("semesterTerm", "Sem-1-2025")
                            .param("departmentId", "CSE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].course_code").value("CS101"));
            System.out.println("[TEST PASS] CourseControllerTest.IntegrationTests::shouldReturnCoursesViaHttp");
        }

        @Test
        @DisplayName("GET /api/students/courses/available returns 200 with empty array for unknown semester")
        void shouldReturnEmptyArrayForUnknownSemester() throws Exception {
            System.out.println("[TEST START] CourseControllerTest.IntegrationTests::shouldReturnEmptyArrayForUnknownSemester");
            when(supabase.getList(eq("courses"), contains("Sem-9-2099")))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/students/courses/available")
                            .param("semesterTerm", "Sem-9-2099"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
            System.out.println("[TEST PASS] CourseControllerTest.IntegrationTests::shouldReturnEmptyArrayForUnknownSemester");
        }

        @Test
        @DisplayName("POST /api/secretary/courses returns 400 for duplicate offering")
        void shouldReturn400ForDuplicateCourseOffering() throws Exception {
            System.out.println("[TEST START] CourseControllerTest.IntegrationTests::shouldReturn400ForDuplicateCourseOffering");
            when(supabase.getSingle(eq("courses"), anyString()))
                    .thenReturn(Map.of("course_code", "CS101"));

            String body = """
                {
                  "course_code": "CS101",
                  "course_name": "Data Structures",
                  "credit_hours": 4,
                  "semester_term": "Sem-1-2025",
                  "department_id": "CSE"
                }""";

            mockMvc.perform(post("/api/secretary/courses")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Course already offered this term."));
            System.out.println("[TEST PASS] CourseControllerTest.IntegrationTests::shouldReturn400ForDuplicateCourseOffering");
        }
    }
}
