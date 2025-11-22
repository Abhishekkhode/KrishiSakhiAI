package com.KrishiSakhi.AI.Controller;

import com.KrishiSakhi.AI.Service.UserService;
import com.KrishiSakhi.AI.dto.UsernameUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint to delete the currently authenticated user's account permanently.
     * Accepts the user ID in the Request Body for security/client compatibility.
     * NOTE: This is secured by the JWT filter.
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
        try {
            Long idToDelete = request.userId();

            // --- SECURITY CHECK (CRITICAL) ---
            // 1. Get the ID of the user authenticated by the JWT token.
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUsername = ((UserDetails) authentication.getPrincipal()).getUsername();

            Long authenticatedId = userService.findUserByUsername(authenticatedUsername)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found."))
                    .getId();

            // 2. Authorization Check: The ID in the token MUST match the ID in the request body.
            if (!authenticatedId.equals(idToDelete)) {
                return ResponseEntity.status(403).body("Authorization Denied: Cannot delete another user's account.");
            }

            // 3. Perform Deletion (Cascade delete handles linked records)
            userService.deleteUser(idToDelete);

            // Return 204 No Content (Standard REST response for successful deletion)
            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            // Handle the case where deleteUser throws an exception (e.g., User not found)
            return ResponseEntity.status(404).body("Deletion Failed: " + e.getMessage());
        }
    }
    @PutMapping("/update-username")
    public ResponseEntity<?> updateUsername(@Valid @RequestBody UsernameUpdateRequest request) {
        try {
            // 1. Retrieve the current username from the JWT (Principal)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = ((UserDetails) authentication.getPrincipal()).getUsername();

            // 2. Perform the update via the Service layer
            userService.updateUsername(currentUsername, request.newUsername());

            // 3. Success: Return 200 OK
            return ResponseEntity.ok("Username updated successfully to: " + request.newUsername());

        } catch (IllegalArgumentException e) {
            // Handle case where the new username is already taken
            return ResponseEntity.status(409).body("Update Failed: " + e.getMessage());
        } catch (Exception e) {
            // Handle other server or database errors
            return ResponseEntity.status(500).body("Internal Error: Could not update username.");
        }
    }

    // DTO to capture the user ID from the JSON request body
    static record DeleteUserRequest(Long userId) {}
}