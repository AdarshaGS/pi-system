package com.investments.stocks.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StockPriceValidator.
 * Tests all validation rules: price range, symbol format, data freshness, circuit breaker limits.
 */
@DisplayName("Stock Price Validator Tests")
class StockPriceValidatorTest {

    private StockPriceValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StockPriceValidator();
    }

    // ==================== Price Validation Tests ====================

    @Test
    @DisplayName("Should accept valid price within range")
    void testValidPrice() {
        assertTrue(validator.isValidPrice(100.50, "RELIANCE"));
        assertTrue(validator.isValidPrice(0.01, "RELIANCE")); // Min price
        assertTrue(validator.isValidPrice(1000000.0, "RELIANCE")); // Max price
    }

    @Test
    @DisplayName("Should reject null price")
    void testInvalidPrice_Null() {
        assertFalse(validator.isValidPrice(null, "RELIANCE"));
    }

    @Test
    @DisplayName("Should reject zero price")
    void testInvalidPrice_Zero() {
        assertFalse(validator.isValidPrice(0.0, "RELIANCE"));
    }

    @Test
    @DisplayName("Should reject negative price")
    void testInvalidPrice_Negative() {
        assertFalse(validator.isValidPrice(-10.50, "RELIANCE"));
    }

    @Test
    @DisplayName("Should reject price below minimum")
    void testInvalidPrice_BelowMinimum() {
        assertFalse(validator.isValidPrice(0.005, "RELIANCE"));
    }

    @Test
    @DisplayName("Should reject price above maximum")
    void testInvalidPrice_TooHigh() {
        assertFalse(validator.isValidPrice(1500000.0, "RELIANCE"));
    }

    // ==================== Symbol Validation Tests ====================

    @Test
    @DisplayName("Should accept valid stock symbols")
    void testValidSymbol() {
        assertTrue(validator.isValidSymbol("RELIANCE"));
        assertTrue(validator.isValidSymbol("TCS"));
        assertTrue(validator.isValidSymbol("INFY"));
        assertTrue(validator.isValidSymbol("HDFCBANK"));
        assertTrue(validator.isValidSymbol("RELIANCE.BSE"));
        assertTrue(validator.isValidSymbol("TCS.NS"));
        assertTrue(validator.isValidSymbol("INFY.BSE"));
        assertTrue(validator.isValidSymbol("ITC123")); // With numbers
    }

    @Test
    @DisplayName("Should reject invalid symbols")
    void testInvalidSymbol() {
        assertFalse(validator.isValidSymbol(null));
        assertFalse(validator.isValidSymbol(""));
        assertFalse(validator.isValidSymbol("  "));
        assertFalse(validator.isValidSymbol("reliance")); // Lowercase
        assertFalse(validator.isValidSymbol("RELIANCE.NYSE")); // Wrong exchange
        assertFalse(validator.isValidSymbol("REL-IANCE")); // Hyphen not allowed
        assertFalse(validator.isValidSymbol("RELIANCE BSE")); // Space not allowed
        assertFalse(validator.isValidSymbol("@RELIANCE")); // Special char not allowed
    }

    // ==================== Data Freshness Tests ====================

    @Test
    @DisplayName("Should accept fresh data (today)")
    void testFreshData_Today() {
        String today = java.time.LocalDate.now().toString(); // Format: 2026-01-31
        assertTrue(validator.isFreshData(today, "RELIANCE"));
    }

    @Test
    @DisplayName("Should accept data within 7 days")
    void testFreshData_WithinWeek() {
        String threeDaysAgo = java.time.LocalDate.now().minusDays(3).toString();
        assertTrue(validator.isFreshData(threeDaysAgo, "RELIANCE"));
        
        String sixDaysAgo = java.time.LocalDate.now().minusDays(6).toString();
        assertTrue(validator.isFreshData(sixDaysAgo, "RELIANCE"));
    }

    @Test
    @DisplayName("Should accept data exactly 7 days old")
    void testFreshData_ExactlySevenDays() {
        String sevenDaysAgo = java.time.LocalDate.now().minusDays(7).toString();
        assertTrue(validator.isFreshData(sevenDaysAgo, "RELIANCE"));
    }

    @Test
    @DisplayName("Should reject stale data (older than 7 days)")
    void testStaleData() {
        String eightDaysAgo = java.time.LocalDate.now().minusDays(8).toString();
        assertFalse(validator.isFreshData(eightDaysAgo, "RELIANCE"));
        
        String thirtyDaysAgo = java.time.LocalDate.now().minusDays(30).toString();
        assertFalse(validator.isFreshData(thirtyDaysAgo, "RELIANCE"));
    }

    @Test
    @DisplayName("Should reject future dates")
    void testFutureDate() {
        String tomorrow = java.time.LocalDate.now().plusDays(1).toString();
        assertFalse(validator.isFreshData(tomorrow, "RELIANCE"));
        
        String nextWeek = java.time.LocalDate.now().plusDays(7).toString();
        assertFalse(validator.isFreshData(nextWeek, "RELIANCE"));
    }

    @Test
    @DisplayName("Should handle invalid date formats")
    void testInvalidDateFormat() {
        assertFalse(validator.isFreshData(null, "RELIANCE"));
        assertFalse(validator.isFreshData("", "RELIANCE"));
        assertFalse(validator.isFreshData("invalid-date", "RELIANCE"));
        assertFalse(validator.isFreshData("31-01-2026", "RELIANCE")); // Wrong format
        assertFalse(validator.isFreshData("2026/01/31", "RELIANCE")); // Wrong format
    }

    // ==================== Combined Validation Tests ====================

    @Test
    @DisplayName("Should pass all validations for valid data")
    void testValidateStockData_AllValid() {
        String today = java.time.LocalDate.now().toString();
        assertTrue(validator.validateStockData("RELIANCE", 2500.50, today));
        assertTrue(validator.validateStockData("TCS", 3200.00, today));
        assertTrue(validator.validateStockData("INFY.BSE", 1500.25, today));
    }

    @Test
    @DisplayName("Should fail if any validation fails")
    void testValidateStockData_AnyInvalid() {
        String today = java.time.LocalDate.now().toString();
        String oldDate = java.time.LocalDate.now().minusDays(10).toString();
        
        // Invalid price
        assertFalse(validator.validateStockData("RELIANCE", -100.0, today));
        assertFalse(validator.validateStockData("RELIANCE", 0.0, today));
        assertFalse(validator.validateStockData("RELIANCE", 2000000.0, today));
        
        // Invalid symbol
        assertFalse(validator.validateStockData("reliance", 2500.50, today));
        assertFalse(validator.validateStockData("", 2500.50, today));
        
        // Invalid date
        assertFalse(validator.validateStockData("RELIANCE", 2500.50, oldDate));
        assertFalse(validator.validateStockData("RELIANCE", 2500.50, "invalid"));
    }

    // ==================== Circuit Breaker Tests ====================

    @Test
    @DisplayName("Should accept reasonable price changes (within ±20%)")
    void testReasonableChange() {
        assertTrue(validator.isReasonableChange("5.25%", "RELIANCE")); // 5.25%
        assertTrue(validator.isReasonableChange("15.00%", "TCS")); // 15%
        assertTrue(validator.isReasonableChange("-10.50%", "INFY")); // -10.5%
        assertTrue(validator.isReasonableChange("19.99%", "HDFCBANK")); // 19.99%
        assertTrue(validator.isReasonableChange("-19.99%", "ITC")); // -19.99%
        assertTrue(validator.isReasonableChange("20.00%", "RELIANCE")); // Exactly 20%
        assertTrue(validator.isReasonableChange("-20.00%", "RELIANCE")); // Exactly -20%
    }

    @Test
    @DisplayName("Should reject unreasonable price changes (beyond ±20%)")
    void testUnreasonableChange() {
        assertFalse(validator.isReasonableChange("25.00%", "RELIANCE")); // 25%
        assertFalse(validator.isReasonableChange("50.00%", "TCS")); // 50%
        assertFalse(validator.isReasonableChange("-25.00%", "INFY")); // -25%
        assertFalse(validator.isReasonableChange("100.00%", "HDFCBANK")); // 100%
        assertFalse(validator.isReasonableChange("-30.00%", "ITC")); // -30%
    }

    @Test
    @DisplayName("Should handle edge cases for change percent")
    void testChangePercent_EdgeCases() {
        assertTrue(validator.isReasonableChange("0.00%", "RELIANCE")); // No change
        assertTrue(validator.isReasonableChange("0.01%", "RELIANCE")); // Minimal change
        assertTrue(validator.isReasonableChange("-0.01%", "RELIANCE")); // Minimal change
        
        // Invalid formats - should return true (default allow)
        assertTrue(validator.isReasonableChange(null, "RELIANCE"));
        assertTrue(validator.isReasonableChange("", "RELIANCE"));
        assertTrue(validator.isReasonableChange("invalid", "RELIANCE"));
    }

    // ==================== Edge Cases and Boundary Tests ====================

    @Test
    @DisplayName("Should handle boundary values correctly")
    void testBoundaryValues() {
        // Price boundaries
        assertTrue(validator.isValidPrice(0.01, "RELIANCE")); // Min
        assertFalse(validator.isValidPrice(0.009999, "RELIANCE")); // Below min
        assertTrue(validator.isValidPrice(1000000.0, "RELIANCE")); // Max
        assertFalse(validator.isValidPrice(1000000.01, "RELIANCE")); // Above max
        
        // Date boundaries
        String sevenDaysAgo = java.time.LocalDate.now().minusDays(7).toString();
        assertTrue(validator.isFreshData(sevenDaysAgo, "RELIANCE")); // Exactly 7 days
        
        String eightDaysAgo = java.time.LocalDate.now().minusDays(8).toString();
        assertFalse(validator.isFreshData(eightDaysAgo, "RELIANCE")); // 8 days
        
        // Change percent boundaries
        assertTrue(validator.isReasonableChange("20.00%", "RELIANCE")); // Exactly 20%
        assertFalse(validator.isReasonableChange("20.01%", "RELIANCE")); // Just above
    }

    @Test
    @DisplayName("Should handle null and empty values gracefully")
    void testNullAndEmpty() {
        // Price null handling
        assertFalse(validator.isValidPrice(null, "RELIANCE"));
        assertFalse(validator.validateStockData("RELIANCE", null, "2026-01-31"));
        
        // Symbol null/empty handling
        assertFalse(validator.isValidSymbol(null));
        assertFalse(validator.isValidSymbol(""));
        assertFalse(validator.validateStockData(null, 2500.0, "2026-01-31"));
        assertFalse(validator.validateStockData("", 2500.0, "2026-01-31"));
        
        // Date null/empty handling
        assertFalse(validator.isFreshData(null, "RELIANCE"));
        assertFalse(validator.isFreshData("", "RELIANCE"));
        assertFalse(validator.validateStockData("RELIANCE", 2500.0, null));
        assertFalse(validator.validateStockData("RELIANCE", 2500.0, ""));
    }
}
