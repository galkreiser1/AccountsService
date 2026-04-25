package com.example.accountserviceproject;


import com.example.accountserviceproject.payroll.PaymentRepository;
import com.example.accountserviceproject.audit.SecurityEvent;
import com.example.accountserviceproject.audit.SecurityEventRepository;
import com.example.accountserviceproject.user.User;
import com.example.accountserviceproject.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:securityeventsdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "server.error.include-message=always"
})
@AutoConfigureMockMvc
class SecurityEventsMockMvcTest {

    private static final User ADMIN = new User("John", "Doe", "johndoe@acme.com", "B3Fagws6zcBa");
    private static final User EMPLOYEE = new User("Jane", "Smith", "janesmith@acme.com", "bZPGqH7fTJWW");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @BeforeEach
    void cleanDatabase() {
        securityEventRepository.deleteAll();
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void accessDenied_isLoggedWhenUserCallsAccountantEndpoint() throws Exception {
        signupUser(ADMIN);
        signupUser(EMPLOYEE);

        addPayment(EMPLOYEE)
                .andExpect(status().isForbidden());

        SecurityEvent event = lastSecurityEvent();
        assertEquals("ACCESS_DENIED", event.getAction());
        assertEquals(EMPLOYEE.getEmail(), event.getSubject());
        assertEquals("/api/acct/payments", event.getObject());
        assertEquals("/api/acct/payments", event.getPath());
    }

    @Test
    void repeatedFailedLogin_locksUserAndLogsBruteForceEvents() throws Exception {
        signupUser(ADMIN);
        signupUser(EMPLOYEE);

        for (int i = 0; i < 6; i++) {
            mockMvc.perform(get("/api/empl/payment")
                            .with(httpBasic(EMPLOYEE.getEmail(), "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }

        User user = userRepository.findByEmailIgnoreCase(EMPLOYEE.getEmail()).orElseThrow();
        assertTrue(user.isAccountLocked());
        assertEquals(6, user.getFailedAttempts());

        List<SecurityEvent> events = securityEventRepository.findAllByOrderByIdAsc();
        List<String> lastActions = events.subList(events.size() - 3, events.size())
                .stream()
                .map(SecurityEvent::getAction)
                .toList();

        assertEquals(List.of("LOGIN_FAILED", "BRUTE_FORCE", "LOCK_USER"), lastActions);
    }

    @Test
    void adminCanUnlockLockedUserAndUnlockIsLogged() throws Exception {
        signupUser(ADMIN);
        signupUser(EMPLOYEE);

        changeUserAccess(ADMIN, EMPLOYEE.getEmail(), "LOCK")
                .andExpect(status().isOk());

        changeUserAccess(ADMIN, EMPLOYEE.getEmail(), "UNLOCK")
                .andExpect(status().isOk());

        User user = userRepository.findByEmailIgnoreCase(EMPLOYEE.getEmail()).orElseThrow();
        assertEquals(false, user.isAccountLocked());
        assertEquals(0, user.getFailedAttempts());

        SecurityEvent event = lastSecurityEvent();
        assertEquals("UNLOCK_USER", event.getAction());
        assertEquals(ADMIN.getEmail(), event.getSubject());
        assertEquals(EMPLOYEE.getEmail(), event.getObject());
        assertEquals("/api/admin/user/access", event.getPath());
    }

    @Test
    void securityEventsEndpoint_isAuditorOnly() throws Exception {
        signupUser(ADMIN);
        signupUser(EMPLOYEE);

        mockMvc.perform(get("/api/security/events")
                        .with(httpBasic(ADMIN.getEmail(), ADMIN.getPassword())))
                .andExpect(status().isForbidden());

        changeUserRole(ADMIN, EMPLOYEE.getEmail(), "AUDITOR", "GRANT")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/security/events")
                        .with(httpBasic(EMPLOYEE.getEmail(), EMPLOYEE.getPassword())))
                .andExpect(status().isOk());
    }

    private ResultActions signupUser(User user) throws Exception {
        return mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "%s",
                            "lastname": "%s",
                            "email": "%s",
                            "password": "%s"
                        }
                        """.formatted(user.getName(), user.getLastname(), user.getEmail(), user.getPassword())));
    }

    private ResultActions addPayment(User user) throws Exception {
        return mockMvc.perform(post("/api/acct/payments")
                .with(httpBasic(user.getEmail(), user.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        [
                            {
                                "employee": "%s",
                                "period": "06-2025",
                                "salary": 5000
                            }
                        ]
                        """.formatted(user.getEmail())));
    }

    private ResultActions changeUserAccess(User admin, String userEmail, String operation) throws Exception {
        return mockMvc.perform(put("/api/admin/user/access")
                .with(httpBasic(admin.getEmail(), admin.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "user": "%s",
                            "operation": "%s"
                        }
                        """.formatted(userEmail, operation)));
    }

    private ResultActions changeUserRole(User admin, String userEmail, String role, String operation) throws Exception {
        return mockMvc.perform(put("/api/admin/user/role")
                .with(httpBasic(admin.getEmail(), admin.getPassword()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "user": "%s",
                            "role": "%s",
                            "operation": "%s"
                        }
                        """.formatted(userEmail, role, operation)));
    }

    private SecurityEvent lastSecurityEvent() {
        List<SecurityEvent> events = securityEventRepository.findAllByOrderByIdAsc();
        return events.get(events.size() - 1);
    }
}
