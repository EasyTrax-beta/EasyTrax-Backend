package com.easytrax.easytraxbackend.chatbot.api.dto.response;

import com.easytrax.easytraxbackend.chatbot.domain.ChatMessage;
import com.easytrax.easytraxbackend.chatbot.domain.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지 응답")
public record ChatMessageResponse(
        @Schema(description = "메시지 ID", example = "1")
        Long id,

        @Schema(description = "메시지 타입", example = "USER")
        MessageType messageType,

        @Schema(description = "메시지 내용", example = "라면을 중국으로 수출할 때 필요한 규정이 무엇인가요?")
        String content,

        @Schema(description = "생성 시간", example = "2024-01-01T10:00:00")
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getMessageType(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }
}