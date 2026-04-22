package com.example.accountserviceproject;


import com.example.accountserviceproject.auth.AuthController;
import com.example.accountserviceproject.auth.ChangePasswordRequest;
import com.example.accountserviceproject.payroll.EmployeeController;
import com.example.accountserviceproject.payroll.PaymentRequest;
import com.example.accountserviceproject.payroll.PaymentService;
import com.example.accountserviceproject.payroll.SalaryResponse;
import com.example.accountserviceproject.auth.SignupRequest;
import com.example.accountserviceproject.user.SignupResponse;
import com.example.accountserviceproject.user.User;
import com.example.accountserviceproject.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "server.error.include-message=always"
})
class AuthControllerSpringTest {

    private static final String VALID_PASSWORD = "B3Fagws6zcBa";
    private static final String NEW_VALID_PASSWORD = "bZPGqH7fTJWW";

    @Autowired
    private AuthController authController;

    @Autowired
    private EmployeeController employeeController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PaymentService paymentService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void signup_savesUserAndReturnsPublicData() {
        SignupRequest request = signupRequest("JohnDoe@acme.com", VALID_PASSWORD);

        SignupResponse response = authController.signup(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getLastname()).isEqualTo("Doe");
        assertThat(response.getEmail()).isEqualTo("johndoe@acme.com");

        User savedUser = userRepository.findByEmailIgnoreCase("johndoe@acme.com").orElseThrow();
        assertThat(savedUser.getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(passwordEncoder.matches(VALID_PASSWORD, savedUser.getPassword())).isTrue();
    }

    @Test
    void signup_withDuplicateEmail_throwsBadRequest() {
        authController.signup(signupRequest("johndoe@acme.com", VALID_PASSWORD));

        SignupRequest secondRequest = signupRequest("JohnDoe@acme.com", "anotherValid1");

        assertThatThrownBy(() -> authController.signup(secondRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400 BAD_REQUEST \"User exist!\"");
    }

    @Test
    void signup_withShortPassword_throwsBadRequest() {
        SignupRequest request = signupRequest("johndoe@acme.com", "secret");

        assertThatThrownBy(() -> authController.signup(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Password length must be 12 chars minimum!");
    }

    @Test
    void signup_withBreachedPassword_throwsBadRequest() {
        SignupRequest request = signupRequest("johndoe@acme.com", "PasswordForJune");

        assertThatThrownBy(() -> authController.signup(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("The password is in the hacker's database!");
    }

    @Test
    void payment_returnsSalaryForRequestedPeriod() {
        authController.signup(signupRequest("johndoe@acme.com", VALID_PASSWORD));

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setEmployee("johndoe@acme.com");
        paymentRequest.setPeriod("01-2021");
        paymentRequest.setSalary(123456L);
        paymentService.addPayments(List.of(paymentRequest));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "JohnDoe@acme.com",
                VALID_PASSWORD
        );

        SalaryResponse response = (SalaryResponse) employeeController.makePayment(authentication, "01-2021");

        assertThat(response.getName()).isEqualTo("John");
        assertThat(response.getLastname()).isEqualTo("Doe");
        assertThat(response.getPeriod()).isEqualTo("January-2021");
        assertThat(response.getSalary()).isEqualTo("1234 dollar(s) 56 cent(s)");
    }

    @Test
    void changePassword_updatesStoredPassword() {
        authController.signup(signupRequest("johndoe@acme.com", VALID_PASSWORD));
        Authentication authentication = new UsernamePasswordAuthenticationToken("johndoe@acme.com", VALID_PASSWORD);
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNew_password(NEW_VALID_PASSWORD);

        AuthController.ChangePasswordResponse response = authController.changePassword(authentication, request);

        assertThat(response.email()).isEqualTo("johndoe@acme.com");
        assertThat(response.status()).isEqualTo("The password has been updated successfully");

        User savedUser = userRepository.findByEmailIgnoreCase("johndoe@acme.com").orElseThrow();
        assertThat(passwordEncoder.matches(VALID_PASSWORD, savedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(NEW_VALID_PASSWORD, savedUser.getPassword())).isTrue();
    }

    @Test
    void changePassword_withSamePassword_throwsBadRequest() {
        authController.signup(signupRequest("johndoe@acme.com", VALID_PASSWORD));
        Authentication authentication = new UsernamePasswordAuthenticationToken("johndoe@acme.com", VALID_PASSWORD);
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNew_password(VALID_PASSWORD);

        assertThatThrownBy(() -> authController.changePassword(authentication, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("The passwords must be different!");
    }

    private SignupRequest signupRequest(String email, String password) {
        SignupRequest request = new SignupRequest();
        request.setName("John");
        request.setLastname("Doe");
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}


