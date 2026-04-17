package com.udiims.tests;

import com.udiims.service.FinanceService;
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
 * Tests for Fee module — UC-05 (student fee view) and UC-13 (Finance Officer fee management).
 * Covers getStudentFees, createFeeRecord, recordFeePayment (partial/installment), and overpayment guard.
 */
@DisplayName("Fee Service Tests")
class FeeServiceTest {

    // ─────────────────────────────────────────────────────────────────────────
    // UNIT TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Unit Tests")
    class UnitTests {

        @Mock  SupabaseService supabase;
        @InjectMocks FinanceService financeService;

        private Map<String, Object> sampleStudent;
        private Map<String, Object> sampleFeeStructure;

        @BeforeEach
        void setUp() {
            sampleStudent = Map.of(
                    "student_id", "S001", "student_name", "Aarav Sharma");
            sampleFeeStructure = Map.of(
                    "structure_id", "FS-S001-Sem-1-2025",
                    "student_id", "S001",
                    "semester_term", "Sem-1-2025",
                    "total_fee", 50000.0);
        }

        @Test
        @DisplayName("Should throw RuntimeException when student not found")
        void shouldThrowForUnknownStudentOnGetFees() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldThrowForUnknownStudentOnGetFees");
            when(supabase.getSingle(eq("students"), anyString())).thenReturn(null);

            assertThrows(RuntimeException.class,
                    () -> financeService.getStudentFees("S999"),
                    "Should throw when student does not exist");
            System.out.println("[TEST PASS] FeeServiceTest::shouldThrowForUnknownStudentOnGetFees");
        }

        @Test
        @DisplayName("Should return fee summary with total_fee and amount_paid")
        void shouldReturnFeeSummaryForStudent() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldReturnFeeSummaryForStudent");
            Map<String, Object> feeRecord = Map.of(
                    "student_id", "S001", "semester_term", "Sem-1-2025",
                    "record_type", "student-fee", "fee_status", "partial",
                    "amount", 50000.0);
            List<Map<String, Object>> payments = List.of(
                    Map.of("payment_id", "FP-001", "amount", 20000.0, "payment_date", "2025-01-10"));

            when(supabase.getSingle(eq("students"), anyString())).thenReturn(sampleStudent);
            when(supabase.getList(eq("financial_records"), anyString())).thenReturn(List.of(feeRecord));
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(sampleFeeStructure);
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(payments);

            Map<String, Object> result = financeService.getStudentFees("S001");

            assertNotNull(result);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fees = (List<Map<String, Object>>) result.get("fees");
            assertFalse(fees.isEmpty());
            assertEquals(50000.0, fees.get(0).get("total_fee"));
            assertEquals(20000.0, fees.get(0).get("amount_paid"));
            assertEquals(30000.0, fees.get(0).get("remaining_balance"));
            System.out.println("[TEST PASS] FeeServiceTest::shouldReturnFeeSummaryForStudent");
        }

        @Test
        @DisplayName("Should prevent overpayment — throw when payment exceeds total fee")
        void shouldPreventOverpayment() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldPreventOverpayment");
            List<Map<String, Object>> existingPayments = List.of(
                    Map.of("amount", 40000.0, "payment_date", "2025-01-10"));

            when(supabase.getSingle(eq("students"), anyString())).thenReturn(sampleStudent);
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(sampleFeeStructure);
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(existingPayments);

            Map<String, Object> body = new HashMap<>();
            body.put("student_id", "S001");
            body.put("semester_term", "Sem-1-2025");
            body.put("amount", 20000.0); // Would total 60000 > 50000

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> financeService.recordFeePayment(body));
            assertTrue(ex.getMessage().contains("Overpayment not allowed"),
                    "Error message should mention overpayment");
            System.out.println("[TEST PASS] FeeServiceTest::shouldPreventOverpayment");
        }

        @Test
        @DisplayName("Should allow partial payment and mark fee_status as partial")
        void shouldAllowPartialPayment() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldAllowPartialPayment");
            when(supabase.getSingle(eq("students"), anyString())).thenReturn(sampleStudent);
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(sampleFeeStructure);
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(Collections.emptyList());
            when(supabase.post(eq("fee_payments"), any())).thenReturn(List.of());
            when(supabase.getList(eq("financial_records"), anyString())).thenReturn(
                    List.of(Map.of("semester_term", "Sem-1-2025", "fee_status", "pending")));
            when(supabase.patch(eq("financial_records"), anyString(), any())).thenReturn(List.of());

            Map<String, Object> body = new HashMap<>();
            body.put("student_id", "S001");
            body.put("semester_term", "Sem-1-2025");
            body.put("amount", 25000.0);

            Map<String, Object> result = financeService.recordFeePayment(body);

            assertNotNull(result);
            assertEquals("partial", result.get("fee_status"), "Status should be partial after half payment");
            assertEquals(25000.0, result.get("amount_paid"));
            assertEquals(25000.0, result.get("remaining_balance"));
            System.out.println("[TEST PASS] FeeServiceTest::shouldAllowPartialPayment");
        }

        @Test
        @DisplayName("Should mark fee as paid when full amount is paid in one installment")
        void shouldMarkAsPaidWhenFullAmountPaid() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldMarkAsPaidWhenFullAmountPaid");
            when(supabase.getSingle(eq("students"), anyString())).thenReturn(sampleStudent);
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(sampleFeeStructure);
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(Collections.emptyList());
            when(supabase.post(eq("fee_payments"), any())).thenReturn(List.of());
            when(supabase.getList(eq("financial_records"), anyString())).thenReturn(
                    List.of(Map.of("semester_term", "Sem-1-2025", "fee_status", "pending")));
            when(supabase.patch(eq("financial_records"), anyString(), any())).thenReturn(List.of());

            Map<String, Object> body = new HashMap<>();
            body.put("student_id", "S001");
            body.put("semester_term", "Sem-1-2025");
            body.put("amount", 50000.0); // full amount

            Map<String, Object> result = financeService.recordFeePayment(body);

            assertEquals("paid", result.get("fee_status"), "Status should be paid after full payment");
            assertEquals(0.0, result.get("remaining_balance"));
            System.out.println("[TEST PASS] FeeServiceTest::shouldMarkAsPaidWhenFullAmountPaid");
        }

        @Test
        @DisplayName("Should throw when no fee structure exists for payment")
        void shouldThrowWhenNoFeeStructureExists() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldThrowWhenNoFeeStructureExists");
            when(supabase.getSingle(eq("students"), anyString())).thenReturn(sampleStudent);
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(null);

            Map<String, Object> body = new HashMap<>();
            body.put("student_id", "S001");
            body.put("semester_term", "Sem-1-2025");
            body.put("amount", 10000.0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> financeService.recordFeePayment(body));
            assertTrue(ex.getMessage().contains("No fee structure found"),
                    "Error should mention missing fee structure");
            System.out.println("[TEST PASS] FeeServiceTest::shouldThrowWhenNoFeeStructureExists");
        }

        @Test
        @DisplayName("Should reject duplicate fee record for same student and semester")
        void shouldRejectDuplicateFeeRecord() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldRejectDuplicateFeeRecord");
            Map<String, Object> existing = Map.of("student_id", "S001", "semester_term", "Sem-1-2025");
            when(supabase.getSingle(eq("financial_records"), anyString())).thenReturn(existing);

            Map<String, Object> body = new HashMap<>();
            body.put("student_id", "S001");
            body.put("semester_term", "Sem-1-2025");
            body.put("amount", 50000.0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> financeService.createFeeRecord(body));
            assertTrue(ex.getMessage().contains("already exists"),
                    "Should reject duplicate fee record creation");
            System.out.println("[TEST PASS] FeeServiceTest::shouldRejectDuplicateFeeRecord");
        }

        @Test
        @DisplayName("Should throw when payment amount is zero or negative")
        void shouldRejectZeroOrNegativePayment() throws Exception {
            System.out.println("[TEST START] FeeServiceTest::shouldRejectZeroOrNegativePayment");
            when(supabase.getSingle(eq("students"), anyString())).thenReturn(sampleStudent);
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(sampleFeeStructure);
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(Collections.emptyList());

            Map<String, Object> body = new HashMap<>();
            body.put("student_id", "S001");
            body.put("semester_term", "Sem-1-2025");
            body.put("amount", 0);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> financeService.recordFeePayment(body));
            assertTrue(ex.getMessage().contains("positive"),
                    "Should reject non-positive payment amount");
            System.out.println("[TEST PASS] FeeServiceTest::shouldRejectZeroOrNegativePayment");
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
            Map<String, Object> student = Map.of(
                    "student_id", "S001", "student_name", "Aarav Sharma");
            Map<String, Object> feeStructure = Map.of(
                    "structure_id", "FS-S001-Sem-1-2025",
                    "student_id", "S001", "semester_term", "Sem-1-2025", "total_fee", 50000.0);
            Map<String, Object> feeRecord = Map.of(
                    "student_id", "S001", "semester_term", "Sem-1-2025",
                    "record_type", "student-fee", "fee_status", "partial", "amount", 50000.0);

            when(supabase.getSingle(eq("students"), anyString())).thenReturn(student);
            when(supabase.getSingle(eq("fee_structures"), anyString())).thenReturn(feeStructure);
            when(supabase.getList(eq("financial_records"), anyString())).thenReturn(List.of(feeRecord));
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(Collections.emptyList());
            when(supabase.post(anyString(), any())).thenReturn(List.of());
            when(supabase.patch(anyString(), anyString(), any())).thenReturn(List.of());
        }

        @Test
        @DisplayName("GET /api/finance/fees/{studentId} returns 200 with fee data")
        void shouldGetStudentFeesViaHttp() throws Exception {
            System.out.println("[TEST START] FeeServiceTest.IntegrationTests::shouldGetStudentFeesViaHttp");
            mockMvc.perform(get("/api/finance/fees/S001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.student_id").value("S001"))
                    .andExpect(jsonPath("$.fees").isArray());
            System.out.println("[TEST PASS] FeeServiceTest.IntegrationTests::shouldGetStudentFeesViaHttp");
        }

        @Test
        @DisplayName("GET /api/finance/fees/{studentId} returns 404 for unknown student")
        void shouldReturn404ForUnknownStudent() throws Exception {
            System.out.println("[TEST START] FeeServiceTest.IntegrationTests::shouldReturn404ForUnknownStudent");
            when(supabase.getSingle(eq("students"), contains("S999"))).thenReturn(null);

            mockMvc.perform(get("/api/finance/fees/S999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").exists());
            System.out.println("[TEST PASS] FeeServiceTest.IntegrationTests::shouldReturn404ForUnknownStudent");
        }

        @Test
        @DisplayName("POST /api/finance/fees/{studentId}/payments records installment payment")
        void shouldRecordFeePaymentViaHttp() throws Exception {
            System.out.println("[TEST START] FeeServiceTest.IntegrationTests::shouldRecordFeePaymentViaHttp");
            String body = """
                    {"semester_term":"Sem-1-2025","amount":25000,"payment_method":"online"}""";

            mockMvc.perform(post("/api/finance/fees/S001/payments")
                            .contentType("application/json").content(body))
                    .andExpect(status().isOk());
            System.out.println("[TEST PASS] FeeServiceTest.IntegrationTests::shouldRecordFeePaymentViaHttp");
        }

        @Test
        @DisplayName("POST /api/finance/fees/{studentId}/payments returns 400 when overpayment")
        void shouldReturn400ForOverpaymentViaHttp() throws Exception {
            System.out.println("[TEST START] FeeServiceTest.IntegrationTests::shouldReturn400ForOverpaymentViaHttp");
            // Already paid 45000, trying to pay 20000 more on total 50000
            when(supabase.getList(eq("fee_payments"), anyString())).thenReturn(
                    List.of(Map.of("amount", 45000.0, "payment_date", "2025-01-10")));

            String body = """
                    {"semester_term":"Sem-1-2025","amount":20000}""";
            mockMvc.perform(post("/api/finance/fees/S001/payments")
                            .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value(
                            org.hamcrest.Matchers.containsString("Overpayment not allowed")));
            System.out.println("[TEST PASS] FeeServiceTest.IntegrationTests::shouldReturn400ForOverpaymentViaHttp");
        }

        @Test
        @DisplayName("GET /api/finance/fees/{studentId}/payments returns payment history")
        void shouldGetFeePaymentsViaHttp() throws Exception {
            System.out.println("[TEST START] FeeServiceTest.IntegrationTests::shouldGetFeePaymentsViaHttp");
            mockMvc.perform(get("/api/finance/fees/S001/payments")
                            .param("semesterTerm", "Sem-1-2025"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.student_id").value("S001"))
                    .andExpect(jsonPath("$.payments").isArray());
            System.out.println("[TEST PASS] FeeServiceTest.IntegrationTests::shouldGetFeePaymentsViaHttp");
        }
    }
}
