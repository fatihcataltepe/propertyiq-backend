package com.propertyiq.portfolio.controller;

import com.propertyiq.common.dto.ApiResponse;
import com.propertyiq.portfolio.dto.*;
import com.propertyiq.portfolio.model.PropertyStatus;
import com.propertyiq.portfolio.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreatePropertyRequest request) {
        PropertyResponse response = propertyService.createProperty(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getProperties(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) PropertyStatus status) {
        List<PropertyResponse> properties = propertyService.getProperties(userId, status);
        return ResponseEntity.ok(ApiResponse.success(properties));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getProperty(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId) {
        PropertyResponse response = propertyService.getProperty(userId, propertyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody UpdatePropertyRequest request) {
        PropertyResponse response = propertyService.updateProperty(userId, propertyId, request);
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully", response));
    }

    @PatchMapping("/{propertyId}/valuation")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateValuation(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody UpdateValuationRequest request) {
        PropertyResponse response = propertyService.updateValuation(userId, propertyId, request);
        return ResponseEntity.ok(ApiResponse.success("Valuation updated successfully", response));
    }

    @PostMapping("/{propertyId}/sold")
    public ResponseEntity<ApiResponse<PropertyResponse>> markAsSold(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId,
            @Valid @RequestBody MarkAsSoldRequest request) {
        PropertyResponse response = propertyService.markAsSold(userId, propertyId, request);
        return ResponseEntity.ok(ApiResponse.success("Property marked as sold", response));
    }

    @PostMapping("/{propertyId}/archive")
    public ResponseEntity<ApiResponse<PropertyResponse>> archiveProperty(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID propertyId) {
        PropertyResponse response = propertyService.archiveProperty(userId, propertyId);
        return ResponseEntity.ok(ApiResponse.success("Property archived successfully", response));
    }
}
