package com.easytrax.easytraxbackend.nutritionlabel.api;

import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.code.status.SuccessStatus;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.request.NutritionLabelCreateRequest;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.request.NutritionLabelOcrRequest;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.response.NutritionLabelListResponse;
import com.easytrax.easytraxbackend.nutritionlabel.api.dto.response.NutritionLabelResponse;
import com.easytrax.easytraxbackend.nutritionlabel.application.NutritionLabelService;
import com.easytrax.easytraxbackend.nutritionlabel.application.NutritionLabelPdfService;
import com.easytrax.easytraxbackend.nutritionlabel.application.NutritionLabelOcrService;
import com.easytrax.easytraxbackend.nutritionlabel.domain.LabelFormat;
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

@Tag(name = "영양성분표", description = "영양성분표 관리 API")
@RestController
@RequestMapping("/api/nutrition-labels")
@RequiredArgsConstructor
public class NutritionLabelController {

    private final NutritionLabelService nutritionLabelService;
    private final NutritionLabelPdfService nutritionLabelPdfService;
    private final NutritionLabelOcrService nutritionLabelOcrService;

    @Operation(
            summary = "영양성분표 생성",
            description = "새로운 영양성분표를 생성합니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<NutritionLabelResponse>> createNutritionLabel(
            @Valid @RequestBody NutritionLabelCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        NutritionLabelResponse response = nutritionLabelService.createNutritionLabel(request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.CREATED, response));
    }

    @Operation(
            summary = "OCR로 영양성분표 생성",
            description = "이미지에서 영양성분 정보를 추출하여 영양성분표를 생성합니다."
    )
    @PostMapping("/from-ocr")
    public ResponseEntity<ApiResponse<NutritionLabelResponse>> createNutritionLabelFromOcr(
            @Parameter(description = "영양성분표 이미지 파일") @RequestPart("image") MultipartFile image,
            @Parameter(description = "OCR 요청 정보") @Valid @RequestPart("request") NutritionLabelOcrRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        NutritionLabelCreateRequest ocrRequest = nutritionLabelOcrService.extractNutritionInfo(
                image, request.projectId(), request.labelFormat());
        
        NutritionLabelResponse response = nutritionLabelService.createNutritionLabel(
                ocrRequest, userDetails.getUserId());
        
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.CREATED, response));
    }

    @Operation(
            summary = "프로젝트별 영양성분표 목록 조회",
            description = "특정 프로젝트의 영양성분표 목록을 페이징으로 조회합니다."
    )
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<Page<NutritionLabelListResponse>>> findNutritionLabelsByProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Page<NutritionLabelListResponse> response = nutritionLabelService.findNutritionLabelsByProject(
                projectId, userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "사용자 영양성분표 목록 조회",
            description = "사용자의 모든 영양성분표 목록을 페이징으로 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NutritionLabelListResponse>>> findNutritionLabelsByUser(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Page<NutritionLabelListResponse> response = nutritionLabelService.findNutritionLabelsByUser(
                userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "영양성분표 상세 조회",
            description = "영양성분표의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionLabelResponse>> findNutritionLabelById(
            @Parameter(description = "영양성분표 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        NutritionLabelResponse response = nutritionLabelService.findNutritionLabelById(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "영양성분표 수정",
            description = "영양성분표 정보를 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NutritionLabelResponse>> updateNutritionLabel(
            @Parameter(description = "영양성분표 ID") @PathVariable Long id,
            @Valid @RequestBody NutritionLabelCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        NutritionLabelResponse response = nutritionLabelService.updateNutritionLabel(
                id, request, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(
            summary = "영양성분표 삭제",
            description = "영양성분표를 삭제합니다."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNutritionLabel(
            @Parameter(description = "영양성분표 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        nutritionLabelService.deleteNutritionLabel(id, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus.OK, null));
    }

    @Operation(
            summary = "영양성분표 PDF 다운로드",
            description = "영양성분표를 PDF 파일로 다운로드합니다."
    )
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadNutritionLabelPdf(
            @Parameter(description = "영양성분표 ID") @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        NutritionLabelResponse nutritionLabel = nutritionLabelService.findNutritionLabelById(id, userDetails.getUserId());
        byte[] pdfBytes = nutritionLabelPdfService.generateNutritionLabelPdf(
                nutritionLabelService.findNutritionLabelByIdAndUserId(id, userDetails.getUserId()));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", 
                "nutrition_label_" + nutritionLabel.productName() + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}