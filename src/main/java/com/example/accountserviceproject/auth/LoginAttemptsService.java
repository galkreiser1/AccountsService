package com.example.accountserviceproject.auth;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptsService {

    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;

    public LoginAttemptsService(UserRepository userRepository, SecurityEventService securityEventService) {
        this.userRepository = userRepository;
        this.securityEventService = securityEventService;
    }

    public void loginFailed(String email, String path) {
        securityEventService.log("LOGIN_FAILED", email, path, path);

        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() > 5 && !user.isAccountLocked()) {
                securityEventService.log("BRUTE_FORCE", email, path, path);
                user.setAccountLocked(true);
                securityEventService.log("LOCK_USER", email, "Lock user " + email, path);
            }

            userRepository.save(user);
        });
    }

    public void loginSucceeded(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            user.setFailedAttempts(0);
            userRepository.save(user);
        });
    }
}
