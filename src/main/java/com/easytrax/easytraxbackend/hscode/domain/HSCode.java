package com.easytrax.easytraxbackend.hscode.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Table(name = "hs_codes")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HSCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hs_code", nullable = false, unique = true, length = 10)
    private String hsCode;

    @Column(name = "korean_name", nullable = false, length = 500)
    private String koreanName;

    @Column(name = "english_name", length = 500)
    private String englishName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "parent_code", length = 10)
    private String parentCode;

    @ElementCollection
    @CollectionTable(name = "hs_code_keywords", joinColumns = @JoinColumn(name = "hs_code_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    @Column(name = "material_info", length = 200)
    private String materialInfo;

    @Column(name = "usage_info", length = 200)
    private String usageInfo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    public HSCode(String hsCode, String koreanName, String englishName, String description,
                  Integer level, String parentCode, List<String> keywords, String materialInfo,
                  String usageInfo, Boolean isActive) {
        this.hsCode = hsCode;
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.description = description;
        this.level = level;
        this.parentCode = parentCode;
        this.keywords = keywords;
        this.materialInfo = materialInfo;
        this.usageInfo = usageInfo;
        this.isActive = isActive != null ? isActive : true;
    }

    public void updateInfo(String koreanName, String englishName, String description) {
        this.koreanName = koreanName;
        this.englishName = englishName;
        this.description = description;
    }

    public void updateKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void deactivate() {
        this.isActive = false;
    }
}