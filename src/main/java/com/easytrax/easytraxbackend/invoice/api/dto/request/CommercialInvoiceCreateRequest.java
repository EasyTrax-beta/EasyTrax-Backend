package com.easytrax.easytraxbackend.invoice.api.dto.request;

import com.easytrax.easytraxbackend.invoice.domain.InvoiceFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "상업송장 생성 요청")
public record CommercialInvoiceCreateRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "송장 번호", example = "COMP-TEST-002")
        @NotBlank(message = "송장 번호는 필수입니다")
        String invoiceNumber,

        @Schema(description = "송장 일자", example = "2025-09-13")
        @NotNull(message = "송장 일자는 필수입니다")
        LocalDate invoiceDate,

        @Schema(description = "발송인/판매자 이름", example = "중합 테스트 수출회사")
        @NotBlank(message = "발송인/판매자 이름은 필수입니다")
        String shipperSellerName,

        @Schema(description = "발송인/판매자 주소", example = "서울시 강남구 테스트로 999")
        @NotBlank(message = "발송인/판매자 주소는 필수입니다")
        String shipperSellerAddress,

        @Schema(description = "발송인/판매자 연락처", example = "02-9999-8888")
        String shipperSellerPhone,

        @Schema(description = "수취인 이름", example = "")
        String consigneeName,

        @Schema(description = "수취인 주소", example = "")
        String consigneeAddress,

        @Schema(description = "구매자 이름", example = "중합 테스트 수입회사")
        @NotBlank(message = "구매자 이름은 필수입니다")
        String buyerName,

        @Schema(description = "구매자 주소", example = "중국 상하이시 테스트구 888호")
        @NotBlank(message = "구매자 주소는 필수입니다")
        String buyerAddress,

        @Schema(description = "구매자 연락처", example = "+86-21-8888-9999")
        String buyerPhone,

        @Schema(description = "L/C 번호", example = "N/A")
        String lcNumber,

        @Schema(description = "L/C 일자", example = "2025-09-13")
        LocalDate lcDate,

        @Schema(description = "출발일", example = "2025-09-15")
        LocalDate departureDate,

        @Schema(description = "선박/항공편", example = "N/A")
        String vesselFlight,

        @Schema(description = "출발 국가", example = "Korea")
        @NotBlank(message = "출발 국가는 필수입니다")
        String fromCountry,

        @Schema(description = "목적지", example = "N/A")
        String toDestination,

        @Schema(description = "선적 마크", example = "N/A")
        String shippingMarks,

        @Schema(description = "인도 조건", example = "FOB")
        String termsOfDelivery,

        @Schema(description = "결제 조건", example = "T/T")
        String paymentTerms,

        @Schema(description = "기타 참조사항", example = "N/A")
        String otherReferences,

        @Schema(description = "송장 포맷", example = "USA_STANDARD")
        @NotNull(message = "송장 포맷은 필수입니다")
        InvoiceFormat invoiceFormat,

        @Schema(description = "상업송장 항목 목록")
        @Valid
        @NotNull(message = "상업송장 항목은 필수입니다")
        List<CommercialInvoiceItemRequest> items
) {
}