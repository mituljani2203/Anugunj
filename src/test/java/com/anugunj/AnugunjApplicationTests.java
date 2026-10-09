package com.anugunj;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.anugunj.dto.ConversationResponse;
import com.anugunj.service.MockAnugunjAIService;
import org.junit.jupiter.api.Test;

class AnugunjApplicationTests {
    private final MockAnugunjAIService service = new MockAnugunjAIService();

    @Test
    void returnsStructuredReflectionForOrdinaryStatement() {
        ConversationResponse response = service.respond("I bought a new tablet");
        assertNotNull(response.response());
        assertNotNull(response.interpretation());
        assertNotNull(response.followUp());
        assertFalse(response.response().isBlank());
    }

    @Test
    void avoidsInventingHiddenEmotionForOrdinaryStatement() {
        ConversationResponse response = service.respond("I bought a new tablet");
        org.junit.jupiter.api.Assertions.assertTrue(
            response.interpretation().contains("won't assume"));
    }

    @Test
    void respondsToUncertaintyWithOneFollowUp() {
        ConversationResponse response = service.respond("I don't know what to do");
        assertTrueContains(response.followUp(), "What part");
    }

    @Test
    void handlesBlankInputWithoutCrashing() {
        ConversationResponse response = service.respond("");
        assertNotNull(response);
        assertFalse(response.response().isBlank());
    }

    private void assertTrueContains(String actual, String expected) {
        org.junit.jupiter.api.Assertions.assertTrue(actual.contains(expected),
            "Expected text to contain: " + expected);
    }
}