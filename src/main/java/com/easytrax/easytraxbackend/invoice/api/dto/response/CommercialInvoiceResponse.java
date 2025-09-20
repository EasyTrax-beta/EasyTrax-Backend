package com.easytrax.easytraxbackend.invoice.api.dto.response;

import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoice;
import com.easytrax.easytraxbackend.invoice.domain.InvoiceFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "상업송장 응답")
public record CommercialInvoiceResponse(
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

        @Schema(description = "발송인/판매자 주소")
        String shipperSellerAddress,

        @Schema(description = "발송인/판매자 연락처")
        String shipperSellerPhone,

        @Schema(description = "수취인 이름")
        String consigneeName,

        @Schema(description = "수취인 주소")
        String consigneeAddress,

        @Schema(description = "구매자 이름")
        String buyerName,

        @Schema(description = "구매자 주소")
        String buyerAddress,

        @Schema(description = "구매자 연락처")
        String buyerPhone,

        @Schema(description = "L/C 번호")
        String lcNumber,

        @Schema(description = "L/C 일자")
        LocalDate lcDate,

        @Schema(description = "출발일")
        LocalDate departureDate,

        @Schema(description = "선박/항공편")
        String vesselFlight,

        @Schema(description = "출발 국가")
        String fromCountry,

        @Schema(description = "목적지")
        String toDestination,

        @Schema(description = "선적 마크")
        String shippingMarks,

        @Schema(description = "인도 조건")
        String termsOfDelivery,

        @Schema(description = "결제 조건")
        String paymentTerms,

        @Schema(description = "기타 참조사항")
        String otherReferences,

        @Schema(description = "총 금액")
        BigDecimal totalAmount,

        @Schema(description = "송장 포맷")
        InvoiceFormat invoiceFormat,

        @Schema(description = "상업송장 항목 목록")
        List<CommercialInvoiceItemResponse> items,

        @Schema(description = "생성일시")
        LocalDateTime createdAt,

        @Schema(description = "수정일시")
        LocalDateTime updatedAt
) {
    public static CommercialInvoiceResponse of(CommercialInvoice commercialInvoice) {
        return new CommercialInvoiceResponse(
                commercialInvoice.getId(),
                commercialInvoice.getProject().getId(),
                commercialInvoice.getInvoiceNumber(),
                commercialInvoice.getInvoiceDate(),
                commercialInvoice.getShipperSellerName(),
                commercialInvoice.getShipperSellerAddress(),
                commercialInvoice.getShipperSellerPhone(),
                commercialInvoice.getConsigneeName(),
                commercialInvoice.getConsigneeAddress(),
                commercialInvoice.getBuyerName(),
                commercialInvoice.getBuyerAddress(),
                commercialInvoice.getBuyerPhone(),
                commercialInvoice.getLcNumber(),
                commercialInvoice.getLcDate(),
                commercialInvoice.getDepartureDate(),
                commercialInvoice.getVesselFlight(),
                commercialInvoice.getFromCountry(),
                commercialInvoice.getToDestination(),
                commercialInvoice.getShippingMarks(),
                commercialInvoice.getTermsOfDelivery(),
                commercialInvoice.getPaymentTerms(),
                commercialInvoice.getOtherReferences(),
                commercialInvoice.getTotalAmount(),
                commercialInvoice.getInvoiceFormat(),
                commercialInvoice.getItems().stream()
                        .map(CommercialInvoiceItemResponse::of)
                        .toList(),
                commercialInvoice.getCreatedAt(),
                commercialInvoice.getUpdatedAt()
        );
    }
}