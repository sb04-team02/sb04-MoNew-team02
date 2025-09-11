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

package com.sprint.team2.monew.domain.interest.integration;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.sprint.team2.monew.domain.interest.dto.response.CursorPageResponseInterestDto;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestAlreadyExistsSimilarityNameException;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.subscription.repository.SubscriptionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // 로컬 Postgresql로 테스트
@ActiveProfiles("dev")
public class InterestIntegrationTest {
    @Autowired
    private InterestService interestService;
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
    }

    @DisplayName("관심사를 생성하고 저장까지 정상 작동한다.")
    @Test
    void saveInterestShouldSucceedWhenCreateInterest() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("모뉴", List.of("관심사","유저","뉴스기사","댓글"));

        // when
        InterestDto createdInterest = interestService.create(interestRegisterRequest);

        // then
        Optional<Interest> savedInterest = interestRepository.findById(createdInterest.id());
        assertThat(savedInterest.isPresent()).isTrue();
        assertThat(savedInterest.get().getId()).isEqualTo(createdInterest.id());
        assertThat(savedInterest.get().getName()).isEqualTo(createdInterest.name());
        assertThat(savedInterest.get().getKeywords()).hasSize(4);
    }

    @DisplayName("이미 저장되어 있는 관심사와 유사한 이름의 관심사는 등록될 수 없다.")
    @Test
    void saveInterestShouldFailWhenInvalidInterestName() {
        // given
        InterestRegisterRequest setUpInterestRegisterRequest = new InterestRegisterRequest("전자기기", List.of("컴퓨터","TV","냉장고"));
        interestService.create(setUpInterestRegisterRequest);
        InterestRegisterRequest similarInterestRegisterRequest = new InterestRegisterRequest("전자기", List.of("노트북","스마트폰","태블릿"));

        // when & then
        Exception exception = assertThrows(InterestAlreadyExistsSimilarityNameException.class, () ->
                interestService.create(similarInterestRegisterRequest)
        );
        assertThat(exception.getMessage()).isEqualTo("비슷한 관심사가 이미 존재합니다.");
    }

    @DisplayName("관심사를 생성하고 삭제까지 정상적으로 동작한다.")
    @Test
    void deleteInterestShouldSucceedAfterCreation() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("전자기기", List.of("컴퓨터","TV","냉장고"));
        InterestDto createdInterest = interestService.create(interestRegisterRequest);

        // when
        Optional<Interest> savedInterest = interestRepository.findById(createdInterest.id());
        // then
        assertThat(savedInterest.isPresent()).isTrue();

        // and when
        interestService.delete(createdInterest.id());
        Optional<Interest> deletedInterest = interestRepository.findById(createdInterest.id());

        // and then
        assertThat(deletedInterest.isPresent()).isFalse();
    }

    @DisplayName("관심사를 생성하고 수정까지 정상적으로 동작한다.")
    @Test
    void updateInteretsShouldSucceedAfterCreate() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("전자기기", List.of("컴퓨터","TV","냉장고"));

        // when
        InterestDto createdInterest = interestService.create(interestRegisterRequest);

        // then
        Optional<Interest> savedInterest = interestRepository.findById(createdInterest.id());
        assertThat(savedInterest.isPresent()).isTrue();
        assertThat(savedInterest.get().getKeywords()).hasSize(3);

        // and given
        InterestUpdateRequest updateRequest = new InterestUpdateRequest(List.of("노트북","스마트폰","태블릿","컴퓨터","TV"));

        // and when
        interestService.update(createdInterest.id(), updateRequest);

        // and then
        Optional<Interest> updatedInterest = interestRepository.findById(createdInterest.id());
        assertThat(updatedInterest.isPresent()).isTrue();
        assertThat(updatedInterest.get().getName()).isEqualTo(createdInterest.name());
        assertThat(updatedInterest.get().getKeywords()).hasSize(5);
    }

    @DisplayName("관심사 생성 후 관심사 목록 조회가 성공적으로 동작한다.")
    @Test
    void readAllInterestWithPagination() {
        // given
        InterestRegisterRequest request1 = new InterestRegisterRequest("전자기기", List.of("컴퓨터","TV","냉장고"));
        InterestRegisterRequest request2 = new InterestRegisterRequest("노래", List.of("발라드","힙합","락","트로트"));
        InterestRegisterRequest request3 = new InterestRegisterRequest("스포츠", List.of("축구","야구","농구"));
        InterestRegisterRequest request4 = new InterestRegisterRequest("게임", List.of("RPG","FPS","스포츠"));

        // when
        InterestDto created1 = interestService.create(request1);
        InterestDto created2 = interestService.create(request2);
        InterestDto created3 = interestService.create(request3);
        InterestDto created4 = interestService.create(request4);

        // then
        Optional<Interest> savedInterest1 = interestRepository.findById(created1.id());
        Optional<Interest> savedInterest2 = interestRepository.findById(created2.id());
        Optional<Interest> savedInterest3 = interestRepository.findById(created3.id());
        Optional<Interest> savedInterest4 = interestRepository.findById(created4.id());

        // when
        assertThat(savedInterest1.isPresent()).isTrue();
        assertThat(savedInterest1.get().getKeywords()).hasSize(3);
        assertThat(savedInterest2.isPresent()).isTrue();
        assertThat(savedInterest2.get().getKeywords()).hasSize(4);
        assertThat(savedInterest3.isPresent()).isTrue();
        assertThat(savedInterest3.get().getKeywords()).hasSize(3);
        assertThat(savedInterest4.isPresent()).isTrue();
        assertThat(savedInterest4.get().getKeywords()).hasSize(3);

        // and given
        String keyword = "스포츠";
        String orderBy = "name";
        String direction = "DESC";
        String cursor = null;
        LocalDateTime after = null;
        int limit = 1;  // hasNext 값을 확인하기 위해 1로 설정
        CursorPageRequestInterestDto cursorPageRequestInterestDto = new CursorPageRequestInterestDto(keyword, orderBy, direction, cursor, after, limit);

        // and when
        CursorPageResponseInterestDto response = interestService.readAll(cursorPageRequestInterestDto, UUID.randomUUID());

        // and then
        assertThat(response).isNotNull();
        assertThat(response.hasNext()).isTrue();
        assertThat(response.totalElements()).isEqualTo(2);
    }

    @DisplayName("관심사를 생성하고 관심사를 구독 후 해지까지 성공한다.")
    @Test
    void subscribeAndUnsubscribeSucceed() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("전자기기", List.of("컴퓨터","TV","냉장고"));

        // when
        InterestDto createdInterest = interestService.create(interestRegisterRequest);

        // then
        Optional<Interest> savedInterest = interestRepository.findById(createdInterest.id());
        assertThat(savedInterest.isPresent()).isTrue();
        assertThat(savedInterest.get().getKeywords()).hasSize(3);

        // and given
        User user = new User("admin@admin.com","admin","amdin");
        userRepository.save(user);
        Optional<User> savedUser = userRepository.findByEmail("admin@admin.com");
        assertThat(savedUser.isPresent()).isTrue();
        UUID userId = savedUser.get().getId();
        UUID interestId = savedInterest.get().getId();

        // and when
        SubscriptionDto createdSubscription = interestService.subscribe(interestId, userId);

        // and then
        assertThat(createdSubscription).isNotNull();

        // and given
        UUID subscriptionId = createdSubscription.id();

        // and when
        interestService.unsubscribe(interestId, userId);

        // and then
        Optional<Subscription> subscription = subscriptionRepository.findById(subscriptionId);

        // and then
        assertThat(subscription.isPresent()).isFalse();
    }
}
