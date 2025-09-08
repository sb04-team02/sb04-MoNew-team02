package com.sprint.team2.monew.domain.notification.service.basic;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.notification.dto.response.CursorPageResponseNotificationDto;
import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.event.CommentLikedEvent;
import com.sprint.team2.monew.domain.notification.event.InterestArticleRegisteredEvent;
import com.sprint.team2.monew.domain.notification.mapper.NotificationMapper;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.notification.service.NotificationService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationsService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final ArticleRepository articleRepository;

    @EventListener
    public void notifyInterestArticleRegistered(InterestArticleRegisteredEvent event) {
        log.info("[알림] 관심사 키워드 기반 기사 등록 알림 생성 시작");

        UUID interestId = event.getInterestId();
        UUID articleId = event.getArticleId();
        UUID userId = event.getUserId();

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("[알림] 이벤트 발행 실패 - 관심사 ID={}에 해당하는 관심사를 찾을 수 없음", interestId);
                    return new IllegalArgumentException("커스텀 오류로 대체 예정");
                });

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.warn("[알림] 이벤트 발행 실패 - 기사 ID={}에 해당하는 기사를 찾을 수 없음", articleId);
                    return new IllegalArgumentException("커스텀 오류로 대체 예정");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[알림] 알림 발신 실패 - 사용자가 존재하지 않음 사용자 ID={}", userId);
                    return new UserNotFoundException();
                });

        String content = String.format("[%s]와 관련된 기사가 1건 등록되었습니다.", interest.getName());

        Notification notification = Notification.builder()
                .user(user)
                .resourceId(interest.getId())
                .resourceType(ResourceType.INTEREST)
                .content(content)
                .confirmed(false)
                .build();
        Notification savedNotification = notificationRepository.save(notification);
    }

    public NotificationDto notifyCommentLiked(CommentLikedEvent event) {
        return null;
    }

    public void confirmNotification(UUID notificationId) {}

    public void confirmAllNotifications(UUID userId) {}

    public CursorPageResponseNotificationDto getAllNotifications(UUID userId, LocalDateTime nextAfter, int size) {
        return null;
    }

    public void deleteConfirmedNotifications() {}
}
