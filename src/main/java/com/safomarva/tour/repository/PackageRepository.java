package com.safomarva.tour.repository;

import com.safomarva.tour.model.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<PackageEntity, Long> {
    Optional<PackageEntity> findByKeyName(String keyName);
}
