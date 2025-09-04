package com.sprint.team2.monew.domain.userActivity.dto.response;
import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserActivityResponseDto (
  UUID id,
  String email,
  String nickname,
  LocalDateTime createdAt,
  List<SubscriptionDto> subscriptions,
  List<CommentActivityDto> comments,
  List<CommentActivityLikeDto> commentLikes,
  List<ArticleViewDto> articleViews
  ) {

}
