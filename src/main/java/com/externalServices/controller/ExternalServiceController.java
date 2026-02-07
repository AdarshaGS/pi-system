package com.externalServices.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.externalServices.dto.CreatePropertyRequest;
import com.externalServices.dto.CreateServiceRequest;
import com.externalServices.dto.UpdatePropertyRequest;
import com.externalServices.service.ExternalService;
import com.externalServices.data.ExternalServiceEntity;
import com.externalServices.data.ExternalServicePropertiesEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/external-services")
@Tag(name = "External Services", description = "APIs for managing external service configurations")
public class ExternalServiceController {

    private final ExternalService externalService;

    public ExternalServiceController(final ExternalService externalService) {
        this.externalService = externalService;
    }

    @GetMapping
    @Operation(summary = "Get all external services", description = "Fetches all available external services.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all services")
    public List<ExternalServiceEntity> getAllServices() {
        return this.externalService.getAllServices();
    }

    @GetMapping("/{serviceName}")
    @Operation(summary = "Get external service properties", description = "Fetches configuration properties for a given external service.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved service properties")
    public List<ExternalServicePropertiesEntity> getExternalService(@PathVariable("serviceName") final String serviceName) {
        return this.externalService.getExternalServicePropertiesByServiceName(serviceName);
    }

    @PostMapping
    @Operation(summary = "Create a new external service", description = "Creates a new external service.")
    @ApiResponse(responseCode = "200", description = "Successfully created service")
    public ResponseEntity<ExternalServiceEntity> createService(@RequestBody CreateServiceRequest request) {
        ExternalServiceEntity service = this.externalService.createService(request.getServiceName());
        return ResponseEntity.ok(service);
    }

    @PostMapping("/properties")
    @Operation(summary = "Create a new service property", description = "Creates a new property for an external service.")
    @ApiResponse(responseCode = "200", description = "Successfully created property")
    public ResponseEntity<ExternalServicePropertiesEntity> createProperty(@RequestBody CreatePropertyRequest request) {
        ExternalServicePropertiesEntity property = this.externalService.createServiceProperty(
                request.getServiceId(),
                request.getName(),
                request.getValue()
        );
        return ResponseEntity.ok(property);
    }

    @PutMapping("/properties/{propertyId}")
    @Operation(summary = "Update a service property", description = "Updates the value of an existing service property.")
    @ApiResponse(responseCode = "200", description = "Successfully updated property")
    public ResponseEntity<ExternalServicePropertiesEntity> updateProperty(
            @PathVariable("propertyId") Long propertyId,
            @RequestBody UpdatePropertyRequest request) {
        ExternalServicePropertiesEntity property = this.externalService.updateServiceProperty(propertyId, request.getValue());
        return ResponseEntity.ok(property);
    }
}
