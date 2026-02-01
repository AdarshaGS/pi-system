package com.tax.service;

import com.tax.dto.ITR1DTO;
import com.tax.dto.ITR2DTO;

/**
 * Service for ITR generation, parsing, and integration
 * Handles ITR-1, ITR-2, Form 16, Form 26AS, and AIS
 */
public interface ITRService {
    
    /**
     * Generate ITR-1 (Sahaj) JSON for filing
     * For individuals with salary, one house property, other sources
     */
    String generateITR1JSON(Long userId, String financialYear);
    
    /**
     * Generate ITR-2 JSON for filing
     * For individuals with capital gains, multiple properties
     */
    String generateITR2JSON(Long userId, String financialYear);
    
    /**
     * Build ITR-1 DTO from user's tax data
     */
    ITR1DTO buildITR1Data(Long userId, String financialYear);
    
    /**
     * Build ITR-2 DTO from user's tax data
     */
    ITR2DTO buildITR2Data(Long userId, String financialYear);
    
    /**
     * Parse Form 16 (PDF or JSON) and extract salary details
     * Returns parsed data for auto-population
     */
    void parseAndImportForm16(Long userId, String financialYear, byte[] form16Data, String fileType);
    
    /**
     * Parse Form 26AS (PDF or JSON) and extract TDS details
     * Returns TDS entries for reconciliation
     */
    void parseAndImportForm26AS(Long userId, String financialYear, byte[] form26ASData, String fileType);
    
    /**
     * Integrate with AIS (Annual Information Statement)
     * Fetches and reconciles data from Income Tax Portal API
     */
    void syncWithAIS(Long userId, String financialYear);
    
    /**
     * Validate ITR data before submission
     * Checks for mandatory fields and data consistency
     */
    boolean validateITRData(Long userId, String financialYear, String itrType);
}
