package com.easytrax.easytraxbackend.nutritionlabel.api.dto.response;

import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import com.easytrax.easytraxbackend.nutritionlabel.domain.NutritionLabel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "영양성분표 목록 응답")
public record NutritionLabelListResponse(
        @Schema(description = "영양성분표 ID")
        Long id,

        @Schema(description = "프로젝트 ID")
        Long projectId,

        @Schema(description = "제품명")
        String productName,

        @Schema(description = "1회 제공량")
        String servingSize,

        @Schema(description = "총 제공 횟수")
        Integer servingsPerContainer,

        @Schema(description = "칼로리")
        Integer calories,

        @Schema(description = "라벨 포맷")
        LabelFormat labelFormat,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
    public static NutritionLabelListResponse of(NutritionLabel nutritionLabel) {
        return new NutritionLabelListResponse(
                nutritionLabel.getId(),
                nutritionLabel.getProject().getId(),
                nutritionLabel.getProductName(),
                nutritionLabel.getServingSize(),
                nutritionLabel.getServingsPerContainer(),
                nutritionLabel.getCalories(),
                nutritionLabel.getLabelFormat(),
                nutritionLabel.getCreatedAt()
        );
    }
}