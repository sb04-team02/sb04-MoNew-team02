/*****************************************************
 *  해당 테스트는 PostgreSQL에 종속되어 있는 쿼리문이 포함되어 있어서
 *  CI 환경(H2 Database) 테스트를 위해 주석처리 하였습니다.
 *
 *  통합 테스트라는 점 때문에 생성을 포함하고 있습니다.
 *  생성 시 유사도 검사를 포함한 쿼리문이 발생합니다.
 *
 *  사용된 모듈 : pg_bigm / bigm_similarity() 유사도 검사
 *  로컬 테스트에서 동작함을 확인하였습니다.
 *
 *  사용한 이유 :
 *  유사도 80% 이상 검사하기 위해 Postgresql의 similarity를 사용.
 *  기본 제공되는 pg_trgm의 similarity는 한글 미지원
 *  한글을 지원하는 pg_bigm의 bigm_similarity 사용.
 *****************************************************/

package com.sprint.team2.monew.domain.interest.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.sprint.team2.monew.domain.interest.dto.response.CursorPageResponseInterestDto;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.subscription.repository.SubscriptionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class InterestE2ETest {
    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private InterestRepository interestRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        interestRepository.deleteAll();
        subscriptionRepository.deleteAll();
        userRepository.deleteAll();
        User user = new User("admin@admin.com", "admin", "admin");
        userRepository.save(user);
    }

    @DisplayName("유저는 관심사를 생성하고 수정 및 삭제까지 가능하다.")
    @Test
    void interestE2ETestShouldSucceed() {
        // given
        Optional<User> user = userRepository.findByEmail("admin@admin.com");
        UUID userId = user.get().getId();
        InterestRegisterRequest postRequest = new InterestRegisterRequest("테스트", List.of("테스트1","테스트2","테스트3"));

        // when
        ResponseEntity<InterestDto> postResponse = rest.postForEntity("/api/interests",postRequest, InterestDto.class);

        // then
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody().name()).isEqualTo("테스트");
        assertThat(postResponse.getBody().keywords()).hasSize(3);

        // and given
        InterestUpdateRequest updateRequest = new InterestUpdateRequest(List.of("TEST1","TEST2","테스트1","테스트2"));
        UUID interestId = postResponse.getBody().id();
        HttpHeaders headers = new HttpHeaders();
        headers.add("monew-request-user-id", String.valueOf(userId));
        HttpEntity<InterestUpdateRequest> entity = new HttpEntity<>(updateRequest, headers);

        // and when
        ResponseEntity<InterestDto> updateResponse = rest.exchange(
                "/api/interests/"+interestId,
                HttpMethod.PATCH, entity, InterestDto.class);

        // and when
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().name()).isEqualTo("테스트");
        assertThat(updateResponse.getBody().keywords()).hasSize(4);

        // and when
        rest.delete("/api/interests/"+interestId);
        Optional<Interest> deleted = interestRepository.findById(interestId);
        assertThat(deleted).isEmpty();
    }

    @DisplayName("유저는 관심사를 구독 및 구독취소가 가능하다.")
    @Test
    void subscriptionAndUnsubscriptionTest() {
        // given
        Optional<User> user = userRepository.findByEmail("admin@admin.com");
        UUID userId = user.get().getId();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Monew-Request-User-Id", String.valueOf(userId));
        InterestRegisterRequest postRequest = new InterestRegisterRequest("테스트", List.of("구독","테스트"));
        HttpEntity<InterestRegisterRequest> entity = new HttpEntity<>(headers);

        // when
        ResponseEntity<InterestDto> postResponse = rest.postForEntity("/api/interests",postRequest, InterestDto.class);

        // then
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getBody().name()).isEqualTo("테스트");
        assertThat(postResponse.getBody().keywords()).hasSize(2);

        // and given
        UUID interestId = postResponse.getBody().id();

        // and when
        ResponseEntity<SubscriptionDto> subscriptionResponse =
                rest.postForEntity("/api/interests/"+interestId+"/subscriptions"
                    ,entity, SubscriptionDto.class);
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionResponse.getBody().id());

        // and then
        assertThat(subscriptionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(subscriptionResponse.getBody().interestId()).isEqualTo(interestId);
        assertThat(subscriptionResponse.getBody().interestSubscriberCount()).isEqualTo(1);
        assertThat(subscription.isPresent()).isTrue();

        // and when
        rest.exchange("/api/interests/"+interestId+"/subscriptions", HttpMethod.DELETE, entity, Void.class);

        // and then
        Optional<Subscription> deleted = subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId);
        assertThat(deleted.isPresent()).isFalse();
    }

    @DisplayName("관심사를 생성하고 구독까지 성공한 뒤 목록을 조회하여 구독여부까지 정상적으로 동작한다.")
    @Test
    void subscribedByMeTrueWhenUserSubscribeInterestTest() {
        // given
        Optional<User> user = userRepository.findByEmail("admin@admin.com");
        UUID userId = user.get().getId();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Monew-Request-User-Id", String.valueOf(userId));
        InterestRegisterRequest request1 = new InterestRegisterRequest("전자기기", List.of("컴퓨터","TV","냉장고"));
        InterestRegisterRequest request2 = new InterestRegisterRequest("노래", List.of("발라드","힙합","락","트로트"));
        InterestRegisterRequest request3 = new InterestRegisterRequest("스포츠", List.of("축구","야구","농구"));
        InterestRegisterRequest request4 = new InterestRegisterRequest("게임", List.of("RPG","FPS","스포츠"));

        // when
        ResponseEntity<InterestDto> postResponse1 = rest.postForEntity("/api/interests",request1, InterestDto.class);
        ResponseEntity<InterestDto> postResponse2 = rest.postForEntity("/api/interests",request2, InterestDto.class);
        ResponseEntity<InterestDto> postResponse3 = rest.postForEntity("/api/interests",request3, InterestDto.class);
        ResponseEntity<InterestDto> postResponse4 = rest.postForEntity("/api/interests",request4, InterestDto.class);

        // then
        assertThat(postResponse1.getBody().name()).isEqualTo("전자기기");
        assertThat(postResponse1.getBody().keywords()).hasSize(3);
        assertThat(postResponse1.getBody().subscribedByMe()).isFalse();

        assertThat(postResponse2.getBody().name()).isEqualTo("노래");
        assertThat(postResponse2.getBody().keywords()).hasSize(4);
        assertThat(postResponse2.getBody().subscribedByMe()).isFalse();

        assertThat(postResponse3.getBody().name()).isEqualTo("스포츠");
        assertThat(postResponse3.getBody().keywords()).hasSize(3);
        assertThat(postResponse3.getBody().subscribedByMe()).isFalse();

        assertThat(postResponse4.getBody().name()).isEqualTo("게임");
        assertThat(postResponse4.getBody().keywords()).hasSize(3);
        assertThat(postResponse4.getBody().subscribedByMe()).isFalse();

        // and given
        UUID interest3Id = postResponse3.getBody().id();
        HttpEntity subscriptionHttpEntity = new HttpEntity<>(headers);

        // and when
        ResponseEntity<SubscriptionDto> subscriptionResponse = rest.postForEntity("/api/interests/"+interest3Id+"/subscriptions",subscriptionHttpEntity, SubscriptionDto.class);

        // and then
        assertThat(subscriptionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(subscriptionResponse.getBody().interestId()).isEqualTo(interest3Id);
        assertThat(subscriptionResponse.getBody().interestSubscriberCount()).isEqualTo(1);

        // and given
        String keyword = "스포츠";
        String orderBy = "name";
        String direction = "ASC";
        String cursor = null;
        LocalDateTime after = null;
        Integer limit = 10;
        CursorPageRequestInterestDto pageRequest = new CursorPageRequestInterestDto(keyword,
                orderBy,
                direction,
                cursor,
                after,
                limit
        );
        HttpEntity pageEntity = new HttpEntity<>(headers);

        // and when
        ResponseEntity<CursorPageResponseInterestDto> response = rest.exchange(
                "/api/interests?keyword={keyword}&orderBy={orderBy}&direction={direction}&limit={limit}",
                HttpMethod.GET,
                pageEntity,
                CursorPageResponseInterestDto.class,
                keyword,orderBy,direction,limit
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().totalElements()).isEqualTo(2);
        Object interestObject = response.getBody().content().get(1);
        InterestDto subscribedInterest = new ObjectMapper().convertValue(interestObject, InterestDto.class);

        assertThat(subscribedInterest.subscribedByMe()).isTrue();
    }
}
