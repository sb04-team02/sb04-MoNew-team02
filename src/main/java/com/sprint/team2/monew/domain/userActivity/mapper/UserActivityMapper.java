package com.sprint.team2.monew.domain.userActivity.mapper;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import com.sprint.team2.monew.domain.userActivity.dto.response.UserActivityResponseDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.events.CommentActivityEvent;
import com.sprint.team2.monew.domain.userActivity.events.CommentActivityLikeEvent;
import com.sprint.team2.monew.domain.userActivity.events.SubscriptionUpdatedEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {

  // from userActivity to UserActivityResponseDto
  UserActivityResponseDto toUserActivityResponseDto(UserActivity userActivity);

  ArticleViewDto toArticleViewDto(ArticleViewEvent event);

  SubscriptionDto toSubscriptionDto(SubscriptionUpdatedEvent event);

  CommentActivityDto toCommentActivityDto(CommentActivityEvent event);

  CommentActivityLikeDto toCommentActivityLikeDto(CommentActivityLikeEvent event);
}