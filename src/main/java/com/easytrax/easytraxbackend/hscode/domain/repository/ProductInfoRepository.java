package com.easytrax.easytraxbackend.hscode.domain.repository;

import com.easytrax.easytraxbackend.hscode.domain.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {

    @Query("SELECT p FROM ProductInfo p WHERE p.project.id = :projectId AND p.project.user.id = :userId")
    Page<ProductInfo> findByProjectIdAndUserId(@Param("projectId") Long projectId, 
                                             @Param("userId") Long userId, 
                                             Pageable pageable);

    @Query("SELECT p FROM ProductInfo p WHERE p.id = :productInfoId AND p.project.user.id = :userId")
    Optional<ProductInfo> findByIdAndUserId(@Param("productInfoId") Long productInfoId, 
                                           @Param("userId") Long userId);

    @Query("SELECT p FROM ProductInfo p WHERE p.project.user.id = :userId AND p.classifiedHsCode IS NOT NULL")
    List<ProductInfo> findClassifiedProductsByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM ProductInfo p WHERE p.project.id = :projectId AND p.extractedFromImage = true")
    List<ProductInfo> findImageExtractedByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT p FROM ProductInfo p WHERE p.project.id = :projectId AND p.extractedFromImage = false")
    List<ProductInfo> findManualInputByProjectId(@Param("projectId") Long projectId);
}