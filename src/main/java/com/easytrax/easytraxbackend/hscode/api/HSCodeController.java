package com.easytrax.easytraxbackend.hscode.api;

import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import com.easytrax.easytraxbackend.hscode.api.dto.request.HSCodeClassifyByImageRequest;
import com.easytrax.easytraxbackend.hscode.api.dto.request.HSCodeClassifyByInfoRequest;
import com.easytrax.easytraxbackend.hscode.api.dto.request.ProductInfoExtractionRequest;
import com.easytrax.easytraxbackend.hscode.api.dto.response.HSCodeClassifyResponse;
import com.easytrax.easytraxbackend.hscode.api.dto.response.ProductInfoExtractionResponse;
import com.easytrax.easytraxbackend.hscode.application.HSCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "HS 코드 분류 API", description = "AI 기반 HS 코드 자동 분류")
@RestController
@RequestMapping("/api/hs-codes")
@RequiredArgsConstructor
public class HSCodeController {

    private final HSCodeService hsCodeService;

    @Operation(summary = "이미지에서 제품 정보 추출", description = "제품 이미지를 업로드하여 OCR로 제품정보를 추출합니다. (HS 코드 분류 없이)")
    @PostMapping("/extract-product-info")
    public ResponseEntity<ApiResponse<ProductInfoExtractionResponse>> extractProductInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "제품 이미지 파일 (JPG, PNG, 최대 10MB)", required = true)
            @RequestPart("image") MultipartFile imageFile,
            @Parameter(description = "제품 정보 추출 요청", required = true)
            @Valid @RequestPart("request") ProductInfoExtractionRequest request) {

        ProductInfoExtractionResponse response = hsCodeService.extractProductInfoFromImage(
                userDetails.getUserId(), request.projectId(), imageFile);
        
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "이미지 기반 HS 코드 분류", description = "제품 이미지를 업로드하여 OCR로 제품정보를 추출하고 HS 코드를 분류합니다.")
    @PostMapping("/classify-by-image")
    public ResponseEntity<ApiResponse<HSCodeClassifyResponse>> classifyByImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "제품 이미지 파일 (JPG, PNG, 최대 10MB)", required = true)
            @RequestPart("image") MultipartFile imageFile,
            @Parameter(description = "분류 요청 정보", required = true)
            @Valid @RequestPart("request") HSCodeClassifyByImageRequest request) {

        HSCodeClassifyResponse response = hsCodeService.classifyByImage(userDetails.getUserId(), request.projectId(), imageFile, request.originCountry(), request.targetCountry());
        
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "수동 입력 기반 HS 코드 분류", description = "제품 정보를 직접 입력하여 HS 코드를 분류합니다.")
    @PostMapping("/classify-by-info")
    public ResponseEntity<ApiResponse<HSCodeClassifyResponse>> classifyByInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "분류 요청 정보", required = true)
            @Valid @RequestPart("request") HSCodeClassifyByInfoRequest request) {

        HSCodeClassifyResponse response = hsCodeService.classifyByInfo(userDetails.getUserId(), request);
        
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "제품 정보 조회", description = "저장된 제품 정보와 HS 코드 분류 결과를 조회합니다.")
    @GetMapping("/product-info/{productInfoId}")
    public ResponseEntity<ApiResponse<HSCodeClassifyResponse>> getProductInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "제품 정보 ID", required = true)
            @PathVariable Long productInfoId) {

        HSCodeClassifyResponse response = hsCodeService.findProductInfo(userDetails.getUserId(), productInfoId);
        
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}
