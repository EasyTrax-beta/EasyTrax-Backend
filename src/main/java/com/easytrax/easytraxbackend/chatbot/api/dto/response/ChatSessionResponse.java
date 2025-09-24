package com.easytrax.easytraxbackend.chatbot.api.dto.response;

import com.easytrax.easytraxbackend.chatbot.domain.ChatSession;
import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "채팅 세션 응답")
public record ChatSessionResponse(
        @Schema(description = "세션 ID", example = "1")
        Long id,

        @Schema(description = "챗봇 타입", example = "COMPLIANCE_ANALYSIS")
        ChatbotType chatbotType,

        @Schema(description = "세션 제목", example = "라면 중국 수출 관련 질문")
        String title,

        @Schema(description = "메시지 목록")
        List<ChatMessageResponse> messages,

        @Schema(description = "생성 시간", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 시간", example = "2024-01-01T10:00:00")
        LocalDateTime updatedAt
) {
    public static ChatSessionResponse from(ChatSession chatSession) {
        return new ChatSessionResponse(
                chatSession.getId(),
                chatSession.getChatbotType(),
                chatSession.getTitle(),
                chatSession.getMessages().stream()
                        .map(ChatMessageResponse::from)
                        .toList(),
                chatSession.getCreatedAt(),
                chatSession.getUpdatedAt()
        );
    }
}