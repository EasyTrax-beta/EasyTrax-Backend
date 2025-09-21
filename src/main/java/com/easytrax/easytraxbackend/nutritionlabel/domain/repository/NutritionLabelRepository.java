package com.easytrax.easytraxbackend.nutritionlabel.domain.repository;

import com.easytrax.easytraxbackend.nutritionlabel.domain.NutritionLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NutritionLabelRepository extends JpaRepository<NutritionLabel, Long> {

    @Query("SELECT n FROM NutritionLabel n WHERE n.project.id = :projectId AND n.project.user.id = :userId")
    Page<NutritionLabel> findByProjectIdAndUserId(@Param("projectId") Long projectId, 
                                                 @Param("userId") Long userId, 
                                                 Pageable pageable);

    @Query("SELECT n FROM NutritionLabel n WHERE n.id = :id AND n.project.user.id = :userId")
    Optional<NutritionLabel> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT n FROM NutritionLabel n WHERE n.project.user.id = :userId")
    Page<NutritionLabel> findByUserId(@Param("userId") Long userId, Pageable pageable);
}