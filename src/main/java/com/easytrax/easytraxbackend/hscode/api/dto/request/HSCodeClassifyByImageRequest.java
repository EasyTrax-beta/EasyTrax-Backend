package com.easytrax.easytraxbackend.hscode.api.dto.request;

import com.easytrax.easytraxbackend.project.domain.Country;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "이미지 기반 HS 코드 분류 요청")
public record HSCodeClassifyByImageRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "원산지", example = "KOREA")
        Country originCountry,

        @Schema(description = "수출 대상국", example = "CHINA")
        Country targetCountry
) {
}