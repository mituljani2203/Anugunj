package com.anugunj.service;

import com.anugunj.dto.ConversationResponse;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Safe offline response engine used when no AI provider is configured.
 * This is a prototype fallback, not a real language model.
 */
@Service
public class MockAnugunjAIService implements AnugunjAIService {
    @Override
    public ConversationResponse respond(String message) {
        String clean = message == null ? "" : message.trim();
        String lower = clean.toLowerCase(Locale.ROOT);

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
        if (clean.length() < 45) {
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
        for (String term : terms) {
            if (text.contains(term)) return true;
        }
        return false;
    }
}