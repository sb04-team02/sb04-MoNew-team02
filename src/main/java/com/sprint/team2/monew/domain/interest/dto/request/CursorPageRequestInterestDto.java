package com.sprint.team2.monew.domain.interest.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record CursorPageRequestInterestDto(
        String keyword,
        @NotBlank
        @Pattern(regexp = "name|subscriberCount")
        String orderBy,
        @NotBlank
        @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE)
        String direction,
        String cursor,
        LocalDateTime after,
        @NotNull
        @Min(1)
        Integer limit
) {
}