package com.example.accountserviceproject.audit;

import java.time.LocalDateTime;

public class SecurityEventResponse {

    private LocalDateTime date;
    private String action;
    private String subject;
    private String object;
    private String path;

    public SecurityEventResponse(LocalDateTime date, String action, String subject, String object, String path) {
        this.date = date;
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

    public String getPath() {
        return path;
    }
}
