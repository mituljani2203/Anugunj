package com.anugunj.service;
import com.anugunj.dto.ConversationResponse;
import org.springframework.stereotype.Service;
@Service
public class MockAnugunjAIService implements AnugunjAIService {
 public ConversationResponse respond(String message) {
  String clean = message == null ? "" : message.trim();
  return new ConversationResponse("Thank you for putting that into words. We can take it one step at a time.",
   "One possible reading is that there may be more to explore here—but I could be wrong. You are the best judge of what fits.",
   clean.length() < 45 ? "What part of this feels most important to you right now?" : "Would you like to explore one part of what you shared?");
 }
}