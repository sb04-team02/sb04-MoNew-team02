package com.sprint.team2.monew.domain.userActivity.entity;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.base.BaseEntity;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document
public class UserActivity extends BaseEntity {

  @Id
  private UUID id; // user id
  private String email;
  private String nickname;
  private LocalDateTime createdAt;

  private List<SubscriptionDto> subscriptions;
  private List<CommentActivityDto> comments;
  private List<CommentActivityLikeDto> commentLikes;
  private List<ArticleViewDto> articleViews;

  public UserActivity(UUID id, String email, String nickname) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
  }

}
