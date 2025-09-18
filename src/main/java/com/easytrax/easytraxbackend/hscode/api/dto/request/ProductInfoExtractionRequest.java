package com.easytrax.easytraxbackend.hscode.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "제품 정보 추출 요청")
public record ProductInfoExtractionRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId
) {
}