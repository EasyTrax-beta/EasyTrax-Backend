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

    @Column(name = "country_of_origin", nullable = false, length = 100)
    private String countryOfOrigin;

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

    @Column(name = "total_fat_dv")
    private Integer totalFatDV;

    @Column(name = "saturated_fat", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal saturatedFat;

    @Column(name = "saturated_fat_dv")
    private Integer saturatedFatDV;

    @Column(name = "trans_fat", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal transFat;

    @Column(name = "cholesterol", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal cholesterol;

    @Column(name = "cholesterol_dv")
    private Integer cholesterolDV;

    @Column(name = "sodium", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal sodium;

    @Column(name = "sodium_dv")
    private Integer sodiumDV;

    @Column(name = "total_carbohydrate", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalCarbohydrate;

    @Column(name = "total_carbohydrate_dv")
    private Integer totalCarbohydrateDV;

    @Column(name = "dietary_fiber", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal dietaryFiber;

    @Column(name = "dietary_fiber_dv")
    private Integer dietaryFiberDV;

    @Column(name = "total_sugars", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal totalSugars;

    @Column(name = "added_sugars", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal addedSugars;

    @Column(name = "added_sugars_dv")
    private Integer addedSugarsDV;

    @Column(name = "protein", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal protein;

    @Column(name = "protein_dv")
    private Integer proteinDV;

    @Column(name = "vitamin_d", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal vitaminD;

    @Column(name = "vitamin_d_dv")
    private Integer vitaminDDV;

    @Column(name = "calcium", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal calcium;

    @Column(name = "calcium_dv")
    private Integer calciumDV;

    @Column(name = "iron", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal iron;

    @Column(name = "iron_dv")
    private Integer ironDV;

    @Column(name = "potassium", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal potassium;

    @Column(name = "potassium_dv")
    private Integer potassiumDV;

    @Column(name = "vitamin_a", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal vitaminA;

    @Column(name = "vitamin_a_dv")
    private Integer vitaminADV;

    @Column(name = "vitamin_c", columnDefinition = "DECIMAL(8,2)")
    private BigDecimal vitaminC;

    @Column(name = "vitamin_c_dv")
    private Integer vitaminCDV;

    @Enumerated(EnumType.STRING)
    @Column(name = "label_format", nullable = false)
    private LabelFormat labelFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder
    public NutritionLabel(String productName, String countryOfOrigin, String servingSize, Integer servingsPerContainer,
                         Integer calories, Integer caloriesFromFat, BigDecimal totalFat, Integer totalFatDV,
                         BigDecimal saturatedFat, Integer saturatedFatDV, BigDecimal transFat, BigDecimal cholesterol, Integer cholesterolDV,
                         BigDecimal sodium, Integer sodiumDV, BigDecimal totalCarbohydrate, Integer totalCarbohydrateDV, BigDecimal dietaryFiber, Integer dietaryFiberDV,
                         BigDecimal totalSugars, BigDecimal addedSugars, Integer addedSugarsDV, BigDecimal protein, Integer proteinDV,
                         BigDecimal vitaminD, Integer vitaminDDV, BigDecimal calcium, Integer calciumDV, BigDecimal iron, Integer ironDV,
                         BigDecimal potassium, Integer potassiumDV, BigDecimal vitaminA, Integer vitaminADV, BigDecimal vitaminC, Integer vitaminCDV,
                         LabelFormat labelFormat, Project project) {
        this.productName = productName;
        this.countryOfOrigin = countryOfOrigin;
        this.servingSize = servingSize;
        this.servingsPerContainer = servingsPerContainer;
        this.calories = calories;
        this.caloriesFromFat = caloriesFromFat;
        this.totalFat = totalFat;
        this.totalFatDV = totalFatDV;
        this.saturatedFat = saturatedFat;
        this.saturatedFatDV = saturatedFatDV;
        this.transFat = transFat;
        this.cholesterol = cholesterol;
        this.cholesterolDV = cholesterolDV;
        this.sodium = sodium;
        this.sodiumDV = sodiumDV;
        this.totalCarbohydrate = totalCarbohydrate;
        this.totalCarbohydrateDV = totalCarbohydrateDV;
        this.dietaryFiber = dietaryFiber;
        this.dietaryFiberDV = dietaryFiberDV;
        this.totalSugars = totalSugars;
        this.addedSugars = addedSugars;
        this.addedSugarsDV = addedSugarsDV;
        this.protein = protein;
        this.proteinDV = proteinDV;
        this.vitaminD = vitaminD;
        this.vitaminDDV = vitaminDDV;
        this.calcium = calcium;
        this.calciumDV = calciumDV;
        this.iron = iron;
        this.ironDV = ironDV;
        this.potassium = potassium;
        this.potassiumDV = potassiumDV;
        this.vitaminA = vitaminA;
        this.vitaminADV = vitaminADV;
        this.vitaminC = vitaminC;
        this.vitaminCDV = vitaminCDV;
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
        if (productName != null) this.productName = productName;
        if (servingSize != null) this.servingSize = servingSize;
        if (servingsPerContainer != null) this.servingsPerContainer = servingsPerContainer;
        if (calories != null) this.calories = calories;
        if (caloriesFromFat != null) this.caloriesFromFat = caloriesFromFat;
        if (totalFat != null) this.totalFat = totalFat;
        if (saturatedFat != null) this.saturatedFat = saturatedFat;
        if (transFat != null) this.transFat = transFat;
        if (cholesterol != null) this.cholesterol = cholesterol;
        if (sodium != null) this.sodium = sodium;
        if (totalCarbohydrate != null) this.totalCarbohydrate = totalCarbohydrate;
        if (dietaryFiber != null) this.dietaryFiber = dietaryFiber;
        if (totalSugars != null) this.totalSugars = totalSugars;
        if (addedSugars != null) this.addedSugars = addedSugars;
        if (protein != null) this.protein = protein;
        if (vitaminD != null) this.vitaminD = vitaminD;
        if (calcium != null) this.calcium = calcium;
        if (iron != null) this.iron = iron;
        if (potassium != null) this.potassium = potassium;
        if (vitaminA != null) this.vitaminA = vitaminA;
        if (vitaminC != null) this.vitaminC = vitaminC;
        if (labelFormat != null) this.labelFormat = labelFormat;
    }
}