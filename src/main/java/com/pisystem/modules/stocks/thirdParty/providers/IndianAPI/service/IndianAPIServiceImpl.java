package com.pisystem.modules.stocks.thirdParty.providers.IndianAPI.service;

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

import com.pisystem.shared.audit.entity.ThirdPartyRequestAudit;
import com.pisystem.shared.audit.service.ThirdPartyAuditService;
import com.pisystem.integrations.externalservices.data.ExternalServicePropertiesEntity;
import com.pisystem.integrations.externalservices.service.ExternalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pisystem.modules.stocks.exception.SymbolNotFoundException;
import com.pisystem.modules.stocks.thirdParty.ThirdPartyResponse;

// import io.github.resilience4j.ratelimiter.RateLimiter;
// import io.github.resilience4j.retry.Retry;
import java.util.function.Supplier;
import org.springframework.cache.annotation.Cacheable;

@Service
public class IndianAPIServiceImpl implements IndianAPIService {

    private static final Logger log = LoggerFactory.getLogger(IndianAPIServiceImpl.class);

    private final HttpClient httpClient;
    private final ExternalService externalService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SERVICE_NAME = "INDIANAPI";
    private final ThirdPartyAuditService auditService;
    // private final RateLimiter rateLimiter;
    // private final Retry retry;

    public IndianAPIServiceImpl(ExternalService externalService,
            ThirdPartyAuditService auditService) {
        this.httpClient = HttpClient.newHttpClient();
        this.externalService = externalService;
        this.auditService = auditService;
        // this.rateLimiter = rateLimiter;
        // this.retry = retry;
    }

    // @Override
    // @Cacheable(value = "stockPrices", key = "#symbol", unless = "#result == null")
    // public ThirdPartyResponse fetchStockData(String symbol) {
    //     Supplier<ThirdPartyResponse> rateLimitedSupplier = RateLimiter.decorateSupplier(rateLimiter,
    //             () -> fetchFromApi(symbol));
    //     Supplier<ThirdPartyResponse> retrySupplier = Retry.decorateSupplier(retry, rateLimitedSupplier);
    //     return retrySupplier.get();
    // }

    private ThirdPartyResponse fetchFromApi(String symbol) {
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

            if (statusCode != null && (statusCode < 200 || statusCode >= 300)) {
                log.error("API returned error status {} for symbol: {}. Response: {}",
                        statusCode, symbol, response);
                throw new SymbolNotFoundException(
                        String.format("API returned error status %d for symbol: %s", statusCode, symbol));
            }
        } catch (Exception e) {
            exceptionMessage = e.getMessage();
            throw new RuntimeException(e);
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

        if (response == null || response.trim().isEmpty()) {
            log.error("Empty or null response received for symbol: {}", symbol);
            throw new SymbolNotFoundException("No data received from third-party API for symbol: " + symbol);
        }

        log.info("Raw API response for symbol {}: {}", symbol, response);

        try {
            return objectMapper.readValue(response, ThirdPartyResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to process JSON response for symbol: {}. Response body: {}. Error: {}",
                    symbol, response, e.getMessage(), e);
            throw new SymbolNotFoundException("Failed to parse response from third-party API for symbol: " + symbol);
        }
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

    @Override
    public ThirdPartyResponse fetchStockData(String symbol) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fetchStockData'");
    }
}
