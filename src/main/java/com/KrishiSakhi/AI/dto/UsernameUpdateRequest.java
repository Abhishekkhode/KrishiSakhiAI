package com.KrishiSakhi.AI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsernameUpdateRequest(
        @NotBlank(message = "New username cannot be empty")
        @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
        String newUsername
) {}