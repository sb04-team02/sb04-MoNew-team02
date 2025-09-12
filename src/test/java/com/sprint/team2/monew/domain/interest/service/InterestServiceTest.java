package com.sprint.team2.monew.domain.interest.service;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.sprint.team2.monew.domain.interest.dto.response.CursorPageResponseInterestDto;
import com.sprint.team2.monew.domain.interest.dto.response.InterestQueryDto;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestErrorCode;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.basic.BasicInterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.subscription.exception.SubscriptionErrorCode;
import com.sprint.team2.monew.domain.subscription.exception.SubscriptionNotFoundException;
import com.sprint.team2.monew.domain.subscription.mapper.SubscriptionMapper;
import com.sprint.team2.monew.domain.subscription.repository.SubscriptionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionAddEvent;
import com.sprint.team2.monew.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class InterestServiceTest {
    @Mock
    private InterestRepository interestRepository;
    @Mock
    private InterestMapper interestMapper;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionMapper subscriptionMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private BasicInterestService interestService;

    @BeforeEach
    void setUp() {
        Interest interest = new Interest("음악",List.of("발라드","힙합","재즈"));
        interestRepository.save(interest);
    }

    @DisplayName("관심사를 생성할 때 이름과 키워드가 제공되면 생성에 성공한다.")
    @Test
    void createInterestShouldSucceedWithNameAndKeywords() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("스포츠",List.of("축구","야구","농구"));
        Interest interest = new Interest(interestRegisterRequest);
        InterestDto interestDto = new InterestDto(UUID.randomUUID(),"스포츠", List.of("축구","야구","농구"),0L,false);
        given(interestRepository.save(interest)).willReturn(interest);
        given(interestMapper.toDto(interest)).willReturn(interestDto);
        given(interestMapper.toEntity(interestRegisterRequest)).willReturn(interest);

        // when
        InterestDto savedInterestDto = interestService.create(interestRegisterRequest);

        // then
        assertNotNull(savedInterestDto);
        assertEquals(savedInterestDto.name(),interest.getName());
        assertEquals(savedInterestDto.keywords(),interest.getKeywords());
    }

    @DisplayName("저장되는 데이터의 이름이 데이터베이스에 존재하는 데이터의 이름과 80% 이상 유사하지 않아야 한다.")
    @Test
    void createInterestShouldSucceedWhenNameSimilarityLessThen80Percent() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("스포츠",List.of("축구","야구","농구"));
        Interest interest = new Interest(interestRegisterRequest);
        InterestDto interestDto = new InterestDto(UUID.randomUUID(),"스포츠", List.of("축구","야구","농구"),0L,false);
        given(interestRepository.existsBySimilarityNameGreaterThan80Percent("스포츠")).willReturn(false);
        given(interestMapper.toEntity(interestRegisterRequest)).willReturn(interest);
        given(interestRepository.save(interest)).willReturn(interest);
        given(interestMapper.toDto(interest)).willReturn(interestDto);

        // when
        InterestDto savedInterestDto = interestService.create(interestRegisterRequest);

        // then
        assertNotNull(savedInterestDto);
        assertEquals(savedInterestDto.name(),interest.getName());
        assertEquals(savedInterestDto.keywords(),interest.getKeywords());
    }

    @DisplayName("데이터베이스에 존재하는 데이터와 80% 이상 유사한 데이터는 저장될 수 없습니다.")
    @Test
    void createInterestShouldFailWhenNameSimilarityGraterThanOrEqualTo80Percent() {
        // given
        InterestRegisterRequest interestRegisterRequest = new InterestRegisterRequest("음악",List.of("힙합","락","팝"));
        given(interestRepository.existsBySimilarityNameGreaterThan80Percent(anyString())).willReturn(true);

        Exception exception = assertThrows(BusinessException.class, () -> interestService.create(interestRegisterRequest));
        assertEquals("비슷한 관심사가 이미 존재합니다.",exception.getMessage());
    }

    @DisplayName("유저Id와 관심사Id로 관심사를 생성할 수 있음")
    @Test
    void subscribeShouldSucceedWhenValidUserAndInterest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        Subscription subscription = new Subscription(new User("email@email.com","password","nickname"), new Interest());
        SubscriptionDto subscriptionDto = new SubscriptionDto(UUID.randomUUID(),interestId,"name",List.of("keyword1","keyword2"),1, LocalDateTime.now());
        given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);
        given(subscriptionMapper.toDto(any(Subscription.class))).willReturn(subscriptionDto);
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(new User("email@email.com","password","nickname")));
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(new Interest()));
        willDoNothing().given(publisher).publishEvent(any(SubscriptionAddEvent.class));
        // when
        SubscriptionDto createdDto = interestService.subscribe(userId,interestId);

        // then
        assertEquals(interestId, createdDto.interestId());
        assertEquals("name",createdDto.interestName());
        assertEquals(1,createdDto.interestSubscriberCount());
    }

    @DisplayName("존재하지 않는 유저는 구독할 수 없습니다")
    @Test
    void subscribeShouldFailWhenInvalidUser() {
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        given(userRepository.findById(any(UUID.class))).willThrow(new BusinessException(InterestErrorCode.INTEREST_NOT_FOUND));
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(new Interest()));
        // when
        Exception exception = assertThrows(BusinessException.class, () -> {
            interestService.subscribe(userId,interestId);
        });
        assertEquals(InterestErrorCode.INTEREST_NOT_FOUND.getMessage(),exception.getMessage());
    }

    @DisplayName("존재하지 않는 관심사에 구독을 할 수 없습니다")
    @Test
    void subscribeShouldFailWhenInvalidInterest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        given(interestRepository.findById(any(UUID.class))).willThrow(new BusinessException(InterestErrorCode.INTEREST_NOT_FOUND));

        // when & then
        Exception exception = assertThrows(BusinessException.class, () -> {
            interestService.subscribe(userId,interestId);
        });
        assertEquals(InterestErrorCode.INTEREST_NOT_FOUND.getMessage(),exception.getMessage());
    }

    @DisplayName("이미 구독중인 관심사를 구독할 수 없습니다.")
    @Test
    void subscribeShouldFailWhenAlreadySubscribed() {
        // given
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(new User("email@email.com","password","nickname")));
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(new Interest()));
        given(subscriptionRepository.existsByInterest_IdAndUser_Id(any(UUID.class), any(UUID.class))).willReturn(true);

        // when & then
        Exception exception = assertThrows(BusinessException.class, () -> {
            interestService.subscribe(userId,interestId);
        });
        assertEquals(SubscriptionErrorCode.SUBSCRIPTION_ALREADY_EXISTS.getMessage(),exception.getMessage());
    }

    @DisplayName("올바른 유저 ID와 올바른 관심사 ID를 받으면 해당 유저 ID와 관심사 ID를 동시에 가진 구독 데이터를 구독 테이블에서 삭제한다.")
    @Test
    void unsubscribeShouldSucceedWhenValidUserIdAndInterestId() {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        given(subscriptionRepository.findByUser_IdAndInterest_Id(userId,interestId)).willReturn(Optional.of(subscription));
        willDoNothing().given(subscriptionRepository).delete(any(Subscription.class));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(new Interest()));

        // when
        interestService.unsubscribe(interestId,userId);

        // then
        then(subscriptionRepository).should(times(1)).delete(any());
        then(subscriptionRepository).should(times(1)).findByUser_IdAndInterest_Id(any(UUID.class), any(UUID.class));
    }

    @DisplayName("구독하지 않은 상태에서 구독 취소를 할 수 없다.")
    @Test
    void unsubscribeShouldFailWhenNotFoundSubscription() {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        given(subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId)).willReturn(Optional.empty());
        // when & then
        Exception exception = assertThrows(SubscriptionNotFoundException.class, () -> interestService.unsubscribe(interestId,userId));
        assertEquals("해당 유저는 해당 관심사를 구독중이 아닙니다.", exception.getMessage());
    }

    @DisplayName("구독 취소에 성공할 경우 Interest에 SubscriberCount를 하나 줄인다.")
    @Test
    void unsubscribeShouldDecreaseSubscriberCountWhenSucceed() {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Interest interest = new Interest("name", List.of("keyword1","keyword2"),1);
        Subscription subscription = new Subscription();
        given(subscriptionRepository.findByUser_IdAndInterest_Id(userId,interestId)).willReturn(Optional.of(subscription));
        willDoNothing().given(subscriptionRepository).delete(any(Subscription.class));
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(interest));

        // when
        interestService.unsubscribe(interestId,userId);

        // then
        then(subscriptionRepository).should(times(1)).findByUser_IdAndInterest_Id(userId,interestId);
        then(subscriptionRepository).should(times(1)).delete(subscription);
        then(interestRepository).should(times(1)).findById(interestId);
        assertEquals(0,interest.getSubscriberCount());
    }

    @DisplayName("관심사 ID가 주어지면 해당 ID가 데이터베이스에 존재하면 성공적으로 삭제된다.")
    @Test
    void deleteInterestShouldSucceedWithValidInterestId() {
        // given
        UUID interestId = UUID.randomUUID();
        Interest interest = new Interest("name", List.of("keyword1","keyword2"),1);
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(interest));
        willDoNothing().given(subscriptionRepository).deleteByInterest(interest);
        willDoNothing().given(interestRepository).delete(interest);

        // when
        interestService.delete(interestId);

        // then
        then(interestRepository).should(times(1)).findById(interestId);
        then(subscriptionRepository).should(times(1)).deleteByInterest(interest);
        then(interestRepository).should(times(1)).delete(interest);
    }

    @DisplayName("주어지는 관심사 ID가 데이터베이스에 존재하지 않으면 삭제할 수 없다.")
    @Test
    void deleteInterestShouldFailWithInvalidInterestId() {
        // given
        UUID interestId = UUID.randomUUID();
        given(interestRepository.findById(interestId)).willReturn(Optional.empty());

        // When & then
        Exception exception = assertThrows(InterestNotFoundException.class, () -> interestService.delete(interestId));
        assertEquals("관심사를 찾을 수 없습니다.", exception.getMessage());
    }

    @DisplayName("주어지는 관심사 ID의 키워드 정보를 주어지는 키워드 정보로 수정한다.")
    @Test
    void updateInterestShouldSucceedWhenValidInterestIdAndKeywords() {
        // given
        UUID interestId = UUID.randomUUID();
        InterestUpdateRequest interestUpdateRequest = new InterestUpdateRequest(List.of("updateKeyword1","updateKeyword2","updateKeyword3"));
        Interest interest = new Interest("name", List.of("keyword1","keyword2"),1);
        InterestDto interestDto = new InterestDto(interestId,"name",List.of("updateKeyword1","updateKeyword2","updateKeyword3"),0L,false);
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(interest));
        given(interestRepository.save(any(Interest.class))).willReturn(interest);
        given(interestMapper.toDto(interest)).willReturn(interestDto);

        // when
        InterestDto responseDto = interestService.update(interestId, interestUpdateRequest);

        // then
        assertEquals(interestId, responseDto.id());
        assertEquals("name",responseDto.name());
        assertEquals(List.of("updateKeyword1","updateKeyword2","updateKeyword3"),responseDto.keywords());
    }

    @DisplayName("페이지네이션 정보가 주어지면 해당 정보를 토대로 커서 기반 페이지네이션을 구현하여 반환한다.")
    @Test
    void readAllInterestShouldSucceedWithCursorPagination() {
        // given
        UUID userId = UUID.randomUUID();
        String keyword = "스포츠";
        String orderBy = "name";
        String direction = "ASC";
        int limit = 50;
        CursorPageRequestInterestDto request = new CursorPageRequestInterestDto(keyword,orderBy,direction,null,null,limit);
        InterestQueryDto interestQueryDto = new InterestQueryDto(UUID.randomUUID(), "name", List.of("1","2"),1,false,LocalDateTime.now());
        InterestDto dto = new InterestDto(interestQueryDto.id(),interestQueryDto.name(),interestQueryDto.keywords(),interestQueryDto.subscriberCount(),interestQueryDto.subscribedByMe());
        given(interestRepository.findAllPage(request,userId)).willReturn(new PageImpl<>(List.of(interestQueryDto), PageRequest.of(0,request.limit()),1));
        given(interestMapper.toDto(any(InterestQueryDto.class))).willReturn(dto);
        // when
        CursorPageResponseInterestDto response = interestService.readAll(request,userId);

        // then
        assertNotNull(response.content());
        assertEquals(dto,response.content().get(0));
        assertFalse(response.hasNext());
    }
}
