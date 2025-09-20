package com.easytrax.easytraxbackend.invoice.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
                               Integer quantity, BigDecimal unitPrice) {
        this.packageCount = packageCount;
        this.packageType = packageType;
        this.goodsDescription = goodsDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    public void assignCommercialInvoice(CommercialInvoice commercialInvoice) {
        this.commercialInvoice = commercialInvoice;
    }

    public void updateItem(Integer packageCount, String packageType, String goodsDescription,
                          Integer quantity, BigDecimal unitPrice) {
        this.packageCount = packageCount;
        this.packageType = packageType;
        this.goodsDescription = goodsDescription;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateAmount();
    }

    private void calculateAmount() {
        this.amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTotalPrice() {
        return this.amount;
    }
}