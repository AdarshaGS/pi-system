package com.investments.stocks.thirdParty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThirdPartyResponse {
    private String companyName;
    private String industry;
    private CompanyProfile companyProfile;
    private currentPrice currentPrice;
    // private StockDetailsReusableData stockDetailsReusableData;
    // private Double marketCap;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyProfile {
        private String companyDescription;
        private String mgIndustry;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeerCompanyList {
        public String tickerId;
        public String companyName;
        public String marketCap;
        public String priceEarningRatio;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class currentPrice {
        private Double BSE;
        private Double NSE;
    }

    // @Data
    // @NoArgsConstructor
    // @AllArgsConstructor
    // @Builder
    // public static class StockDetailsReusableData {
    // private Double marketCap;
    // }

}
