package com.anugunj;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anugunj.dto.ConversationResponse;
import com.anugunj.service.MockAnugunjAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
class AnugunjApplicationTests {
    private final MockAnugunjAIService service = new MockAnugunjAIService();

    @Autowired
    private MockMvc mockMvc;

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
        org.junit.jupiter.api.Assertions.assertTrue(response.interpretation().contains("won't assume"));
    }

    @Test
    void respondsToUncertaintyWithOneFollowUp() {
        ConversationResponse response = service.respond("I don't know what to do");
        org.junit.jupiter.api.Assertions.assertTrue(response.followUp().contains("What part"));
    }

    @Test
    void handlesBlankInputWithoutCrashing() {
        ConversationResponse response = service.respond("");
        assertNotNull(response);
        assertFalse(response.response().isBlank());
    }

    @Test
    void conversationEndpointReturnsStructuredResponseAndCanResetSession() throws Exception {
        var message = post("/api/conversation/message")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"I bought a new tablet\"}");

        var result = mockMvc.perform(message)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isNotEmpty())
                .andExpect(jsonPath("$.interpretation").isNotEmpty())
                .andExpect(jsonPath("$.followUp").isNotEmpty())
                .andReturn();

        String cookie = result.getResponse().getCookie("JSESSIONID") == null ? null
                : result.getResponse().getCookie("JSESSIONID").getValue();
        var reset = post("/api/conversation/reset");
        if (cookie != null) reset.cookie(new jakarta.servlet.http.Cookie("JSESSIONID", cookie));
        mockMvc.perform(reset)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESET"));
    }

    @Test
    void rejectsBlankMessageWithSafeClientError() throws Exception {
        mockMvc.perform(post("/api/conversation/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").isNotEmpty());
    }

    @Test
    void limitsRepeatedRequestsWithinOneSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(post("/api/conversation/message")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"message":"Hello"}
                                    """))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/api/conversation/message")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Hello"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"));
    }
}
