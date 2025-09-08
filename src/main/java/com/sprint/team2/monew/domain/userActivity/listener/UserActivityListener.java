package com.sprint.team2.monew.domain.userActivity.listener;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.events.CommentActivityEvent;
import com.sprint.team2.monew.domain.userActivity.events.CommentActivityLikeEvent;
import com.sprint.team2.monew.domain.userActivity.events.SubscriptionUpdatedEvent;
import com.sprint.team2.monew.domain.userActivity.events.UserCreatedEvent;
import com.sprint.team2.monew.domain.userActivity.mapper.UserActivityMapper;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
  public void handleUserCreate(UserCreatedEvent event) {
    UUID id = event.getId();
    String email = event.getEmail();
    String nickname = event.getNickname();
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

  // ================================== 구독 ==================================
  // 구독 업데이트
  @EventListener
  public void handleSubscriptionAdd(SubscriptionUpdatedEvent event) {
    UUID userId = event.getId();
    UUID interestId = event.getInterestId();
    SubscriptionDto subscriptionDto = userActivityMapper.toSubscriptionDto(event);

    log.info("[사용자 활동] 구독 추가 시작 - interestId = {}",interestId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + userId));
    List<SubscriptionDto> subscriptionDtos = userActivity.getSubscriptions();

    if (!subscriptionDtos.contains(subscriptionDto)) {
      subscriptionDtos.add(0, subscriptionDto);
    }

    if (subscriptionDtos.size() > 10) {
      subscriptionDtos.remove(subscriptionDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 구독 추가 완료 - interestId = {}", interestId);
  }

  // 구독 업데이트
  @EventListener
  public void handleSubscriptionDelete(SubscriptionUpdatedEvent event) {
    UUID userId = event.getId();
    UUID interestId = event.getInterestId();
    SubscriptionDto subscriptionDto = userActivityMapper.toSubscriptionDto(event);

    log.info("[사용자 활동] 구독 삭제 시작 - interestId = {}",interestId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + userId));
    List<SubscriptionDto> subscriptionDtos = userActivity.getSubscriptions();

    userActivity.getSubscriptions()
        .removeIf(s -> s.interestId().equals(event.getInterestId()));

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 구독 삭제 완료 - interestId = {}", interestId);
  }

  // ================================== 댓글 (최신 10개) ==================================
  // 유저 댓글 추가
  @EventListener
  public void handleCommentAdd(CommentActivityEvent event) {
    UUID commentId = event.getId();
    UUID userId = event.getUserId();
    CommentActivityDto commentActivityDto = userActivityMapper.toCommentActivityDto(event);

    log.info("[사용자 활동] 댓글 추가 시작 - commentId = {}",commentId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + userId));
    List<CommentActivityDto> commentActivityDtos = userActivity.getComments();

    commentActivityDtos.add(0, commentActivityDto);

    if (commentActivityDtos.size() > 10) {
      commentActivityDtos.remove(commentActivityDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 추가 완료- commentId = {}", commentId);
  }

  // 유저 댓글 삭제
  @EventListener
  public void handleCommentDelete(CommentActivityEvent event) {
    UUID commentId = event.getId();
    UUID userId = event.getUserId();

    log.info("[사용자 활동] 댓글 삭제 시작 - commentId = {}", commentId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + userId));

    userActivity.getComments()
        .removeIf(c -> c.id().equals(commentId));

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 삭제 완료- commentId = {}", commentId);
  }

  // ================================== 댓글 좋아요 (최신 10개) ==================================

  // 유저 댓글 좋아요
  @EventListener
  public void handleCommentLikeAdd(CommentActivityLikeEvent event) {
    UUID commentId = event.getCommentId();
    UUID commentUserId = event.getCommentUserId();
    CommentActivityLikeDto commentActivityLikeDto = userActivityMapper.toCommentActivityLikeDto(event);

    log.info("[사용자 활동] 댓글 좋아요 추가 시작 - commentId = {}",commentId);

    UserActivity userActivity = userActivityRepository.findById(commentUserId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + commentUserId));
    List<CommentActivityLikeDto> commentActivityLikeDtos = userActivity.getCommentLikes();

    if (!commentActivityLikeDtos.contains(commentActivityLikeDto)) {
      commentActivityLikeDtos.add(0, commentActivityLikeDto);
    }

    if (commentActivityLikeDtos.size() > 10) {
      commentActivityLikeDtos.remove(commentActivityLikeDtos.size() - 1); // removing oldest item
    }

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 좋아요 추가 완료 - commentId = {}", commentId);
  }

  // 유저 댓글 좋아요 취소
  @EventListener
  public void handleCommentLikeCancel(CommentActivityLikeEvent event) {
    UUID commentId = event.getCommentId();
    UUID commentUserId = event.getCommentUserId();

    log.info("[사용자 활동] 댓글 좋아요 삭제 시작 - commentId = {}", commentId);

    UserActivity userActivity = userActivityRepository.findById(commentUserId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + commentUserId));

    userActivity.getCommentLikes()
        .removeIf(c -> c.commentId().equals(commentId));

    userActivityRepository.save(userActivity);

    log.info("[사용자 활동] 댓글 좋아요 삭제 완료 - commentId = {}", commentId);

  }

  // ================================== 읽은 기사 (최신 10개) ==================================

  // 유저가 최근에 읽은 기사
  @EventListener
  public void handleArticleViewAdd(ArticleViewEvent event) {
    UUID userId = event.getId();
    UUID articleId = event.getArticleId();
    ArticleViewDto articleViewDto = userActivityMapper.toArticleViewDto(event);

    log.info("[사용자 활동] 읽은 기사 추가 시작 - articleId = {}", articleId);

    UserActivity userActivity = userActivityRepository.findById(userId)
        // replace with custom error <<<<<<<<<<<<<<<<
        .orElseThrow(() -> new IllegalStateException("User activity not found for user: " + userId));
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



