package com.externalServices.mutualfund.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata information for a mutual fund scheme from mfapi.in
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MFSchemeMeta {
    
    @JsonProperty("fund_house")
    private String fundHouse;
    
    @JsonProperty("scheme_type")
    private String schemeType;
    
    @JsonProperty("scheme_category")
    private String schemeCategory;
    
    @JsonProperty("scheme_code")
    private Long schemeCode;
    
    @JsonProperty("scheme_name")
    private String schemeName;
    
    @JsonProperty("isin_growth")
    private String isinGrowth;
    
    @JsonProperty("isin_div_reinvestment")
    private String isinDivReinvestment;
}
