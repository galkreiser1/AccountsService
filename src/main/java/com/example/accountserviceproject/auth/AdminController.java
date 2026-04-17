package com.example.accountserviceproject.auth;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/user")
    public List<SignupResponse> getAllUsers(){
        return adminService.getAllUsers();
    }

    @DeleteMapping("/user/{email}")
    public DeleteUserResponse deleteUser(@PathVariable String email){
        return adminService.deleteUser(email);
    }




}
