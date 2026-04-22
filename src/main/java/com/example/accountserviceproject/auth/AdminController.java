package com.example.accountserviceproject.auth;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
    public DeleteUserResponse deleteUser(@PathVariable String email, Authentication authentication){
        return adminService.deleteUser(email, authentication);
    }

    @PutMapping("/user/role")
    public SignupResponse updateUserRole(@Valid @RequestBody RoleChangeRequest request, Authentication authentication) {
        return adminService.changeRole(request, authentication);
    }

    @PutMapping("/user/access")
    public StatusResponse changeUserAccess(@Valid @RequestBody UserAccessRequest request, Authentication authentication) {
        return adminService.lockOperation(request.getUser(), request.getOperation(), authentication);
    }




}
