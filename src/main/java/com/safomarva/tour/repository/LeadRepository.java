package com.safomarva.tour.repository;

import com.safomarva.tour.model.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<LeadEntity, Long> {
    List<LeadEntity> findFirst10ByOrderByIdDesc();
    List<LeadEntity> findAllByOrderByCreatedAtDesc();
}
