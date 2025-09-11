package com.sprint.team2.monew.domain.interest.service.basic;

import com.sprint.team2.monew.domain.interest.dto.InterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import com.sprint.team2.monew.domain.interest.dto.request.InterestUpdateRequest;
import com.sprint.team2.monew.domain.interest.dto.response.CursorPageResponseInterestDto;
import com.sprint.team2.monew.domain.interest.dto.response.InterestQueryDto;
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
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionDeleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    // User Activity 이벤트
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    @Override
    public CursorPageResponseInterestDto readAll(CursorPageRequestInterestDto pageRequestDto, UUID userId) {
        log.info("[관심사] 목록 조회 실행 userId = {}", userId);
        Slice<InterestQueryDto> pageQuery = interestRepository.findAllPage(pageRequestDto, userId);

        if (pageQuery.getContent().isEmpty()) {
            return CursorPageResponseInterestDto.from(Page.empty(),null,null);
        }

        InterestQueryDto lastDto = pageQuery.getContent().get(pageQuery.getContent().size() - 1);
        String lastItemCursor = null;

        if ("name".equalsIgnoreCase(pageRequestDto.orderBy())){
            lastItemCursor = lastDto.name();
        } else {
            lastItemCursor = String.valueOf(lastDto.subscriberCount());
        }

        LocalDateTime lastItemAfter = lastDto.createdAt();
        Slice<InterestDto> page = pageQuery.map(interestMapper::toDto);
        Long totalElements = interestRepository.countTotalElements(pageRequestDto.keyword());
        CursorPageResponseInterestDto response = CursorPageResponseInterestDto.from(page,lastItemCursor,lastItemAfter,totalElements);
        log.info("[관심사] 목록 조회 완료 userId = {}, 검색어 = {}, 결과수 = {}",userId, pageRequestDto.keyword(),response.content().size());
        log.debug("[관심사] 목록 조회 완료 cursor = {}, after = {}", lastItemCursor, lastItemAfter);

        return response;
    }

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

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new SubscriptionAddEvent(
            subscription.getId(),
            interest.getId(),
            interest.getName(),
            interest.getKeywords(),
            interest.getSubscriberCount(),
            interest.getCreatedAt(),
            user.getId()
        ));

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

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new SubscriptionDeleteEvent(
            subscription.getId(),
            interestId,
            userId
        ));
    }

    @Override
    @Transactional
    public void delete(UUID interestId) {
        log.info("[관심사] 관심사 삭제 호출");
        Interest interest = validateInterest(interestId);
        subscriptionRepository.deleteByInterest(interest);
        log.debug("[구독] 관심사 삭제에 따라 구독 삭제");
        interestRepository.delete(interest);
        log.info("[관심사] 관심사 삭제 완료");
    }

    @Override
    @Transactional
    public InterestDto update(UUID interestId, InterestUpdateRequest interestUpdateRequest) {
        log.info("[관심사] 관심사 수정 호출 id = {}", interestId);
        Interest interest = validateInterest(interestId);
        log.debug("[관심사] 수정 전 키워드 keywords = {}",interest.getKeywords());
        interest.setKeywords(interestUpdateRequest.keywords());
        log.debug("[관심사] 수정 후 키워드 keywords = {}",interest.getKeywords());
        interestRepository.save(interest);
        log.info("[관심사] 관심사 수정 완료 id = {}", interestId);
        return interestMapper.toDto(interest);
    }

    private User validateUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("[관심사] 유저를 찾을 수 없음, id = {}", userId);
            return UserNotFoundException.withId(userId);
        });
    }

    private Interest validateInterest(UUID interestId) {
        return interestRepository.findById(interestId).orElseThrow(() -> {
            log.error("[관심사] 관심사를 찾을 수 없음, id = {}", interestId);
            return InterestNotFoundException.notFound(interestId);
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
                .orElseThrow(() -> {
                    log.error("[구독] 해당 유저는 해당 관심사를 구독중이 아님. userId= {}, interestId = {}", userId, interestId);
                    return SubscriptionNotFoundException.notFound(interestId, userId);
                });
    }
}
