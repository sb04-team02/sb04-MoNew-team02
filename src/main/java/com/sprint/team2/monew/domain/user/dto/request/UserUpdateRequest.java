package com.sprint.team2.monew.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 작성해주세요")
        @NotBlank(message = "이 입력란을 작성하세요.")
        String nickname
) {
}
