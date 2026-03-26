package com.investments.stocks.diversification.sectors.service;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SectorNormalizer {

    private static final java.util.Map<String, String> KEYWORD_MAPPING = new java.util.HashMap<>();

    static {
        // Finance -> Financials
        KEYWORD_MAPPING.put("bank", "Financials");
        KEYWORD_MAPPING.put("finance", "Financials");
        KEYWORD_MAPPING.put("invest", "Financials");
        KEYWORD_MAPPING.put("insurance", "Financials");
        KEYWORD_MAPPING.put("holdings", "Financials");

        // Alcohol -> Consumer Staples
        KEYWORD_MAPPING.put("breweries", "Consumer Staples");
        KEYWORD_MAPPING.put("distilleries", "Consumer Staples");

        // Tech -> Information Technology
        KEYWORD_MAPPING.put("software", "Information Technology");
        KEYWORD_MAPPING.put("computers", "Information Technology");
        KEYWORD_MAPPING.put("it", "Information Technology");
        KEYWORD_MAPPING.put("technology", "Information Technology");

        // Healthcare -> Health Care
        KEYWORD_MAPPING.put("pharma", "Health Care");
        KEYWORD_MAPPING.put("drug", "Health Care");
        KEYWORD_MAPPING.put("health", "Health Care");
        KEYWORD_MAPPING.put("biotech", "Health Care");
        KEYWORD_MAPPING.put("hospital", "Health Care");

        // Energy -> Energy / Utilities
        KEYWORD_MAPPING.put("oil", "Energy");
        KEYWORD_MAPPING.put("gas", "Energy");
        KEYWORD_MAPPING.put("petro", "Energy");
        KEYWORD_MAPPING.put("power", "Utilities");
        KEYWORD_MAPPING.put("energy", "Energy");
        KEYWORD_MAPPING.put("utilities", "Utilities");

        // Consumer
        KEYWORD_MAPPING.put("auto", "Consumer Discretionary");
        KEYWORD_MAPPING.put("motor", "Consumer Discretionary");
        KEYWORD_MAPPING.put("vehicle", "Consumer Discretionary");
        KEYWORD_MAPPING.put("textile", "Consumer Discretionary");
        KEYWORD_MAPPING.put("retail", "Consumer Discretionary");
        KEYWORD_MAPPING.put("fmcg", "Consumer Staples");
        KEYWORD_MAPPING.put("food", "Consumer Staples");
        KEYWORD_MAPPING.put("consumer", "Consumer Staples");

        // Industrials
        KEYWORD_MAPPING.put("engineer", "Industrials");
        KEYWORD_MAPPING.put("construct", "Industrials");
        KEYWORD_MAPPING.put("infra", "Industrials");
        KEYWORD_MAPPING.put("logistic", "Industrials");
        KEYWORD_MAPPING.put("transport", "Industrials");
        KEYWORD_MAPPING.put("shipping", "Industrials");
        KEYWORD_MAPPING.put("defence", "Industrials");

        // Materials
        KEYWORD_MAPPING.put("steel", "Materials");
        KEYWORD_MAPPING.put("metal", "Materials");
        KEYWORD_MAPPING.put("cement", "Materials");
        KEYWORD_MAPPING.put("chem", "Materials");
        KEYWORD_MAPPING.put("fertilizer", "Materials");
        KEYWORD_MAPPING.put("mining", "Materials");
        KEYWORD_MAPPING.put("oil", "Materials");
        KEYWORD_MAPPING.put("Coal", "Materials");

        // Telecom / Real Estate
        KEYWORD_MAPPING.put("telecom", "Communication Services");
        KEYWORD_MAPPING.put("media", "Communication Services");
        KEYWORD_MAPPING.put("entertainment", "Communication Services");
        KEYWORD_MAPPING.put("real estate", "Real Estate");
        KEYWORD_MAPPING.put("realty", "Real Estate");
    }

    public String normalize(String rawIndustry) {
        if (rawIndustry == null || rawIndustry.isBlank()) {
            return "Others";
        }

        String lower = rawIndustry.toLowerCase();

        // 1. Direct Keyword Match
        for (Map.Entry<String, String> entry : KEYWORD_MAPPING.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Fallback
        return "Others";
    }
}
