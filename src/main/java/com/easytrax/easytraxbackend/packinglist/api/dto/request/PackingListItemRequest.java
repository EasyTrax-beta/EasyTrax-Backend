package com.easytrax.easytraxbackend.packinglist.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "포장명세서 항목 요청")
public record PackingListItemRequest(
        @Schema(description = "상품 설명", example = "Apple Juice 1L")
        @NotBlank(message = "상품 설명은 필수입니다")
        String description,

        @Schema(description = "수량", example = "100")
        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity,

        @Schema(description = "단위", example = "boxes")
        @NotBlank(message = "단위는 필수입니다")
        String unitOfMeasure,

        @Schema(description = "총 중량 (kg)", example = "120.5")
        @NotNull(message = "총 중량은 필수입니다")
        @DecimalMin(value = "0.01", message = "총 중량은 0보다 커야 합니다")
        Double grossWeight,

        @Schema(description = "순 중량 (kg)", example = "110.0")
        @NotNull(message = "순 중량은 필수입니다")
        @DecimalMin(value = "0.01", message = "순 중량은 0보다 커야 합니다")
        Double netWeight,

        @Schema(description = "부피 (m³)", example = "2.5")
        @DecimalMin(value = "0.01", message = "부피는 0보다 커야 합니다")
        Double volume,

        @Schema(description = "크기", example = "30x40x50cm")
        String dimensions,

        @Schema(description = "포장 유형", example = "Carton Box")
        String packageType
) {
}