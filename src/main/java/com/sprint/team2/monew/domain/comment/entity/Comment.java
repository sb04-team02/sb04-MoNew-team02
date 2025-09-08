package com.sprint.team2.monew.domain.comment.entity;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.base.DeletableEntity;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends DeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0L;

    public void update(String content) {
        this.content = content;
    }

    public long getLikeCount() {
        return this.likeCount;
    }
}
