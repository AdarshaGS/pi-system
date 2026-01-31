package com.aa.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.aa.data.AAFIRequestEntity;
import com.aa.data.ConsentRequest;
import com.aa.data.EncryptedFIPayload;
import com.aa.data.FIPayload;
import com.aa.data.FIRequest;
import com.aa.mock.MockEncryptionService;
import com.aa.repo.AAConsentRepository;
import com.aa.repo.AAFIRequestRepository;
import com.aa.service.AAService;

import com.common.security.AuthenticationHelper;
import com.portfolio.engine.PortfolioEngine;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/aa")
@Tag(name = "Account Aggregator", description = "Endpoints for Mock AA data flow")
@Slf4j
public class AAController {

    private final AAService aaService;
    private final MockEncryptionService encryptionService;
    private final PortfolioEngine portfolioEngine;
    private final AAFIRequestRepository fiRequestRepository;
    private final AAConsentRepository consentRepository;
    private final AuthenticationHelper authenticationHelper;

    public AAController(AAService aaService,
            MockEncryptionService encryptionService,
            PortfolioEngine portfolioEngine,
            AAFIRequestRepository fiRequestRepository,
            AAConsentRepository consentRepository,
            AuthenticationHelper authenticationHelper) {
        this.aaService = aaService;
        this.encryptionService = encryptionService;
        this.portfolioEngine = portfolioEngine;
        this.fiRequestRepository = fiRequestRepository;
        this.consentRepository = consentRepository;
        this.authenticationHelper = authenticationHelper;
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @GetMapping("/consent/templates")
    @Operation(summary = "Get Consent Templates")
    public ResponseEntity<List<Map<String, String>>> getTemplates() {
        return ResponseEntity.ok(aaService.getConsentTemplates());
    }

    @PostMapping("/consent")
    @Operation(summary = "Create Consent")
    public ResponseEntity<?> createConsent(@Valid @RequestBody ConsentRequest request) {
        authenticationHelper.validateUserAccess(Long.parseLong(request.getUserId()));
        return ResponseEntity.ok(aaService.createConsent(request));
    }

    @GetMapping("/consent/{consentId}/status")
    @Operation(summary = "Get Consent Status")
    public ResponseEntity<?> getConsentStatus(@PathVariable("consentId") String consentId) {
        validateConsentOwnership(consentId);
        return ResponseEntity.ok(aaService.getConsentStatus(consentId));
    }

    @PostMapping("/fetch")
    @Operation(summary = "Request Financial Information (Async Simulator)")
    public ResponseEntity<?> initiateFetch(@Valid @RequestBody FIRequest request) {
        validateConsentOwnership(request.getConsentId());
        String requestId = "req-" + java.util.UUID.randomUUID().toString().substring(0, 8);

        AAFIRequestEntity entity = AAFIRequestEntity.builder()
                .requestId(requestId)
                .consentId(request.getConsentId())
                .status("PENDING")
                .build();
        fiRequestRepository.save(entity);

        // Simulate Async processing
        scheduler.schedule(() -> {
            try {
                log.info("Mock AA: Processing FI Fetch for requestId: {}", requestId);
                EncryptedFIPayload encrypted = aaService.fetchFinancialInformation(request);

                AAFIRequestEntity updatedEntity = fiRequestRepository.findByRequestId(requestId)
                        .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

                updatedEntity.setEncryptedData(encrypted.getData());
                updatedEntity.setStatus("READY");
                fiRequestRepository.save(updatedEntity);

                log.info("Mock AA: FI Data READY for requestId: {}", requestId);
            } catch (Exception e) {
                fiRequestRepository.findByRequestId(requestId).ifPresent(ent -> {
                    ent.setStatus("FAILED: " + e.getMessage());
                    fiRequestRepository.save(ent);
                });
                log.error("Mock AA: FI Fetch FAILED for requestId: {}", requestId, e);
            }
        }, 3, TimeUnit.SECONDS);

        return ResponseEntity.ok(Map.of("requestId", requestId, "status", "PENDING"));
    }

    @GetMapping("/fetch/{requestId}/status")
    @Operation(summary = "Poll FI Fetch Status")
    public ResponseEntity<?> getFetchStatus(@PathVariable("requestId") String requestId) {
        AAFIRequestEntity entity = fiRequestRepository.findByRequestId(requestId).orElse(null);
        if (entity == null) {
            return ResponseEntity.status(404).body(Map.of("status", "NOT_FOUND"));
        }
        validateConsentOwnership(entity.getConsentId());
        return ResponseEntity.ok(Map.of("requestId", requestId, "status", entity.getStatus()));
    }

    @GetMapping("/fetch/{requestId}/data")
    @Operation(summary = "Get Decrypted Portfolio Insights")
    public ResponseEntity<?> getDecryptedData(@PathVariable("requestId") String requestId) {
        AAFIRequestEntity entity = fiRequestRepository.findByRequestId(requestId)
                .orElse(null);

        if (entity == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Request not found"));
        }

        validateConsentOwnership(entity.getConsentId());

        if (!"READY".equals(entity.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Data not ready or request failed"));
        }

        EncryptedFIPayload encrypted = EncryptedFIPayload.builder()
                .data(entity.getEncryptedData())
                .keyId("mock-key-001")
                .build();

        FIPayload payload = encryptionService.decrypt(encrypted);
        PortfolioEngine.PortfolioMetrics metrics = portfolioEngine.computeMetrics(payload);

        return ResponseEntity.ok(Map.of(
                "consentId", payload.getConsentId(),
                "metrics", metrics,
                "rawData", payload.getFinancialData()));
    }

    @DeleteMapping("/consent/{consentId}")
    @Operation(summary = "Revoke Consent")
    public ResponseEntity<?> revokeConsent(@PathVariable("consentId") String consentId) {
        validateConsentOwnership(consentId);
        aaService.revokeConsent(consentId);
        return ResponseEntity.ok().build();
    }

    private void validateConsentOwnership(String consentId) {
        consentRepository.findByConsentId(consentId)
                .ifPresent(consent -> authenticationHelper.validateUserAccess(Long.parseLong(consent.getUserId())));
    }
}
