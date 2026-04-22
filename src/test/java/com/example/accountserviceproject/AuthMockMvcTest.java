package com.example.accountserviceproject;


import com.example.accountserviceproject.payroll.PaymentRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mockmvctestdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "server.error.include-message=always"
})
@AutoConfigureMockMvc
class AuthMockMvcTest {

    private static final User FIRST_USER = new User("John", "Doe", "johndoe@acme.com", "B3Fagws6zcBa");
    private static final User SECOND_USER = new User("Jane", "Smith", "janesmith@acme.com", "bZPGqH7fTJWW");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void signup_firstUserGetsAdministratorRole() throws Exception {
        signupUser(FIRST_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMINISTRATOR"));
    }

    @Test
    void signup_secondUserGetsUserRole() throws Exception {
        signupUser(FIRST_USER);

        signupUser(SECOND_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void signup_secondUserCantAccessAdminEndpoint() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);

        mockMvc.perform(get("/api/admin/user")
                        .with(httpBasic(SECOND_USER.getEmail(), SECOND_USER.getPassword())))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_canSeeUserList() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);

        mockMvc.perform(get("/api/admin/user")
                        .with(httpBasic(FIRST_USER.getEmail(), FIRST_USER.getPassword())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roles[0]").value("ROLE_ADMINISTRATOR"))
                .andExpect(jsonPath("$[1].roles[0]").value("ROLE_USER"));
    }

    @Test
    void adminEndpoint_canChangeUserRole() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);

        changeUserRole(FIRST_USER, SECOND_USER.getEmail(), "ACCOUNTANT", "GRANT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[?(@ == 'ROLE_ACCOUNTANT')]").exists())
                .andExpect(jsonPath("$.roles[?(@ == 'ROLE_USER')]").exists());
    }

    @Test
    void userEndpoint_cannotAddPaymentWithoutAccountantRole() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);

        addPayment(SECOND_USER)
                .andExpect(status().isForbidden());
    }

    @Test
    void accountantEndpoint_canAddPayment() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);
        changeUserRole(FIRST_USER, SECOND_USER.getEmail(), "ACCOUNTANT", "GRANT")
                .andExpect(status().isOk());

        addPayment(SECOND_USER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Added successfully!"));
    }

    @Test
    void accountantEndpoint_canAddPaymentAndEmployeeCanReadIt() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);
        changeUserRole(FIRST_USER, SECOND_USER.getEmail(), "ACCOUNTANT", "GRANT")
                .andExpect(status().isOk());
        addPayment(SECOND_USER)
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/empl/payment?period=06-2025")
                        .with(httpBasic(SECOND_USER.getEmail(), SECOND_USER.getPassword())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(SECOND_USER.getName()))
                .andExpect(jsonPath("$.lastname").value(SECOND_USER.getLastname()))
                .andExpect(jsonPath("$.period").value("June-2025"))
                .andExpect(jsonPath("$.salary").value("50 dollar(s) 0 cent(s)"));
    }

    @Test
    void adminEndpoint_canDeleteUser() throws Exception {
        signupUser(FIRST_USER);
        signupUser(SECOND_USER);

        mockMvc.perform(delete("/api/admin/user/{email}", SECOND_USER.getEmail())
                        .with(httpBasic(FIRST_USER.getEmail(), FIRST_USER.getPassword())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value(SECOND_USER.getEmail()))
                .andExpect(jsonPath("$.status").value("Deleted successfully!"));
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
}
