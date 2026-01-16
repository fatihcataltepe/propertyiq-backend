package com.propertyiq.portfolio.mortgage.repository;

import com.propertyiq.portfolio.mortgage.model.Mortgage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MortgageRepository extends JpaRepository<Mortgage, UUID> {

    List<Mortgage> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Mortgage> findByUserIdAndIsActiveTrueOrderBySequenceNumber(UUID userId);

    List<Mortgage> findByPropertyIdOrderBySequenceNumber(UUID propertyId);

    List<Mortgage> findByPropertyIdAndIsActiveTrue(UUID propertyId);

    Optional<Mortgage> findByIdAndUserId(UUID mortgageId, UUID userId);

    @Query("SELECT COALESCE(MAX(m.sequenceNumber), 0) + 1 FROM Mortgage m WHERE m.propertyId = :propertyId")
    Integer findNextSequenceNumber(UUID propertyId);

    boolean existsByPropertyIdAndIsActiveTrue(UUID propertyId);

    @Query("""
        SELECT m FROM Mortgage m 
        WHERE m.isActive = true 
        AND m.startDate <= :dueDate 
        AND m.endDate >= :dueDate
    """)
    List<Mortgage> findByPaymentDueDate(LocalDate dueDate);
}
