package com.stocks.thirdParty.service;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stocks.exception.SymbolNotFoundException;
import com.stocks.thirdParty.ThirdPartyResponse;

import tools.jackson.databind.ObjectMapper;

@Service
public class IndianAPIServiceImpl implements IndianAPIService {

    private final HttpClient httpClient;
    private final String apiEndpoint = "https://stock.indianapi.in/stock";  // have to make these dynamic
    private final String apiKey = "sk-live-ZzyW04WpYVKBJ8e1Q9HKiUWAth5AShHUOg3KWjOq"; // should not hardcode in real app
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IndianAPIServiceImpl() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public ThirdPartyResponse fetchStockData(String symbol) {
        Map<String, String> headers = constructHeaders(apiKey);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(this.apiEndpoint + "?name=" + encodeSymbol(symbol)))
                .GET();

        headers.forEach(builder::header);
        

        HttpRequest request = builder.build();

        HttpResponse<String> httpResponse = this.httpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .join();

        String response = httpResponse.body();

        ThirdPartyResponse thirdPartyResponse = objectMapper.readValue(response, ThirdPartyResponse.class);
        if (thirdPartyResponse == null) {
            throw new SymbolNotFoundException("Symbol not found in third-party API: " + symbol);
        }
        return thirdPartyResponse;
    }

    private Map<String, String> constructHeaders(final String apiKey) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("x-api-key",  apiKey);
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
