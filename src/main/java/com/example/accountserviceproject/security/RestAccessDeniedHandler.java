package com.example.accountserviceproject.security;



import com.example.accountserviceproject.audit.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;


@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityEventService securityEventService;

    public RestAccessDeniedHandler(SecurityEventService securityEventService) {
        this.securityEventService = securityEventService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "Anonymous";

        securityEventService.log("ACCESS_DENIED", email, request.getRequestURI(), request.getRequestURI());

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
    }


}
