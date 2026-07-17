package com.cryptopal.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiQueryRequest(

        @NotBlank(message = "question is required")
        @Size(max = 500, message = "question must be at most 500 characters")
        String question
) {
}
