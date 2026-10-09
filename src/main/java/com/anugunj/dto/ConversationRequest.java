package com.anugunj.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ConversationRequest(@NotBlank(message="Please enter a message.") @Size(max=4000,message="Please keep your message under 4000 characters.") String message) {}