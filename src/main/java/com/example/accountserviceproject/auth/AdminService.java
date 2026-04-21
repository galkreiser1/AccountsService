package com.example.accountserviceproject.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;

    private static final String GRANT = "GRANT";
    private static final String REMOVE = "REMOVE";
    private static final Set<String> VALID_OPERATIONS = Set.of(GRANT, REMOVE);

    private static final Set<String> VALID_ROLES = Set.of("USER", "ACCOUNTANT", "ADMINISTRATOR", "AUDITOR");

    public AdminService(UserRepository userRepository, SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.securityEventService = securityEventService;
    }

    public List<SignupResponse> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc()
                .stream()
                .map(user -> new SignupResponse(
                        user.getId(),
                        user.getName(),
                        user.getLastname(),
                        user.getEmail(),
                        user.getRoles()
                ))
                .toList();
    }

    public DeleteUserResponse deleteUser(String email, Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRoles().contains("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete an admin user");
        }

        userRepository.delete(user);

        logDeleteUser(email, authentication);

        return new DeleteUserResponse(user.getEmail(), "Deleted successfully!");
    }

    private void logDeleteUser(String email, Authentication authentication) {
        securityEventService.log(
                "DELETE_USER",
                authentication.getName(),
                email,
                "/api/admin/user"
        );
    }

    public SignupResponse changeRole(RoleChangeRequest request, Authentication authentication) {

        if (!VALID_ROLES.contains(request.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role!");
        }
        if (!VALID_OPERATIONS.contains(request.getOperation())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operation!");
        }

        User user = userRepository.findByEmailIgnoreCase(request.getUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getOperation().equals(GRANT)) {
            addRoleToUser(user, "ROLE_" + request.getRole());
        } else {
            removeRoleFromUser(user, "ROLE_" + request.getRole());
        }

        logRoleChange(request, authentication, user);

        return new SignupResponse(user.getId(), user.getName(), user.getLastname(), user.getEmail(), user.getRoles());
    }

    private void logRoleChange(RoleChangeRequest request, Authentication authentication, User user) {
        String object = request.getOperation().equals(GRANT)
                ? "Grant role " + request.getRole() + " to " + user.getEmail()
                : "Remove role " + request.getRole() + " from " + user.getEmail();

        securityEventService.log(
                request.getOperation() + "_ROLE",
                authentication.getName(),
                object,
                "/api/admin/user/role"
        );
    }

    private void addRoleToUser(User user, String role) {

        if (role.equals("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign administrator role!");
        }
        if (user.getRoles().contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already has the role!");
        }
        user.getRoles().add(role);
        userRepository.save(user);
    }

    private void removeRoleFromUser(User user, String role) {
        if (role.equals("ROLE_ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove administrator role!");
        }
        if (user.getRoles().size() == 1 && user.getRoles().contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have at least one role!");
        }
        if (!user.getRoles().contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have the role!");
        }
        user.getRoles().remove(role);
        userRepository.save(user);
    }
}
