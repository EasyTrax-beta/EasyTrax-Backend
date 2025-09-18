package com.easytrax.easytraxbackend.hscode.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "OCR 제품 정보 추출 결과")
@Builder
public record ProductOcrResult(
        @Schema(description = "제품명", example = "신라면")
        String productName,

        @Schema(description = "용도", example = "식품, 인스턴트 라면")
        String purpose,

        @Schema(description = "제품 설명", example = "매운맛 인스턴트 라면")
        String description,

        @Schema(description = "재질/원료", example = "밀가루, 야자유, 조미료")
        String material,

        @Schema(description = "신뢰도 점수 (0.0-1.0)", example = "0.95")
        Double confidenceScore
) {
}