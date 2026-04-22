package com.example.accountserviceproject.audit;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public void log(String action, String subject, String object, String path){
        SecurityEvent event = new SecurityEvent(action, subject, object, path);
        event.setDate(LocalDateTime.now());
        securityEventRepository.save(event);
    }
}
