package com.KrishiSakhi.AI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationAndUsernameRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotNull Double latitude,
        @NotNull Double longitude
) {}