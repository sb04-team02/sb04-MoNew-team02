package com.sprint.team2.monew.domain.userActivity.listener;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityCancelDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeCancelEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentUpdateEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionCancelEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionKeywordUpdateEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserCreateEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserLoginEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserUpdateEvent;
import com.sprint.team2.monew.domain.userActivity.exception.UserActivityNotFoundException;
import com.sprint.team2.monew.domain.userActivity.mapper.UserActivityMapper;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepositoryCustom;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserActivityListener {

  public final UserActivityRepository userActivityRepository;
  public final UserActivityMapper userActivityMapper;
  private final UserActivityRepositoryCustom userActivityRepositoryCustom;

  // ================================== 사용자 ==================================
  @TransactionalEventListener
  public void handleUserCreate(UserCreateEvent event) {
    UUID id = event.id();
    String email = event.email();
    String nickname = event.nickname();
    UserActivity newUserActivity = new UserActivity(
        id,
        email,
        nickname
    );
    log.info("[사용자 활동] 생성 시작 - id = {}",id);
    userActivityRepository.insert(newUserActivity);
    log.info("[사용자 활동] 생성 완료 - id = {}", id);
  }

  @TransactionalEventListener
  public void handleUserLogin(UserLoginEvent event) {
    UUID id = event.id();
    if (userActivityRepository.existsById(id)) {
      log.info("[사용자 활동] 로그인 - id = {}",id);
      return;
    }
    String email = event.email();
    String nickname = event.nickname();
    UserActivity newUserActivity = new UserActivity(
        id,
        email,
        nickname
    );
    log.info("[사용자 활동] (로그인) 생성 시작 - id = {}",id);
    userActivityRepository.save(newUserActivity);
    log.info("[사용자 활동] (로그인) 생성 완료 - id = {}", id);
  }

  @TransactionalEventListener
  public void handleUserUpdate(UserUpdateEvent event) {

    UUID userId = event.id();
    String nickname = event.nickname();

    log.info("[사용자 활동] 닉네임 수정 시작 - userId = {}", userId);
    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));
    userActivity.setNickname(nickname);
    userActivityRepository.save(userActivity);
    log.info("[사용자 활동] 닉네임 수정 완료 - userId = {}", userId);
  }

  @TransactionalEventListener
  public void handleUserDelete(UserDeleteEvent event) {
    UUID userId = event.userId();

    log.info("[사용자 활동] 유저 삭제 시작 - userId = {}", userId);
    userActivityRepository.deleteById(userId);
    log.info("[사용자 활동] 유저 삭제 완료 - userId = {}", userId);
  }

  // ================================== 구독 ==================================
  @TransactionalEventListener
  public void handleSubscriptionAdd(SubscriptionAddEvent event) {
    UUID userId = event.userId();
    UUID subscriptionId = event.id();
    SubscriptionDto subscriptionDto = userActivityMapper.toSubscriptionDto(event);

    log.info("[사용자 활동] 구독 추가 시작 - subscriptionId = {}",subscriptionId);
    userActivityRepositoryCustom.addSubscription(userId, subscriptionDto);
    log.info("[사용자 활동] 구독 추가 완료 - subscriptionId = {}", subscriptionId);
  }

  @TransactionalEventListener
  public void handleSubscriptionCancel(SubscriptionCancelEvent event) {
    UUID userId = event.userId();
    UUID subscriptionId = event.id();

    log.info("[사용자 활동] 구독 취소 시작 - subscriptionId = {}",subscriptionId);
    userActivityRepositoryCustom.cancelSubscription(userId, subscriptionId);
    log.info("[사용자 활동] 구독 취소 완료 - subscriptionId = {}", subscriptionId);
  }

  @TransactionalEventListener
  public void handleSubscriptionDelete(SubscriptionDeleteEvent event) {
    UUID interestId = event.interestId();

    log.info("[사용자 활동] 구독 삭제 시작 - interestId = {}",interestId);
    userActivityRepositoryCustom.deleteSubscription(interestId);
    log.info("[사용자 활동] 구독 취소 완료 - interestId = {}", interestId);
  }

  @TransactionalEventListener
  public void handleSubscriptionKeywordUpdate(SubscriptionKeywordUpdateEvent event) {
    UUID interestId = event.interestId();
    List<String> keywords = event.keywords();

    log.info("[사용자 활동] 구독 키워드 수정 시작 - interestId = {}",interestId);
    userActivityRepositoryCustom.updateSubscriptionKeyword(interestId, keywords);
    log.info("[사용자 활동] 구독 키워드 수정 시작 - interestId = {}", interestId);
  }

  // ================================== 댓글 ==================================
  @TransactionalEventListener
  public void handleCommentAdd(CommentAddEvent event) {
    UUID commentId = event.id();
    CommentActivityDto commentActivityDto = userActivityMapper.toCommentActivityDto(event);

    log.info("[사용자 활동] 댓글 추가 시작 - commentId = {}",commentId);
    userActivityRepositoryCustom.addComment(commentActivityDto);
    log.info("[사용자 활동] 댓글 추가 완료- commentId = {}", commentId);
  }

  @TransactionalEventListener
  public void handleCommentUpdate(CommentUpdateEvent event) {
    UUID commentId = event.id();
    CommentActivityDto commentActivityDto = userActivityMapper.toCommentActivityDto(event);

    log.info("[사용자 활동] 댓글 수정 시작 - commentId = {}", commentId);
    userActivityRepositoryCustom.updateComment(commentActivityDto);
    log.info("[사용자 활동] 댓글 수정 완료 - commentId = {}", commentId);
  }

  @TransactionalEventListener
  public void handleCommentDelete(CommentDeleteEvent event) {
    UUID commentId = event.commentId();
    CommentActivityDto commentActivityDto = userActivityMapper.toCommentActivityDto(event);

    log.info("[사용자 활동] 댓글 삭제 시작 - commentId = {}", commentId);
    userActivityRepositoryCustom.deleteComment(commentActivityDto);
    log.info("[사용자 활동] 댓글 삭제 완료- commentId = {}", commentId);
  }

  @TransactionalEventListener
  public void handleCommentLikeAdd(CommentLikeAddEvent event) {
    UUID commentId = event.commentId();
    CommentActivityLikeDto commentActivityLikeDto = userActivityMapper.toCommentActivityLikeDto(event);

    log.info("[사용자 활동] 댓글 좋아요 추가 시작 - commentId,  = {}",commentId);
    userActivityRepositoryCustom.addCommentLike(commentActivityLikeDto);
    userActivityRepositoryCustom.updateLikeCountInMyComments(
        event.commentUserId(),
        commentId,
        event.commentLikeCount()
      );

    log.info("[사용자 활동] 댓글 좋아요 추가 완료 - commentId = {}", commentId);
  }

  @TransactionalEventListener
  public void handleCommentLikeCancel(CommentLikeCancelEvent event) {
    UUID commentId = event.commentId();
    CommentActivityCancelDto commentActivityCancelDto = userActivityMapper.toCommentActivityCancelDto(event);

    log.info("[사용자 활동] 댓글 좋아요 삭제 시작 - commentId = {}", commentId);
    userActivityRepositoryCustom.cancelCommentLike(commentActivityCancelDto);
    userActivityRepositoryCustom.updateLikeCountInMyComments(
        event.commentUserId(),
        commentId,
        event.commentLikeCount()
    );
    log.info("[사용자 활동] 댓글 좋아요 삭제 완료 - commentId = {}", commentId);
  }

  // ================================== 기사 ==================================

  @TransactionalEventListener
  public void handleArticleViewAdd(ArticleViewEvent event) {
    UUID articleId = event.articleId();
    ArticleViewDto articleViewDto = userActivityMapper.toArticleViewDto(event);

    log.info("[사용자 활동] 읽은 기사 추가 시작 - articleId = {}", articleId);
    userActivityRepositoryCustom.addArticleView(articleViewDto);
    log.info("[사용자 활동] 읽은 기사 추가 완료 - articleId = {}", articleId);
  }

  @TransactionalEventListener
  public void handleArticleDelete(ArticleDeleteEvent event) {
    UUID articleId = event.articleId();

    log.info("[사용자 활동] 기사 관련 활동 삭제 시작 - articleId = {}", articleId);
    userActivityRepositoryCustom.deleteByArticleId(articleId);
    log.info("[사용자 활동] 기사 관련 활동 삭제 완료 - articleId = {}", articleId);

  }


}



