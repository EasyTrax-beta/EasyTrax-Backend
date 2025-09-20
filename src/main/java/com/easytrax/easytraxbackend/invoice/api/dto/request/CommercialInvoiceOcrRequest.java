package com.easytrax.easytraxbackend.invoice.api.dto.request;

import com.easytrax.easytraxbackend.invoice.domain.InvoiceFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상업송장 OCR 요청")
public record CommercialInvoiceOcrRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "송장 포맷", example = "USA_STANDARD")
        @NotNull(message = "송장 포맷은 필수입니다")
        InvoiceFormat invoiceFormat
) {
}