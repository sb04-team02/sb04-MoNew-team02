package com.sprint.team2.monew.domain.interest.service.basic;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestAlreadyExistsSimilarityNameException;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.interest.service.InterestService;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.subscription.exception.SubscriptionAlreadyExistsException;
import com.sprint.team2.monew.domain.subscription.exception.SubscriptionNotFoundException;
import com.sprint.team2.monew.domain.subscription.mapper.SubscriptionMapper;
import com.sprint.team2.monew.domain.subscription.repository.SubscriptionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicInterestService implements InterestService {
    private final InterestRepository interestRepository;
    private final InterestMapper interestMapper;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Transactional
    @Override
    public InterestDto create(InterestRegisterRequest interestRegisterRequest) {
        log.info("[관심사] 생성 서비스 호출");
        validInterestName(interestRegisterRequest.name());
        Interest interest = interestMapper.toEntity(interestRegisterRequest);
        interestRepository.save(interest);
        InterestDto interestDto = interestMapper.toDto(interest);
        log.info("[관심사] 생성 완료, Id = {}", interestDto.id());
        return interestDto;
    }

    @Override
    @Transactional
    public SubscriptionDto subscribe(UUID interestId, UUID userId) {
        log.info("[구독] 구독 등록 호출");
        Interest interest = validateInterest(interestId);
        User user = validateUser(userId);
        validateDuplicateSubscription(interestId,userId);
        Subscription subscription = new Subscription(user,interest);
        subscriptionRepository.save(subscription);
        interest.increaseSubscriber();
        log.info("[구독] 구독 등록 완료 id = {}", subscription.getId());
        return subscriptionMapper.toDto(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(UUID interestId, UUID userId) {
        log.info("[구독] 구독 취소 호출");
        Subscription subscription = validateSubscription(interestId, userId);
        Interest interest = validateInterest(interestId);
        interest.decreaseSubscriber();
        subscriptionRepository.delete(subscription);
        log.info("[구독] 구독 취소 완료");
    }

    private User validateUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("[관심사] 유저를 찾을 수 없음, id = {}", userId);
            throw UserNotFoundException.withId(userId);
        });
    }

    private Interest validateInterest(UUID interestId) {
        return interestRepository.findById(interestId).orElseThrow(() -> {
            log.error("[관심사] 관심사를 찾을 수 없음, id = {}", interestId);
            throw InterestNotFoundException.notFound(interestId);
        });
    }

    private void validateDuplicateSubscription(UUID interestId, UUID userId) {
        if (subscriptionRepository.existsByInterest_IdAndUser_Id(interestId, userId)) {
            log.error("[관심사] 이미 구독 중인 관심사를 구독할 수 없음, userId = {}, interestId = {}", userId, interestId);
            throw SubscriptionAlreadyExistsException.alreadyExists(interestId, userId);
        }
    }

    private void validInterestName(String name) {
        if (interestRepository.existsBySimilarityNameGreaterThan80Percent(name)){
            log.error("[관심사] 생성 실패: 유사한 관심사 존재 name = {}",name);
            throw InterestAlreadyExistsSimilarityNameException.alreadyExistsSimilarityName(name);
        }
    }

    private Subscription validateSubscription(UUID interestId, UUID userId) {
        return subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId)
                .orElseThrow(() -> SubscriptionNotFoundException.notFound(interestId, userId));
    }
}
