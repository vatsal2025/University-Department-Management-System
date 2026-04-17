package com.udiims.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for StudentService — covers UC-01 (profile), UC-04 (backlogs), UC-05 (fees).
 * Unit tests mock SupabaseService; the integration test uses full Spring context
 * with @MockBean to avoid hitting real Supabase.
 */
@DisplayName("StudentService Tests")
class StudentServiceTest {

    // ─────────────────────────────────────────────────────────────────────────
    // UNIT TESTS  (@ExtendWith so no Spring context is spun up)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit Tests")
    class UnitTests {

        @Mock  SupabaseService supabase;
        @Mock  GpaService      gpaService;
        @InjectMocks StudentService studentService;

        private Map<String, Object> sampleStudent;

        @BeforeEach
        void setUp() {
            sampleStudent = new HashMap<>();
            sampleStudent.put("student_id",   "S001");
            sampleStudent.put("student_name", "Aarav Sharma");
            sampleStudent.put("program",      "B.Tech CSE");
            sampleStudent.put("semester",     4);
            sampleStudent.put("sgpa",         8.5);
            sampleStudent.put("cgpa",         8.2);
            sampleStudent.put("backlog_count",1);
            sampleStudent.put("department_id","CSE");
        }

        @Test
        @DisplayName("Should return student profile for valid student ID")
        void shouldReturnStudentById() throws Exception {
            System.out.println("[TEST START] StudentServiceTest::shouldReturnStudentById");
            when(supabase.getSingle(eq("students"), contains("S001")))
                    .thenReturn(sampleStudent);

            Map<String, Object> result = studentService.getStudent("S001");

            assertNotNull(result, "Result must not be null");
            assertEquals("S001",         result.get("student_id"));
            assertEquals("Aarav Sharma", result.get("student_name"));
            assertEquals("B.Tech CSE",   result.get("program"));
            assertEquals(4,              result.get("semester"));
            System.out.println("[TEST PASS] StudentServiceTest::shouldReturnStudentById");
        }

        @Test
        @DisplayName("Should return null when student ID does not exist")
        void shouldReturnNullForInvalidStudentId() throws Exception {
            System.out.println("[TEST START] StudentServiceTest::shouldReturnNullForInvalidStudentId");
            when(supabase.getSingle(eq("students"), contains("S999")))
                    .thenReturn(null);

            Map<String, Object> result = studentService.getStudent("S999");

            assertNull(result, "Nonexistent student should return null");
            System.out.println("[TEST PASS] StudentServiceTest::shouldReturnNullForInvalidStudentId");
        }

        @Test
        @DisplayName("Should return fee records for existing student")
        void shouldReturnFeeStatusForStudent() throws Exception {
            System.out.println("[TEST START] StudentServiceTest::shouldReturnFeeStatusForStudent");
            Map<String, Object> feeRecord = Map.of(
                    "student_id",    "S001",
                    "semester_term", "Sem-1-2025",
                    "fee_status",    "paid",
                    "amount",        50000.0);
            when(supabase.getList(eq("financial_records"), contains("S001")))
                    .thenReturn(List.of(feeRecord));

            List<Map<String, Object>> fees = studentService.getFeeStatus("S001");

            assertNotNull(fees);
            assertFalse(fees.isEmpty(), "Fee list must not be empty");
            assertEquals("paid", fees.get(0).get("fee_status"));
            System.out.println("[TEST PASS] StudentServiceTest::shouldReturnFeeStatusForStudent");
        }

        @Test
        @DisplayName("Should throw RuntimeException when no fee records exist (edge case)")
        void shouldThrowWhenNoFeeRecords() throws Exception {
            System.out.println("[TEST START] StudentServiceTest::shouldThrowWhenNoFeeRecords");
            when(supabase.getList(eq("financial_records"), contains("S999")))
                    .thenReturn(Collections.emptyList());

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> studentService.getFeeStatus("S999"),
                    "Should throw RuntimeException for student with no fee records");
            assertTrue(ex.getMessage().contains("No fee records available"),
                    "Exception message must mention 'No fee records available'");
            System.out.println("[TEST PASS] StudentServiceTest::shouldThrowWhenNoFeeRecords");
        }

        @Test
        @DisplayName("Should return backlog tracking with empty list when no registrations")
        void shouldReturnEmptyBacklogTrackingWhenNoRecords() throws Exception {
            System.out.println("[TEST START] StudentServiceTest::shouldReturnEmptyBacklogTrackingWhenNoRecords");
            when(supabase.getList(eq("course_registrations"), contains("S010")))
                    .thenReturn(Collections.emptyList());

            Map<String, Object> result = studentService.getBacklogTracking("S010");

            assertNotNull(result);
            assertTrue(result.containsKey("message"), "Should have a message key");
            assertEquals(0, result.get("credits_earned"));
            System.out.println("[TEST PASS] StudentServiceTest::shouldReturnEmptyBacklogTrackingWhenNoRecords");
        }

        @Test
        @DisplayName("Should correctly count credits earned (excluding backlogs)")
        void shouldCountCreditsCorrectlyExcludingBacklogs() throws Exception {
            System.out.println("[TEST START] StudentServiceTest::shouldCountCreditsCorrectlyExcludingBacklogs");
            List<Map<String, Object>> regs = List.of(
                    Map.of("registration_status", "completed", "backlog_flag", false, "credit_hours", 4, "grade", "A"),
                    Map.of("registration_status", "completed", "backlog_flag", false, "credit_hours", 3, "grade", "B"),
                    Map.of("registration_status", "completed", "backlog_flag", true,  "credit_hours", 4, "grade", "F")
            );
            when(supabase.getList(eq("course_registrations"), contains("S001")))
                    .thenReturn(regs);

            Map<String, Object> result = studentService.getBacklogTracking("S001");

            assertEquals(7, result.get("credits_earned"),
                    "Only non-backlog completed credits (4+3=7) should be counted");
            @SuppressWarnings("unchecked")
            List<?> backlogs = (List<?>) result.get("backlogs");
            assertEquals(1, backlogs.size(), "One backlog expected");
            System.out.println("[TEST PASS] StudentServiceTest::shouldCountCreditsCorrectlyExcludingBacklogs");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTEGRATION TEST  (full Spring context, Supabase mocked)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Autowired  MockMvc     mockMvc;
        @MockBean   SupabaseService supabase;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUp() throws Exception {
            Map<String, Object> student = Map.of(
                    "student_id", "S001", "student_name", "Aarav Sharma",
                    "program", "B.Tech CSE", "semester", 4,
                    "sgpa", 8.5, "cgpa", 8.2, "backlog_count", 0, "department_id", "CSE");
            when(supabase.getSingle(eq("students"), contains("S001")))
                    .thenReturn(student);
            when(supabase.getSingle(eq("students"), contains("S999")))
                    .thenReturn(null);
            when(supabase.getList(eq("financial_records"), contains("S001")))
                    .thenReturn(List.of(Map.of(
                            "student_id", "S001", "semester_term", "Sem-1-2025",
                            "fee_status", "paid", "amount", 50000.0)));
        }

        @Test
        @DisplayName("GET /api/students/{id} returns 200 for valid student")
        void shouldReturn200ForValidStudentId() throws Exception {
            System.out.println("[TEST START] StudentServiceTest.IntegrationTests::shouldReturn200ForValidStudentId");
            mockMvc.perform(get("/api/students/S001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.student_id").value("S001"))
                    .andExpect(jsonPath("$.student_name").value("Aarav Sharma"))
                    .andExpect(jsonPath("$.program").value("B.Tech CSE"));
            System.out.println("[TEST PASS] StudentServiceTest.IntegrationTests::shouldReturn200ForValidStudentId");
        }

        @Test
        @DisplayName("GET /api/students/{id} returns 404 for non-existent student")
        void shouldReturn404ForInvalidStudentId() throws Exception {
            System.out.println("[TEST START] StudentServiceTest.IntegrationTests::shouldReturn404ForInvalidStudentId");
            mockMvc.perform(get("/api/students/S999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").exists());
            System.out.println("[TEST PASS] StudentServiceTest.IntegrationTests::shouldReturn404ForInvalidStudentId");
        }

        @Test
        @DisplayName("GET /api/students/{id}/fees returns 200 with fee data")
        void shouldReturn200ForStudentFees() throws Exception {
            System.out.println("[TEST START] StudentServiceTest.IntegrationTests::shouldReturn200ForStudentFees");
            mockMvc.perform(get("/api/students/S001/fees"))
                    .andExpect(status().isOk());
            System.out.println("[TEST PASS] StudentServiceTest.IntegrationTests::shouldReturn200ForStudentFees");
        }
    }
}
