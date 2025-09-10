package com.sprint.team2.monew.domain.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterestUpdateRequest(
        @NotNull
        @Size(min = 1, max = 10, message = "키워드는 1개 이상이어야 하며 10개까지 가능합니다.")
        List<@NotBlank String> keywords
) {
}
