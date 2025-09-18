package com.easytrax.easytraxbackend.hscode.api.dto.response;

import com.easytrax.easytraxbackend.hscode.domain.ProductInfo;
import com.easytrax.easytraxbackend.project.domain.Country;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "HS 코드 분류 응답")
public record HSCodeClassifyResponse(
        @Schema(description = "제품 정보 ID", example = "1")
        Long productInfoId,

        @Schema(description = "제품명", example = "신라면")
        String productName,

        @Schema(description = "용도", example = "식품, 인스턴트 라면")
        String purpose,

        @Schema(description = "제품 설명", example = "매운맛 인스턴트 라면")
        String description,

        @Schema(description = "재질", example = "밀가루, 야자유, 조미료")
        String material,

        @Schema(description = "원산지", example = "KOREA")
        Country originCountry,

        @Schema(description = "수출 대상국", example = "CHINA")
        Country targetCountry,

        @Schema(description = "이미지에서 추출되었는지 여부", example = "false")
        Boolean extractedFromImage,

        @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/...")
        String imageUrl,

        @Schema(description = "분류된 HS 코드", example = "1902301000")
        String classifiedHsCode,

        @Schema(description = "분류 신뢰도 (0.0-1.0)", example = "0.95")
        Double classificationConfidence,

        @Schema(description = "OCR 신뢰도 (0.0-1.0)", example = "0.90")
        Double ocrConfidence
) {
    public static HSCodeClassifyResponse from(ProductInfo productInfo) {
        return new HSCodeClassifyResponse(
                productInfo.getId(),
                productInfo.getProductName(),
                productInfo.getPurpose(),
                productInfo.getDescription(),
                productInfo.getMaterial(),
                productInfo.getOriginCountry(),
                productInfo.getTargetCountry(),
                productInfo.getExtractedFromImage(),
                productInfo.getImageUrl(),
                productInfo.getClassifiedHsCode(),
                productInfo.getClassificationConfidence(),
                productInfo.getConfidenceScore()
        );
    }
}