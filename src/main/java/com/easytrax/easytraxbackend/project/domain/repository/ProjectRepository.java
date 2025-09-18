package com.easytrax.easytraxbackend.project.domain.repository;

import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.ProjectStatus;
import com.easytrax.easytraxbackend.project.domain.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId")
    Page<Project> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId AND p.status = :status")
    Page<Project> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId AND p.priority = :priority")
    Page<Project> findByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") Priority priority, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.id = :projectId AND p.user.id = :userId")
    Optional<Project> findByIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    boolean existsByIdAndUserId(Long projectId, Long userId);
}
