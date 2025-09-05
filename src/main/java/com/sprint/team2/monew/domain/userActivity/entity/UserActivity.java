package com.sprint.team2.monew.domain.userActivity.entity;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.base.BaseEntity;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserActivity extends BaseEntity {

  @Id
  private UUID id;
  private LocalDateTime createdAt;
  private String email;
  private String nickname;

  private List<SubscriptionDto> subscriptions;
  private List<CommentActivityDto> comments;
  private List<CommentActivityLikeDto> commentLikes;
  private List<ArticleViewDto> articleViews;

}
