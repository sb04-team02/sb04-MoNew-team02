package com.sprint.team2.monew.domain.userActivity.entity;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import jakarta.persistence.Id;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document
public class UserActivity {

  @Id
  private UUID id; // user id
  private String email;
  private String nickname;

  @CreatedDate
  private LocalDateTime createdAt;
  @LastModifiedDate
  private LocalDateTime updatedAt;

  private List<SubscriptionDto> subscriptions = new ArrayList<>();
  private List<CommentActivityDto> comments = new ArrayList<>();
  private List<CommentActivityLikeDto> commentLikes = new ArrayList<>();
  private List<ArticleViewDto> articleViews = new ArrayList<>();

  public UserActivity(UUID id, String email, String nickname) {
    this.id = id;
    this.email = email;
    this.nickname = nickname;
    this.createdAt = LocalDateTime.now();
  }
}