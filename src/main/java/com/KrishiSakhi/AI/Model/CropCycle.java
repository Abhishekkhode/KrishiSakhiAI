package com.KrishiSakhi.AI.Model;

// CropCycle.java
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
@Table(name = "ks_crop_cycle")
public class CropCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "farm_profile_id", referencedColumnName = "id", nullable = false)
    private FarmProfile farmProfile;

    @Column(nullable = false)
    private String cropName; // Chosen crop

    @Column(nullable = false)
    private Instant sowingDate; // Anchor date for GDD

    private Double accumulatedGdd = 0.0; // Updated by scheduled job
    private Double visualScaleFactor = 0.0; // Predicted by ML

    @Column(nullable = false)
    private String status; // 'PLANNING', 'ACTIVE', 'HARVESTED'
}