package com.easytrax.easytraxbackend.invoice.api;

import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.code.status.SuccessStatus;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import com.easytrax.easytraxbackend.invoice.api.dto.request.CommercialInvoiceCreateRequest;
import com.easytrax.easytraxbackend.invoice.api.dto.request.CommercialInvoiceOcrRequest;
import com.easytrax.easytraxbackend.invoice.api.dto.response.CommercialInvoiceListResponse;
import com.easytrax.easytraxbackend.invoice.api.dto.response.CommercialInvoiceResponse;
import com.easytrax.easytraxbackend.invoice.application.CommercialInvoiceService;
import com.easytrax.easytraxbackend.invoice.application.CommercialInvoicePdfService;
import com.easytrax.easytraxbackend.invoice.application.CommercialInvoiceOcrService;
import com.easytrax.easytraxbackend.invoice.domain.InvoiceFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "상업송장", description = "상업송장 관리 API")
@RestController
@RequestMapping("/api/commercial-invoices")
@RequiredArgsConstructor
public class CommercialInvoiceController {

    private final CommercialInvoiceService commercialInvoiceService;
    private final CommercialInvoicePdfService commercialInvoicePdfService;
    private final CommercialInvoiceOcrService commercialInvoiceOcrService;

    @Operation(
            summary = "상업송장 생성",
            description = "새로운 상업송장을 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<CommercialInvoiceResponse>> createCommercialInvoice(
            @Valid @RequestBody CommercialInvoiceCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        CommercialInvoiceResponse response = commercialInvoiceService.createCommercialInvoice(request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.CREATED, response));
    }

    @Operation(
            summary = "OCR로 상업송장 생성",
            description = "이미지에서 상업송장 정보를 추출하여 상업송장을 생성합니다."
    )
    @PostMapping("/from-ocr")
    public ResponseEntity<ApiResponse<CommercialInvoiceResponse>> createCommercialInvoiceFromOcr(
            @Parameter(description = "상업송장 이미지 파일") @RequestPart("image") MultipartFile image,
            @Parameter(description = "OCR 요청 정보") @Valid @RequestPart("request") CommercialInvoiceOcrRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        CommercialInvoiceCreateRequest ocrRequest = commercialInvoiceOcrService.extractInvoiceInfo(
                image, request.projectId(), request.invoiceFormat());
        
        CommercialInvoiceResponse response = commercialInvoiceService.createCommercialInvoice(
                ocrRequest, userDetails.getUserId());
        
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.CREATED, response));
    }

    @Operation(
            summary = "프로젝트별 상업송장 목록 조회",
            description = "특정 프로젝트의 상업송장 목록을 페이징으로 조회합니다."
    )
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<CommercialInvoiceListResponse>>> findCommercialInvoicesByProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Page<CommercialInvoiceListResponse> response = commercialInvoiceService.findCommercialInvoicesByProject(
                projectId, userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "사용자 상업송장 목록 조회",
            description = "사용자의 모든 상업송장 목록을 페이징으로 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommercialInvoiceListResponse>>> findCommercialInvoicesByUser(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Page<CommercialInvoiceListResponse> response = commercialInvoiceService.findCommercialInvoicesByUser(
                userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "상업송장 상세 조회",
            description = "상업송장의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommercialInvoiceResponse>> findCommercialInvoiceById(
            @Parameter(description = "상업송장 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        CommercialInvoiceResponse response = commercialInvoiceService.findCommercialInvoiceById(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "상업송장 수정",
            description = "상업송장 정보를 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommercialInvoiceResponse>> updateCommercialInvoice(
            @Parameter(description = "상업송장 ID") @PathVariable Long id,
            @Valid @RequestBody CommercialInvoiceCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        CommercialInvoiceResponse response = commercialInvoiceService.updateCommercialInvoice(
                id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "상업송장 삭제",
            description = "상업송장을 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCommercialInvoice(
            @Parameter(description = "상업송장 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        commercialInvoiceService.deleteCommercialInvoice(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.OK, null));
    }

    @Operation(
            summary = "상업송장 PDF 다운로드",
            description = "상업송장을 PDF 파일로 다운로드합니다."
    )
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadCommercialInvoicePdf(
            @Parameter(description = "상업송장 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        CommercialInvoiceResponse commercialInvoice = commercialInvoiceService.findCommercialInvoiceById(id, userDetails.getUserId());
        byte[] pdfBytes = commercialInvoicePdfService.generateCommercialInvoicePdf(
                commercialInvoiceService.findCommercialInvoiceByIdAndUserId(id, userDetails.getUserId()));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "commercial_invoice_" + commercialInvoice.invoiceNumber() + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}