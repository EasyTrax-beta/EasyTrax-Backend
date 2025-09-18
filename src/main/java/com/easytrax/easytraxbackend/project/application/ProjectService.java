package com.easytrax.easytraxbackend.project.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.project.api.dto.request.ProjectCreateRequest;
import com.easytrax.easytraxbackend.project.api.dto.request.ProjectUpdateRequest;
import com.easytrax.easytraxbackend.project.api.dto.response.ProjectListResponse;
import com.easytrax.easytraxbackend.project.api.dto.response.ProjectResponse;
import com.easytrax.easytraxbackend.project.domain.Priority;
import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.ProjectStatus;
import com.easytrax.easytraxbackend.project.domain.repository.ProjectRepository;
import com.easytrax.easytraxbackend.user.domain.User;
import com.easytrax.easytraxbackend.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(Long userId, ProjectCreateRequest request) {
        User user = findUserById(userId);
        Project project = request.toEntity(user);
        Project savedProject = projectRepository.save(project);
        return ProjectResponse.from(savedProject);
    }

    public ProjectResponse findProject(Long userId, Long projectId) {
        Project project = findProjectByIdAndUserId(projectId, userId);
        return ProjectResponse.from(project);
    }

    public Page<ProjectListResponse> findProjects(Long userId, ProjectStatus status, Priority priority, Pageable pageable) {
        Page<Project> projects;
        
        if (status != null && priority != null) {
            projects = projectRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (status != null) {
            projects = projectRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (priority != null) {
            projects = projectRepository.findByUserIdAndPriority(userId, priority, pageable);
        } else {
            projects = projectRepository.findByUserId(userId, pageable);
        }
        
        return projects.map(ProjectListResponse::from);
    }

    @Transactional
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectUpdateRequest request) {
        Project project = findProjectByIdAndUserId(projectId, userId);
        
        project.updateProject(
                request.projectName(),
                request.productName(),
                request.targetCountry(),
                request.description(),
                request.expectedCompletionDate(),
                request.priority()
        );
        
        return ProjectResponse.from(project);
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new GeneralException(ErrorStatus.PROJECT_NOT_FOUND);
        }
        projectRepository.deleteById(projectId);
    }

    @Transactional
    public ProjectResponse updateProjectStatus(Long userId, Long projectId, ProjectStatus status) {
        Project project = findProjectByIdAndUserId(projectId, userId);
        project.updateStatus(status);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProjectProgress(Long userId, Long projectId, Integer progressPercentage) {
        Project project = findProjectByIdAndUserId(projectId, userId);
        project.updateProgress(progressPercentage);
        return ProjectResponse.from(project);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private Project findProjectByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND));
    }
}