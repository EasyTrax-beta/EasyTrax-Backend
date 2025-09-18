package com.easytrax.easytraxbackend.hscode.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지에서 추출된 제품 정보 응답")
public record ProductInfoExtractionResponse(
        @Schema(description = "제품명", example = "신라면")
        String productName,

        @Schema(description = "용도", example = "식품, 인스턴트 라면")
        String purpose,

        @Schema(description = "제품 설명", example = "매운맛 인스턴트 라면")
        String description,

        @Schema(description = "재질/원료", example = "밀가루, 야자유, 조미료")
        String material,

        @Schema(description = "이미지 URL", example = "https://s3.amazonaws.com/...")
        String imageUrl,

        @Schema(description = "OCR 신뢰도 (0.0-1.0)", example = "0.90")
        Double confidenceScore
) {
    public static ProductInfoExtractionResponse from(ProductOcrResult ocrResult, String imageUrl) {
        return new ProductInfoExtractionResponse(
                ocrResult.productName(),
                ocrResult.purpose(),
                ocrResult.description(),
                ocrResult.material(),
                imageUrl,
                ocrResult.confidenceScore()
        );
    }
}