package com.example.accountserviceproject.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SignupRequest {

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Lastname must not be blank")
    private String lastname;

    @NotBlank(message = "Email must not be blank")
    @Pattern(regexp = ".+@acme\\.com$", message = "Email must end with @acme.com")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;

    public SignupRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
