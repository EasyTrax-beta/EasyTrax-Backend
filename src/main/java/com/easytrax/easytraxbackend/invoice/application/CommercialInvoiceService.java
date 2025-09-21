package com.easytrax.easytraxbackend.invoice.application;

import com.easytrax.easytraxbackend.global.code.status.ErrorStatus;
import com.easytrax.easytraxbackend.global.exception.GeneralException;
import com.easytrax.easytraxbackend.invoice.api.dto.request.CommercialInvoiceCreateRequest;
import com.easytrax.easytraxbackend.invoice.api.dto.response.CommercialInvoiceListResponse;
import com.easytrax.easytraxbackend.invoice.api.dto.response.CommercialInvoiceResponse;
import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoice;
import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoiceItem;
import com.easytrax.easytraxbackend.invoice.domain.repository.CommercialInvoiceRepository;
import com.easytrax.easytraxbackend.project.domain.Project;
import com.easytrax.easytraxbackend.project.domain.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommercialInvoiceService {

    private final CommercialInvoiceRepository commercialInvoiceRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public CommercialInvoiceResponse createCommercialInvoice(CommercialInvoiceCreateRequest request, Long userId) {
        validateInvoiceNumber(request.invoiceNumber());
        Project project = findProjectByIdAndUserId(request.projectId(), userId);

        CommercialInvoice commercialInvoice = CommercialInvoice.builder()
                .invoiceNumber(request.invoiceNumber())
                .invoiceDate(request.invoiceDate())
                .shipperSellerName(request.shipperSellerName())
                .shipperSellerAddress(request.shipperSellerAddress())
                .shipperSellerPhone(request.shipperSellerPhone())
                .consigneeName(request.consigneeName())
                .consigneeAddress(request.consigneeAddress())
                .buyerName(request.buyerName())
                .buyerAddress(request.buyerAddress())
                .buyerPhone(request.buyerPhone())
                .lcNumber(request.lcNumber())
                .lcDate(request.lcDate())
                .departureDate(request.departureDate())
                .vesselFlight(request.vesselFlight())
                .fromCountry(request.fromCountry())
                .toDestination(request.toDestination())
                .shippingMarks(request.shippingMarks())
                .termsOfDelivery(request.termsOfDelivery())
                .paymentTerms(request.paymentTerms())
                .otherReferences(request.otherReferences())
                .invoiceFormat(request.invoiceFormat())
                .project(project)
                .build();

        request.items().forEach(itemRequest -> {
            CommercialInvoiceItem item = CommercialInvoiceItem.builder()
                    .packageCount(itemRequest.packageCount())
                    .packageType(itemRequest.packageType())
                    .goodsDescription(itemRequest.goodsDescription())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .build();
            commercialInvoice.addItem(item);
        });

        CommercialInvoice savedInvoice = commercialInvoiceRepository.save(commercialInvoice);
        return CommercialInvoiceResponse.of(savedInvoice);
    }

    public Page<CommercialInvoiceListResponse> findCommercialInvoicesByProject(Long projectId, Long userId, Pageable pageable) {
        return commercialInvoiceRepository.findByProjectIdAndUserId(projectId, userId, pageable)
                .map(CommercialInvoiceListResponse::of);
    }

    public Page<CommercialInvoiceListResponse> findCommercialInvoicesByUser(Long userId, Pageable pageable) {
        return commercialInvoiceRepository.findByUserId(userId, pageable)
                .map(CommercialInvoiceListResponse::of);
    }

    public CommercialInvoiceResponse findCommercialInvoiceById(Long id, Long userId) {
        CommercialInvoice commercialInvoice = findCommercialInvoiceByIdAndUserId(id, userId);
        return CommercialInvoiceResponse.of(commercialInvoice);
    }

    @Transactional
    public CommercialInvoiceResponse updateCommercialInvoice(Long id, CommercialInvoiceCreateRequest request, Long userId) {
        CommercialInvoice commercialInvoice = findCommercialInvoiceByIdAndUserId(id, userId);
        
        if (!commercialInvoice.getInvoiceNumber().equals(request.invoiceNumber())) {
            validateInvoiceNumberForUpdate(request.invoiceNumber(), id);
        }

        commercialInvoice.getItems().clear();
        
        commercialInvoice.updateCommercialInvoice(
                request.invoiceNumber(),
                request.invoiceDate(),
                request.shipperSellerName(),
                request.shipperSellerAddress(),
                request.shipperSellerPhone(),
                request.consigneeName(),
                request.consigneeAddress(),
                request.buyerName(),
                request.buyerAddress(),
                request.buyerPhone(),
                request.lcNumber(),
                request.lcDate(),
                request.departureDate(),
                request.vesselFlight(),
                request.fromCountry(),
                request.toDestination(),
                request.shippingMarks(),
                request.termsOfDelivery(),
                request.paymentTerms(),
                request.otherReferences(),
                request.invoiceFormat()
        );

        request.items().forEach(itemRequest -> {
            CommercialInvoiceItem item = CommercialInvoiceItem.builder()
                    .packageCount(itemRequest.packageCount())
                    .packageType(itemRequest.packageType())
                    .goodsDescription(itemRequest.goodsDescription())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .build();
            commercialInvoice.addItem(item);
        });

        return CommercialInvoiceResponse.of(commercialInvoice);
    }

    @Transactional
    public void deleteCommercialInvoice(Long id, Long userId) {
        CommercialInvoice commercialInvoice = findCommercialInvoiceByIdAndUserId(id, userId);
        commercialInvoiceRepository.delete(commercialInvoice);
    }

    private void validateInvoiceNumber(String invoiceNumber) {
        if (commercialInvoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }
    }

    private void validateInvoiceNumberForUpdate(String invoiceNumber, Long excludeId) {
        if (commercialInvoiceRepository.findByInvoiceNumberAndIdNot(invoiceNumber, excludeId).isPresent()) {
            throw new GeneralException(ErrorStatus.DUPLICATE_RESOURCE);
        }
    }

    private Project findProjectByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND));
    }

    public CommercialInvoice findCommercialInvoiceByIdAndUserId(Long id, Long userId) {
        return commercialInvoiceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMERCIAL_INVOICE_NOT_FOUND));
    }
}