package com.sprint.team2.monew.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
        @Email(message = "유효한 이메일 주소를 입력해주세요")
        @NotBlank(message = "이 입력란을 작성하세요.")
        String email,

        @Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하로 작성해주세요")
        @NotBlank(message = "이 입력란을 작성하세요.")
        String password
) {
}