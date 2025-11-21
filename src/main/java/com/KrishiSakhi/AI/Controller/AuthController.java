//package com.KrishiSakhi.AI.Controller;
//
//import com.KrishiSakhi.AI.Model.User;
//import com.KrishiSakhi.AI.Model.RefreshToken;
//import com.KrishiSakhi.AI.Service.OtpService;
//import com.KrishiSakhi.AI.Service.RefreshTokenService;
//import com.KrishiSakhi.AI.Security.JwtUtils; // New dependency
//import com.KrishiSakhi.AI.dto.OtpVerificationRequest;
//import com.KrishiSakhi.AI.dto.PhoneNumberRequest;
//import lombok.SneakyThrows;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//
//@RestController
//@RequestMapping("/api/v1/auth")
//public class AuthController {
//
//    private final OtpService otpService;
//    private final JwtUtils jwtUtils;
//    private final RefreshTokenService refreshTokenService;
//
//    public AuthController(OtpService otpService, JwtUtils jwtUtils, RefreshTokenService refreshTokenService) {
//        this.otpService = otpService;
//        this.jwtUtils = jwtUtils;
//        this.refreshTokenService = refreshTokenService;
//    }
//
//    // DTOs for structured responses (remain the same)
//    public record JwtResponse(String accessToken, String refreshToken, Long id, String username, boolean isProfileComplete) {}
//    public record TokenRefreshRequest(String refreshToken) {}
//
//    // Endpoint 1.1: Generate OTP (Receives JSON: {"phoneNumber": "+91..."})
//    @PostMapping("/otp/generate")
//    public ResponseEntity<String> generateOtp(@RequestBody PhoneNumberRequest request) {
//        // Accesses the phone number via the DTO method
//        String message = otpService.generateAndSendOtp(request.phoneNumber());
//        return ResponseEntity.ok(message);
//    }
//
//    // Endpoint 1.2: Verify OTP and ISSUE TOKENS (Receives JSON: {"phoneNumber": "+91...", "otpCode": "123456"})
//    @PostMapping("/otp/verify")
//    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
//        try {
//            // Use DTO fields to verify the user
//            User user = otpService.verifyOtp(request.phoneNumber(), request.otpCode());
//
//            // 1. Generate short-lived Access Token
//            String accessToken = jwtUtils.generateJwtToken(user.getUsername());
//
//            // 2. Generate long-lived Refresh Token and save it to the DB
//            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
//
//            // 3. Check if the user completed the Farm Profile step (for dashboard routing)
//            boolean profileComplete = user.getFarmProfile() != null;
//
//            return ResponseEntity.ok(new JwtResponse(
//                    accessToken,
//                    refreshToken.getToken(),
//                    user.getId(),
//                    user.getUsername(),
//                    profileComplete
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//        }
//    }
//
//    // NEW Endpoint: Renews the Access Token using the long-lived Refresh Token
//    @SneakyThrows
//    @PostMapping("/token/refresh")
//    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
//        String requestRefreshToken = request.refreshToken();
//
//        return refreshTokenService.findByToken(requestRefreshToken)
//                .map(tokenObj -> {
//                    try {
//                        // 1. Verify token is not expired
//                        tokenObj = refreshTokenService.verifyExpiration(tokenObj);
//
//                        // 2. Issue a new short-lived Access Token
//                        String newAccessToken = jwtUtils.generateTokenFromUsername(
//                                tokenObj.getUser().getUsername()
//                        );
//
//                        return ResponseEntity.ok(new JwtResponse(
//                                newAccessToken,
//                                requestRefreshToken, // Return the same long-lived token
//                                tokenObj.getUser().getId(),
//                                tokenObj.getUser().getUsername(),
//                                tokenObj.getUser().getFarmProfile() != null
//                        ));
//                    } catch (Exception e) {
//                        // Handle renewal failure
//                        throw new RuntimeException("Token refresh failed: " + e.getMessage(), e);
//                    }
//                })
//                .orElseThrow(() -> new RuntimeException("Refresh token is invalid or not found in database."));
//    }
//
//}









package com.KrishiSakhi.AI.Controller;

import com.KrishiSakhi.AI.Model.User;
import com.KrishiSakhi.AI.Model.RefreshToken;
import com.KrishiSakhi.AI.Service.OtpService;
import com.KrishiSakhi.AI.Service.RefreshTokenService;
import com.KrishiSakhi.AI.Security.JwtUtils; // New dependency
import com.KrishiSakhi.AI.dto.OtpVerificationRequest;
import com.KrishiSakhi.AI.dto.PhoneNumberRequest;
import com.KrishiSakhi.AI.dto.RegistrationRequest;
import jakarta.validation.Valid;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final OtpService otpService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthController(OtpService otpService, JwtUtils jwtUtils, RefreshTokenService refreshTokenService) {
        this.otpService = otpService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    // DTOs for structured responses
    public record JwtResponse(String accessToken, String refreshToken, Long id, String username, boolean isProfileComplete) {}
    public record TokenRefreshRequest(String refreshToken) {}

    // Endpoint 1.1: Generate OTP (UPDATED: Accepts username and phone number)
//    @PostMapping("/otp/generate")
//    public ResponseEntity<String> generateOtp(@Valid @RequestBody RegistrationRequest request) {
//        String message = otpService.generateAndSendOtp(request.phoneNumber(), request.username());
//        return ResponseEntity.ok(message);
//    }
    @PostMapping("/otp/generate")
    public ResponseEntity<String> generateOtp(@Valid @RequestBody PhoneNumberRequest request) {
        // Generates a temporary unique username based on time for initial security context
        String tempUsername = "user_" + Instant.now().getEpochSecond();

        String message = otpService.generateAndSendOtp(request.phoneNumber(), tempUsername);
        return ResponseEntity.ok(message);
    }

    // Endpoint 1.2: Verify OTP and ISSUE TOKENS (Logic remains the same)
    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        try {
            User user = otpService.verifyOtp(request.phoneNumber(), request.otpCode());

            String accessToken = jwtUtils.generateJwtToken(user.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
            boolean profileComplete = user.getFarmProfile() != null;

            return ResponseEntity.ok(new JwtResponse(
                    accessToken,
                    refreshToken.getToken(),
                    user.getId(),
                    user.getUsername(),
                    profileComplete
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // NEW Endpoint: Renews the Access Token using the long-lived Refresh Token (Logic remains the same)
    @SneakyThrows
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(tokenObj -> {
                    try {
                        tokenObj = refreshTokenService.verifyExpiration(tokenObj);

                        String newAccessToken = jwtUtils.generateTokenFromUsername(
                                tokenObj.getUser().getUsername()
                        );

                        return ResponseEntity.ok(new JwtResponse(
                                newAccessToken,
                                requestRefreshToken,
                                tokenObj.getUser().getId(),
                                tokenObj.getUser().getUsername(),
                                tokenObj.getUser().getFarmProfile() != null
                        ));
                    } catch (Exception e) {
                        throw new RuntimeException("Token refresh failed: " + e.getMessage(), e);
                    }
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is invalid or not found in database."));
    }
//    public record JwtResponse(String accessToken, String refreshToken, Long id, String username, boolean isProfileComplete) {}
//    public record TokenRefreshRequest(String refreshToken) {}
}