package com.propertyiq.portfolio.mortgage.controller;

import com.propertyiq.common.dto.ApiResponse;
import com.propertyiq.portfolio.mortgage.dto.CreateMortgageRequest;
import com.propertyiq.portfolio.mortgage.dto.MortgageResponse;
import com.propertyiq.portfolio.mortgage.dto.MortgageSummary;
import com.propertyiq.portfolio.mortgage.service.MortgageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MortgageController {

    private final MortgageService mortgageService;

    @PostMapping("/properties/{propertyId}/mortgages")
    public ResponseEntity<ApiResponse<MortgageResponse>> createMortgage(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody CreateMortgageRequest request) {
        MortgageResponse response = mortgageService.createMortgage(userId, propertyId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mortgage created successfully", response));
    }

    @GetMapping("/properties/{propertyId}/mortgages")
    public ResponseEntity<ApiResponse<List<MortgageResponse>>> getMortgagesForProperty(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<MortgageResponse> mortgages;
        if (Boolean.TRUE.equals(activeOnly)) {
            mortgages = mortgageService.getActiveMortgagesForProperty(userId, propertyId);
        } else {
            mortgages = mortgageService.getMortgagesForProperty(userId, propertyId);
        }
        return ResponseEntity.ok(ApiResponse.success(mortgages));
    }

    @GetMapping("/mortgages/{mortgageId}")
    public ResponseEntity<ApiResponse<MortgageResponse>> getMortgage(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID mortgageId) {
        MortgageResponse response = mortgageService.getMortgage(userId, mortgageId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mortgages/{mortgageId}/summary")
    public ResponseEntity<ApiResponse<MortgageSummary>> getMortgageSummary(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID mortgageId) {
        MortgageSummary summary = mortgageService.getMortgageSummary(userId, mortgageId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/mortgages")
    public ResponseEntity<ApiResponse<List<MortgageResponse>>> getAllMortgagesForUser(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
        List<MortgageResponse> mortgages;
        if (Boolean.TRUE.equals(activeOnly)) {
            mortgages = mortgageService.getActiveMortgagesForUser(userId);
        } else {
            mortgages = mortgageService.getAllMortgagesForUser(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(mortgages));
    }
}
