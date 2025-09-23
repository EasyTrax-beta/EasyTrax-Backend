package com.easytrax.easytraxbackend.compliance.api;

import com.easytrax.easytraxbackend.compliance.api.dto.request.ComplianceAnalysisRequest;
import com.easytrax.easytraxbackend.compliance.api.dto.request.InvoiceComplianceAnalysisRequest;
import com.easytrax.easytraxbackend.compliance.api.dto.response.ComplianceAnalysisResponse;
import com.easytrax.easytraxbackend.compliance.application.ComplianceAnalysisService;
import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.code.status.SuccessStatus;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "수출 준수성 분석", description = "수출 라벨/문서 준수성 분석 API")
@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
public class ComplianceAnalysisController {

    private final ComplianceAnalysisService complianceAnalysisService;

    @Operation(
            summary = "수출 준수성 분석",
            description = "영양성분표 PDF를 분석하여 목적지 국가의 수출 규정 준수 여부를 AI로 검사합니다."
    )
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<ComplianceAnalysisResponse>> analyzeCompliance(
            @Valid @RequestBody ComplianceAnalysisRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ComplianceAnalysisResponse response = complianceAnalysisService.analyzeCompliance(
                request, userDetails.getUserId());
        
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.OK, response));
    }

    @Operation(
            summary = "상업송장 준수성 분석",
            description = "상업송장을 분석하여 목적지 국가의 무역 규정 준수 여부를 AI로 검사합니다."
    )
    @PostMapping("/analyze-invoice")
    public ResponseEntity<ApiResponse<ComplianceAnalysisResponse>> analyzeInvoiceCompliance(
            @Valid @RequestBody InvoiceComplianceAnalysisRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        ComplianceAnalysisResponse response = complianceAnalysisService.analyzeInvoiceCompliance(
                request, userDetails.getUserId());
        
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.OK, response));
    }
}