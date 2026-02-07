package com.investments.stocks.thirdParty.providers.IndianAPI.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.audit.entity.ThirdPartyRequestAudit;
import com.audit.service.ThirdPartyAuditService;
import com.externalServices.data.ExternalServicePropertiesEntity;
import com.externalServices.service.ExternalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investments.stocks.exception.SymbolNotFoundException;
import com.investments.stocks.thirdParty.ThirdPartyResponse;

@Service
public class IndianAPIServiceImpl implements IndianAPIService {

    private static final Logger log = LoggerFactory.getLogger(IndianAPIServiceImpl.class);

    private final HttpClient httpClient;
    private final ExternalService externalService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SERVICE_NAME = "INDIANAPI";
    private final ThirdPartyAuditService auditService;

    public IndianAPIServiceImpl(ExternalService externalService,
            ThirdPartyAuditService auditService) {
        this.httpClient = HttpClient.newHttpClient();
        this.externalService = externalService;
        this.auditService = auditService;
    }

    @Override
    public ThirdPartyResponse fetchStockData(String symbol) {

        final List<ExternalServicePropertiesEntity> properties = this.externalService
                .getExternalServicePropertiesByServiceName(
                        SERVICE_NAME);
        Map<String, String> headers = constructHeaders(properties);

        String apiEndpoint = properties.stream()
                .filter(prop -> "endpoint".equalsIgnoreCase(prop.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("API endpoint not found"));

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(apiEndpoint + "?name=" + encodeSymbol(symbol)))
                .GET();

        headers.forEach(builder::header);

        HttpRequest request = builder.build();

        long startTime = System.currentTimeMillis();
        String response = null;
        Integer statusCode = null;
        String exceptionMessage = null;

        try {
            HttpResponse<String> httpResponse = this.httpClient
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .join();

            statusCode = httpResponse.statusCode();
            response = httpResponse.body();

            // Validate successful HTTP response
            if (statusCode != null && (statusCode < 200 || statusCode >= 300)) {
                log.error("API returned error status {} for symbol: {}. Response: {}", 
                    statusCode, symbol, response);
                throw new SymbolNotFoundException(
                    String.format("API returned error status %d for symbol: %s", statusCode, symbol));
            }
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            ThirdPartyRequestAudit audit = ThirdPartyRequestAudit.builder()
                    .providerName(SERVICE_NAME)
                    .url(request.uri().toString())
                    .method(request.method())
                    .responseStatus(statusCode)
                    .responseBody(response)
                    .timeTakenMs(duration)
                    .timestamp(java.time.LocalDateTime.now())
                    .exceptionMessage(exceptionMessage)
                    .build();

            auditService.logOnly(audit);
        }

        // Validate response is not null or empty
        if (response == null || response.trim().isEmpty()) {
            log.error("Empty or null response received for symbol: {}", symbol);
            throw new SymbolNotFoundException("No data received from third-party API for symbol: " + symbol);
        }

        // Log the raw response for debugging
        log.info("Raw API response for symbol {}: {}", symbol, response);

        ThirdPartyResponse thirdPartyResponse = null;
        try {
            thirdPartyResponse = objectMapper.readValue(response, ThirdPartyResponse.class);
        } catch (JsonMappingException e) {
            log.error("Failed to map JSON response for symbol: {}. Response body: {}. Error: {}", 
                symbol, response, e.getMessage(), e);
            throw new SymbolNotFoundException("Invalid response format from third-party API for symbol: " + symbol);
        } catch (JsonProcessingException e) {
            log.error("Failed to process JSON response for symbol: {}. Response body: {}. Error: {}", 
                symbol, response, e.getMessage(), e);
            throw new SymbolNotFoundException("Failed to parse response from third-party API for symbol: " + symbol);
        }
        if (thirdPartyResponse == null) {
            throw new SymbolNotFoundException("Symbol not found in third-party API: " + symbol);
        }
        return thirdPartyResponse;
    }

    private Map<String, String> constructHeaders(final List<ExternalServicePropertiesEntity> properties) {

        String apiKey = properties.stream()
                .filter(prop -> "x-api-key".equalsIgnoreCase(prop.getName()))
                .map(ExternalServicePropertiesEntity::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("API key not found"));

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("x-api-key", apiKey);
        return headers;
    }

    private String encodeSymbol(String symbol) {
        try {
            return URLEncoder.encode(symbol, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to encode symbol", e);
        }
    }
}
