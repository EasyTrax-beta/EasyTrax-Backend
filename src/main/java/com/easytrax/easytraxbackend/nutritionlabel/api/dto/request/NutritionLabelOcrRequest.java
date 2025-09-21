package com.easytrax.easytraxbackend.nutritionlabel.api.dto.request;

import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "영양성분표 OCR 요청")
public record NutritionLabelOcrRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "라벨 포맷", example = "USA_FDA")
        @NotNull(message = "라벨 포맷은 필수입니다")
        LabelFormat labelFormat
) {
}