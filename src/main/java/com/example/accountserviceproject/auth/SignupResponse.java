package com.example.accountserviceproject.auth;

import java.util.List;
import java.util.Set;

public class SignupResponse {
    private final Long id;
    private final String name;
    private final String lastname;
    private final String email;
    private final List<String> roles;

    public SignupResponse(Long id, String name, String lastname, String email, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.roles = roles.stream().sorted().toList();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
