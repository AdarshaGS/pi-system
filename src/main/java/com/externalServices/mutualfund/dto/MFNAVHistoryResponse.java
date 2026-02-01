package com.externalServices.mutualfund.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for NAV history from mfapi.in
 * Endpoint: GET /mf/{scheme_code}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFNAVHistoryResponse {
    
    @JsonProperty("meta")
    private MFSchemeMeta meta;
    
    @JsonProperty("data")
    private List<MFNAVData> data;
    
    @JsonProperty("status")
    private String status;
}
