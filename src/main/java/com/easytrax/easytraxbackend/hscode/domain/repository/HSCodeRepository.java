package com.easytrax.easytraxbackend.hscode.domain.repository;

import com.easytrax.easytraxbackend.hscode.domain.HSCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HSCodeRepository extends JpaRepository<HSCode, Long> {

    Optional<HSCode> findByHsCodeAndIsActiveTrue(String hsCode);

    @Query("SELECT h FROM HSCode h WHERE h.isActive = true AND h.level = :level")
    List<HSCode> findByLevelAndIsActiveTrue(@Param("level") Integer level);

    @Query("SELECT h FROM HSCode h WHERE h.isActive = true AND h.parentCode = :parentCode")
    List<HSCode> findByParentCodeAndIsActiveTrue(@Param("parentCode") String parentCode);

    @Query("SELECT h FROM HSCode h WHERE h.isActive = true AND " +
           "(LOWER(h.koreanName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.englishName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT k FROM h.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<HSCode> findByKeywordContaining(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT h FROM HSCode h WHERE h.isActive = true AND " +
           "(LOWER(h.materialInfo) LIKE LOWER(CONCAT('%', :material, '%')) OR " +
           "EXISTS (SELECT k FROM h.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :material, '%'))))")
    List<HSCode> findByMaterialContaining(@Param("material") String material);

    @Query("SELECT h FROM HSCode h WHERE h.isActive = true AND " +
           "(LOWER(h.usageInfo) LIKE LOWER(CONCAT('%', :usage, '%')) OR " +
           "EXISTS (SELECT k FROM h.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :usage, '%'))))")
    List<HSCode> findByUsageContaining(@Param("usage") String usage);

    @Query("SELECT h FROM HSCode h WHERE h.isActive = true AND " +
           "(LOWER(h.koreanName) LIKE LOWER(CONCAT('%', :productName, '%')) OR " +
           "LOWER(h.englishName) LIKE LOWER(CONCAT('%', :productName, '%')) OR " +
           "EXISTS (SELECT k FROM h.keywords k WHERE LOWER(k) LIKE LOWER(CONCAT('%', :productName, '%'))))")
    List<HSCode> findByProductNameContaining(@Param("productName") String productName);
}