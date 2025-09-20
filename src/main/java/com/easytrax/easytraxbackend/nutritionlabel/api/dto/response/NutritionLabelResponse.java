package com.easytrax.easytraxbackend.nutritionlabel.api.dto.response;

import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import com.easytrax.easytraxbackend.nutritionlabel.domain.NutritionLabel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "영양성분표 응답")
public record NutritionLabelResponse(
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

        @Schema(description = "지방 칼로리")
        Integer caloriesFromFat,

        @Schema(description = "총 지방 (g)")
        BigDecimal totalFat,

        @Schema(description = "포화지방 (g)")
        BigDecimal saturatedFat,

        @Schema(description = "트랜스지방 (g)")
        BigDecimal transFat,

        @Schema(description = "콜레스테롤 (mg)")
        BigDecimal cholesterol,

        @Schema(description = "나트륨 (mg)")
        BigDecimal sodium,

        @Schema(description = "총 탄수화물 (g)")
        BigDecimal totalCarbohydrate,

        @Schema(description = "식이섬유 (g)")
        BigDecimal dietaryFiber,

        @Schema(description = "총 당류 (g)")
        BigDecimal totalSugars,

        @Schema(description = "첨가당 (g)")
        BigDecimal addedSugars,

        @Schema(description = "단백질 (g)")
        BigDecimal protein,

        @Schema(description = "비타민 D (mcg)")
        BigDecimal vitaminD,

        @Schema(description = "칼슘 (mg)")
        BigDecimal calcium,

        @Schema(description = "철분 (mg)")
        BigDecimal iron,

        @Schema(description = "칼륨 (mg)")
        BigDecimal potassium,

        @Schema(description = "비타민 A (mcg)")
        BigDecimal vitaminA,

        @Schema(description = "비타민 C (mg)")
        BigDecimal vitaminC,

        @Schema(description = "라벨 포맷")
        LabelFormat labelFormat,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static NutritionLabelResponse of(NutritionLabel nutritionLabel) {
        return new NutritionLabelResponse(
                nutritionLabel.getId(),
                nutritionLabel.getProject().getId(),
                nutritionLabel.getProductName(),
                nutritionLabel.getServingSize(),
                nutritionLabel.getServingsPerContainer(),
                nutritionLabel.getCalories(),
                nutritionLabel.getCaloriesFromFat(),
                nutritionLabel.getTotalFat(),
                nutritionLabel.getSaturatedFat(),
                nutritionLabel.getTransFat(),
                nutritionLabel.getCholesterol(),
                nutritionLabel.getSodium(),
                nutritionLabel.getTotalCarbohydrate(),
                nutritionLabel.getDietaryFiber(),
                nutritionLabel.getTotalSugars(),
                nutritionLabel.getAddedSugars(),
                nutritionLabel.getProtein(),
                nutritionLabel.getVitaminD(),
                nutritionLabel.getCalcium(),
                nutritionLabel.getIron(),
                nutritionLabel.getPotassium(),
                nutritionLabel.getVitaminA(),
                nutritionLabel.getVitaminC(),
                nutritionLabel.getLabelFormat(),
                nutritionLabel.getCreatedAt(),
                nutritionLabel.getUpdatedAt()
        );
    }
}