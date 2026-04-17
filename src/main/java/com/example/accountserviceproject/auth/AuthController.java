package com.example.accountserviceproject.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;

    public record ChangePasswordResponse(String email, String status) {}

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordPolicyService passwordPolicyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicyService = passwordPolicyService;

    }

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {

        String email = request.getEmail().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setLastname(request.getLastname());
        user.setEmail(email);

        passwordPolicyService.validatePassword(request.getPassword());

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if(userRepository.count() == 0){
            user.getRoles().add("ROLE_ADMINISTRATOR");
        }
        else{
            user.getRoles().add("ROLE_USER");
        }

        userRepository.save(user);

        return new SignupResponse(user.getId(), user.getName(), user.getLastname(), user.getEmail(), user.getRoles());


    }

    @PostMapping("/changepass")
    public ChangePasswordResponse changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (passwordEncoder.matches(request.getNew_password(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The passwords must be different!"
            );
        }

        passwordPolicyService.validatePassword(request.getNew_password());

        user.setPassword(passwordEncoder.encode(request.getNew_password()));
        userRepository.save(user);

        return new ChangePasswordResponse(
                user.getEmail(),
                "The password has been updated successfully"
        );
    }
}
