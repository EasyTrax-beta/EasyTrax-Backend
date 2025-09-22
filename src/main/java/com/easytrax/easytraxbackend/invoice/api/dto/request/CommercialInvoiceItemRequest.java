package com.easytrax.easytraxbackend.invoice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "상업송장 항목 요청")
public record CommercialInvoiceItemRequest(
        @Schema(description = "포장 개수", example = "500")
        @NotNull(message = "포장 개수는 필수입니다")
        @Min(value = 1, message = "포장 개수는 1 이상이어야 합니다")
        Integer packageCount,

        @Schema(description = "포장 유형", example = "boxes")
        @NotBlank(message = "포장 유형은 필수입니다")
        String packageType,

        @Schema(description = "상품 설명", example = "Apple Juice Concentrate")
        @NotBlank(message = "상품 설명은 필수입니다")
        String goodsDescription,

        @Schema(description = "HS 코드", example = "2009.11.00")
        @NotBlank(message = "HS 코드는 필수입니다")
        @Size(max = 20, message = "HS 코드는 20자를 초과할 수 없습니다")
        String hsCode,

        @Schema(description = "원산지", example = "Korea")
        @NotBlank(message = "원산지는 필수입니다")
        @Size(max = 100, message = "원산지는 100자를 초과할 수 없습니다")
        String countryOfOrigin,

        @Schema(description = "수량", example = "500")
        @NotNull(message = "수량은 필수입니다")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        Integer quantity,

        @Schema(description = "단가", example = "12.50")
        @NotNull(message = "단가는 필수입니다")
        @DecimalMin(value = "0.01", message = "단가는 0보다 커야 합니다")
        BigDecimal unitPrice
) {
}