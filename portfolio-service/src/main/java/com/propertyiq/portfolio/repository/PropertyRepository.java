package com.propertyiq.portfolio.repository;

import com.propertyiq.portfolio.model.Property;
import com.propertyiq.portfolio.model.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    List<Property> findByUserId(UUID userId);

    List<Property> findByUserIdAndStatus(UUID userId, PropertyStatus status);

    Optional<Property> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);
}
