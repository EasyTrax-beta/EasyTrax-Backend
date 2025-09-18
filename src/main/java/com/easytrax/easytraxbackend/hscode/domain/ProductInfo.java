package com.easytrax.easytraxbackend.hscode.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import com.easytrax.easytraxbackend.project.domain.Country;
import com.easytrax.easytraxbackend.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "product_infos")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "purpose", length = 500)
    private String purpose;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "material", length = 200)
    private String material;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_country", length = 50)
    private Country originCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_country", length = 50)
    private Country targetCountry;

    @Column(name = "extracted_from_image", nullable = false)
    private Boolean extractedFromImage;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "classified_hs_code", length = 10)
    private String classifiedHsCode;

    @Column(name = "classification_confidence")
    private Double classificationConfidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder
    public ProductInfo(String productName, String purpose, String description, String material,
                      Country originCountry, Country targetCountry, Boolean extractedFromImage,
                      String imageUrl, Double confidenceScore, String classifiedHsCode,
                      Double classificationConfidence, Project project) {
        this.productName = productName;
        this.purpose = purpose;
        this.description = description;
        this.material = material;
        this.originCountry = originCountry;
        this.targetCountry = targetCountry;
        this.extractedFromImage = extractedFromImage != null ? extractedFromImage : false;
        this.imageUrl = imageUrl;
        this.confidenceScore = confidenceScore;
        this.classifiedHsCode = classifiedHsCode;
        this.classificationConfidence = classificationConfidence;
        this.project = project;
    }

    public void updateManualInfo(String productName, String purpose, String description,
                               String material, Country originCountry, Country targetCountry) {
        this.productName = productName;
        this.purpose = purpose;
        this.description = description;
        this.material = material;
        this.originCountry = originCountry;
        this.targetCountry = targetCountry;
    }

    public void updateClassificationResult(String classifiedHsCode, Double classificationConfidence) {
        this.classifiedHsCode = classifiedHsCode;
        this.classificationConfidence = classificationConfidence;
    }

    public void updateOcrResult(String productName, String purpose, String description,
                              String material, Double confidenceScore) {
        this.productName = productName;
        this.purpose = purpose;
        this.description = description;
        this.material = material;
        this.confidenceScore = confidenceScore;
    }
}