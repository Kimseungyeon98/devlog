package com.devlog.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SnippetRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotBlank String code,
        @NotBlank String language
) {}
