package com.udiims.tests;

import com.udiims.exception.InvalidCredentialsException;
import com.udiims.exception.InvalidInputException;
import com.udiims.service.AuthService;
import com.udiims.service.SupabaseService;
import com.udiims.util.IdValidationUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Auth and ID Validation Tests")
class AuthAndValidationTest {

    @Nested
    @DisplayName("ID Validation Utils")
    class IdValidationUtilsTests {

        @Test
        @DisplayName("Accepts valid uppercase IDs")
        void acceptsValidUppercaseIds() {
            assertDoesNotThrow(() -> IdValidationUtils.validateFacultyId("F001"));
            assertDoesNotThrow(() -> IdValidationUtils.validateProjectId("PRJ12"));
            assertDoesNotThrow(() -> IdValidationUtils.validateInventoryId("INV7"));
            assertDoesNotThrow(() -> IdValidationUtils.validateStudentId("S001"));
        }

        @Test
        @DisplayName("Rejects malformed IDs")
        void rejectsMalformedIds() {
            assertEquals("Faculty ID must start with F followed by numbers only.",
                    assertThrows(InvalidInputException.class, () -> IdValidationUtils.validateFacultyId("f001")).getMessage());
            assertEquals("Project ID must start with PRJ followed by numbers only.",
                    assertThrows(InvalidInputException.class, () -> IdValidationUtils.validateProjectId("Prj12")).getMessage());
            assertEquals("Inventory ID must start with INV followed by numbers only.",
                    assertThrows(InvalidInputException.class, () -> IdValidationUtils.validateInventoryId("INVX")).getMessage());
            assertEquals("Student ID must start with S followed by numbers only.",
                    assertThrows(InvalidInputException.class, () -> IdValidationUtils.validateStudentId("")).getMessage());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AuthService Unit Tests")
    class AuthServiceUnitTests {

        @Mock
        SupabaseService supabase;

        @InjectMocks
        AuthService authService;

        @Test
        @DisplayName("Returns invalid ID when student record does not exist")
        void returnsInvalidIdForUnknownStudent() throws Exception {
            when(supabase.getSingle(eq("students"), contains("student_id=eq.S999"))).thenReturn(null);

            InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                    () -> authService.loginStudent("S999", "pass123"));

            assertEquals("Invalid ID", ex.getMessage());
        }

        @Test
        @DisplayName("Returns invalid password when student password does not match")
        void returnsInvalidPasswordForStudent() throws Exception {
            when(supabase.getSingle(eq("students"), contains("student_id=eq.S001"))).thenReturn(new HashMap<>(Map.of(
                    "student_id", "S001",
                    "student_name", "Aarav Sharma",
                    "program", "B.Tech CSE",
                    "semester", 4,
                    "sgpa", 8.1,
                    "cgpa", 8.0,
                    "backlog_count", 0,
                    "department_id", "CSE",
                    "password", "pass123"
            )));

            InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                    () -> authService.loginStudent("S001", "wrongpass"));

            assertEquals("Invalid password", ex.getMessage());
        }

        @Test
        @DisplayName("Logs in successfully when student credentials match")
        void logsInSuccessfullyForStudent() throws Exception {
            when(supabase.getSingle(eq("students"), contains("student_id=eq.S001"))).thenReturn(new HashMap<>(Map.of(
                    "student_id", "S001",
                    "student_name", "Aarav Sharma",
                    "program", "B.Tech CSE",
                    "semester", 4,
                    "sgpa", 8.1,
                    "cgpa", 8.0,
                    "backlog_count", 0,
                    "department_id", "CSE",
                    "password", "pass123"
            )));

            Map<String, Object> result = authService.loginStudent("S001", "pass123");

            assertEquals("student", result.get("role"));
            assertEquals(true, result.get("dashboard_access"));
            verify(supabase).patch(eq("students"), eq("student_id=eq.S001"), eq(Map.of("dashboard_access", true)));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("test")
    @DisplayName("Controller Integration Tests")
    class ControllerIntegrationTests {

        @Autowired
        MockMvc mockMvc;

        @MockBean
        SupabaseService supabase;

        @Test
        @DisplayName("Rejects malformed faculty ID on create")
        void rejectsMalformedFacultyId() throws Exception {
            mockMvc.perform(post("/api/secretary/faculty")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "faculty_id": "f001",
                                      "faculty_name": "Dr. Rao",
                                      "designation": "Professor"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Faculty ID must start with F followed by numbers only."));
        }

        @Test
        @DisplayName("Rejects malformed project ID on create")
        void rejectsMalformedProjectId() throws Exception {
            mockMvc.perform(post("/api/secretary/projects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "project_id": "prj101",
                                      "project_title": "AI Lab",
                                      "department_id": "CSE"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Project ID must start with PRJ followed by numbers only."));
        }

        @Test
        @DisplayName("Rejects malformed inventory ID on create")
        void rejectsMalformedInventoryId() throws Exception {
            mockMvc.perform(post("/api/secretary/inventory")
                            .param("departmentId", "CSE")
                            .param("isTechnical", "true")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "item_id": "inv22",
                                      "item_name": "Oscilloscope",
                                      "category": "Electronics",
                                      "quantity": 1
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Inventory ID must start with INV followed by numbers only."));
        }

        @Test
        @DisplayName("Rejects malformed student ID on login")
        void rejectsMalformedStudentIdOnLogin() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "role": "student",
                                      "id": "s001",
                                      "password": "pass123"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Student ID must start with S followed by numbers only."));
        }

        @Test
        @DisplayName("Returns invalid ID when login record does not exist")
        void returnsInvalidIdOnLogin() throws Exception {
            when(supabase.getSingle(eq("students"), contains("student_id=eq.S999"))).thenReturn(null);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "role": "student",
                                      "id": "S999",
                                      "password": "pass123"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid ID"));
        }

        @Test
        @DisplayName("Returns invalid password when login password is wrong")
        void returnsInvalidPasswordOnLogin() throws Exception {
            when(supabase.getSingle(eq("students"), contains("student_id=eq.S001"))).thenReturn(new HashMap<>(Map.of(
                    "student_id", "S001",
                    "student_name", "Aarav Sharma",
                    "program", "B.Tech CSE",
                    "semester", 4,
                    "sgpa", 8.1,
                    "cgpa", 8.0,
                    "backlog_count", 0,
                    "department_id", "CSE",
                    "password", "pass123"
            )));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "role": "student",
                                      "id": "S001",
                                      "password": "wrongpass"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid password"));
        }

        @Test
        @DisplayName("Returns success when login credentials are valid")
        void returnsSuccessOnValidLogin() throws Exception {
            when(supabase.getSingle(eq("students"), contains("student_id=eq.S001"))).thenReturn(new HashMap<>(Map.of(
                    "student_id", "S001",
                    "student_name", "Aarav Sharma",
                    "program", "B.Tech CSE",
                    "semester", 4,
                    "sgpa", 8.1,
                    "cgpa", 8.0,
                    "backlog_count", 0,
                    "department_id", "CSE",
                    "password", "pass123"
            )));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "role": "student",
                                      "id": "S001",
                                      "password": "pass123"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.student_id").value("S001"))
                    .andExpect(jsonPath("$.role").value("student"));
        }
    }
}
