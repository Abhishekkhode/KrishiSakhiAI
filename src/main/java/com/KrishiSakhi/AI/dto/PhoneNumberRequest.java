package com.KrishiSakhi.AI.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

public record PhoneNumberRequest(
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in E.164 format (e.g., +91XXXXXXXXXX)")
        String phoneNumber
) {}