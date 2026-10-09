package com.anugunj.controller;
import com.anugunj.dto.ConversationRequest;
import com.anugunj.dto.ConversationResponse;
import com.anugunj.service.AnugunjAIService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/conversation")
public class ConversationController {
 private final AnugunjAIService aiService;
 public ConversationController(AnugunjAIService aiService) { this.aiService = aiService; }
 @PostMapping("/message")
 public ResponseEntity<ConversationResponse> message(@Valid @RequestBody ConversationRequest request) {
  return ResponseEntity.ok(aiService.respond(request.message()));
 }
}