package com.sprint.team2.monew.domain.userActivity.listener;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeCancelEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentUpdateEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionCancelEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserCreateEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.userEvent.UserUpdateEvent;
import com.sprint.team2.monew.domain.userActivity.exception.UserActivityNotFoundException;
import com.sprint.team2.monew.domain.userActivity.mapper.UserActivityMapper;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityListener {

  /**
   * 나중에 최적화:
   * - repository에 @Query, @Update 알아보기
   */

  public final UserActivityRepository userActivityRepository;
  public final UserActivityMapper userActivityMapper;

  // ================================== 사용자 ==================================
  // 사용자 생성
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
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
    // save to mongodb
    userActivityRepository.save(newUserActivity);

    log.info("[사용자 활동] 생성 완료 - id = {}", id);
  }

  // 사용자 닉네임 수정
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleUserUpdate(UserUpdateEvent event) {

    UUID userId = event.id();
    String nickname = event.nickname();

    log.info("[사용자 활동] 닉네임 수정 시작 - userId = {}", userId);
    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));
    // save to mongodb
    userActivity.setNickname(nickname);
    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 닉네임 수정 완료 - userId = {}", userId);
  }

  // 사용자 삭제
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleUserDelete(UserDeleteEvent event) {

    UUID userId = event.userId();

    log.info("[사용자 활동] 유저 삭제 시작 - userId = {}", userId);
    userActivityRepository.deleteById(userId);

    log.info("[사용자 활동] 유저 삭제 완료 - userId = {}", userId);
  }

  // ================================== 구독 ==================================
  // 구독 업데이트
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSubscriptionAdd(SubscriptionAddEvent event) {
    UUID userId = event.userId();
    UUID subscriptionId = event.id();
    SubscriptionDto subscriptionDto = userActivityMapper.toSubscriptionDto(event);

    log.info("[사용자 활동] 구독 추가 시작 - subscriptionId = {}",subscriptionId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));
    List<SubscriptionDto> subscriptionDtos = userActivity.getSubscriptions();

//    if (!subscriptionDtos.contains(subscriptionDto)) {
//      subscriptionDtos.add(0, subscriptionDto);
//    }
    subscriptionDtos.add(0, subscriptionDto);

    if (subscriptionDtos.size() > 10) {
      subscriptionDtos.remove(subscriptionDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 구독 추가 완료 - subscriptionId = {}", subscriptionId);
  }

  // 구독 업데이트
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSubscriptionCancel(SubscriptionCancelEvent event) {
    UUID userId = event.userId();
    UUID subscriptionId = event.id();

    log.info("[사용자 활동] 구독 취소 시작 - subscriptionId = {}",subscriptionId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));

    userActivity.getSubscriptions()
        .removeIf(s -> s.interestId().equals(event.interestId()));

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 구독 취소 완료 - subscriptionId = {}", subscriptionId);
  }

  // 관심사가 삭제되었을 때, 유저가 구독했으면 삭제
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleSubscriptionDelete(SubscriptionDeleteEvent event) {
    UUID interestId = event.interestId();

    log.info("[사용자 활동] 구독 삭제 시작 - interestId = {}",interestId);

    List<UserActivity> userActivityList = userActivityRepository.findBySubscriptionsInterestId(interestId);
    if (userActivityList.isEmpty()) {
      log.info("[사용자 활동] 해당 관심사를 구독한 사용자가 없습니다 - interestId = {}",interestId);
      return;
    }
    for (UserActivity userActivity : userActivityList) {
      userActivity.getSubscriptions()
          .removeIf(s -> s.interestId().equals(interestId));
    }

    userActivityRepository.saveAll(userActivityList);
    log.info("[사용자 활동] 구독 취소 완료 - interestId = {}", interestId);
  }

  // ================================== 댓글 (최신 10개) ==================================
  // 유저 댓글 추가
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleCommentAdd(CommentAddEvent event) {
    UUID userId = event.userId();
    UUID commentId = event.id();
    CommentActivityDto commentActivityDto = userActivityMapper.toCommentActivityDto(event);

    log.info("[사용자 활동] 댓글 추가 시작 - commentId = {}",commentId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));
    List<CommentActivityDto> commentActivityDtos = userActivity.getComments();

    commentActivityDtos.add(0, commentActivityDto);

    if (commentActivityDtos.size() > 10) {
      commentActivityDtos.remove(commentActivityDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 추가 완료- commentId = {}", commentId);
  }

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleCommentUpdate(CommentUpdateEvent event) {
    UUID commentId = event.id();
    UUID userId = event.userId();

    log.info("[사용자 활동] 댓글 수정 시작 - commentId = {}", commentId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));

    List<CommentActivityDto> comments = userActivity.getComments();
    int indexUpdate = -1;
    for (int i = 0; i < comments.size(); i++) {
      if (comments.get(i).id().equals(commentId)) {
        indexUpdate = i;
      }
    }
    if (indexUpdate != -1) {
      CommentActivityDto commentActivityDto = userActivityMapper.toCommentActivityDto(event);
      comments.set(indexUpdate, commentActivityDto);
    } else {
      log.warn("[사용자 활동] 수정할 댓글 Id {}를 최근 활동 내역에서 찾지 못했습니다.", commentId);
    }
    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 수정 완료 - commentId = {}", commentId);
  }

  // 유저 댓글 삭제
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleCommentDelete(CommentDeleteEvent event) {
    UUID userId = event.userId();
    UUID commentId = event.commentId();

    log.info("[사용자 활동] 댓글 삭제 시작 - commentId = {}", commentId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));

    userActivity.getComments()
        .removeIf(c -> c.id().equals(commentId));

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 삭제 완료- commentId = {}", commentId);
  }

  // ================================== 댓글 좋아요 (최신 10개) ==================================

  // 유저 댓글 좋아요
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleCommentLikeAdd(CommentLikeAddEvent event) {
    UUID commentId = event.commentId();
    UUID commentUserId = event.commentUserId();
    CommentActivityLikeDto commentActivityLikeDto = userActivityMapper.toCommentActivityLikeDto(event);

    log.info("[사용자 활동] 댓글 좋아요 추가 시작 - commentId = {}",commentId);

    UserActivity userActivity = userActivityRepository.findById(commentUserId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(commentUserId));
    List<CommentActivityLikeDto> commentActivityLikeDtos = userActivity.getCommentLikes();

//    if (!commentActivityLikeDtos.contains(commentActivityLikeDto)) {
//      commentActivityLikeDtos.add(0, commentActivityLikeDto);
//    }

    commentActivityLikeDtos.add(0, commentActivityLikeDto);

    if (commentActivityLikeDtos.size() > 10) {
      commentActivityLikeDtos.remove(commentActivityLikeDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 좋아요 추가 완료 - commentId = {}", commentId);
  }

  // 유저 댓글 좋아요 취소
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleCommentLikeCancel(CommentLikeCancelEvent event) {
    UUID commentId = event.id();
    UUID commentUserId = event.commentUserId();

    log.info("[사용자 활동] 댓글 좋아요 삭제 시작 - commentId = {}", commentId);

    UserActivity userActivity = userActivityRepository.findById(commentUserId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(commentUserId));

    userActivity.getCommentLikes()
        .removeIf(c -> c.commentId().equals(commentId));

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 좋아요 삭제 완료 - commentId = {}", commentId);

  }

  // ================================== 읽은 기사 (최신 10개) ==================================

  // 유저가 최근에 읽은 기사
  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleArticleViewAdd(ArticleViewEvent event) {
    UUID userId = event.getId();
    UUID articleId = event.getArticleId();
    ArticleViewDto articleViewDto = userActivityMapper.toArticleViewDto(event);

    log.info("[사용자 활동] 읽은 기사 추가 시작 - articleId = {}", articleId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        .orElseThrow(() -> UserActivityNotFoundException.withId(userId));
    List<ArticleViewDto> articleViewDtos = userActivity.getArticleViews();

//    if (!articleViewDtos.contains(articleViewDto)) {
//      articleViewDtos.add(0, articleViewDto);
//    }

    articleViewDtos.add(0, articleViewDto);

    if (articleViewDtos.size() > 10) {
      articleViewDtos.remove(articleViewDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 읽은 기사 추가 완료 - articleId = {}", articleId);
  }
}



