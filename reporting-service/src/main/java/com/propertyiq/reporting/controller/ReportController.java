package com.propertyiq.reporting.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @PostMapping("/tax-summary/{propertyId}")
    public String generateTaxSummary(@PathVariable String propertyId,
                                      @RequestParam(required = false) String taxYear) {
        return "Reporting Service - Generate tax summary for property: " + propertyId + ", year: " + taxYear;
    }

    @PostMapping("/annual-returns/{propertyId}")
    public String generateAnnualReturns(@PathVariable String propertyId,
                                         @RequestParam(required = false) Integer years) {
        return "Reporting Service - Generate annual returns for property: " + propertyId + ", years: " + years;
    }

    @GetMapping
    public String getReports(@RequestParam(required = false) String propertyId,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String year) {
        return "Reporting Service - List reports";
    }

    @GetMapping("/{id}")
    public String getReport(@PathVariable String id) {
        return "Reporting Service - Get report: " + id;
    }
}
