package com.sprint.team2.monew.domain.user.controller;

import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserUpdateRequest;
import com.sprint.team2.monew.domain.user.dto.response.UserDto;
import com.sprint.team2.monew.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid UserRegisterRequest request) {
        log.info("[사용자] 등록 요청 수신 - email={}, nickname={}",
                request.email(),
                request.nickname()
        );
        UserDto createdUserDto = userService.create(request);
        log.info("[사용자] 등록 응답 - id={}, email={}, nickname={}, createdAt={}",
                createdUserDto.id(),
                createdUserDto.email(),
                createdUserDto.nickname(),
                createdUserDto.createdAt()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUserDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid UserLoginRequest request) {
        log.info("[사용자] 로그인 요청 수신");
        UserDto createdUserDto = userService.login(request);
        log.info("[사용자] 로그인 응답 - id={}, email={}, nickname={}, createdAt={}",
                createdUserDto.id(),
                createdUserDto.email(),
                createdUserDto.nickname(),
                createdUserDto.createdAt()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(createdUserDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@PathVariable("userId") UUID userId,
                                          @RequestBody @Valid UserUpdateRequest request,
                                          @RequestHeader("Monew-Request-User-ID") UUID loginUserId) {
        log.info("[사용자] 정보 수정 요청 수신");
        UserDto updatedUserDto = userService.update(userId, request, loginUserId);
        log.info("[사용자] 정보 수정 응답 - nickname={}",
                updatedUserDto.nickname());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(updatedUserDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteLogically(@PathVariable("userId") UUID userId,
                                                @RequestHeader("Monew-Request-User-ID") UUID loginUserId) {
        userService.deleteLogically(userId, loginUserId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
