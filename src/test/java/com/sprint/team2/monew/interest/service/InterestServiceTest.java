package com.sprint.team2.monew.interest.service;

import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.basic.BasicInterestService;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.subscription.exception.SubscriptionNotFoundException;
import com.sprint.team2.monew.domain.subscription.mapper.SubscriptionMapper;
import com.sprint.team2.monew.domain.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

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

    @InjectMocks
    private BasicInterestService interestService;

    @DisplayName("올바른 유저 ID와 올바른 관심사 ID를 받으면 해당 유저 ID와 관심사 ID를 동시에 가진 구독 데이터를 구독 테이블에서 삭제한다.")
    @Test
    void unsubscribeShouldSucceedWhenValidUserIdAndInterestId() {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Subscription subscription = new Subscription();
        given(subscriptionRepository.findByUser_IdAndInterest_Id(userId,interestId)).willReturn(Optional.of(subscription));
        willDoNothing().given(subscriptionRepository).delete(subscription);
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
        willDoNothing().given(subscriptionRepository).delete(subscription);
        given(interestRepository.findById(any(UUID.class))).willReturn(Optional.of(interest));

        // when
        interestService.unsubscribe(interestId,userId);

        // then
        then(subscriptionRepository).should(times(1)).findByUser_IdAndInterest_Id(userId,interestId);
        then(subscriptionRepository).should(times(1)).delete(subscription);
        then(interestRepository).should(times(1)).findById(interestId);
        assertEquals(0,interest.getSubscriberCount());
    }
}
