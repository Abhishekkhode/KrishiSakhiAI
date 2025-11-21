package com.KrishiSakhi.AI.Model;

import com.KrishiSakhi.AI.Model.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
@Table(name = "ks_farm_profile")
public class FarmProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data captured in Page 2 (Auto-filled by Reverse Geocoding)
    private String district;
    private String pincode; // New field
    private Double latitude;
    private Double longitude;
    private Instant creationTimestamp; // New field to mark when profile was created

    // Data captured in Page 3 (Land Details - which is now the next step)
    private Double landSizeValue;
    private String landSizeUnit;
    private String soilType;
    private String previousCrop;

    // Link back to the User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
}