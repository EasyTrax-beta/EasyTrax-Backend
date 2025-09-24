package com.easytrax.easytraxbackend.chatbot.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "채팅 메시지 전송 요청")
public record ChatMessageRequest(
        @Schema(description = "사용자 메시지 내용", example = "라면을 중국으로 수출할 때 필요한 규정이 무엇인가요?")
        @NotBlank(message = "메시지 내용은 필수입니다")
        @Size(max = 2000, message = "메시지는 2000자를 초과할 수 없습니다")
        String content
) {
}