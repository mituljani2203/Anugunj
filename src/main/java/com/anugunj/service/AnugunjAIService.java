package com.anugunj.service;

import com.anugunj.dto.ConversationResponse;
import java.util.List;
import java.util.Map;

public interface AnugunjAIService {
    ConversationResponse respond(String message);

    default ConversationResponse respond(String message, List<Map<String, String>> history) {
        return respond(message);
    }
}