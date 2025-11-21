package com.KrishiSakhi.AI.Repository;

import com.KrishiSakhi.AI.Model.FarmProfile;
import org.springframework.data.jpa.repository.JpaRepository;

// FarmProfile entity uses Long as its primary key type
public interface FarmProfileRepository extends JpaRepository<FarmProfile, Long> {

    // Spring Data JPA automatically provides save(), findById(), etc.
    // No custom methods needed here yet.
}