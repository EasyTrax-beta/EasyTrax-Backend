package com.easytrax.easytraxbackend.packinglist.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import com.easytrax.easytraxbackend.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "packing_lists")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PackingList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipper_name", nullable = false)
    private String shipperName;

    @Column(name = "shipper_address", nullable = false, length = 500)
    private String shipperAddress;

    @Column(name = "consignee_name", nullable = false)
    private String consigneeName;

    @Column(name = "consignee_address", nullable = false, length = 500)
    private String consigneeAddress;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "destination")
    private String destination;

    @Column(name = "vessel_name")
    private String vesselName;

    @Column(name = "voyage_number")
    private String voyageNumber;

    @Column(name = "total_packages")
    private Integer totalPackages;

    @Column(name = "total_gross_weight", columnDefinition = "DECIMAL(10,2)")
    private Double totalGrossWeight;

    @Column(name = "total_net_weight", columnDefinition = "DECIMAL(10,2)")
    private Double totalNetWeight;

    @Column(name = "total_volume", columnDefinition = "DECIMAL(10,2)")
    private Double totalVolume;

    @Column(name = "marks_and_numbers", columnDefinition = "TEXT")
    private String marksAndNumbers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "packingList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PackingListItem> items = new ArrayList<>();

    @Builder
    public PackingList(String shipperName, String shipperAddress, String consigneeName, 
                      String consigneeAddress, LocalDate departureDate, String destination,
                      String vesselName, String voyageNumber, Integer totalPackages,
                      Double totalGrossWeight, Double totalNetWeight, Double totalVolume,
                      String marksAndNumbers, Project project) {
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.consigneeName = consigneeName;
        this.consigneeAddress = consigneeAddress;
        this.departureDate = departureDate;
        this.destination = destination;
        this.vesselName = vesselName;
        this.voyageNumber = voyageNumber;
        this.totalPackages = totalPackages;
        this.totalGrossWeight = totalGrossWeight;
        this.totalNetWeight = totalNetWeight;
        this.totalVolume = totalVolume;
        this.marksAndNumbers = marksAndNumbers;
        this.project = project;
    }

    public void updatePackingList(String shipperName, String shipperAddress, String consigneeName,
                                String consigneeAddress, LocalDate departureDate, String destination,
                                String vesselName, String voyageNumber, String marksAndNumbers) {
        this.shipperName = shipperName;
        this.shipperAddress = shipperAddress;
        this.consigneeName = consigneeName;
        this.consigneeAddress = consigneeAddress;
        this.departureDate = departureDate;
        this.destination = destination;
        this.vesselName = vesselName;
        this.voyageNumber = voyageNumber;
        this.marksAndNumbers = marksAndNumbers;
    }

    public void calculateTotals() {
        this.totalPackages = items.stream()
                .mapToInt(PackingListItem::getQuantity)
                .sum();
        
        this.totalGrossWeight = items.stream()
                .mapToDouble(item -> item.getGrossWeight() * item.getQuantity())
                .sum();
        
        this.totalNetWeight = items.stream()
                .mapToDouble(item -> item.getNetWeight() * item.getQuantity())
                .sum();
        
        this.totalVolume = items.stream()
                .mapToDouble(item -> item.getVolume() * item.getQuantity())
                .sum();
    }

    public void addItem(PackingListItem item) {
        items.add(item);
        item.assignPackingList(this);
        calculateTotals();
    }

    public void removeItem(PackingListItem item) {
        items.remove(item);
        calculateTotals();
    }
}