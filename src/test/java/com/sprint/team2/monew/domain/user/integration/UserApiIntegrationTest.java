package com.sprint.team2.monew.domain.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.sprint.team2.monew.domain.user.dto.request.UserLoginRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserRegisterRequest;
import com.sprint.team2.monew.domain.user.dto.request.UserUpdateRequest;
import com.sprint.team2.monew.domain.user.exception.UserErrorCode;
import com.sprint.team2.monew.global.error.BaseErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("사용자 생성 API 통합 테스트")
    void createUserSuccess() throws Exception {
        // given
        // 사용자 생성 요청 생성
        String email = "registerTest@email.com";
        String nickname = "registerTest";
        String password = "test1234";

        // when & then
        // 사용자 생성 성공
        registerUser(email, nickname, password);
    }

    @Test
    @DisplayName("사용자 생성 실패 API 통합 테스트 - 입력값 검증 실패")
    void createUserFailureInvalidRequest() throws Exception {
        // given
        // 사용자 생성 요청 생성
        String invalidEmail = "invalidEmail"; // 이메일 형식 위반
        String invalidNickname = "invalidNicknameInvalidNickname"; // 닉네임 1자 이상, 20자 이하 위반
        String invalidPassword = "invalidPasswordInvalidPassword"; // 비밀번호 6자 이상, 20자 이하 위반

        UserRegisterRequest registerRequest = new UserRegisterRequest(
                invalidEmail,
                invalidNickname,
                invalidPassword
        );
        String requestBody = objectMapper.writeValueAsString(registerRequest);

        // when & then
        // 사용자 생성 실패 - 이메일, 닉네임, 비밀번호 입력값 검증 실패
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is(BaseErrorCode.INVALID_INPUT_VALUE.toString())))
                .andExpect(jsonPath("$.details.email").value(invalidEmail))
                .andExpect(jsonPath("$.details.nickname").value(invalidNickname))
                .andExpect(jsonPath("$.details.password").value(invalidPassword));
    }

    @Test
    @DisplayName("사용자 생성 실패 API 통합 테스트 - 이메일 중복")
    void createUserFailureDuplicatedEmail() throws Exception {
        // given
        // 사용자 생성 요청 2개 생성, 중복된 이메일
        String duplicatedEmail = "duplicatedEmail@email.com";
        String nickname = "tset";
        String password = "test1234";

        UserRegisterRequest duplicateUserRegisterRequest = new UserRegisterRequest(
                duplicatedEmail,
                "test2",
                "test1234"
        );

        String duplicateUserRequestBody = objectMapper.writeValueAsString(duplicateUserRegisterRequest);

        // when & then
        // 첫 번째 사용자 생성 성공
        registerUser(duplicatedEmail, nickname, password);

        // 두 번째 사용자 생성 실패 - 중복된 이메일
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUserRequestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is(UserErrorCode.EMAIL_ALREADY_EXISTS.toString())))
                .andExpect(jsonPath("$.details.email").value(containsString(duplicatedEmail)));
    }

    @Test
    @DisplayName("사용자 로그인 API 통합 테스트")
    void loginUserSuccess() throws Exception {
        // given
        // 사용자 생성, 로그인 요청 생성
        String email = "loginTest@email.com";
        String nickname = "loginTest";
        String password = "test1234";

        UserLoginRequest loginRequest = new UserLoginRequest(
                email,
                password
        );

        String loginRequestBody = objectMapper.writeValueAsString(loginRequest);

        // when & then
        // 사용자 생성
        Map<String, String> registerUserMap = registerUser(email, nickname, password);
        String userId = registerUserMap.get("id");
        String createdAt = registerUserMap.get("createdAt");

        // 로그인 성공
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.nickname", is(nickname)))
                .andExpect(jsonPath("$.createdAt").value(createdAt));
    }

    @Test
    @DisplayName("사용자 로그인 실패 API 통합 테스트 - 입력값 검증 실패")
    void loginUserFailureInvalidRequest() throws Exception {
        // given
        // 로그인 요청 생성
        String invalidEmail = "invalidEmail";  // 이메일 형식 위반
        String invalidPassword = "invalidPasswordInvalidPassword";  // 비밀번호 6자 이상 20자 이하 위반
        UserLoginRequest loginRequest = new UserLoginRequest(
                invalidEmail,
                invalidPassword
        );
        String loginRequestBody = objectMapper.writeValueAsString(loginRequest);

        // when & then
        // 로그인 실패 - 이메일, 비밀번호 입력값 검증 실패
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(BaseErrorCode.INVALID_INPUT_VALUE.toString()))
                .andExpect(jsonPath("$.details.email").value(invalidEmail))
                .andExpect(jsonPath("$.details.password").value(invalidPassword));
    }

    @Test
    @DisplayName("사용자 로그인 실패 API 통합 테스트 - 이메일 혹은 비밀번호가 잘못됨")
    void loginUserFailureWrongEmailOrPassword() throws Exception {
        // given
        // 사용자 생성, 로그인 요청 생성
        String email = "loginTest@email.com";
        String nickname = "loginTest";
        String password = "test1234";

        String wrongEmail = "wrongEmail@email.com";
        String wrongPassword = "wrongPassword";

        UserLoginRequest wrongLoginRequest = new UserLoginRequest(
                wrongEmail,
                wrongPassword
        );
        String wrongLoginRequestBody = objectMapper.writeValueAsString(wrongLoginRequest);

        // when & then
        // 사용자 생성
        registerUser(email, nickname, password);

        // 로그인 실패 - 잘못된 이메일, 비밀번호
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongLoginRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(UserErrorCode.LOGIN_FAILED.toString()));
    }

    @Test
    @DisplayName("사용자 업데이트 API 통합 테스트")
    void updateUserSuccess() throws Exception {
        // given
        // 사용자 생성, 로그인, 업데이트 요청 생성
        String email = "updateTest@email.com";
        String nickname = "updateTest";
        String password = "test1234";

        String newNickname = "newNickname";
        UserUpdateRequest updateRequest = new UserUpdateRequest(newNickname);
        String updateRequestBody = objectMapper.writeValueAsString(updateRequest);

        // when & then
        // 사용자 생성 (업데이트 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (업데이트 시도자 = 업데이트 대상자)
        Map<String, String> loginUserMap = loginUser(email, password);
        String loginUserId = loginUserMap.get("id");
        String createdAt = loginUserMap.get("createdAt");

        // 업데이트 성공 - 업데이트 대상자와 시도자가 같고 닉네임이 제약조건에 위배되지 않음
        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loginUserId))
                .andExpect(jsonPath("$.email", is(email)))
                .andExpect(jsonPath("$.nickname", is(newNickname)))
                .andExpect(jsonPath("$.createdAt").value(createdAt));
    }

    @Test
    @DisplayName("사용자 업데이트 실패 API 통합 테스트 - 입력값 검증 실패")
    void updateUserFailureInvalidRequest() throws Exception {
        // given
        // 사용자 생성, 로그인, 업데이트 요청 생성
        String email = "updateTest@email.com";
        String nickname = "updateTest";
        String password = "test1234";

        String invalidNewNickname = "invalidNewNicknameInvalidNewNickname"; // 닉네임 2자 이상 20자 이하 위반
        UserUpdateRequest updateRequest = new UserUpdateRequest(invalidNewNickname);
        String updateRequestBody = objectMapper.writeValueAsString(updateRequest);

        // when & then
        // 사용자 생성 (업데이트 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (업데이트 시도자 = 업데이트 대상자)
        String loginUserId = loginUser(email, password).get("id");

        // 업데이트 실패 - 닉네임 제약조건 위반 (1자 이상 20자 이하 위반)
        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(BaseErrorCode.INVALID_INPUT_VALUE.toString()))
                .andExpect(jsonPath("$.details.nickname").value(invalidNewNickname));
    }

    @Test
    @DisplayName("사용자 업데이트 실패 API 통합 테스트 - 사용자 정보 수정 권한 없음")
    void updateUserFailureForbiddenUserAuthority() throws Exception {
        // given
        // 2명의 사용자 생성 요청과 로그인, 업데이트 요청 생성
        String email = "updateTest@email.com";
        String nickname = "updateTest";
        String password = "test1234";

        String otherEmail = "otherEmail@email.com";
        String otherNickname = "other";
        String otherPassword = "other1234";

        String newNickname = "newNickname"; // 닉네임 2자 이상 20자 이하 위반
        UserUpdateRequest updateRequest = new UserUpdateRequest(newNickname);
        String updateRequestBody = objectMapper.writeValueAsString(updateRequest);

        // when & then
        // 회원가입
        // 사용자 생성 (업데이트 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 생성 (업데이트 시도자)
        registerUser(otherEmail, otherNickname, otherPassword);

        // 사용자 로그인 - 업데이트 시도자로 로그인 (업데이트 시도자 =/ 업데이트 대상자)
        String otherLoginUserId = loginUser(otherEmail, otherPassword).get("id");

        // 업데이트 실패 - 대상자를 업데이트할 권한이 시도자에게 없음 (ID가 다름)
        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody)
                        .header("Monew-Request-User-ID", otherLoginUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(UserErrorCode.FORBIDDEN_USER_UPDATE.toString()))
                .andExpect(jsonPath("$.details.userId").value(userId))
                .andExpect(jsonPath("$.details.loginUserId").value(otherLoginUserId));
    }

    @Test
    @DisplayName("사용자 업데이트 실패 API 통합 테스트 - 사용자 정보 없음")
    void updateUserFailureUserNotFound() throws Exception {
        // given
        // 사용자 생성, 로그인, 업데이트 요청 생성
        String email = "updateTest@email.com";
        String nickname = "updateTest";
        String password = "test1234";

        String newNickname = "newNickname";
        UserUpdateRequest updateRequest = new UserUpdateRequest(newNickname);
        String updateRequestBody = objectMapper.writeValueAsString(updateRequest);

        // when & then
        // 사용자 생성 (업데이트 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (업데이트 시도자 = 업데이트 대상자)
        String loginUserId = loginUser(email, password).get("id");

        // 사용자 논리적 삭제
        deleteUserLogically(userId, loginUserId);

        // 사용자 업데이트 실패 - 논리적 삭제된 사용자는 업데이트 할 수 없음 (사용자 정보 없음)
        mockMvc.perform(patch("/api/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(UserErrorCode.USER_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.details.userId").value(userId));
    }

    @Test
    @DisplayName("사용자 논리적 삭제 API 통합 테스트")
    void deleteLogicallyUserSuccess() throws Exception {
        // given
        // 사용자 생성, 로그인 요청용 데이터 생성
        String email = "deleteTest@email.com";
        String nickname = "deleteTest";
        String password = "test1234";

        // when & then
        // 사용자 생성 (논리적 삭제 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (논리적 삭제 시도자 = 논리적 삭제 대상자)
        String loginUserId = loginUser(email, password).get("id");

        // 사용자 논리적 삭제 성공
        deleteUserLogically(userId, loginUserId);
    }

    @Test
    @DisplayName("사용자 논리적 삭제 실패 API 통합 테스트 - 삭제 권한 없음")
    void deleteLogicallyUserFailureForbiddenUserAuthority() throws Exception {
        // given
        // 2명의 사용자 생성 요청과 로그인 요청용 데이터 생성
        String email = "deleteTest@email.com";
        String nickname = "deleteTest";
        String password = "test1234";

        String otherEmail = "otherEmail@email.com";
        String otherNickname = "other";
        String otherPassword = "other1234";

        // when & then
        // 회원가입
        // 사용자 생성 (논리적 삭제 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 생성 (논리적 삭제 시도자)
        registerUser(otherEmail, otherNickname, otherPassword);

        // 사용자 로그인 - 논리적 삭제 시도자로 로그인 (논리적 삭제 시도자 =/ 논리적 삭제 대상자)
        String otherLoginUserId = loginUser(otherEmail, otherPassword).get("id");

        // 사용자 논리적 삭제 실패 - 대상자를 논리적으로 삭제할 권한이 시도자에게 없음 (ID가 다름)
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .header("Monew-Request-User-ID", otherLoginUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(UserErrorCode.FORBIDDEN_USER_DELETE.toString()))
                .andExpect(jsonPath("$.details.userId").value(userId))
                .andExpect(jsonPath("$.details.loginUserId").value(otherLoginUserId));
    }

    @Test
    @DisplayName("사용자 논리적 삭제 실패 API 통합 테스트 - 사용자 정보 없음")
    void deleteLogicallyUserFailureUserNotFound() throws Exception {
        // given
        // 사용자 생성, 로그인 요청용 데이터 생성
        String email = "deleteTest@email.com";
        String nickname = "deleteTest";
        String password = "test1234";

        // when & then
        // 사용자 생성 (논리적 삭제 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (논리적 삭제 시도자 = 논리적 삭제 대상자)
        String loginUserId = loginUser(email, password).get("id");

        // 사용자 물리적 삭제(강제)
        deleteUserPhysically(userId, loginUserId);

        // 사용자 논리적 삭제 실패 - 사용자 데이터 없음
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(UserErrorCode.USER_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.details.userId").value(userId));
    }

    @Test
    @DisplayName("사용자 물리적 삭제 성공 API 통합 테스트")
    void deletePhysicallyUserSuccess() throws Exception {
        // given
        // 사용자 생성, 로그인 요청용 데이터 생성
        String email = "deleteTest@email.com";
        String nickname = "deleteTest";
        String password = "test1234";

        // when & then
        // 사용자 생성 (물리적 삭제 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (물리적 삭제 시도자 = 물리적 삭제 대상자)
        String loginUserId = loginUser(email, password).get("id");

        // 사용자 물리적 삭제 성공
        deleteUserPhysically(userId, loginUserId);
    }

    @Test
    @DisplayName("사용자 물리적 삭제 실패 API 통합 테스트 - 삭제 권한 없음")
    void deletePhysicallyUserFailureForbiddenUserAuthority() throws Exception {
        // given
        // 사용자 2명 생성 요청용 데이터 생성
        String email = "test@email.com";
        String nickname = "test";
        String password = "test1234";

        String otherEmail = "otherEmail@email.com";
        String otherNickname = "other";
        String otherPassword = "other1234";

        // when & then
        // 사용자 생성 (물리적 삭제 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 생성 (물리적 삭제 시도자)
        registerUser(otherEmail, otherNickname, otherPassword);

        // 사용자 로그인 (물리적 삭제 시도자 =/ 물리적 삭제 대상자)
        String loginUserId = loginUser(otherEmail, otherPassword).get("id");

        // 사용자 물리적 삭제 실패 - 대상자를 물리적으로 삭제할 권한이 시도자에게 없음 (ID가 다름)
        mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(UserErrorCode.FORBIDDEN_USER_DELETE.toString()))
                .andExpect(jsonPath("$.details.userId").value(userId))
                .andExpect(jsonPath("$.details.loginUserId").value(loginUserId));
    }

    @Test
    @DisplayName("사용자 물리적 삭제 실패 API 통합 테스트 - 사용자 정보 없음")
    void deletePhysicallyUserFailureUserNotFound() throws Exception {
        // given
        // 사용자 생성 요청 데이터 생성
        String email = "test@email.com";
        String password = "test1234";
        String nickname = "test";

        // when & then
        // 사용자 생성 (물리적 삭제 대상자)
        String userId = registerUser(email, nickname, password).get("id");

        // 사용자 로그인 (물리적 삭제 시도자 = 물리적 삭제 대상자)
        String loginUserId = loginUser(email, password).get("id");

        // 사용자 물리적 삭제
        deleteUserPhysically(userId, loginUserId);

        // 사용자 물리적 삭제 실패 - 사용자 정보 없음 (이미 삭제됨)
        mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(UserErrorCode.USER_NOT_FOUND.toString()))
                .andExpect(jsonPath("$.details.userId").value(userId));
    }

    private Map<String, String> registerUser(String email, String nickname, String password) throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest(
                email,
                nickname,
                password
        );

        String registerRequestBody = objectMapper.writeValueAsString(registerRequest);

        MvcResult registerResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value(registerRequest.email()))
                .andExpect(jsonPath("$.nickname").value(registerRequest.nickname()))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        String id = JsonPath.parse(registerResponse).read("$.id");
        String createdAt = JsonPath.parse(registerResponse).read("$.createdAt");
        Map<String, String> registerResultMap = new HashMap<>();
        registerResultMap.put("id", id);
        registerResultMap.put("createdAt", createdAt);
        return registerResultMap;
    }

    private Map<String, String> loginUser(String email, String password) throws Exception {
        UserLoginRequest loginRequest = new UserLoginRequest(
                email,
                password
        );

        String loginRequestBody = objectMapper.writeValueAsString(loginRequest);

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value(loginRequest.email()))
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        String id = JsonPath.parse(loginResponse).read("$.id");
        String createdAt = JsonPath.parse(loginResponse).read("$.createdAt");
        Map<String, String> loginResultMap = new HashMap<>();
        loginResultMap.put("id", id);
        loginResultMap.put("createdAt", createdAt);
        return loginResultMap;
    }

    private void deleteUserLogically(String userId, String loginUserId) throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", userId)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isNoContent());
    }

    private void deleteUserPhysically(String userId, String loginUserId) throws Exception {
        mockMvc.perform(delete("/api/users/{userId}/hard", userId)
                        .header("Monew-Request-User-ID", loginUserId))
                .andExpect(status().isNoContent());
    }
}
