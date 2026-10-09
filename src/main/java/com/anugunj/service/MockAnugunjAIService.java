package com.anugunj.service;

import com.anugunj.dto.ConversationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

/**
 * Configurable AI adapter with a safe local fallback. Provider secrets are read
 * only from server environment variables and are never sent to the browser.
 */
@Service
public class MockAnugunjAIService implements AnugunjAIService {
    private static final Logger log = LoggerFactory.getLogger(MockAnugunjAIService.class);
    private static final String SYSTEM_PROMPT = "You are Anugunj, a gentle voice-first reflection companion. Reply in the same language as the user, including Gujarati or Hindi when appropriate. Be calm, concise, context-aware and curious, not certain. Never claim to read minds or diagnose. Do not force emotional analysis for ordinary statements. Offer interpretations only as possibilities, respect user corrections, and ask at most one useful follow-up. Return only a JSON object with string keys response, interpretation, followUp.";

    @Value("${ANUGUNJ_AI_API_KEY:}")
    private String apiKey = "";

    @Value("${ANUGUNJ_AI_BASE_URL:https://api.openai.com/v1}")
    private String baseUrl = "https://api.openai.com/v1";

    @Value("${ANUGUNJ_AI_MODEL:gpt-4o-mini}")
    private String model = "gpt-4o-mini";

    @Autowired(required = false)
    private RestClient.Builder restClientBuilder;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Override
    public ConversationResponse respond(String message) {
        return respond(message, List.of());
    }

    @Override
    public ConversationResponse respond(String message, List<Map<String, String>> history) {
        String clean = message == null ? "" : message.trim();
        if (clean.isEmpty()) {
            return localResponse(clean);
        }
        if (apiKey == null || apiKey.isBlank() || restClientBuilder == null || objectMapper == null) {
            return localResponse(clean);
        }
        try {
            String base = baseUrl == null ? "https://api.openai.com/v1" : baseUrl.replaceAll("/+$", "");
            Map<String, Object> body = Map.of(
                "model", model == null || model.isBlank() ? "gpt-4o-mini" : model,
                "temperature", 0.5,
                "response_format", Map.of("type", "json_object"),
                "messages", buildMessages(history, clean)
            );
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(5000);
            requestFactory.setReadTimeout(20000);
            RestClient client = RestClient.builder().requestFactory(requestFactory).build();
            JsonNode result = client.post()
                .uri(base + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey.trim())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
            JsonNode content = result == null ? null : result.path("choices").path(0).path("message").path("content");
            if (content == null || !content.isTextual() || content.asText().isBlank()) {
                throw new IllegalStateException("AI provider returned empty content");
            }
            JsonNode json = objectMapper.readTree(content.asText());
            String response = readText(json, "response");
            String interpretation = readText(json, "interpretation");
            String followUp = readText(json, "followUp");
            if (response == null || interpretation == null || followUp == null) {
                throw new IllegalStateException("AI provider returned incomplete fields");
            }
            return new ConversationResponse(response, interpretation, followUp);
        } catch (Exception ex) {
            log.warn("AI provider request failed; using local fallback. Cause type: {}", ex.getClass().getSimpleName());
            return localResponse(clean);
        }
    }

    private List<Map<String, String>> buildMessages(List<Map<String, String>> history, String currentMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        if (history != null) {
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> item = history.get(i);
                if (item == null) continue;
                String role = item.get("role");
                String content = item.get("content");
                if (("user".equals(role) || "assistant".equals(role)) && content != null && !content.isBlank()) {
                    String bounded = content.length() > 1500 ? content.substring(0, 1500) : content;
                    messages.add(Map.of("role", role, "content", bounded));
                }
            }
        }
        messages.add(Map.of("role", "user", "content", currentMessage));
        return messages;
    }

    private String readText(JsonNode node, String key) {
        JsonNode value = node == null ? null : node.get(key);
        if (value == null || !value.isTextual() || value.asText().isBlank()) return null;
        String text = value.asText().trim();
        return text.length() > 2000 ? text.substring(0, 2000) : text;
    }

    private ConversationResponse localResponse(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (containsAny(lower, "i bought", "i got", "i finished", "i visited", "i watched", "i learned")) {
            return new ConversationResponse(
                "Thanks for sharing that. I'd like to understand what stood out to you.",
                "I won't assume there's a hidden feeling behind it; it may simply be something you wanted to share.",
                "What would you like me to know about it?"
            );
        }
        if (containsAny(lower, "don't know", "do not know", "confused", "unsure", "uncertain", "સમજાતું નથી", "ખબર નથી", "पता नहीं", "समझ नहीं")) {
            return new ConversationResponse(
                "It sounds like you're still finding clarity around this. You don't have to solve it all at once.",
                "There may be a few ways to understand what's happening, and we don't have to choose one too quickly.",
                "What part feels least clear to you right now?"
            );
        }
        if (containsAny(lower, "anxious", "anxiety", "sad", "upset", "angry", "lonely", "afraid", "worried", "stress", "નિરાશ", "ચિંતા", "દુઃખ", "ગુસ્સો", "परेशान", "चिंता", "दुखी", "गुस्सा")) {
            return new ConversationResponse(
                "I hear you. You've put a feeling into words, even if the whole picture may not be clear yet.",
                "One possibility is that this feeling deserves a little attention, but I could be wrong. Does that fit your experience?",
                "Would you like to explore what seems connected to it, or would you prefer just to be heard?"
            );
        }
        if (message.length() < 45) {
            return new ConversationResponse(
                "Thank you for putting that into words. We can stay with it without rushing to a conclusion.",
                "I may not have the full context yet, so I won't read more into it than you've shared.",
                "What part of this feels most important to you right now?"
            );
        }
        return new ConversationResponse(
            "There's a bit to unpack here. Let's take the part that feels most relevant to you first.",
            "I can reflect on what you've written, but this is only a starting point—not a verdict about what you feel or mean.",
            "Which part would you like to explore first?"
        );
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}