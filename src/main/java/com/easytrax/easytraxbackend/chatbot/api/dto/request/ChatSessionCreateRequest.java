package com.easytrax.easytraxbackend.chatbot.api.dto.request;

import com.easytrax.easytraxbackend.chatbot.domain.ChatbotType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "채팅 세션 생성 요청")
public record ChatSessionCreateRequest(
        @Schema(description = "챗봇 타입", example = "COMPLIANCE_ANALYSIS")
        @NotNull(message = "챗봇 타입은 필수입니다")
        ChatbotType chatbotType,

        @Schema(description = "세션 제목", example = "라면 중국 수출 관련 질문")
        String title
) {
}