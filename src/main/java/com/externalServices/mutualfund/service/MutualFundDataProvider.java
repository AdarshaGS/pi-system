package com.externalServices.mutualfund.service;

import java.util.List;

import com.externalServices.mutualfund.dto.MFLatestNAVResponse;
import com.externalServices.mutualfund.dto.MFNAVHistoryResponse;
import com.externalServices.mutualfund.dto.MFSchemeListItem;
import com.externalServices.mutualfund.dto.MFSchemeSearchResult;

/**
 * Service interface for fetching mutual fund data from third-party API (mfapi.in)
 * This service provides integration with the external mutual fund API
 * 
 * API Documentation: https://www.mfapi.in/docs/
 * Base URL: https://api.mfapi.in
 */
public interface MutualFundDataProvider {

    /**
     * Search for mutual fund schemes by name
     * Endpoint: GET /mf/search?q={query}
     * 
     * @param query Search query (e.g., "HDFC", "SBI")
     * @return List of matching schemes with scheme code and name
     */
    List<MFSchemeSearchResult> searchSchemes(String query);

    /**
     * List all mutual fund schemes with pagination
     * Endpoint: GET /mf?limit={limit}&offset={offset}
     * 
     * @param limit Number of results per page (default: 100, max: 500)
     * @param offset Pagination offset (default: 0)
     * @return List of all schemes with scheme code and name
     */
    List<MFSchemeListItem> listAllSchemes(Integer limit, Integer offset);

    /**
     * Get NAV history for a specific mutual fund scheme
     * Endpoint: GET /mf/{scheme_code}
     * 
     * @param schemeCode Unique scheme code
     * @return NAV history with metadata
     */
    MFNAVHistoryResponse getSchemeNAVHistory(Long schemeCode);

    /**
     * Get NAV history for a specific mutual fund scheme with date range
     * Endpoint: GET /mf/{scheme_code}?startDate={startDate}&endDate={endDate}
     * 
     * @param schemeCode Unique scheme code
     * @param startDate Start date in format YYYY-MM-DD
     * @param endDate End date in format YYYY-MM-DD
     * @return NAV history within the date range
     */
    MFNAVHistoryResponse getSchemeNAVHistory(Long schemeCode, String startDate, String endDate);

    /**
     * Get latest NAV for a specific mutual fund scheme
     * Endpoint: GET /mf/{scheme_code}/latest
     * 
     * @param schemeCode Unique scheme code
     * @return Latest NAV with metadata
     */
    MFLatestNAVResponse getLatestNAV(Long schemeCode);

    /**
     * Get provider name
     * 
     * @return Provider name (e.g., "MFAPI.in")
     */
    String getProviderName();
}
