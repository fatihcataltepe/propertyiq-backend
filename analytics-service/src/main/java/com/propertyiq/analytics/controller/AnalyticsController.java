package com.propertyiq.analytics.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @GetMapping("/property/{id}")
    public String getPropertyAnalytics(@PathVariable String id) {
        return "Analytics Service - Property analytics for: " + id;
    }

    @GetMapping("/portfolio")
    public String getPortfolioAnalytics() {
        return "Analytics Service - Portfolio-level analytics";
    }
}
