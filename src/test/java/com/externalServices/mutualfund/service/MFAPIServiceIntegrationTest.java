package com.externalServices.mutualfund.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.externalServices.mutualfund.dto.MFLatestNAVResponse;
import com.externalServices.mutualfund.dto.MFNAVHistoryResponse;
import com.externalServices.mutualfund.dto.MFSchemeListItem;
import com.externalServices.mutualfund.dto.MFSchemeSearchResult;
import com.investments.mutualfunds.service.MutualFundService;

/**
 * Integration tests for Mutual Fund External API
 * These tests verify the integration through MutualFundService
 * 
 * Note: These tests require internet connectivity
 * Consider mocking for unit tests in production
 */
@SpringBootTest
class MFAPIServiceIntegrationTest {

    @Autowired
    private MutualFundService mutualFundService;

    @Test
    void testSearchSchemes_withValidQuery() {
        // Arrange
        String query = "HDFC";

        // Act
        List<MFSchemeSearchResult> results = mutualFundService.searchSchemes(query);

        // Assert
        assertNotNull(results, "Search results should not be null");
        assertFalse(results.isEmpty(), "Search results should contain schemes");
        assertTrue(results.stream()
                .anyMatch(scheme -> scheme.getSchemeName().contains("HDFC")),
                "Results should contain schemes with HDFC in name");
        
        // Print first few results for manual verification
        System.out.println("Search results for 'HDFC':");
        results.stream().limit(3).forEach(scheme -> 
            System.out.println("  " + scheme.getSchemeCode() + ": " + scheme.getSchemeName())
        );
    }

    @Test
    void testListAllSchemes_withPagination() {
        // Arrange
        int limit = 10;
        int offset = 0;

        // Act
        List<MFSchemeListItem> schemes = mutualFundService.listAllSchemes(limit, offset);

        // Assert
        assertNotNull(schemes, "Schemes list should not be null");
        assertFalse(schemes.isEmpty(), "Schemes list should not be empty");
        assertTrue(schemes.size() <= limit, "Should not return more than limit");
        
        System.out.println("Total schemes fetched: " + schemes.size());
    }

    @Test
    void testGetLatestNAV_withValidSchemeCode() {
        // Arrange
        Long schemeCode = 125497L; // HDFC Top 100 Fund - Direct Plan - Growth

        // Act
        MFLatestNAVResponse response = mutualFundService.getLatestNAV(schemeCode);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");
        assertNotNull(response.getMeta(), "Meta should not be null");
        assertNotNull(response.getData(), "Data should not be null");
        assertFalse(response.getData().isEmpty(), "Data should contain at least one NAV entry");
        
        // Verify metadata
        assertEquals(schemeCode, response.getMeta().getSchemeCode());
        assertNotNull(response.getMeta().getFundHouse());
        assertNotNull(response.getMeta().getSchemeName());
        
        // Verify NAV data
        assertNotNull(response.getData().get(0).getDate());
        assertNotNull(response.getData().get(0).getNav());
        
        System.out.println("Latest NAV for scheme " + schemeCode + ":");
        System.out.println("  Fund House: " + response.getMeta().getFundHouse());
        System.out.println("  Scheme: " + response.getMeta().getSchemeName());
        System.out.println("  NAV: ₹" + response.getData().get(0).getNav() + 
                          " (as of " + response.getData().get(0).getDate() + ")");
    }

    @Test
    void testGetSchemeNAVHistory_withoutDateRange() {
        // Arrange
        Long schemeCode = 125497L;

        // Act
        MFNAVHistoryResponse response = mutualFundService.getNAVHistory(schemeCode);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");
        assertNotNull(response.getMeta(), "Meta should not be null");
        assertNotNull(response.getData(), "Data should not be null");
        assertFalse(response.getData().isEmpty(), "Data should contain NAV history");
        
        System.out.println("NAV History for scheme " + schemeCode + ":");
        System.out.println("  Total records: " + response.getData().size());
        System.out.println("  Latest: " + response.getData().get(0).getDate() + 
                          " - ₹" + response.getData().get(0).getNav());
    }

    @Test
    void testGetSchemeNAVHistory_withDateRange() {
        // Arrange
        Long schemeCode = 125497L;
        String startDate = "2023-01-01";
        String endDate = "2023-12-31";

        // Act
        MFNAVHistoryResponse response = mutualFundService.getNAVHistory(
            schemeCode, startDate, endDate
        );

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("SUCCESS", response.getStatus(), "Status should be SUCCESS");
        assertNotNull(response.getData(), "Data should not be null");
        
        System.out.println("NAV History for scheme " + schemeCode + 
                          " from " + startDate + " to " + endDate + ":");
        System.out.println("  Total records: " + response.getData().size());
    }

    @Test
    void testSearchSchemes_withMultipleQueries() {
        // Test different fund houses
        String[] queries = {"SBI", "ICICI", "Axis", "Kotak"};
        
        for (String query : queries) {
            List<MFSchemeSearchResult> results = mutualFundService.searchSchemes(query);
            assertNotNull(results);
            assertFalse(results.isEmpty(), "Should find schemes for " + query);
            System.out.println(query + ": Found " + results.size() + " schemes");
        }
    }
}
