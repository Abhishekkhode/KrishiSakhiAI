package com.KrishiSakhi.AI.Controller;

import com.KrishiSakhi.AI.Model.User;
import com.KrishiSakhi.AI.Model.FarmProfile;
import com.KrishiSakhi.AI.Model.RefreshToken;
import com.KrishiSakhi.AI.Security.JwtUtils;
import com.KrishiSakhi.AI.Service.RefreshTokenService;
import com.KrishiSakhi.AI.Service.UserService;
import com.KrishiSakhi.AI.dto.FarmProfileRequest;
import com.KrishiSakhi.AI.dto.LocationAndUsernameRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public OnboardingController(UserService userService, JwtUtils jwtUtils, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;

    }
    public record JwtResponse(String accessToken, String refreshToken, Long id, String username, boolean isProfileComplete) {}
    public record TokenRefreshRequest(String refreshToken) {}


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
            String accessToken = jwtUtils.generateJwtToken(username);

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

//    @PostMapping("/capture-context")
//    public ResponseEntity<?> captureContext(@Valid @RequestBody LocationAndUsernameRequest request) {
//        try {
//            // Securely retrieve the authenticated TEMPORARY Username from the token context
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String tempUsername = ((UserDetails) authentication.getPrincipal()).getUsername();
//
//            // The UserService handles:
//            // 1. Inverse Geocoding (Lat/Lon -> District/Pincode).
//            // 2. Updating the User's TEMPORARY username to the FINAL manual input.
//            // 3. Saving the FarmProfile with the geo-data and timestamp.
//            String accessToken = jwtUtils.generateJwtToken(((UserDetails) authentication.getPrincipal()).getUsername());
//            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
//            boolean profileComplete = user.getFarmProfile() != null;
//            FarmProfile savedProfile = userService.captureUserContext(
//                    tempUsername,
//                    request.username(),
//                    request.latitude(),
//                    request.longitude()
//            );
//
//
////            return ResponseEntity.ok(new AuthController.JwtResponse(accessToken));
//            return ResponseEntity.ok(new AuthController.JwtResponse(
//                    accessToken,
//                    refreshToken.getToken(),
//                    user.getId(),
//                    user.getUsername(),
//                    profileComplete
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Error capturing user context: " + e.getMessage());
//        }
//    }




    @PostMapping("/capture-context")
    public ResponseEntity<?> captureContext(@Valid @RequestBody LocationAndUsernameRequest request) {
        try {
            // 1. Get the authenticated TEMPORARY username (from the old JWT)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String tempUsername = ((UserDetails) authentication.getPrincipal()).getUsername();

            // 2. Perform the database update and profile creation
            FarmProfile savedProfile = userService.captureUserContext(
                    tempUsername,
                    request.username(), // The new permanent username
                    request.latitude(),
                    request.longitude()
            );

            // 3. Retrieve the updated user data to get the new ID/Username
            // The captureUserContext method updates the username but the security context still holds the old one.
            // We fetch the user again using the NEW permanent username.
            com.KrishiSakhi.AI.Model.User updatedUser = userService.findUserByUsername(request.username())
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve updated user data."));

            // --- TOKEN RENEWAL LOGIC (CRUCIAL STEP) ---

            // 4. Generate NEW Access Token using the permanent username
            String newAccessToken = jwtUtils.generateJwtToken(updatedUser.getUsername());

            // 5. Generate NEW Refresh Token (using the permanent User ID)
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(updatedUser.getId());

            // 6. Check profile status
            boolean profileComplete = savedProfile != null; // True since this call creates the profile

            // 7. Return the new tokens and user details
            return ResponseEntity.ok(new JwtResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    updatedUser.getId(),
                    updatedUser.getUsername(),
                    profileComplete
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error capturing user context: " + e.getMessage());
        }
    }
}