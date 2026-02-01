package com.externalServices.mutualfund.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.externalServices.mutualfund.dto.MFLatestNAVResponse;
import com.externalServices.mutualfund.dto.MFNAVHistoryResponse;
import com.externalServices.mutualfund.dto.MFSchemeListItem;
import com.externalServices.mutualfund.dto.MFSchemeSearchResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MutualFundDataProvider using mfapi.in API
 * 
 * API Documentation: https://www.mfapi.in/docs/
 * Features:
 * - No authentication required (free open API)
 * - Rate limiting applied
 * - JSON responses
 */
@Service
@Slf4j
public class MFAPIService implements MutualFundDataProvider {

    private static final String PROVIDER_NAME = "MFAPI.in";
    
    @Value("${external.service.mfapi.base-url:https://api.mfapi.in}")
    private String baseUrl;
    
    private final RestTemplate restTemplate;

    public MFAPIService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<MFSchemeSearchResult> searchSchemes(String query) {
        try {
            String url = baseUrl + "/mf/search?q=" + query;
            log.info("Searching mutual fund schemes with query: {}", query);
            
            ResponseEntity<List<MFSchemeSearchResult>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MFSchemeSearchResult>>() {}
            );
            
            log.info("Successfully fetched {} schemes for query: {}", 
                response.getBody() != null ? response.getBody().size() : 0, query);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error searching mutual fund schemes for query: {}", query, e);
            throw new RuntimeException("Failed to search mutual fund schemes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MFSchemeListItem> listAllSchemes(Integer limit, Integer offset) {
        try {
            // Default values
            limit = (limit != null && limit > 0) ? Math.min(limit, 500) : 100;
            offset = (offset != null && offset >= 0) ? offset : 0;
            
            String url = String.format("%s/mf?limit=%d&offset=%d", baseUrl, limit, offset);
            log.info("Listing all mutual fund schemes with limit: {}, offset: {}", limit, offset);
            
            ResponseEntity<List<MFSchemeListItem>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MFSchemeListItem>>() {}
            );
            
            log.info("Successfully fetched {} schemes", 
                response.getBody() != null ? response.getBody().size() : 0);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error listing mutual fund schemes", e);
            throw new RuntimeException("Failed to list mutual fund schemes: " + e.getMessage(), e);
        }
    }

    @Override
    public MFNAVHistoryResponse getSchemeNAVHistory(Long schemeCode) {
        try {
            String url = baseUrl + "/mf/" + schemeCode;
            log.info("Fetching NAV history for scheme code: {}", schemeCode);
            
            ResponseEntity<MFNAVHistoryResponse> response = restTemplate.getForEntity(
                url,
                MFNAVHistoryResponse.class
            );
            
            log.info("Successfully fetched NAV history for scheme code: {}", schemeCode);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching NAV history for scheme code: {}", schemeCode, e);
            throw new RuntimeException("Failed to fetch NAV history for scheme: " + schemeCode, e);
        }
    }

    @Override
    public MFNAVHistoryResponse getSchemeNAVHistory(Long schemeCode, String startDate, String endDate) {
        try {
            String url = String.format("%s/mf/%d?startDate=%s&endDate=%s", 
                baseUrl, schemeCode, startDate, endDate);
            log.info("Fetching NAV history for scheme code: {} from {} to {}", 
                schemeCode, startDate, endDate);
            
            ResponseEntity<MFNAVHistoryResponse> response = restTemplate.getForEntity(
                url,
                MFNAVHistoryResponse.class
            );
            
            log.info("Successfully fetched NAV history for scheme code: {} with date range", schemeCode);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching NAV history for scheme code: {} with date range", schemeCode, e);
            throw new RuntimeException("Failed to fetch NAV history for scheme: " + schemeCode, e);
        }
    }

    @Override
    public MFLatestNAVResponse getLatestNAV(Long schemeCode) {
        try {
            String url = baseUrl + "/mf/" + schemeCode + "/latest";
            log.info("Fetching latest NAV for scheme code: {}", schemeCode);
            
            ResponseEntity<MFLatestNAVResponse> response = restTemplate.getForEntity(
                url,
                MFLatestNAVResponse.class
            );
            
            log.info("Successfully fetched latest NAV for scheme code: {}", schemeCode);
            
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Error fetching latest NAV for scheme code: {}", schemeCode, e);
            throw new RuntimeException("Failed to fetch latest NAV for scheme: " + schemeCode, e);
        }
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }
}
