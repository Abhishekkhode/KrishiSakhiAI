package com.KrishiSakhi.AI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FarmProfileRequest(
        @NotNull Double landSizeValue,
        @NotBlank String landSizeUnit,
        @NotBlank String soilType,
        String previousCrop
) {}