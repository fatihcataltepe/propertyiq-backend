package com.propertyiq.portfolio.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @GetMapping
    public String getProperties() {
        return "Portfolio Service - Get all properties";
    }

    @GetMapping("/{id}")
    public String getProperty(@PathVariable String id) {
        return "Portfolio Service - Get property: " + id;
    }

    @PostMapping
    public String createProperty() {
        return "Portfolio Service - Create property";
    }
}
