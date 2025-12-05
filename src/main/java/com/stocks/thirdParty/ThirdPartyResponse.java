package com.stocks.thirdParty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ThirdPartyResponse {
    private String companyName;
    private String industry;
    private CompanyProfile companyProfile;
    private Double currentPrice;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyProfile {
        private String companyDescription;
        private String mgIndustry;
        private PeerCompanyList[] peerCompanyList;
    }

    public class PeerCompanyList {
        public String tickerId;
        public String companyName;
        public String marketCap;
        public String priceEarningRatio;
        public String price;
    }
}


