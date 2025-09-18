package com.easytrax.easytraxbackend.hscode.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "HS 코드 분류 결과")
@Builder
public record HSCodeClassificationResult(
        @Schema(description = "분류된 HS 코드", example = "1902301000")
        String hsCode,

        @Schema(description = "분류 신뢰도 (0.0-1.0)", example = "0.95")
        Double confidence,

        @Schema(description = "분류 근거", example = "제품명과 재질 정보를 종합하여 인스턴트 라면으로 분류")
        String reason
) {
}