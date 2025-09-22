package com.easytrax.easytraxbackend.compliance.api.dto.request;

import com.easytrax.easytraxbackend.global.validation.annotation.ValidCountryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수출 준수성 분석 요청")
public record ComplianceAnalysisRequest(
        
        @Schema(description = "영양성분표 ID", example = "1")
        @NotNull(message = "영양성분표 ID는 필수입니다")
        Long nutritionLabelId,
        
        @Schema(description = "제품 카테고리", example = "FOOD_SUPPLEMENT")
        @NotNull(message = "제품 카테고리는 필수입니다")
        String productCategory
) {
}