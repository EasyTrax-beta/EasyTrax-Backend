package com.easytrax.easytraxbackend.nutritionlabel.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import com.easytrax.easytraxbackend.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Table(name = "nutrition_labels")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NutritionLabel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "serving_size", nullable = false)
    private String servingSize;

    @Column(name = "servings_per_container")
    private Integer servingsPerContainer;

    @Column(name = "calories", nullable = false)
    private Integer calories;

    @Column(name = "calories_from_fat")
    private Integer caloriesFromFat;

    @Column(name = "total_fat", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalFat;

    @Column(name = "saturated_fat", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal saturatedFat;

    @Column(name = "trans_fat", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal transFat;

    @Column(name = "cholesterol", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal cholesterol;

    @Column(name = "sodium", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal sodium;

    @Column(name = "total_carbohydrate", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalCarbohydrate;

    @Column(name = "dietary_fiber", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal dietaryFiber;

    @Column(name = "total_sugars", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalSugars;

    @Column(name = "added_sugars", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal addedSugars;

    @Column(name = "protein", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal protein;

    @Column(name = "vitamin_d", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal vitaminD;

    @Column(name = "calcium", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal calcium;

    @Column(name = "iron", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal iron;

    @Column(name = "potassium", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal potassium;

    @Column(name = "vitamin_a", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal vitaminA;

    @Column(name = "vitamin_c", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal vitaminC;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_format", nullable = false)
    private LabelFormat labelFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder
    public NutritionLabel(String productName, String servingSize, Integer servingsPerContainer,
                         Integer calories, Integer caloriesFromFat, BigDecimal totalFat,
                         BigDecimal saturatedFat, BigDecimal transFat, BigDecimal cholesterol,
                         BigDecimal sodium, BigDecimal totalCarbohydrate, BigDecimal dietaryFiber,
                         BigDecimal totalSugars, BigDecimal addedSugars, BigDecimal protein,
                         BigDecimal vitaminD, BigDecimal calcium, BigDecimal iron,
                         BigDecimal potassium, BigDecimal vitaminA, BigDecimal vitaminC,
                         LabelFormat labelFormat, Project project) {
        this.productName = productName;
        this.servingSize = servingSize;
        this.servingsPerContainer = servingsPerContainer;
        this.calories = calories;
        this.caloriesFromFat = caloriesFromFat;
        this.totalFat = totalFat;
        this.saturatedFat = saturatedFat;
        this.transFat = transFat;
        this.cholesterol = cholesterol;
        this.sodium = sodium;
        this.totalCarbohydrate = totalCarbohydrate;
        this.dietaryFiber = dietaryFiber;
        this.totalSugars = totalSugars;
        this.addedSugars = addedSugars;
        this.protein = protein;
        this.vitaminD = vitaminD;
        this.calcium = calcium;
        this.iron = iron;
        this.potassium = potassium;
        this.vitaminA = vitaminA;
        this.vitaminC = vitaminC;
        this.labelFormat = labelFormat;
        this.project = project;
    }

    public void updateNutritionLabel(String productName, String servingSize, Integer servingsPerContainer,
                                   Integer calories, Integer caloriesFromFat, BigDecimal totalFat,
                                   BigDecimal saturatedFat, BigDecimal transFat, BigDecimal cholesterol,
                                   BigDecimal sodium, BigDecimal totalCarbohydrate, BigDecimal dietaryFiber,
                                   BigDecimal totalSugars, BigDecimal addedSugars, BigDecimal protein,
                                   BigDecimal vitaminD, BigDecimal calcium, BigDecimal iron,
                                   BigDecimal potassium, BigDecimal vitaminA, BigDecimal vitaminC,
                                   LabelFormat labelFormat) {
        this.productName = productName;
        this.servingSize = servingSize;
        this.servingsPerContainer = servingsPerContainer;
        this.calories = calories;
        this.caloriesFromFat = caloriesFromFat;
        this.totalFat = totalFat;
        this.saturatedFat = saturatedFat;
        this.transFat = transFat;
        this.cholesterol = cholesterol;
        this.sodium = sodium;
        this.totalCarbohydrate = totalCarbohydrate;
        this.dietaryFiber = dietaryFiber;
        this.totalSugars = totalSugars;
        this.addedSugars = addedSugars;
        this.protein = protein;
        this.vitaminD = vitaminD;
        this.calcium = calcium;
        this.iron = iron;
        this.potassium = potassium;
        this.vitaminA = vitaminA;
        this.vitaminC = vitaminC;
        this.labelFormat = labelFormat;
    }
}