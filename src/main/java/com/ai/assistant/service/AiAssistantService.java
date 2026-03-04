package com.ai.assistant.service;

import com.ai.assistant.dto.ChatRequest;
import com.ai.assistant.dto.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiAssistantService {

    private final FinancialContextService contextService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    // @Value("${ai.api.key:AIzaSyDl8AzsPepkVShZdv6wlz_xLf20jQZBe6w}") -> mine
    @Value("${ai.api.key:AIzaSyDU7whJi8mjmAn9ybZHCHyZRukQac4AUtk}")
    private String apiKey;

    // models I can use
    // gemini-2.5-flash -> 20 RPD

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
    // Google Gemini API Configuration (Stable v1)
    private static final String SYSTEM_PROMPT = """
                        You are an AI Analysis Engine operating behind an LLM Router inside the PI System.

            SYSTEM ROLE:
            - You do NOT know which LLM provider is being used.
            - You do NOT know API keys, endpoints, or infrastructure details.
            - Model selection, routing, retries, and failover are handled externally by the PI System.

            ANALYSIS MODE:
            - The system provides an explicit ANALYSIS_LENS.
            - You MUST restrict reasoning strictly to the active lens.

            SUPPORTED ANALYSIS LENSES:
            1. EXPENSE ANALYST – Spending patterns, categories, budget utilization.
            2. PROTECTION ANALYST – Insurance coverage gaps only.
            3. LIQUIDITY ANALYST – Emergency fund adequacy and stability buffers.
            4. GROWTH ANALYST – Portfolio allocation, asset mix, and growth readiness.

            STRICT CONSTRAINTS:
            1. NEVER reference model names, providers, APIs, keys, or infrastructure.
            2. NEVER assume or infer data not explicitly present in USER_CONTEXT.
            3. NEVER generate content outside supported financial modules:
               Portfolio, Net Worth, Budgeting, Loans, Protection, Recurring.
            4. NEVER use advisory or imperative language such as "you should", "buy", or "start".
               Use only neutral phrasing:
               - "The system identifies..."
               - "Data indicates..."
               - "Mathematical analysis shows..."

            STRICT CALCULATION RULES:
            - Anchor every statement to numeric values in USER_CONTEXT.
            - Emergency Fund = 6 × totalSpent (only for LIQUIDITY lens).
            - Currency must ALWAYS be INR (₹). Never use $.

            STRUCTURAL ADJUSTMENT RULES:
            - You MUST identify logic-based observations framed as "System Adjustments".
            - Adjustments describe structural conditions, NOT user actions.
            - If no adjustments are logically possible, explicitly state this.

            MANDATORY OUTPUT FORMAT (NO DEVIATION):

            LENS: <ANALYSIS_LENS>

            ### Data Observations
            - Bullet-point factual observations derived directly from USER_CONTEXT.

            ### Mathematical Implications
            - Bullet-point mathematical interpretations or derived ratios.
            - If no calculations are possible, state why.

            ### Suggested System Adjustments
            - Bullet-point logical structural signals.
            - If none apply, state:
              "No system-level structural adjustments are logically indicated based on the current data."

            FAIL-SAFE CONDITIONS:
            - If required data is missing:
              "The system lacks sufficient data for this analysis."
            - If the request is out of scope:
              "The requested analysis falls outside the available financial data scope."

            You are a deterministic financial analysis narrator, not a conversational assistant.
                        """;

    public ChatResponse processChat(Long userId, ChatRequest request) {
        String userMessage = request.getMessage() != null ? request.getMessage().toLowerCase() : "";

        // --- IN-SCOPE LAYER (PRE-LLM CHECK) ---
        if (!isWithinScope(userMessage)) {
            log.info("Request blocked by In-Scope Layer: {}", userMessage);
            return ChatResponse.builder()
                    .response(
                            "The PI Financial Analyst system is designed to process and interpret financial data provided within the user context. The current request falls outside the scope of financial metadata analysis.")
                    .build();
        }

        String detectedLens = detectLens(userMessage);

        try {
            Map<String, Object> context = contextService.getUserFinancialSnapshot(userId);

            String contextJson = objectMapper.writeValueAsString(context);
            log.info("FINAL_PROMPT_CONTEXT for User {}: {}", userId, contextJson);
            log.info("Sending context to Gemini for User: {}", userId);

            String fullPrompt = String.format("%s\n\nDETECTED_LENS: %s\n\nUSER_CONTEXT: %s\n\nUSER_MESSAGE: %s",
                    SYSTEM_PROMPT, detectedLens, contextJson, request.getMessage());

            // Gemini Request Structure
            Map<String, Object> contents = Map.of(
                    "parts", java.util.List.of(Map.of("text", fullPrompt)));
            Map<String, Object> body = Map.of(
                    "contents", java.util.List.of(contents));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(GEMINI_URL + apiKey, entity, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

                    if (content != null) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content
                                .get("parts");

                        if (parts != null && !parts.isEmpty()) {
                            String resultText = (String) parts.get(0).get("text");

                            return ChatResponse.builder()
                                    .response(resultText)
                                    .suggestion("Review your Liability structure")
                                    .build();
                        }
                    }
                }
            }

            return ChatResponse.builder()
                    .response(
                            "I've analyzed your data, but I'm having trouble formulating a plan. Your profile seems stable.")
                    .build();

        } catch (Exception e) {
            log.error("Gemini API Error", e);
            String errorMsg = e.getMessage();
            // Handle specific 429 or credential issues
            if (errorMsg.contains("403") || errorMsg.contains("401")) {
                errorMsg = "Invalid Gemini API Key or Permissions.";
            }
            return ChatResponse.builder()
                    .response("I'm having trouble connecting to my Gemini brain. Detail: " + errorMsg)
                    .build();
        }
    }

    /**
     * Programmatic scope check to prevent unnecessary LLM calls.
     * Checks if the message contains keywords related to supported project modules.
     */
    private boolean isWithinScope(String message) {
        if (message == null || message.trim().isEmpty())
            return false;

        List<String> scopeKeywords = List.of(
                "portfolio", "net worth", "asset", "liability", "liabilities", "budget", "expense", "expenses",
                "income", "loan", "loans", "emi", "debt", "insurance", "coverage", "sip", "recurring",
                "wealth", "financial", "analyze", "structure", "improvement", "adjustment", "invest", "spending",
                "saving", "protection", "risk", "health", "life", "plan", "strategy", "improve", "shortfall",
                "surplus");

        return scopeKeywords.stream().anyMatch(message::contains);
    }

    /**
     * Identifies the active analysis lens based on keywords.
     */
    private String detectLens(String message) {
        if (message.contains("expense") || message.contains("spending") || message.contains("budget")) {
            return "EXPENSE ANALYST";
        }
        if (message.contains("insurance") || message.contains("protection") || message.contains("coverage")
                || message.contains("life") || message.contains("health")) {
            return "PROTECTION ANALYST";
        }
        if (message.contains("emergency") || message.contains("liquidity") || message.contains("stability")
                || message.contains("savings")) {
            return "LIQUIDITY ANALYST";
        }
        if (message.contains("portfolio") || message.contains("growth") || message.contains("investment")
                || message.contains("stock") || message.contains("net worth")) {
            return "GROWTH ANALYST";
        }
        return "GENERAL FINANCIAL ANALYST";
    }
}
