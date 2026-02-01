package com.externalServices.mutualfund.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for listing all mutual fund schemes from mfapi.in
 * Endpoint: GET /mf
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFSchemeListItem {
    
    @JsonProperty("schemeCode")
    private Long schemeCode;
    
    @JsonProperty("schemeName")
    private String schemeName;
}
