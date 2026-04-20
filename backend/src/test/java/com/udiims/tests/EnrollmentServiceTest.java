package com.udiims.tests;

import com.udiims.service.GpaService;
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
 * Tests for Enrollment module — UC-02: Course Registration.
 * Covers registerCourses, dropCourse, and getRegistrations.
 */
@DisplayName("Enrollment Service Tests")
class EnrollmentServiceTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit Tests")
    class UnitTests {

        @Mock  SupabaseService supabase;
        @Mock  GpaService      gpaService;
        @InjectMocks StudentService studentService;

        private Map<String, Object> sampleCourse;

        @BeforeEach
        void setUp() {
            sampleCourse = new HashMap<>();
            sampleCourse.put("course_code",   "CS101");
            sampleCourse.put("course_name",   "Data Structures");
            sampleCourse.put("credit_hours",  4);
            sampleCourse.put("semester_term", "Sem-1-2025");
            sampleCourse.put("department_id", "CSE");
        }

        @Test
        @DisplayName("Should register student in a valid course successfully")
        void shouldRegisterStudentInValidCourse() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest::shouldRegisterStudentInValidCourse");
            when(supabase.getSingle(eq("courses"), contains("CS101"))).thenReturn(sampleCourse);
            when(supabase.getSingle(eq("course_registrations"),
                    and(contains("S001"), and(contains("CS101"), contains("completed"))))).thenReturn(null);
            when(supabase.getSingle(eq("course_registrations"),
                    and(contains("S001"), and(contains("CS101"), contains("active"))))).thenReturn(null);

            Map<String, Object> result = studentService.registerCourses("S001", "Sem-1-2025", List.of("CS101"));

            @SuppressWarnings("unchecked")
            List<String> registered = (List<String>) result.get("registered");
            @SuppressWarnings("unchecked")
            List<String> failed     = (List<String>) result.get("failed");

            assertTrue(registered.contains("CS101"), "CS101 should be in registered list");
            assertTrue(failed.isEmpty(), "Failed list should be empty");
            System.out.println("[TEST PASS] EnrollmentServiceTest::shouldRegisterStudentInValidCourse");
        }

        @Test
        @DisplayName("Should not register course that student already completed")
        void shouldNotRegisterAlreadyCompletedCourse() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest::shouldNotRegisterAlreadyCompletedCourse");
            when(supabase.getSingle(eq("courses"), contains("CS101"))).thenReturn(sampleCourse);
            when(supabase.getSingle(eq("course_registrations"),
                    and(contains("S001"), and(contains("CS101"), contains("completed")))))
                    .thenReturn(Map.of("registration_status", "completed"));

            Map<String, Object> result = studentService.registerCourses("S001", "Sem-1-2025", List.of("CS101"));

            @SuppressWarnings("unchecked")
            List<String> failed = (List<String>) result.get("failed");
            assertTrue(failed.stream().anyMatch(f -> f.contains("CS101")), "CS101 should appear in failed list");
            assertTrue(failed.stream().anyMatch(f -> f.contains("already completed")),
                    "Failure reason should mention 'already completed'");
            System.out.println("[TEST PASS] EnrollmentServiceTest::shouldNotRegisterAlreadyCompletedCourse");
        }

        @Test
        @DisplayName("Should not register course already actively registered")
        void shouldNotRegisterDuplicateActiveRegistration() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest::shouldNotRegisterDuplicateActiveRegistration");
            when(supabase.getSingle(eq("courses"), contains("CS101"))).thenReturn(sampleCourse);
            when(supabase.getSingle(eq("course_registrations"),
                    and(contains("S001"), and(contains("CS101"), contains("completed"))))).thenReturn(null);
            when(supabase.getSingle(eq("course_registrations"),
                    and(contains("S001"), and(contains("CS101"), contains("active")))))
                    .thenReturn(Map.of("registration_status", "active"));

            Map<String, Object> result = studentService.registerCourses("S001", "Sem-1-2025", List.of("CS101"));

            @SuppressWarnings("unchecked")
            List<String> failed = (List<String>) result.get("failed");
            assertTrue(failed.stream().anyMatch(f -> f.contains("already registered")),
                    "Failure reason should mention 'already registered'");
            System.out.println("[TEST PASS] EnrollmentServiceTest::shouldNotRegisterDuplicateActiveRegistration");
        }

        @Test
        @DisplayName("Should fail when course code does not exist in offerings")
        void shouldFailForNonExistentCourseCode() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest::shouldFailForNonExistentCourseCode");
            when(supabase.getSingle(eq("courses"), contains("XX999"))).thenReturn(null);

            Map<String, Object> result = studentService.registerCourses("S001", "Sem-1-2025", List.of("XX999"));

            @SuppressWarnings("unchecked")
            List<String> failed = (List<String>) result.get("failed");
            assertTrue(failed.stream().anyMatch(f -> f.contains("XX999")));
            assertTrue(failed.stream().anyMatch(f -> f.contains("not found")));
            System.out.println("[TEST PASS] EnrollmentServiceTest::shouldFailForNonExistentCourseCode");
        }

        @Test
        @DisplayName("Should partially register when some courses fail and others succeed")
        void shouldPartiallyRegisterMixedCourses() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest::shouldPartiallyRegisterMixedCourses");
            when(supabase.getSingle(eq("courses"), contains("CS101"))).thenReturn(sampleCourse);
            when(supabase.getSingle(eq("courses"), contains("XX999"))).thenReturn(null);
            when(supabase.getSingle(eq("course_registrations"), anyString())).thenReturn(null);
            when(supabase.post(eq("course_registrations"), any())).thenReturn(List.of());

            Map<String, Object> result = studentService.registerCourses(
                    "S001", "Sem-1-2025", List.of("CS101", "XX999"));

            @SuppressWarnings("unchecked") List<String> reg  = (List<String>) result.get("registered");
            @SuppressWarnings("unchecked") List<String> fail = (List<String>) result.get("failed");
            assertTrue(reg.contains("CS101"));
            assertTrue(fail.stream().anyMatch(f -> f.contains("XX999")));
            System.out.println("[TEST PASS] EnrollmentServiceTest::shouldPartiallyRegisterMixedCourses");
        }

        @Test
        @DisplayName("Should drop a course (set status to dropped)")
        void shouldDropCourse() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest::shouldDropCourse");
            when(supabase.patch(eq("course_registrations"), anyString(), any())).thenReturn(List.of());

            assertDoesNotThrow(
                    () -> studentService.dropCourse("S001", "CS101", "Sem-1-2025"),
                    "dropCourse should not throw for valid inputs");

            verify(supabase, times(1)).patch(
                    eq("course_registrations"), anyString(),
                    argThat(m -> "dropped".equals(m.get("registration_status"))));
            System.out.println("[TEST PASS] EnrollmentServiceTest::shouldDropCourse");
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
            Map<String, Object> course = Map.of(
                    "course_code", "CS101", "course_name", "Data Structures",
                    "credit_hours", 4, "semester_term", "Sem-1-2025", "department_id", "CSE");
            when(supabase.getSingle(eq("courses"), anyString())).thenReturn(course);
            when(supabase.getSingle(eq("course_registrations"), anyString())).thenReturn(null);
            when(supabase.post(eq("course_registrations"), any())).thenReturn(List.of());
            when(supabase.getList(eq("course_registrations"), anyString())).thenReturn(List.of());
            when(supabase.patch(eq("course_registrations"), anyString(), any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("POST /api/students/{id}/registrations registers courses successfully")
        void shouldRegisterCoursesViaHttp() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest.IntegrationTests::shouldRegisterCoursesViaHttp");
            String body = """
                    {"semester_term":"Sem-1-2025","course_codes":["CS101"]}""";
            mockMvc.perform(post("/api/students/S001/registrations")
                            .contentType("application/json").content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").isArray());
            System.out.println("[TEST PASS] EnrollmentServiceTest.IntegrationTests::shouldRegisterCoursesViaHttp");
        }

        @Test
        @DisplayName("POST /api/students/{id}/registrations returns 400 when no course codes given")
        void shouldReturn400WhenNoCourseCodesGiven() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest.IntegrationTests::shouldReturn400WhenNoCourseCodesGiven");
            String body = """
                    {"semester_term":"Sem-1-2025","course_codes":[]}""";
            mockMvc.perform(post("/api/students/S001/registrations")
                            .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
            System.out.println("[TEST PASS] EnrollmentServiceTest.IntegrationTests::shouldReturn400WhenNoCourseCodesGiven");
        }

        @Test
        @DisplayName("GET /api/students/{id}/registrations returns 200 with list")
        void shouldGetRegistrationsViaHttp() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest.IntegrationTests::shouldGetRegistrationsViaHttp");
            mockMvc.perform(get("/api/students/S001/registrations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            System.out.println("[TEST PASS] EnrollmentServiceTest.IntegrationTests::shouldGetRegistrationsViaHttp");
        }

        @Test
        @DisplayName("DELETE /api/students/{id}/registrations/{code} drops course")
        void shouldDropCourseViaHttp() throws Exception {
            System.out.println("[TEST START] EnrollmentServiceTest.IntegrationTests::shouldDropCourseViaHttp");
            mockMvc.perform(delete("/api/students/S001/registrations/CS101")
                            .param("semesterTerm", "Sem-1-2025"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Course dropped successfully."));
            System.out.println("[TEST PASS] EnrollmentServiceTest.IntegrationTests::shouldDropCourseViaHttp");
        }
    }
}
