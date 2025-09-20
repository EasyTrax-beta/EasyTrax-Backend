package com.easytrax.easytraxbackend.invoice.domain;

import com.easytrax.easytraxbackend.global.entity.BaseEntity;
import com.easytrax.easytraxbackend.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "commercial_invoices")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommercialInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "shipper_seller_name", nullable = false)
    private String shipperSellerName;

    @Column(name = "shipper_seller_address", nullable = false, length = 500)
    private String shipperSellerAddress;

    @Column(name = "shipper_seller_phone")
    private String shipperSellerPhone;

    @Column(name = "consignee_name")
    private String consigneeName;

    @Column(name = "consignee_address", length = 500)
    private String consigneeAddress;

    @Column(name = "buyer_name", nullable = false)
    private String buyerName;

    @Column(name = "buyer_address", nullable = false, length = 500)
    private String buyerAddress;

    @Column(name = "buyer_phone")
    private String buyerPhone;

    @Column(name = "lc_number")
    private String lcNumber;

    @Column(name = "lc_date")
    private LocalDate lcDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    @Column(name = "vessel_flight")
    private String vesselFlight;

    @Column(name = "from_country", nullable = false)
    private String fromCountry;

    @Column(name = "to_destination")
    private String toDestination;

    @Column(name = "shipping_marks")
    private String shippingMarks;

    @Column(name = "terms_of_delivery")
    private String termsOfDelivery;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "other_references")
    private String otherReferences;

    @Column(name = "total_amount", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_format", nullable = false)
    private InvoiceFormat invoiceFormat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "commercialInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommercialInvoiceItem> items = new ArrayList<>();

    @Builder
    public CommercialInvoice(String invoiceNumber, LocalDate invoiceDate, String shipperSellerName,
                           String shipperSellerAddress, String shipperSellerPhone, String consigneeName,
                           String consigneeAddress, String buyerName, String buyerAddress,
                           String buyerPhone, String lcNumber, LocalDate lcDate, LocalDate departureDate,
                           String vesselFlight, String fromCountry, String toDestination,
                           String shippingMarks, String termsOfDelivery, String paymentTerms,
                           String otherReferences, InvoiceFormat invoiceFormat, Project project) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.shipperSellerName = shipperSellerName;
        this.shipperSellerAddress = shipperSellerAddress;
        this.shipperSellerPhone = shipperSellerPhone;
        this.consigneeName = consigneeName;
        this.consigneeAddress = consigneeAddress;
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.buyerPhone = buyerPhone;
        this.lcNumber = lcNumber;
        this.lcDate = lcDate;
        this.departureDate = departureDate;
        this.vesselFlight = vesselFlight;
        this.fromCountry = fromCountry;
        this.toDestination = toDestination;
        this.shippingMarks = shippingMarks;
        this.termsOfDelivery = termsOfDelivery;
        this.paymentTerms = paymentTerms;
        this.otherReferences = otherReferences;
        this.invoiceFormat = invoiceFormat != null ? invoiceFormat : InvoiceFormat.USA_STANDARD;
        this.project = project;
        this.totalAmount = BigDecimal.ZERO;
    }

    public void updateCommercialInvoice(String invoiceNumber, LocalDate invoiceDate, String shipperSellerName,
                                      String shipperSellerAddress, String shipperSellerPhone, String consigneeName,
                                      String consigneeAddress, String buyerName, String buyerAddress,
                                      String buyerPhone, String lcNumber, LocalDate lcDate, LocalDate departureDate,
                                      String vesselFlight, String fromCountry, String toDestination,
                                      String shippingMarks, String termsOfDelivery, String paymentTerms,
                                      String otherReferences, InvoiceFormat invoiceFormat) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.shipperSellerName = shipperSellerName;
        this.shipperSellerAddress = shipperSellerAddress;
        this.shipperSellerPhone = shipperSellerPhone;
        this.consigneeName = consigneeName;
        this.consigneeAddress = consigneeAddress;
        this.buyerName = buyerName;
        this.buyerAddress = buyerAddress;
        this.buyerPhone = buyerPhone;
        this.lcNumber = lcNumber;
        this.lcDate = lcDate;
        this.departureDate = departureDate;
        this.vesselFlight = vesselFlight;
        this.fromCountry = fromCountry;
        this.toDestination = toDestination;
        this.shippingMarks = shippingMarks;
        this.termsOfDelivery = termsOfDelivery;
        this.paymentTerms = paymentTerms;
        this.otherReferences = otherReferences;
        this.invoiceFormat = invoiceFormat != null ? invoiceFormat : InvoiceFormat.USA_STANDARD;
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(CommercialInvoiceItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(CommercialInvoiceItem item) {
        items.add(item);
        item.assignCommercialInvoice(this);
        calculateTotalAmount();
    }

    public void removeItem(CommercialInvoiceItem item) {
        items.remove(item);
        calculateTotalAmount();
    }
}