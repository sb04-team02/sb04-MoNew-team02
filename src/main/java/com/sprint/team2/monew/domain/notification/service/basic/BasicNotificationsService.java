package com.sprint.team2.monew.domain.notification.service.basic;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.event.CommentLikedEvent;
import com.sprint.team2.monew.domain.notification.event.InterestArticleRegisteredEvent;
import com.sprint.team2.monew.domain.notification.exception.NotificationNotFoundException;
import com.sprint.team2.monew.domain.notification.mapper.NotificationMapper;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.notification.service.NotificationService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationsService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    @EventListener
    public void notifyInterestArticleRegistered(InterestArticleRegisteredEvent event) {
        log.info("[알림] 관심사 키워드 기반 기사 등록 알림 생성 시작");

        UUID interestId = event.interestId();
        UUID articleId = event.articleId();
        UUID receiverId = event.receiverId();

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("[알림] 이벤트 발행 실패 - 관심사 ID={}에 해당하는 관심사를 찾을 수 없음", interestId);
                    return new EntityNotFoundException("커스텀 예외로 대체 예정");
                });

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.warn("[알림] 이벤트 발행 실패 - 기사 ID={}에 해당하는 기사를 찾을 수 없음", articleId);
                    return new EntityNotFoundException("커스텀 예외로 대체 예정");
                });

        User user = userRepository.findById(receiverId)
                .orElseThrow(() -> {
                    log.warn("[알림] 알림 발신 실패 - 사용자가 존재하지 않음 사용자 ID={}", receiverId);
                    return UserNotFoundException.withId(receiverId);
                });

        String content = String.format("[%s]와 관련된 기사가 1건 등록되었습니다.", interest.getName());

        Notification notification = Notification.builder()
                .user(user)
                .resourceId(interest.getId())
                .resourceType(ResourceType.INTEREST)
                .content(content)
                .confirmed(false)
                .build();
        notificationRepository.save(notification);
        log.info("[알림] 구독 키워드에 대한 기사 등록 알림 저장 완료 - 기사 ID={}, 수신자 ID={}", articleId, receiverId);
    }

    @Override
    @Transactional
    @EventListener
    public void notifyCommentLiked(CommentLikedEvent event) {
        log.info("[알림] 댓글에 좋아요 눌림 알림 생성 시작");

        UUID commentId = event.commentId();
        UUID receiverId = event.receiverId();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("[알림] 이벤트 발행 실패 - 댓글 ID={}에 해당하는 댓글을 찾을 수 없음", commentId);
                    return new EntityNotFoundException("커스텀 예외로 대체 예정");
        });

        User receiverUser = userRepository.findById(receiverId)
                .orElseThrow(() -> {
                    log.warn("[알림] 알림 이밴트 발행 실패 - 대상자 ID={}에 해당하는 사용자를 찾을 수 없음", receiverId);
                    return UserNotFoundException.withId(receiverId);
                });
        String content = String.format("[%s]님이 나의 댓글을 좋아합니다.", receiverUser.getNickname());

        Notification notification = Notification.builder()
                .user(receiverUser)
                .resourceId(comment.getId())
                .resourceType(ResourceType.COMMENT)
                .content(content)
                .confirmed(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("[알림] 댓글에 대한 좋아요 알림 저장 완료 - 댓글 ID={}, 수신자 ID={}", savedNotification.getResourceId(), receiverId);
    }

    @Override
    @Transactional
    public void confirmNotification(UUID userId, UUID notificationId) {
        log.info("[알림] 알림 확인 여부 단건 수정 시작 / 사용자 ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[알림] 알림 단건 수정 실패 - 사용자가 존재하지 않음 / 사용자 ID={}", userId);
                    return UserNotFoundException.withId(userId);
                });

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> {
                    log.warn("[알림] 알림 단건 수정 실패 - 알림이 존재하지 않음 / 알림 ID={}", notificationId);
                    return NotificationNotFoundException.withId(notificationId);
                });

        if (notification.isConfirmed()) {
            log.info("[알림] 이미 확인된 알림입니다. 알림 ID={}", notificationId);
            return;
        }

        notification.setConfirmed(true);
        log.info("[알림] 단건 알림 확인 완료 - 알림 ID={}", notificationId);
    }

    @Override
    @Transactional
    public void confirmAllNotifications(UUID userId, LocalDateTime nextAfter, int size) {
        log.info("[알림] 알림 확인 여부 전건 수정 시작 / 사용자 ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[알림] 알림 전건 수정 실패 - 사용자가 존재하지 않음 / 사용자 ID={}", userId);
                    return UserNotFoundException.withId(userId);
                });

        Pageable pageRequest = PageRequest.of(0,size,Sort.by("createdAt").descending());
        Slice<Notification> slice = notificationRepository.findAllByUserIdAndConfirmedFalseAndOrderByCreatedAtDesc(
                userId, nextAfter, pageRequest);
        slice.forEach(notification -> notification.setConfirmed(true));

        notificationRepository.saveAll(slice.getContent());
        log.info("[알림] 알림 확인 여부 전건 수정 완료 / 수정 건수={}", slice.getContent().size());
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseNotificationDto getAllNotifications(UUID userId, LocalDateTime nextAfter, int size) {
        log.info("[알림] 알림 목록 조회 시작 / 사용자 ID={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[알림] 알림 조회 실패 - 사용자가 존재하지 않음 / 사용자 ID={}", userId);
                    return UserNotFoundException.withId(userId);
                });
        //Pageable 설정
        Pageable pageableRequest = PageRequest.of(0, size, Sort.by("createdAt").descending());
        Slice<Notification> slice = notificationRepository.findAllByUserIdAndConfirmedFalseAndOrderByCreatedAtDesc(userId, nextAfter, pageableRequest);

        List<Notification> notifications = slice.getContent();

        log.info("[알림] 알림 조회 결과 - 건수={}, hasNext={}", notifications.size(), slice.hasNext());

        List<NotificationDto> content = notifications.stream()
                .map(notificationMapper::toNotificationDto)
                .toList();

        String nextCursor = null;
        if (!notifications.isEmpty()) {
            nextCursor = notifications.get(notifications.size() - 1).getCreatedAt().toString();
        }

        Long totalElements = notificationRepository.countByUserId((userId));

        log.info("[알림] 응답 생성 완료 - contentSize={}, totalElements={}, hasNext={}",
                content.size(), totalElements, slice.hasNext());

        return new CursorPageResponseNotificationDto(
                content,
                nextCursor,
                nextAfter,
                size,
                totalElements,
                slice.hasNext()
        );
    }

    public void deleteConfirmedNotifications() {}
}
