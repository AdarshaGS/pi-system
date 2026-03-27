package com.pisystem.modules.ai.assistant.controller;

import com.pisystem.modules.ai.assistant.dto.ChatRequest;
import com.pisystem.modules.ai.assistant.dto.ChatResponse;
import com.pisystem.modules.ai.assistant.service.AiAssistantService;
import com.pisystem.shared.security.AuthenticationHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Context-aware financial advisor chat")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final AuthenticationHelper authenticationHelper;

    @PostMapping("/chat")
    @Operation(summary = "Chat with Pi-Assistant", description = "Sends a message to the AI assistant with full financial context")
    @ApiResponse(responseCode = "200", description = "Response received from AI")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        Long userId = authenticationHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(aiAssistantService.processChat(userId, request));
    }
}
