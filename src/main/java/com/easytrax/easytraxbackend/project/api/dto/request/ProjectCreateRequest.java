package com.easytrax.easytraxbackend.project.api.dto.request;

import com.easytrax.easytraxbackend.project.domain.Country;
import com.easytrax.easytraxbackend.project.domain.Priority;
import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.ProjectStatus;
import com.easytrax.easytraxbackend.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "프로젝트 생성 요청")
public record ProjectCreateRequest(
        @Schema(description = "프로젝트 이름", example = "중국 시장을 대상으로 한 라면 수출 프로젝트")
        @NotBlank(message = "프로젝트 이름은 필수입니다")
        @Size(max = 100, message = "프로젝트 이름은 100자를 초과할 수 없습니다")
        String projectName,

        @Schema(description = "제품명", example = "라면")
        @NotBlank(message = "제품명은 필수입니다")
        @Size(max = 50, message = "제품명은 50자를 초과할 수 없습니다")
        String productName,

        @Schema(description = "수출 대상국", example = "CHINA")
        @NotNull(message = "수출 대상국은 필수입니다")
        Country targetCountry,

        @Schema(description = "프로젝트 설명", example = "중국 시장 진출을 위한 라면 수출 프로젝트입니다.")
        @Size(max = 1000, message = "프로젝트 설명은 1000자를 초과할 수 없습니다")
        String description,

        @Schema(description = "예상 완료일", example = "2025-02-15")
        LocalDate expectedCompletionDate,

        @Schema(description = "우선순위", example = "HIGH")
        @NotNull(message = "우선순위는 필수입니다")
        Priority priority
) {
    public Project toEntity(User user) {
        return Project.builder()
                .projectName(projectName)
                .productName(productName)
                .targetCountry(targetCountry)
                .description(description)
                .expectedCompletionDate(expectedCompletionDate)
                .priority(priority)
                .status(ProjectStatus.IN_PROGRESS)
                .progressPercentage(0)
                .user(user)
                .build();
    }
}