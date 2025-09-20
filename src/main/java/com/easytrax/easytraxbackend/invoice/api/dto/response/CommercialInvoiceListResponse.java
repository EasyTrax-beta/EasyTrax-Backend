package com.easytrax.easytraxbackend.invoice.api.dto.response;

import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "상업송장 목록 응답")
public record CommercialInvoiceListResponse(
        @Schema(description = "상업송장 ID")
        Long id,

        @Schema(description = "프로젝트 ID")
        Long projectId,

        @Schema(description = "송장 번호")
        String invoiceNumber,

        @Schema(description = "송장 일자")
        LocalDate invoiceDate,

        @Schema(description = "발송인/판매자 이름")
        String shipperSellerName,

        @Schema(description = "구매자 이름")
        String buyerName,

        @Schema(description = "출발 국가")
        String fromCountry,

        @Schema(description = "총 금액")
        BigDecimal totalAmount,

        @Schema(description = "생성일시")
        LocalDateTime createdAt
) {
    public static CommercialInvoiceListResponse of(CommercialInvoice commercialInvoice) {
        return new CommercialInvoiceListResponse(
                commercialInvoice.getId(),
                commercialInvoice.getProject().getId(),
                commercialInvoice.getInvoiceNumber(),
                commercialInvoice.getInvoiceDate(),
                commercialInvoice.getShipperSellerName(),
                commercialInvoice.getBuyerName(),
                commercialInvoice.getFromCountry(),
                commercialInvoice.getTotalAmount(),
                commercialInvoice.getCreatedAt()
        );
    }
}