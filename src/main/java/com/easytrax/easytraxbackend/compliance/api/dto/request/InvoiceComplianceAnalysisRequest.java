package com.easytrax.easytraxbackend.compliance.api.dto.request;

import com.easytrax.easytraxbackend.compliance.domain.TradeType;
import com.easytrax.easytraxbackend.global.validation.annotation.ValidCountryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "상업송장 준수성 분석 요청")
public record InvoiceComplianceAnalysisRequest(
        
        @Schema(description = "상업송장 ID", example = "1")
        @NotNull(message = "상업송장 ID는 필수입니다")
        Long commercialInvoiceId,
        
        @Schema(description = "무역 유형", example = "EXPORT")
        @NotNull(message = "무역 유형은 필수입니다")
        TradeType tradeType
) {
}