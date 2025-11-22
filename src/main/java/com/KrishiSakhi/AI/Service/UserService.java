package com.KrishiSakhi.AI.Service;
import com.KrishiSakhi.AI.Model.FarmProfile;
import com.KrishiSakhi.AI.Model.User;
import com.KrishiSakhi.AI.Repository.FarmProfileRepository;
import com.KrishiSakhi.AI.Repository.UserRepository;
import com.KrishiSakhi.AI.Service.GeoLocationService.GeoCoordinates;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import com.KrishiSakhi.AI.Service.GeoLocationService.GeoData;
//@Service
//public class UserService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//    private final FarmProfileRepository farmProfileRepository;
//    private final GeoLocationService geoLocationService;
//
//    public UserService(UserRepository userRepository, FarmProfileRepository farmProfileRepository, GeoLocationService geoLocationService) {
//        this.userRepository = userRepository;
//        this.farmProfileRepository = farmProfileRepository;
//        this.geoLocationService = geoLocationService;
//    }
//
//    // --- 1. CORE SECURITY: Used by AuthTokenFilter to load user details from JWT ---
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        // Find the User based on the username stored in the JWT subject
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
//
//        // Return a Spring Security UserDetails object
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                "", // Password field is empty because authentication is via JWT token
//                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//    }
//
//    // --- 2. ONBOARDING STEP (PAGE 2): Capture final username and GPS data ---
//    /**
//     * Handles the complex logic of Page 2: Inverse Geocoding, Username update, and saving the profile.
//     * @param tempUsername The temporary username stored in the current JWT.
//     * @param manualUsername The permanent username provided by the user in the form.
//     * @param latitude GPS latitude from the client.
//     * @param longitude GPS longitude from the client.
//     */
//    @Transactional
//    public FarmProfile captureUserContext(String tempUsername, String manualUsername, Double latitude, Double longitude) throws Exception {
//
//        // a. Find the user using the temporary username from the JWT
//        User user = userRepository.findByUsername(tempUsername)
//                .orElseThrow(() -> new RuntimeException("User not found after verification."));
//
//        // b. Update the username field with the permanent manual input
//        user.setUsername(manualUsername);
//        userRepository.save(user);
//
//        // c. Geocode the Lat/Lon to get District/Pincode (Inverse Geocoding)
//        GeoLocationService.GeoData geoData = geoLocationService.reverseGeocode(latitude, longitude);
//
//        // d. Create and persist the FarmProfile with core geo-context
//        FarmProfile profile = new FarmProfile();
//        profile.setUser(user);
//        profile.setDistrict(geoData.district());
//        profile.setPincode(geoData.pincode());
//        profile.setLatitude(latitude);
//        profile.setLongitude(longitude);
//        profile.setCreationTimestamp(Instant.now());
//
//        return farmProfileRepository.save(profile);
//    }
//
//    // --- 3. HELPER METHOD: Used by the AuthController (not secure load) ---
//    public Optional<User> findUserByUsername(String username) {
//        return userRepository.findByUsername(username);
//    }
//}


// NOTE: The GeoData record is defined here for internal use, assuming it matches the GeoLocationService.
// If you are using the separate GeoLocationService file, ensure the import is correct.
// In the integrated codebase, this structure is fine.




//record GeoData(String district, String pincode) {}

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final FarmProfileRepository farmProfileRepository;
    private final GeoLocationService geoLocationService;

    public UserService(UserRepository userRepository, FarmProfileRepository farmProfileRepository, GeoLocationService geoLocationService) {
        this.userRepository = userRepository;
        this.farmProfileRepository = farmProfileRepository;
        this.geoLocationService = geoLocationService;
    }

    // --- 1. CORE SECURITY: loadUserByUsername ---
    // Used by AuthTokenFilter to authenticate the user via JWT.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                "", // Password field is empty
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // --- 2. ONBOARDING STEP (PAGE 2): Capture final username and GPS data ---
    // Executed first, creating the initial FarmProfile with geo-context.
    @Transactional
    public FarmProfile captureUserContext(String tempUsername, String manualUsername, Double latitude, Double longitude) throws Exception {

        User user = userRepository.findByUsername(tempUsername)
                .orElseThrow(() -> new RuntimeException("User not found after verification."));

        user.setUsername(manualUsername);
        userRepository.save(user);

        // Inverse Geocoding: Lat/Lon -> District/Pincode
        GeoData geoData = geoLocationService.reverseGeocode(latitude, longitude);

        FarmProfile profile = new FarmProfile();
        profile.setUser(user);
        profile.setDistrict(geoData.district());
        profile.setPincode(geoData.pincode());
        profile.setLatitude(latitude);
        profile.setLongitude(longitude);
        profile.setCreationTimestamp(Instant.now());

        return farmProfileRepository.save(profile);
    }

    // --- 3. FINAL ONBOARDING STEP (PAGE 3): Save comprehensive land details ---
    // This method resolves the error in your OnboardingController.
    @Transactional
    public FarmProfile saveFarmProfile(Long userId, Double landSizeValue, String landSizeUnit, String soilType, String previousCrop) throws Exception {

        // 1. Retrieve the existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found."));

        // 2. Retrieve the existing FarmProfile (created in the captureUserContext step)
        FarmProfile farmProfile = user.getFarmProfile();
        if (farmProfile == null) {
            throw new Exception("Farm context is incomplete. GPS/Username capture step must be completed first.");
        }

        // 3. Update fields with the final Page 3 data
        farmProfile.setLandSizeValue(landSizeValue);
        farmProfile.setLandSizeUnit(landSizeUnit);
        farmProfile.setSoilType(soilType);
        farmProfile.setPreviousCrop(previousCrop);

        // 4. Save and return the updated profile
        return farmProfileRepository.save(farmProfile);
    }

    // --- 4. HELPER METHOD ---
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    @Transactional
    public void deleteUser(Long userId) throws RuntimeException {

        // Spring Data JPA's deleteById is used, which is highly efficient.
        // However, it relies on the entity relationship (cascade) to clean up linked data.

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User with ID " + userId + " not found.");
        }

        userRepository.deleteById(userId);
    }

    @Transactional
    public User updateUsername(String authenticatedUsername, String newUsername) throws Exception {

        // 1. Check for availability of the new username
        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new IllegalArgumentException("Username '" + newUsername + "' is already taken.");
        }

        // 2. Retrieve the currently authenticated user
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user profile not found."));

        // 3. Update the username
        user.setUsername(newUsername);

        // 4. Save and return the updated user (This operation is transactional)
        return userRepository.save(user);
    }
}