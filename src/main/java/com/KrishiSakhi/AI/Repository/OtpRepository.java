package com.KrishiSakhi.AI.Repository;


import com.KrishiSakhi.AI.Model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    // Custom Query Method
    Optional<Otp> findByPhoneNumberAndOtpCode(String phoneNumber, String otpCode);
    Optional<Otp> findByPhoneNumber(String phoneNumber);
}