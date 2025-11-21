package com.KrishiSakhi.AI.Controller;


import com.KrishiSakhi.AI.Model.*;
import com.KrishiSakhi.AI.Service.UserService;
import com.KrishiSakhi.AI.dto.FarmProfileRequest;
import com.KrishiSakhi.AI.dto.LocationAndUsernameRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final UserService userService;

    public OnboardingController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for Page 2: Saves land details (size, soil, previous crop).
     * Requires valid JWT in the Authorization header.
     */
    @PostMapping("/farm-profile")
    public ResponseEntity<?> saveFarmProfile(@Valid @RequestBody FarmProfileRequest request) {
        try {
            // 1. Securely retrieve the authenticated User ID from the token context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();

            Long userId = userService.findUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in database."))
                    .getId();

            // 2. Call the UserService with the new fields
            FarmProfile savedProfile = userService.saveFarmProfile(
                    userId,
                    request.landSizeValue(),
                    request.landSizeUnit(),
                    request.soilType(),
                    request.previousCrop()
            );

            // Success: Return the saved profile details
            return ResponseEntity.ok(savedProfile);

        } catch (Exception e) {
            // Returns 400 Bad Request on validation or service failure
            return ResponseEntity.badRequest().body("Error saving farm profile: " + e.getMessage());
        }
    }

    @PostMapping("/capture-context")
    public ResponseEntity<?> captureContext(@Valid @RequestBody LocationAndUsernameRequest request) {
        try {
            // Securely retrieve the authenticated TEMPORARY Username from the token context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String tempUsername = ((UserDetails) authentication.getPrincipal()).getUsername();

            // The UserService handles:
            // 1. Inverse Geocoding (Lat/Lon -> District/Pincode).
            // 2. Updating the User's TEMPORARY username to the FINAL manual input.
            // 3. Saving the FarmProfile with the geo-data and timestamp.

            FarmProfile savedProfile = userService.captureUserContext(
                    tempUsername,
                    request.username(),
                    request.latitude(),
                    request.longitude()
            );

            return ResponseEntity.ok(savedProfile);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error capturing user context: " + e.getMessage());
        }
    }
}