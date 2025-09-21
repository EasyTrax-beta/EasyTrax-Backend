package com.easytrax.easytraxbackend.invoice.api.dto.response;

import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoiceItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "상업송장 항목 응답")
public record CommercialInvoiceItemResponse(
        @Schema(description = "항목 ID")
        Long id,

        @Schema(description = "포장 개수")
        Integer packageCount,

        @Schema(description = "포장 유형")
        String packageType,

        @Schema(description = "상품 설명")
        String goodsDescription,

        @Schema(description = "수량")
        Integer quantity,

        @Schema(description = "단가")
        BigDecimal unitPrice,

        @Schema(description = "금액")
        BigDecimal amount
) {
    public static CommercialInvoiceItemResponse of(CommercialInvoiceItem item) {
        return new CommercialInvoiceItemResponse(
                item.getId(),
                item.getPackageCount(),
                item.getPackageType(),
                item.getGoodsDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getAmount()
        );
    }
}