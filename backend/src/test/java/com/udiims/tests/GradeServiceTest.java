package com.udiims.tests;

import com.udiims.service.GpaService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for Grade / GPA module — UC-03: GPA Calculation.
 * Covers calculateAndUpdateGpa: SGPA/CGPA calculation, fail grades, backlog counting.
 */
@DisplayName("Grade & GPA Service Tests")
class GradeServiceTest {

    // ─────────────────────────────────────────────────────────────────────────
    // UNIT TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit Tests")
    class UnitTests {

        @Mock  SupabaseService supabase;
        @InjectMocks GpaService gpaService;

        @Test
        @DisplayName("Should return zero GPA message when no completed registrations")
        void shouldReturnZeroGpaForNoRegistrations() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldReturnZeroGpaForNoRegistrations");
            when(supabase.getList(eq("course_registrations"), anyString()))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = gpaService.calculateAndUpdateGpa("S001");

            assertNotNull(result);
            assertEquals(0.0, result.get("sgpa"));
            assertEquals(0.0, result.get("cgpa"));
            assertTrue(result.containsKey("message"));
            System.out.println("[TEST PASS] GradeServiceTest::shouldReturnZeroGpaForNoRegistrations");
        }

        @Test
        @DisplayName("Should calculate correct SGPA for A and B grades")
        void shouldCalculateCorrectSgpaForGrades() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldCalculateCorrectSgpaForGrades");
            // A=10pts * 4cr + B=8pts * 4cr => (40+32)/8 = 9.0
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "A",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R002", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "B",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());

            Map<String, Object> result = gpaService.calculateAndUpdateGpa("S001");

            assertEquals(9.0, result.get("sgpa"), "SGPA should be 9.0 for A(10)+B(8)/2 courses");
            assertEquals(9.0, result.get("cgpa"), "CGPA equals SGPA for single semester");
            System.out.println("[TEST PASS] GradeServiceTest::shouldCalculateCorrectSgpaForGrades");
        }

        @Test
        @DisplayName("Should identify fail grades and increment backlog count")
        void shouldIncrementBacklogCountForFailGrades() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldIncrementBacklogCountForFailGrades");
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "A",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R002", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "F",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R003", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "NP",
                           "credit_hours", 2, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());

            Map<String, Object> result = gpaService.calculateAndUpdateGpa("S001");

            assertEquals(2, result.get("backlog_count"), "Should count 2 fail grades (F and NP)");
            // Patch should be called for each backlog flag update + final student update
            verify(supabase, atLeastOnce()).patch(eq("course_registrations"), anyString(),
                    argThat(m -> Boolean.TRUE.equals(m.get("backlog_flag"))));
            System.out.println("[TEST PASS] GradeServiceTest::shouldIncrementBacklogCountForFailGrades");
        }

        @Test
        @DisplayName("Should calculate CGPA across multiple semesters")
        void shouldCalculateCgpaAcrossMultipleSemesters() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldCalculateCgpaAcrossMultipleSemesters");
            // Sem1: A(10)*4cr = 40pts, 4cr => SGPA=10.0
            // Sem2: B(8)*4cr = 32pts, 4cr => SGPA=8.0
            // CGPA = (40+32)/8 = 9.0
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "A",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R002", "student_id", "S001",
                           "semester_term", "Sem-2-2025", "grade", "B",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());

            Map<String, Object> result = gpaService.calculateAndUpdateGpa("S001");

            assertEquals(9.0, result.get("cgpa"), "CGPA should average across both semesters");
            @SuppressWarnings("unchecked")
            Map<String, Double> semSgpa = (Map<String, Double>) result.get("semester_sgpa");
            assertEquals(10.0, semSgpa.get("Sem-1-2025"), "Sem-1 SGPA should be 10.0");
            assertEquals(8.0,  semSgpa.get("Sem-2-2025"), "Sem-2 SGPA should be 8.0");
            System.out.println("[TEST PASS] GradeServiceTest::shouldCalculateCgpaAcrossMultipleSemesters");
        }

        @Test
        @DisplayName("Should exclude non-graded statuses (I, R, W, AU) from calculation")
        void shouldExcludeIncompleteGradesFromCalculation() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldExcludeIncompleteGradesFromCalculation");
            // A(10)*4 + I(excluded) => sum=40, credits=4 => SGPA=10.0
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "A",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R002", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "I",
                           "credit_hours", 3, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());

            Map<String, Object> result = gpaService.calculateAndUpdateGpa("S001");

            assertEquals(10.0, result.get("sgpa"), "Incomplete (I) grade should not affect SGPA");
            System.out.println("[TEST PASS] GradeServiceTest::shouldExcludeIncompleteGradesFromCalculation");
        }

        @Test
        @DisplayName("Should write GPA back to student record via patch")
        void shouldWriteGpaToStudentRecord() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldWriteGpaToStudentRecord");
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "A-",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());

            gpaService.calculateAndUpdateGpa("S001");

            verify(supabase, atLeastOnce()).patch(
                    eq("students"),
                    contains("S001"),
                    argThat(m -> m.containsKey("sgpa") && m.containsKey("cgpa")));
            System.out.println("[TEST PASS] GradeServiceTest::shouldWriteGpaToStudentRecord");
        }

        @Test
        @DisplayName("Should treat AF and U grades as fail grades")
        void shouldTreatAfAndUAsFailGrades() throws Exception {
            System.out.println("[TEST START] GradeServiceTest::shouldTreatAfAndUAsFailGrades");
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "AF",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R002", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "U",
                           "credit_hours", 3, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());

            Map<String, Object> result = gpaService.calculateAndUpdateGpa("S001");

            assertEquals(2, result.get("backlog_count"), "AF and U should both be counted as backlogs");
            System.out.println("[TEST PASS] GradeServiceTest::shouldTreatAfAndUAsFailGrades");
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
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_id", "R001", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "A",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false),
                    Map.of("registration_id", "R002", "student_id", "S001",
                           "semester_term", "Sem-1-2025", "grade", "B",
                           "credit_hours", 4, "registration_status", "completed", "backlog_flag", false)
            );
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(regs);
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("POST /api/students/{id}/gpa/calculate returns 200 with sgpa and cgpa")
        void shouldCalculateGpaViaHttp() throws Exception {
            System.out.println("[TEST START] GradeServiceTest.IntegrationTests::shouldCalculateGpaViaHttp");
            mockMvc.perform(post("/api/students/S001/gpa/calculate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sgpa").exists())
                    .andExpect(jsonPath("$.cgpa").exists())
                    .andExpect(jsonPath("$.backlog_count").exists());
            System.out.println("[TEST PASS] GradeServiceTest.IntegrationTests::shouldCalculateGpaViaHttp");
        }

        @Test
        @DisplayName("POST /api/students/{id}/gpa/calculate returns 200 for student with no grades")
        void shouldReturn200WithMessageForNoGrades() throws Exception {
            System.out.println("[TEST START] GradeServiceTest.IntegrationTests::shouldReturn200WithMessageForNoGrades");
            when(supabase.getList(eq("course_registrations"), anyString()))
                    .thenReturn(Collections.emptyList());

            mockMvc.perform(post("/api/students/S999/gpa/calculate"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sgpa").value(0.0))
                    .andExpect(jsonPath("$.cgpa").value(0.0));
            System.out.println("[TEST PASS] GradeServiceTest.IntegrationTests::shouldReturn200WithMessageForNoGrades");
        }

        @Test
        @DisplayName("GET /api/students/{id}/gpa returns 200 with gpa fields")
        void shouldGetGpaViaHttp() throws Exception {
            System.out.println("[TEST START] GradeServiceTest.IntegrationTests::shouldGetGpaViaHttp");
            Map<String, Object> student = Map.of(
                    "student_id", "S001", "student_name", "Aarav Sharma",
                    "program", "B.Tech CSE", "semester", 4, "sgpa", 9.0, "cgpa", 8.8);
            when(supabase.getSingle(eq("students"), contains("S001"))).thenReturn(student);

            mockMvc.perform(get("/api/students/S001/gpa"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sgpa").exists())
                    .andExpect(jsonPath("$.cgpa").exists());
            System.out.println("[TEST PASS] GradeServiceTest.IntegrationTests::shouldGetGpaViaHttp");
        }

        @Test
        @DisplayName("GET /api/students/{id}/gpa returns 404 for non-existent student")
        void shouldReturn404ForUnknownStudentGpa() throws Exception {
            System.out.println("[TEST START] GradeServiceTest.IntegrationTests::shouldReturn404ForUnknownStudentGpa");
            when(supabase.getSingle(eq("students"), contains("S000"))).thenReturn(null);

            mockMvc.perform(get("/api/students/S000/gpa"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").exists());
            System.out.println("[TEST PASS] GradeServiceTest.IntegrationTests::shouldReturn404ForUnknownStudentGpa");
        }
    }
}
