package com.easytrax.easytraxbackend.project.api;

import com.easytrax.easytraxbackend.global.code.dto.ApiResponse;
import com.easytrax.easytraxbackend.global.security.CustomUserDetails;
import com.easytrax.easytraxbackend.project.api.dto.request.ProjectCreateRequest;
import com.easytrax.easytraxbackend.project.api.dto.request.ProjectUpdateRequest;
import com.easytrax.easytraxbackend.project.api.dto.response.ProjectListResponse;
import com.easytrax.easytraxbackend.project.api.dto.response.ProjectResponse;
import com.easytrax.easytraxbackend.project.application.ProjectService;
import com.easytrax.easytraxbackend.project.domain.Priority;
import com.easytrax.easytraxbackend.project.domain.ProjectStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "프로젝트 관리 API", description = "수출 프로젝트 생성, 조회, 수정, 삭제")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성", description = "새로운 수출 프로젝트를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse response = projectService.createProject(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트 ID로 특정 프로젝트의 상세 정보를 조회합니다.")
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId) {
        ProjectResponse response = projectService.findProject(userDetails.getUserId(), projectId);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "프로젝트 목록 조회", description = "사용자의 프로젝트 목록을 페이징하여 조회합니다. 상태 및 우선순위로 필터링 가능합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectListResponse>>> getProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "프로젝트 상태 필터")
            @RequestParam(required = false) ProjectStatus status,
            @Parameter(description = "우선순위 필터")
            @RequestParam(required = false) Priority priority,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProjectListResponse> response = projectService.findProjects(
                userDetails.getUserId(), status, priority, pageable);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "프로젝트 수정", description = "기존 프로젝트의 정보를 수정합니다.")
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        ProjectResponse response = projectService.updateProject(userDetails.getUserId(), projectId, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다.")
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId) {
        projectService.deleteProject(userDetails.getUserId(), projectId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @Operation(summary = "프로젝트 상태 변경", description = "프로젝트의 상태를 변경합니다.")
    @PatchMapping("/{projectId}/status")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "변경할 상태", required = true)
            @RequestParam ProjectStatus status) {
        ProjectResponse response = projectService.updateProjectStatus(userDetails.getUserId(), projectId, status);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }

    @Operation(summary = "프로젝트 진행률 업데이트", description = "프로젝트의 진행률을 업데이트합니다.")
    @PatchMapping("/{projectId}/progress")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectProgress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId,
            @Parameter(description = "진행률 (0-100)", required = true)
            @RequestParam Integer progressPercentage) {
        ProjectResponse response = projectService.updateProjectProgress(userDetails.getUserId(), projectId, progressPercentage);
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}