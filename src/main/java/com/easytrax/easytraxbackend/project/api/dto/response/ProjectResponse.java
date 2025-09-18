package com.easytrax.easytraxbackend.project.api.dto.response;

import com.easytrax.easytraxbackend.project.domain.Country;
import com.easytrax.easytraxbackend.project.domain.Priority;
import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "프로젝트 상세 응답")
public record ProjectResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long id,

        @Schema(description = "프로젝트 이름", example = "중국 시장을 대상으로 한 라면 수출 프로젝트")
        String projectName,

        @Schema(description = "제품명", example = "라면")
        String productName,

        @Schema(description = "수출 대상국", example = "CHINA")
        Country targetCountry,

        @Schema(description = "프로젝트 설명", example = "중국 시장 진출을 위한 라면 수출 프로젝트입니다.")
        String description,

        @Schema(description = "예상 완료일", example = "2025-02-15")
        LocalDate expectedCompletionDate,

        @Schema(description = "우선순위", example = "HIGH")
        Priority priority,

        @Schema(description = "프로젝트 상태", example = "IN_PROGRESS")
        ProjectStatus status,

        @Schema(description = "진행률 (0-100)", example = "75")
        Integer progressPercentage,

        @Schema(description = "생성일", example = "2025-09-07T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "마지막 업데이트", example = "2025-09-07T15:30:00")
        LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getProjectName(),
                project.getProductName(),
                project.getTargetCountry(),
                project.getDescription(),
                project.getExpectedCompletionDate(),
                project.getPriority(),
                project.getStatus(),
                project.getProgressPercentage(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}