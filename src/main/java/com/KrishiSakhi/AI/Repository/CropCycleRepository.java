package com.KrishiSakhi.AI.Repository;

// CropCycleRepository.java

import com.KrishiSakhi.AI.Model.CropCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CropCycleRepository extends JpaRepository<CropCycle, Long> {
    Optional<CropCycle> findByFarmProfileIdAndStatus(Long farmProfileId, String status);
}