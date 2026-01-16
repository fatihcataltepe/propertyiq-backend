package com.propertyiq.portfolio.mortgage.repository;

import com.propertyiq.portfolio.mortgage.model.MortgagePayment;
import com.propertyiq.portfolio.mortgage.model.PaymentStatus;
import com.propertyiq.portfolio.mortgage.model.PaymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MortgagePaymentRepository extends JpaRepository<MortgagePayment, UUID> {

    List<MortgagePayment> findByMortgageIdOrderByDueDateDesc(UUID mortgageId);

    Page<MortgagePayment> findByMortgageIdOrderByDueDateDesc(UUID mortgageId, Pageable pageable);

    List<MortgagePayment> findByMortgageIdAndStatusOrderByDueDateDesc(UUID mortgageId, PaymentStatus status);

    List<MortgagePayment> findByMortgageIdAndPaymentTypeOrderByDueDateDesc(UUID mortgageId, PaymentType paymentType);

    Optional<MortgagePayment> findByMortgageIdAndPaymentNumber(UUID mortgageId, Integer paymentNumber);

    @Query("SELECT mp FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.dueDate = :dueDate AND mp.paymentType = 'SCHEDULED'")
    Optional<MortgagePayment> findScheduledPaymentByMortgageIdAndDueDate(
            @Param("mortgageId") UUID mortgageId,
            @Param("dueDate") LocalDate dueDate);

    @Query("SELECT mp FROM MortgagePayment mp WHERE mp.status = 'SCHEDULED' AND mp.dueDate <= :date")
    List<MortgagePayment> findOverduePayments(@Param("date") LocalDate date);

    @Query("SELECT mp FROM MortgagePayment mp WHERE mp.status = 'SCHEDULED' AND mp.dueDate = :date")
    List<MortgagePayment> findPaymentsDueOn(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(MAX(mp.paymentNumber), 0) FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId")
    Integer findMaxPaymentNumber(@Param("mortgageId") UUID mortgageId);

    @Query("SELECT COUNT(mp) FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.status = :status")
    Long countByMortgageIdAndStatus(@Param("mortgageId") UUID mortgageId, @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(mp.principal), 0) FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.status = 'PAID'")
    BigDecimal sumPrincipalPaidByMortgageId(@Param("mortgageId") UUID mortgageId);

    @Query("SELECT COALESCE(SUM(mp.interest), 0) FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.status = 'PAID'")
    BigDecimal sumInterestPaidByMortgageId(@Param("mortgageId") UUID mortgageId);

    @Query("SELECT COALESCE(SUM(mp.totalAmount), 0) FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.status = 'PAID'")
    BigDecimal sumTotalPaidByMortgageId(@Param("mortgageId") UUID mortgageId);

    @Query("SELECT mp FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.status = 'PAID' ORDER BY mp.actualPaymentDate DESC")
    List<MortgagePayment> findPaidPaymentsByMortgageId(@Param("mortgageId") UUID mortgageId);

    @Query("SELECT mp FROM MortgagePayment mp WHERE mp.mortgageId = :mortgageId AND mp.paymentType = 'TOPUP' ORDER BY mp.actualPaymentDate DESC")
    List<MortgagePayment> findTopUpPaymentsByMortgageId(@Param("mortgageId") UUID mortgageId);

    boolean existsByMortgageIdAndDueDateAndPaymentType(UUID mortgageId, LocalDate dueDate, PaymentType paymentType);
}
