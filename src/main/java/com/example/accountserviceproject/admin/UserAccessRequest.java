package com.example.accountserviceproject.admin;


import com.example.accountserviceproject.user.User;
import jakarta.validation.constraints.NotBlank;

public class UserAccessRequest {

    @NotBlank(message = "User cannot be blank")
    private String user;

    @NotBlank(message = "Operation cannot be blank")
    private String operation;

    public UserAccessRequest() {}

    public String getUser() {
        return user;
    }

    public String getOperation() {
        return operation;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
