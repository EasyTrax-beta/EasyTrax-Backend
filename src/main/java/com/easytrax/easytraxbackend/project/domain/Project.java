package com.easytrax.easytraxbackend.project.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import com.easytrax.easytraxbackend.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "projects")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_name", nullable = false)
    private String projectName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_country", nullable = false)
    private Country targetCountry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectStatus status;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Project(String projectName, String productName, Country targetCountry, String description, 
                   LocalDate expectedCompletionDate, Priority priority, ProjectStatus status, 
                   Integer progressPercentage, User user) {
        this.projectName = projectName;
        this.productName = productName;
        this.targetCountry = targetCountry;
        this.description = description;
        this.expectedCompletionDate = expectedCompletionDate;
        this.priority = priority;
        this.status = status;
        this.progressPercentage = progressPercentage != null ? progressPercentage : 0;
        this.user = user;
    }

    public void updateProject(String projectName, String productName, Country targetCountry, 
                            String description, LocalDate expectedCompletionDate, Priority priority) {
        this.projectName = projectName;
        this.productName = productName;
        this.targetCountry = targetCountry;
        this.description = description;
        this.expectedCompletionDate = expectedCompletionDate;
        this.priority = priority;
    }

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

    public void updateProgress(Integer progressPercentage) {
        if (progressPercentage >= 0 && progressPercentage <= 100) {
            this.progressPercentage = progressPercentage;
        }
    }
}