package com.easytrax.easytraxbackend.packinglist.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "packing_list_items")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PackingListItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    @Column(name = "gross_weight", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double grossWeight;

    @Column(name = "net_weight", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private Double netWeight;

    @Column(name = "volume", columnDefinition = "DECIMAL(10,2)")
    private Double volume;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "package_type", length = 50)
    private String packageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_list_id", nullable = false)
    private PackingList packingList;

    @Builder
    public PackingListItem(String description, Integer quantity, String unitOfMeasure,
                          Double grossWeight, Double netWeight, Double volume,
                          String dimensions, String packageType) {
        this.description = description;
        this.quantity = quantity;
        this.unitOfMeasure = unitOfMeasure;
        this.grossWeight = grossWeight;
        this.netWeight = netWeight;
        this.volume = volume;
        this.dimensions = dimensions;
        this.packageType = packageType;
    }

    public void assignPackingList(PackingList packingList) {
        this.packingList = packingList;
    }

    public void updateItem(String description, Integer quantity, String unitOfMeasure,
                          Double grossWeight, Double netWeight, Double volume,
                          String dimensions, String packageType) {
        this.description = description;
        this.quantity = quantity;
        this.unitOfMeasure = unitOfMeasure;
        this.grossWeight = grossWeight;
        this.netWeight = netWeight;
        this.volume = volume;
        this.dimensions = dimensions;
        this.packageType = packageType;
    }
}