package com.sprint.team2.monew.domain.user.controller;

import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid UserRegisterRequest request) {
        log.info("사용자 생성 요청: {}", request);
        UserDto createdUser = userService.create(request);
        log.debug("사용자 생성 응답: {}", createdUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }
}
