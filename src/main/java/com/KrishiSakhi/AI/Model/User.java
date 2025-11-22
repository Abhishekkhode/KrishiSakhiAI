package com.KrishiSakhi.AI.Model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "ks_user") // 'user' is a reserved keyword in some DBs
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    // True after OTP verification
    private boolean isVerified = false;

    // One-to-One mapping to FarmProfile once created
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private FarmProfile farmProfile;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private RefreshToken refreshToken;
}