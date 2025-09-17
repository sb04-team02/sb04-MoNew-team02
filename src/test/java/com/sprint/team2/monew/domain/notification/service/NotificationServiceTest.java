package com.sprint.team2.monew.domain.notification.service;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.event.CommentLikedEvent;
import com.sprint.team2.monew.domain.notification.event.InterestArticleRegisteredEvent;
import com.sprint.team2.monew.domain.notification.exception.NotificationNotFoundException;
import com.sprint.team2.monew.domain.notification.factory.*;
import com.sprint.team2.monew.domain.notification.mapper.NotificationMapper;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.notification.service.basic.BasicNotificationsService;
import com.sprint.team2.monew.domain.subscription.repository.SubscriptionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @Mock private NotificationMapper notificationMapper;

    @Mock private UserRepository userRepository;

    @Mock private InterestRepository interestRepository;

    @Mock private ArticleRepository articleRepository;

    @Mock private CommentRepository commentRepository;

    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks private BasicNotificationsService notificationService;

    @Nested
    @DisplayName("create()")
    class CreateNotification {

        @Test
        @DisplayName("관심사 키워드와 매치되는 기사가 등록되면 알림 생성됨")
        void shouldCreateNotificationWhenArticleMatchesInterest() {
            //given
            Interest interest = TestInterestFactory.createInterest();
            Article article = TestArticleFactory.createArticle();

            UUID interestId = interest.getId();
            UUID articleId = article.getId();

            User user1 = TestUserFactory.createUser();
            User user2 = TestUserFactory.createUser();
            ReflectionTestUtils.setField(user1, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(user2, "id", UUID.randomUUID());
            UUID user1Id = user1.getId();
            UUID user2Id = user2.getId();
            List<UUID> receiverIds = List.of(user1Id, user2Id);

            InterestArticleRegisteredEvent event = new InterestArticleRegisteredEvent(interestId, articleId);

            given(subscriptionRepository.findUserIdsByInterestId(interestId)).willReturn(receiverIds);
            given(userRepository.findById(user1Id)).willReturn(Optional.of(user1));
            given(userRepository.findById(user2Id)).willReturn(Optional.of(user2));
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

            //when
            notificationService.notifyInterestArticleRegistered(event);

            //then
            verify(notificationRepository, times(2)).save(any(Notification.class));
        }

        @Test
        @DisplayName("알림을 보내려는 사용자가 존재하지 않는 경우")
        void shouldThrowWhenUserDoesNotExist() {
            // given
            Interest interest = TestInterestFactory.createInterest();
            Article article = TestArticleFactory.createArticle();
            UUID interestId = interest.getId();
            UUID articleId = article.getId();
            UUID nonExistentUserId = UUID.randomUUID();

            InterestArticleRegisteredEvent event = new InterestArticleRegisteredEvent(
                    interestId, articleId);
            given(subscriptionRepository.findUserIdsByInterestId(interestId)).willReturn(List.of(nonExistentUserId));
            given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

            // when & then
            assertThrows(UserNotFoundException.class, () -> {
                notificationService.notifyInterestArticleRegistered(event);
            });
        }

        @Test
        @DisplayName("사용자 본인이 작성한 댓글에 좋아요가 생기면 알림 생성됨")
        void shouldCreateNotificationWhenCommentGetsLiked() {
            //given
            Reaction like = TestLikeFactory.createLike();
            Comment comment = TestCommentFactory.createComment();
            User receiver = like.getComment().getUser();
            User likedUser = like.getUser();

            UUID receiverId = receiver.getId();
            UUID commentId = like.getComment().getId();
            UUID likedUserId = likedUser.getId();

            given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

            CommentLikedEvent event = new CommentLikedEvent(commentId, receiverId, likedUserId);

            //알림 객체 생성
            Notification notification = Notification.builder()
                    .user(receiver)
                    .resourceType(ResourceType.COMMENT)
                    .resourceId(commentId)
                    .confirmed(false)
                    .content("[%s]님이 나의 댓글을 좋아합니다.")
                    .build();

            given(notificationRepository.save(any(Notification.class))).willReturn(notification);

            //when
            notificationService.notifyCommentLiked(event);

            //then
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("좋아요 알림 대상자인 댓글작성자가 탈퇴 등의 이유로 존재하지 않을 경우 예외 발생")
        void shouldThrowWhenReceiverDoesNotExist() {
            // given
            Comment comment = TestCommentFactory.createComment();
            User likedUser = TestUserFactory.createUser();

            UUID commentId = comment.getId();
            UUID likedUserId = likedUser.getId();
            UUID nonExistentUserId = UUID.randomUUID();

            CommentLikedEvent event = new CommentLikedEvent(commentId, nonExistentUserId, likedUserId);

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
            given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

            //when & then
            assertThrows(UserNotFoundException.class, () -> {
                notificationService.notifyCommentLiked(event);
            });
        }
    }

    @Nested
    @DisplayName("find()")
    class FindNotification {

        @Test
        @DisplayName("요청이 유효할 경우 사용자가 확인하지 않은 알림 목록을 조회함")
        void shouldReturnUnreadNotificationsWithPagination() {
            //given
            User user = TestUserFactory.createUser();
            UUID userId = UUID.randomUUID();
            LocalDateTime nextAfter = LocalDateTime.now().minusDays(1);
            String nextCursor = LocalDateTime.now().minusHours(3).toString();
            int size = 10;

            Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            Notification n1 = TestNotificationFactory.createNotification(ResourceType.COMMENT, UUID.randomUUID(),"[테스트1] 님이 나의 댓글을 좋아합니다.");
            Notification n2 = TestNotificationFactory.createNotification(ResourceType.INTEREST, UUID.randomUUID(),"[테스트] 와 관련된 기사가 1건 등록되었습니다.");
            List<Notification> notifications = List.of(n1, n2);
            Slice<Notification> slice = new SliceImpl<>(notifications, pageable, false);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationRepository.findAllByUserIdWithCursorPaging(userId, nextAfter, pageable))
                    .willReturn(slice);

            // when
            notificationService.getAllNotifications(nextCursor, userId,nextAfter, size);

            // then
            verify(notificationRepository).findAllByUserIdWithCursorPaging(userId, nextAfter, pageable);
        }

        @Test
        @DisplayName("반환할 알림이 없는 경우 빈 슬라이스를 반환한다.")
        void shouldReturnEmptySliceWhenNoNotifications() {
            // given
            User user = TestUserFactory.createUser();
            UUID userId = UUID.randomUUID();
            int size = 10;
            Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
            LocalDateTime nextAfter = LocalDateTime.now().minusDays(1);
            String nextCursor = LocalDateTime.now().minusHours(3).toString();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationRepository.findAllByUserIdWithCursorPaging(userId, nextAfter, pageable))
                    .willReturn(new SliceImpl<>(List.of(),pageable, false));

            // when
            var result = notificationService.getAllNotifications(nextCursor, userId, nextAfter, size);

            // then
            assertThat(result.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateNotification {

        @Test
        @DisplayName("요청이 유효할 경우 사용자의 모든 알림을 '확인'상태로 일괄 수정함")
        void shouldUpdateAllNotificationsAsConfirmed() {
            // given
            User user = TestUserFactory.createUser();
            UUID userId = user.getId();


            Notification n1 = TestNotificationFactory.createNotification(ResourceType.COMMENT, UUID.randomUUID(),"[테스트1] 님이 나의 댓글을 좋아합니다.");
            Notification n2 = TestNotificationFactory.createNotification(ResourceType.INTEREST, UUID.randomUUID(),"[테스트] 와 관련된 기사가 1건 등록되었습니다.");
            List<Notification> notifications = List.of(n1, n2);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationRepository.findAllByUserIdAndConfirmedIsFalse(userId))
                    .willReturn(notifications);

            // when
            notificationService.confirmAllNotifications(userId);

            //then
            assertThat(n1.isConfirmed()).isTrue();
            assertThat(n2.isConfirmed()).isTrue();
            verify(notificationRepository).saveAll(notifications);
        }

        @Test
        @DisplayName("알림 수정 요청 중 사용자가 존재하지 않는 경우 예외 발생")
        void shouldThrowWhenUserDoesNotExist() {
            // given
            UUID nonExistentUserId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();
            int size = 10;
            Pageable pageable = PageRequest.of(0, size  , Sort.by("createdAt").descending());
            given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

            // when & then
            assertThrows(UserNotFoundException.class, () -> {
                notificationService.confirmAllNotifications(nonExistentUserId);
            });
        }

        @Test
        @DisplayName("요청이 유효할 경우 사용자의 알림을 '확인' 상태로 단건 수정함")
        void shouldUpdateNotificationAsConfirmed() {
            // given
            User user = TestUserFactory.createUser();
            Notification notification = TestNotificationFactory.createNotification(ResourceType.COMMENT, UUID.randomUUID(),"[테스트1] 님이 나의 댓글을 좋아합니다.");
            UUID userId = user.getId();
            UUID notificationId = notification.getId();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

            //when
            notificationService.confirmNotification(userId, notificationId);

            //then
            assertThat(notification.isConfirmed()).isTrue();
        }

        @Test
        @DisplayName("알림 수정 요청 중 알림이 존재하지 않을 경우 예외 발생")
        void shouldThrowWhenNotificationDoesNotExist() {
            // given
            User user = TestUserFactory.createUser();
            UUID userId = user.getId();
            UUID notificationId = UUID.randomUUID();

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

            assertThrows(NotificationNotFoundException.class, () -> {
                notificationService.confirmNotification(userId,notificationId );
            });
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteNotification {

        @Test
        @DisplayName("알림 삭제 성공")
        void shouldDeleteConfirmedNotification() {
            // given
            Notification noti1 = TestNotificationFactory.createConfirmNotification();
            Notification noti2 = TestNotificationFactory.createConfirmNotification();
            List<Notification> notifications = List.of(noti1, noti2);

            LocalDateTime threshold = LocalDateTime.of(2024, 1, 1, 0, 0);
            given(notificationRepository.findAllByConfirmedIsTrueAndUpdatedAtBefore(any()))
                    .willReturn(notifications);

            // when
            notificationService.deleteAllConfirmedNotifications();

            //then
            assertThat(noti1.isConfirmed()).isTrue();
            assertThat(noti2.isConfirmed()).isTrue();
            verify(notificationRepository).deleteAll(notifications);
        }

        @Test
        @DisplayName("삭제할 알림이 없는 경우 삭제 발생하지 않음")
        void shouldThrowWhenNotificationDoesNotExist() {
            //given
            LocalDateTime threshold = LocalDateTime.of(2024, 1, 1, 0, 0);
            given(notificationRepository.findAllByConfirmedIsTrueAndUpdatedAtBefore(any()))
                    .willReturn(List.of());
            //when
            notificationService.deleteAllConfirmedNotifications();

            //then
            verify(notificationRepository, never()).deleteAll(any());
        }
    }
}
