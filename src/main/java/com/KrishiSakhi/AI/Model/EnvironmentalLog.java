package com.KrishiSakhi.AI.Model;

//public class EnvironmentalLog   {
//}
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
@Table(name = "ks_environmental_log")
public class EnvironmentalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The mandatory time-series partitioning column for TimescaleDB
    @Column(nullable = false, columnDefinition = "TIMESTAMPTZ NOT NULL")
    private Instant time;

    // Foreign key to link to the specific farm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_profile_id", nullable = false)
    private FarmProfile farmProfile;

    // Data for GDD calculation
    private Double maxTemperatureC;
    private Double minTemperatureC;
    private Double totalRainfallMm;
}
