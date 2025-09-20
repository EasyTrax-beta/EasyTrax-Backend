package com.easytrax.easytraxbackend.invoice.domain.repository;

import com.easytrax.easytraxbackend.invoice.domain.CommercialInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommercialInvoiceRepository extends JpaRepository<CommercialInvoice, Long> {

    @Query("SELECT c FROM CommercialInvoice c WHERE c.project.id = :projectId AND c.project.user.id = :userId")
    Page<CommercialInvoice> findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT c FROM CommercialInvoice c LEFT JOIN FETCH c.items WHERE c.id = :id AND c.project.user.id = :userId")
    Optional<CommercialInvoice> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT c FROM CommercialInvoice c WHERE c.project.user.id = :userId")
    Page<CommercialInvoice> findByUserId(@Param("userId") Long userId, Pageable pageable);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT c FROM CommercialInvoice c WHERE c.invoiceNumber = :invoiceNumber AND c.id != :id")
    Optional<CommercialInvoice> findByInvoiceNumberAndIdNot(@Param("invoiceNumber") String invoiceNumber, @Param("id") Long id);
}
