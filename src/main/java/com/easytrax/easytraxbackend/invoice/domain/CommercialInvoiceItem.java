package com.easytrax.easytraxbackend.invoice.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Table(name = "commercial_invoice_items")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialInvoiceItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_count", nullable = false)
    private Integer packageCount;

    @Column(name = "package_type", nullable = false)
    private String packageType;

    @Column(name = "goods_description", nullable = false, length = 500)
    private String goodsDescription;

    @Column(name = "hs_code", nullable = false, length = 20)
    private String hsCode;

    @Column(name = "country_of_origin", nullable = false, length = 100)
    private String countryOfOrigin;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, columnDefinition = "DECIMAL(10,2)")
    private BigDecimal unitPrice;

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commercial_invoice_id", nullable = false)
    private CommercialInvoice commercialInvoice;

    @Builder
    public CommercialInvoiceItem(Integer packageCount, String packageType, String goodsDescription,
                               String hsCode, String countryOfOrigin, Integer quantity, BigDecimal unitPrice) {
        this.packageCount = packageCount;
        this.packageType = packageType;
        this.goodsDescription = goodsDescription;
        this.hsCode = hsCode;
        this.countryOfOrigin = countryOfOrigin;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    public void assignCommercialInvoice(CommercialInvoice commercialInvoice) {
        this.commercialInvoice = commercialInvoice;
    }

    public void updateItem(Integer packageCount, String packageType, String goodsDescription,
                          String hsCode, String countryOfOrigin, Integer quantity, BigDecimal unitPrice) {
        if (packageCount != null) this.packageCount = packageCount;
        if (packageType != null) this.packageType = packageType;
        if (goodsDescription != null) this.goodsDescription = goodsDescription;
        if (hsCode != null) this.hsCode = hsCode;
        if (countryOfOrigin != null) this.countryOfOrigin = countryOfOrigin;
        if (quantity != null) this.quantity = quantity;
        if (unitPrice != null) this.unitPrice = unitPrice;
        calculateAmount();
        if (this.commercialInvoice != null) {
            this.commercialInvoice.calculateTotalAmount();
        }
    }

    private void calculateAmount() {
        this.amount = unitPrice
            .multiply(BigDecimal.valueOf(quantity))
            .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPrice() {
        return this.amount;
    }
}