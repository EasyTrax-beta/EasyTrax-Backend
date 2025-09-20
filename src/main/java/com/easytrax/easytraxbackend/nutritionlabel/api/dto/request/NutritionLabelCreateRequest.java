package com.easytrax.easytraxbackend.nutritionlabel.api.dto.request;

import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "영양성분표 생성 요청")
public record NutritionLabelCreateRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "제품명", example = "Apple Juice")
        @NotBlank(message = "제품명은 필수입니다")
        String productName,

        @Schema(description = "1회 제공량", example = "2/3 cup (55g)")
        @NotBlank(message = "1회 제공량은 필수입니다")
        String servingSize,

        @Schema(description = "총 제공 횟수", example = "8")
        @Min(value = 1, message = "총 제공 횟수는 1 이상이어야 합니다")
        Integer servingsPerContainer,

        @Schema(description = "칼로리", example = "230")
        @NotNull(message = "칼로리는 필수입니다")
        @Min(value = 0, message = "칼로리는 0 이상이어야 합니다")
        Integer calories,

        @Schema(description = "지방 칼로리", example = "72")
        @Min(value = 0, message = "지방 칼로리는 0 이상이어야 합니다")
        Integer caloriesFromFat,

        @Schema(description = "총 지방 (g)", example = "8.0")
        @DecimalMin(value = "0.0", message = "총 지방은 0 이상이어야 합니다")
        BigDecimal totalFat,

        @Schema(description = "포화지방 (g)", example = "1.0")
        @DecimalMin(value = "0.0", message = "포화지방은 0 이상이어야 합니다")
        BigDecimal saturatedFat,

        @Schema(description = "트랜스지방 (g)", example = "0.0")
        @DecimalMin(value = "0.0", message = "트랜스지방은 0 이상이어야 합니다")
        BigDecimal transFat,

        @Schema(description = "콜레스테롤 (mg)", example = "0.0")
        @DecimalMin(value = "0.0", message = "콜레스테롤은 0 이상이어야 합니다")
        BigDecimal cholesterol,

        @Schema(description = "나트륨 (mg)", example = "160.0")
        @DecimalMin(value = "0.0", message = "나트륨은 0 이상이어야 합니다")
        BigDecimal sodium,

        @Schema(description = "총 탄수화물 (g)", example = "37.0")
        @DecimalMin(value = "0.0", message = "총 탄수화물은 0 이상이어야 합니다")
        BigDecimal totalCarbohydrate,

        @Schema(description = "식이섬유 (g)", example = "4.0")
        @DecimalMin(value = "0.0", message = "식이섬유는 0 이상이어야 합니다")
        BigDecimal dietaryFiber,

        @Schema(description = "총 당류 (g)", example = "12.0")
        @DecimalMin(value = "0.0", message = "총 당류는 0 이상이어야 합니다")
        BigDecimal totalSugars,

        @Schema(description = "첨가당 (g)", example = "10.0")
        @DecimalMin(value = "0.0", message = "첨가당은 0 이상이어야 합니다")
        BigDecimal addedSugars,

        @Schema(description = "단백질 (g)", example = "3.0")
        @DecimalMin(value = "0.0", message = "단백질은 0 이상이어야 합니다")
        BigDecimal protein,

        @Schema(description = "비타민 D (mcg)", example = "2.0")
        @DecimalMin(value = "0.0", message = "비타민 D는 0 이상이어야 합니다")
        BigDecimal vitaminD,

        @Schema(description = "칼슘 (mg)", example = "260.0")
        @DecimalMin(value = "0.0", message = "칼슘은 0 이상이어야 합니다")
        BigDecimal calcium,

        @Schema(description = "철분 (mg)", example = "8.0")
        @DecimalMin(value = "0.0", message = "철분은 0 이상이어야 합니다")
        BigDecimal iron,

        @Schema(description = "칼륨 (mg)", example = "235.0")
        @DecimalMin(value = "0.0", message = "칼륨은 0 이상이어야 합니다")
        BigDecimal potassium,

        @Schema(description = "비타민 A (mcg)", example = "0.0")
        @DecimalMin(value = "0.0", message = "비타민 A는 0 이상이어야 합니다")
        BigDecimal vitaminA,

        @Schema(description = "비타민 C (mg)", example = "0.0")
        @DecimalMin(value = "0.0", message = "비타민 C는 0 이상이어야 합니다")
        BigDecimal vitaminC,

        @Schema(description = "라벨 포맷", example = "USA_FDA")
        @NotNull(message = "라벨 포맷은 필수입니다")
        LabelFormat labelFormat
) {
}