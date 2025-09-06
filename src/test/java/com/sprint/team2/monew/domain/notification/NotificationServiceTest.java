package com.sprint.team2.monew.domain.notification;

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
import com.sprint.team2.monew.domain.notification.factory.*;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.notification.service.basic.BasicNotificationsService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;

    @Mock private UserRepository userRepository;

    @Mock private InterestRepository interestRepository;

    @Mock private ArticleRepository articleRepository;

    @Mock private CommentRepository commentRepository;

    @InjectMocks private BasicNotificationsService notificationService;

    @Test
    @DisplayName("관심사 키워드와 매치되는 기사가 등록되면 알림 생성됨")
    void shouldCreateNotification_whenArticleMatchesInterest() {
        //given
        User user = TestUserFactory.createUser();
        Interest interest = TestInterestFactory.createInterest();
        Article article = TestArticleFactory.createArticle();

        UUID userId = user.getId();
        UUID interestId = interest.getId();
        UUID articleId = article.getId();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        InterestArticleRegisteredEvent event = new InterestArticleRegisteredEvent(interestId, articleId, userId);

        // 알림 객체 생성
        Notification notification = Notification.builder()
                .user(user)
                .resourceType(ResourceType.INTEREST)
                .resourceId(interest.getId())
                .confirmed(false)
                .content("[관심사] 와 관련된 기사가 1건 등록되었습니다.")
                .build();

        //when
        notificationService.notifyInterestArticleRegistered(event);

        //then
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림을 보내려는 사용자가 존재하지 않는 경우")
    void shouldThrow_whenUserDoesNotExist() {
        // given
        Interest interest = TestInterestFactory.createInterest();
        Article article = TestArticleFactory.createArticle();

        UUID interestId = interest.getId();
        UUID articleId = article.getId();
        UUID nonExistentUserId = UUID.randomUUID();

        InterestArticleRegisteredEvent event = new InterestArticleRegisteredEvent(
                interestId, articleId, nonExistentUserId);

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
    void shouldCreateNotificationWhenCommentGetsLiked () {
        //given
        Reaction like = TestLikeFactory.createLike();
        Comment comment = TestCommentFactory.createComment();
        User receiver = like.getComment().getUser();

        UUID receiverId = receiver.getId();
        UUID commentId = like.getComment().getId();

        given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentLikedEvent event = new CommentLikedEvent(commentId, receiverId);

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


    }
}
