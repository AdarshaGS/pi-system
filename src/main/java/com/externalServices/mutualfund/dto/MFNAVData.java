package com.externalServices.mutualfund.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NAV (Net Asset Value) data point from mfapi.in
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFNAVData {
    
    @JsonProperty("date")
    private String date;
    
    @JsonProperty("nav")
    private String nav;
}
