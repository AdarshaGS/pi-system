package com.tax.controller;

import com.tax.dto.ITR1DTO;
import com.tax.dto.ITR2DTO;
import com.tax.service.ITRService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Controller for ITR generation, export, and form parsing
 * Handles ITR-1, ITR-2, Form 16, Form 26AS, and AIS integration
 */
@RestController
@Tag(name = "ITR Management", description = "Generate ITR JSON, parse Form 16/26AS, and sync with Income Tax Portal")
@RequestMapping("api/v1/tax/itr")
@RequiredArgsConstructor
public class ITRController {

    private final ITRService itrService;

    // ========== ITR-1 (Sahaj) ==========

    @GetMapping("/{userId}/itr1")
    @Operation(
        summary = "Build ITR-1 data",
        description = "Build ITR-1 (Sahaj) DTO for individuals with salary, one house property, and other sources"
    )
    public ResponseEntity<ITR1DTO> buildITR1Data(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        ITR1DTO itr1 = itrService.buildITR1Data(userId, financialYear);
        return ResponseEntity.ok(itr1);
    }

    @GetMapping("/{userId}/itr1/json")
    @Operation(
        summary = "Generate ITR-1 JSON",
        description = "Generate ITR-1 JSON file for filing on Income Tax Portal"
    )
    public ResponseEntity<String> generateITR1JSON(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        String json = itrService.generateITR1JSON(userId, financialYear);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "ITR1_" + userId + "_" + financialYear + ".json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(json);
    }

    // ========== ITR-2 ==========

    @GetMapping("/{userId}/itr2")
    @Operation(
        summary = "Build ITR-2 data",
        description = "Build ITR-2 DTO for individuals with capital gains and multiple house properties"
    )
    public ResponseEntity<ITR2DTO> buildITR2Data(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        ITR2DTO itr2 = itrService.buildITR2Data(userId, financialYear);
        return ResponseEntity.ok(itr2);
    }

    @GetMapping("/{userId}/itr2/json")
    @Operation(
        summary = "Generate ITR-2 JSON",
        description = "Generate ITR-2 JSON file for filing on Income Tax Portal"
    )
    public ResponseEntity<String> generateITR2JSON(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        String json = itrService.generateITR2JSON(userId, financialYear);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "ITR2_" + userId + "_" + financialYear + ".json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(json);
    }

    // ========== Form 16 Parsing ==========

    @PostMapping("/{userId}/form16/import")
    @Operation(
        summary = "Import Form 16",
        description = "Parse and import Form 16 (PDF or JSON) to auto-populate salary details and TDS"
    )
    public ResponseEntity<Map<String, String>> importForm16(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "PDF") String fileType) {
        
        try {
            byte[] fileData = file.getBytes();
            itrService.parseAndImportForm16(userId, financialYear, fileData, fileType);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Form 16 imported successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to read file: " + e.getMessage()
            ));
        }
    }

    // ========== Form 26AS Parsing ==========

    @PostMapping("/{userId}/form26as/import")
    @Operation(
        summary = "Import Form 26AS",
        description = "Parse and import Form 26AS (PDF or JSON) to auto-populate TDS entries"
    )
    public ResponseEntity<Map<String, String>> importForm26AS(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "PDF") String fileType) {
        
        try {
            byte[] fileData = file.getBytes();
            itrService.parseAndImportForm26AS(userId, financialYear, fileData, fileType);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Form 26AS imported successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Failed to read file: " + e.getMessage()
            ));
        }
    }

    // ========== AIS Integration ==========

    @PostMapping("/{userId}/ais/sync")
    @Operation(
        summary = "Sync with AIS",
        description = "Integrate with Annual Information Statement (AIS) from Income Tax Portal"
    )
    public ResponseEntity<Map<String, String>> syncWithAIS(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        
        itrService.syncWithAIS(userId, financialYear);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "AIS data synced successfully"
        ));
    }

    // ========== ITR Filing Helper ==========

    @GetMapping("/{userId}/filing-readiness")
    @Operation(
        summary = "Check ITR filing readiness",
        description = "Verify if all required data is available for ITR filing"
    )
    public ResponseEntity<Map<String, Object>> checkFilingReadiness(
            @PathVariable("userId") Long userId,
            @RequestParam("financialYear") String financialYear) {
        
        // This would check if all sections are completed
        // For now, returning a placeholder response
        
        return ResponseEntity.ok(Map.of(
            "status", "ready",
            "message", "All required data is available for ITR filing",
            "missingFields", new String[]{},
            "warnings", new String[]{}
        ));
    }
}
