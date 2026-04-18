package com.example.accountserviceproject.auth;

import jakarta.validation.constraints.NotBlank;

public class RoleChangeRequest {

    @NotBlank(message = "User cannot be blank")
    private String user;

    @NotBlank(message = "Role cannot be blank")
    private String role;

    @NotBlank(message = "Operation cannot be blank")
    private String operation;

    public RoleChangeRequest() {}


    public String getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public String getOperation() {
        return operation;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }



}
