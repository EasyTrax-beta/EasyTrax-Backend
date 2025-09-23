package com.easytrax.easytraxbackend.nutritionlabel.api.dto.request;

import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "영양성분표 생성 요청")
public record NutritionLabelCreateRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "제품명", example = "Apple Juice")
        @NotBlank(message = "제품명은 필수입니다")
        @Size(max = 255, message = "제품명은 255자를 초과할 수 없습니다")
        String productName,

        @Schema(description = "원산지", example = "Korea")
        @NotBlank(message = "원산지는 필수입니다")
        @Size(max = 100, message = "원산지는 100자를 초과할 수 없습니다")
        String countryOfOrigin,

        @Schema(description = "1회 제공량", example = "2/3 cup (55g)")
        @NotBlank(message = "1회 제공량은 필수입니다")
        @Size(max = 255, message = "1회 제공량은 255자를 초과할 수 없습니다")
        String servingSize,

        @Schema(description = "총 제공 횟수", example = "8")
        @NotNull(message = "총 제공 횟수는 필수입니다")
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

        @Schema(description = "총 지방 DV%", example = "10")
        @Min(value = 0, message = "총 지방 DV%는 0 이상이어야 합니다")
        Integer totalFatDV,

        @Schema(description = "포화지방 (g)", example = "1.0")
        @DecimalMin(value = "0.0", message = "포화지방은 0 이상이어야 합니다")
        BigDecimal saturatedFat,

        @Schema(description = "포화지방 DV%", example = "5")
        @Min(value = 0, message = "포화지방 DV%는 0 이상이어야 합니다")
        Integer saturatedFatDV,

        @Schema(description = "트랜스지방 (g)", example = "0.0")
        @DecimalMin(value = "0.0", message = "트랜스지방은 0 이상이어야 합니다")
        BigDecimal transFat,

        @Schema(description = "콜레스테롤 (mg)", example = "0.0")
        @DecimalMin(value = "0.0", message = "콜레스테롤은 0 이상이어야 합니다")
        BigDecimal cholesterol,

        @Schema(description = "콜레스테롤 DV%", example = "0")
        @Min(value = 0, message = "콜레스테롤 DV%는 0 이상이어야 합니다")
        Integer cholesterolDV,

        @Schema(description = "나트륨 (mg)", example = "160.0")
        @DecimalMin(value = "0.0", message = "나트륨은 0 이상이어야 합니다")
        BigDecimal sodium,

        @Schema(description = "나트륨 DV%", example = "7")
        @Min(value = 0, message = "나트륨 DV%는 0 이상이어야 합니다")
        Integer sodiumDV,

        @Schema(description = "총 탄수화물 (g)", example = "37.0")
        @DecimalMin(value = "0.0", message = "총 탄수화물은 0 이상이어야 합니다")
        BigDecimal totalCarbohydrate,

        @Schema(description = "총 탄수화물 DV%", example = "13")
        @Min(value = 0, message = "총 탄수화물 DV%는 0 이상이어야 합니다")
        Integer totalCarbohydrateDV,

        @Schema(description = "식이섬유 (g)", example = "4.0")
        @DecimalMin(value = "0.0", message = "식이섬유는 0 이상이어야 합니다")
        BigDecimal dietaryFiber,

        @Schema(description = "식이섬유 DV%", example = "14")
        @Min(value = 0, message = "식이섬유 DV%는 0 이상이어야 합니다")
        Integer dietaryFiberDV,

        @Schema(description = "총 당류 (g)", example = "12.0")
        @DecimalMin(value = "0.0", message = "총 당류는 0 이상이어야 합니다")
        BigDecimal totalSugars,

        @Schema(description = "첨가당 (g)", example = "10.0")
        @DecimalMin(value = "0.0", message = "첨가당은 0 이상이어야 합니다")
        BigDecimal addedSugars,

        @Schema(description = "첨가당 DV%", example = "20")
        @Min(value = 0, message = "첨가당 DV%는 0 이상이어야 합니다")
        Integer addedSugarsDV,

        @Schema(description = "단백질 (g)", example = "3.0")
        @DecimalMin(value = "0.0", message = "단백질은 0 이상이어야 합니다")
        BigDecimal protein,

        @Schema(description = "단백질 DV%", example = "6")
        @Min(value = 0, message = "단백질 DV%는 0 이상이어야 합니다")
        Integer proteinDV,

        @Schema(description = "비타민 D (mcg)", example = "2.0")
        @DecimalMin(value = "0.0", message = "비타민 D는 0 이상이어야 합니다")
        BigDecimal vitaminD,

        @Schema(description = "비타민 D DV%", example = "10")
        @Min(value = 0, message = "비타민 D DV%는 0 이상이어야 합니다")
        Integer vitaminDDV,

        @Schema(description = "칼슘 (mg)", example = "260.0")
        @DecimalMin(value = "0.0", message = "칼슘은 0 이상이어야 합니다")
        BigDecimal calcium,

        @Schema(description = "칼슘 DV%", example = "20")
        @Min(value = 0, message = "칼슘 DV%는 0 이상이어야 합니다")
        Integer calciumDV,

        @Schema(description = "철분 (mg)", example = "8.0")
        @DecimalMin(value = "0.0", message = "철분은 0 이상이어야 합니다")
        BigDecimal iron,

        @Schema(description = "철분 DV%", example = "45")
        @Min(value = 0, message = "철분 DV%는 0 이상이어야 합니다")
        Integer ironDV,

        @Schema(description = "칼륨 (mg)", example = "235.0")
        @DecimalMin(value = "0.0", message = "칼륨은 0 이상이어야 합니다")
        BigDecimal potassium,

        @Schema(description = "칼륨 DV%", example = "5")
        @Min(value = 0, message = "칼륨 DV%는 0 이상이어야 합니다")
        Integer potassiumDV,

        @Schema(description = "비타민 A (mcg)", example = "0.0")
        @DecimalMin(value = "0.0", message = "비타민 A는 0 이상이어야 합니다")
        BigDecimal vitaminA,

        @Schema(description = "비타민 A DV%", example = "0")
        @Min(value = 0, message = "비타민 A DV%는 0 이상이어야 합니다")
        Integer vitaminADV,

        @Schema(description = "비타민 C (mg)", example = "0.0")
        @DecimalMin(value = "0.0", message = "비타민 C는 0 이상이어야 합니다")
        BigDecimal vitaminC,

        @Schema(description = "비타민 C DV%", example = "0")
        @Min(value = 0, message = "비타민 C DV%는 0 이상이어야 합니다")
        Integer vitaminCDV,

        @Schema(description = "라벨 포맷", example = "USA_FDA")
        @NotNull(message = "라벨 포맷은 필수입니다")
        LabelFormat labelFormat
) {
}