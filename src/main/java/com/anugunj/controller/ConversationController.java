package com.anugunj.controller;

import com.anugunj.dto.ConversationRequest;
import com.anugunj.dto.ConversationResponse;
import com.anugunj.service.AnugunjAIService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
    private static final String HISTORY_KEY = "anugunjConversationHistory";
    private static final int MAX_HISTORY_MESSAGES = 8;
    private final AnugunjAIService aiService;

    public ConversationController(AnugunjAIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/message")
    public ResponseEntity<ConversationResponse> message(
            @Valid @RequestBody ConversationRequest request, HttpSession session) {
        List<Map<String, String>> history = getHistory(session);
        ConversationResponse response = aiService.respond(request.message(), List.copyOf(history));

        history.add(Map.of("role", "user", "content", request.message().trim()));
        history.add(Map.of("role", "assistant", "content",
                response.response() + "\n" + response.interpretation() + "\n" + response.followUp()));
        while (history.size() > MAX_HISTORY_MESSAGES) {
            history.remove(0);
        }
        session.setAttribute(HISTORY_KEY, history);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> reset(HttpSession session) {
        session.removeAttribute(HISTORY_KEY);
        return ResponseEntity.ok(Map.of("status", "RESET"));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getHistory(HttpSession session) {
        Object stored = session.getAttribute(HISTORY_KEY);
        if (stored instanceof List<?>) {
            List<?> values = (List<?>) stored;
            List<Map<String, String>> valid = new ArrayList<>();
            for (Object value : values) {
                if (value instanceof Map<?, ?> item
                        && ("user".equals(item.get("role")) || "assistant".equals(item.get("role")))
                        && item.get("content") instanceof String text) {
                    String bounded = text.length() > 1500 ? text.substring(0, 1500) : text;
                    valid.add(Map.of("role", (String) item.get("role"), "content", bounded));
                }
            }
            while (valid.size() > MAX_HISTORY_MESSAGES) valid.remove(0);
            return valid;
        }
        return new ArrayList<>();
    }
}