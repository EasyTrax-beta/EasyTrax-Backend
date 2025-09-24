package com.easytrax.easytraxbackend.chatbot.api.dto.response;

import com.easytrax.easytraxbackend.chatbot.domain.ChatSession;
import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅 세션 목록 응답")
public record ChatSessionListResponse(
        @Schema(description = "세션 ID", example = "1")
        Long id,

        @Schema(description = "챗봇 타입", example = "COMPLIANCE_ANALYSIS")
        ChatbotType chatbotType,

        @Schema(description = "세션 제목", example = "라면 중국 수출 관련 질문")
        String title,

        @Schema(description = "마지막 메시지 내용", example = "네, 도움이 되었습니다.")
        String lastMessage,

        @Schema(description = "메시지 개수", example = "5")
        int messageCount,

        @Schema(description = "생성 시간", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 시간", example = "2024-01-01T10:00:00")
        LocalDateTime updatedAt
) {
    public static ChatSessionListResponse from(ChatSession chatSession) {
        String lastMessage = chatSession.getMessages().isEmpty() ? 
                null : 
                chatSession.getMessages().get(chatSession.getMessages().size() - 1).getContent();
        
        return new ChatSessionListResponse(
                chatSession.getId(),
                chatSession.getChatbotType(),
                chatSession.getTitle(),
                lastMessage,
                chatSession.getMessages().size(),
                chatSession.getCreatedAt(),
                chatSession.getUpdatedAt()
        );
    }
    
    public ChatSessionListResponse(Long id, ChatbotType chatbotType, String title, 
                                   String lastMessage, Long messageCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, chatbotType, title, lastMessage, messageCount.intValue(), createdAt, updatedAt);
    }
}