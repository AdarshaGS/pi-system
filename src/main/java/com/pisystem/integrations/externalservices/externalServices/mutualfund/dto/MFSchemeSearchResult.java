package com.externalServices.mutualfund.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for mutual fund scheme search results from mfapi.in
 * Endpoint: GET /mf/search?q={query}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFSchemeSearchResult {
    
    @JsonProperty("schemeCode")
    private Long schemeCode;
    
    @JsonProperty("schemeName")
    private String schemeName;
}
