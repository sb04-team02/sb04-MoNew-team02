package com.sprint.team2.monew.domain.userActivity.mapper;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityCancelDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import com.sprint.team2.monew.domain.userActivity.dto.response.UserActivityResponseDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeCancelEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentUpdateEvent;
import com.sprint.team2.monew.domain.userActivity.events.subscriptionEvent.SubscriptionAddEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {

  // from userActivity to UserActivityResponseDto
  UserActivityResponseDto toUserActivityResponseDto(UserActivity userActivity);

  // for testing
  UserActivity toUserActivity(UserActivityResponseDto userActivityResponseDto);

  ArticleViewDto toArticleViewDto(ArticleViewEvent event);

  ArticleViewEvent toArticleViewEvent(ArticleViewDto dto);

  SubscriptionDto toSubscriptionDto(SubscriptionAddEvent event);

  CommentActivityDto toCommentActivityDto(CommentUpdateEvent event);

  CommentActivityDto toCommentActivityDto(CommentAddEvent event);

  @Mapping(source = "commentId", target = "id")
  CommentActivityDto toCommentActivityDto(CommentDeleteEvent event);

  CommentActivityLikeDto toCommentActivityLikeDto(CommentLikeAddEvent event);

  CommentActivityCancelDto toCommentActivityCancelDto(CommentLikeCancelEvent event);

}