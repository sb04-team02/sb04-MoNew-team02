package com.sprint.team2.monew.domain.comment.entity;

import com.sprint.team2.monew.domain.base.DeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@NoArgsConstructor
@Getter
public class Comment extends DeletableEntity {
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false)
    private String likeCount;


}
