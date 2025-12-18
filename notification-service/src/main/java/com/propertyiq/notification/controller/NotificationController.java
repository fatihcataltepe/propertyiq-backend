package com.propertyiq.notification.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @PostMapping("/email")
    public String sendEmail() {
        return "Notification Service - Send email";
    }

    @PostMapping("/alert")
    public String sendAlert() {
        return "Notification Service - Send alert";
    }
}
