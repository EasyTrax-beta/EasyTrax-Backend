package com.easytrax.easytraxbackend.hscode.api.dto.request;

import com.easytrax.easytraxbackend.hscode.domain.ProductInfo;
import com.easytrax.easytraxbackend.project.domain.Country;
import com.easytrax.easytraxbackend.project.domain.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "수동 입력 기반 HS 코드 분류 요청")
public record HSCodeClassifyByInfoRequest(
        @Schema(description = "프로젝트 ID", example = "1")
        @NotNull(message = "프로젝트 ID는 필수입니다")
        Long projectId,

        @Schema(description = "제품명", example = "신라면")
        @NotBlank(message = "제품명은 필수입니다")
        @Size(max = 200, message = "제품명은 200자를 초과할 수 없습니다")
        String productName,

        @Schema(description = "용도", example = "식품, 인스턴트 라면")
        @Size(max = 500, message = "용도는 500자를 초과할 수 없습니다")
        String purpose,

        @Schema(description = "제품 설명", example = "매운맛 인스턴트 라면으로 한국의 대표적인 라면")
        @Size(max = 1000, message = "제품 설명은 1000자를 초과할 수 없습니다")
        String description,

        @Schema(description = "재질", example = "밀가루, 야자유, 조미료")
        @Size(max = 200, message = "재질은 200자를 초과할 수 없습니다")
        String material,

        @Schema(description = "원산지", example = "KOREA")
        Country originCountry,

        @Schema(description = "수출 대상국", example = "CHINA")
        Country targetCountry
) {
    public ProductInfo toEntity(Project project) {
        return ProductInfo.builder()
                .productName(productName)
                .purpose(purpose)
                .description(description)
                .material(material)
                .originCountry(originCountry)
                .targetCountry(targetCountry)
                .extractedFromImage(false)
                .project(project)
                .build();
    }
}