package com.example.accountserviceproject.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityEventController {

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventController(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @GetMapping("/events")
    public List<SecurityEventResponse> getSecurityEvents(){
        return securityEventRepository.findAllByOrderByIdAsc().stream()
                .map(securityEvent -> new SecurityEventResponse(
                        securityEvent.getDate(),
                        securityEvent.getAction(),
                        securityEvent.getSubject(),
                        securityEvent.getObject(),
                        securityEvent.getPath()
                ))
                .toList();
    }}

