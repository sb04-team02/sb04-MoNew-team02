package com.sprint.team2.monew.domain.article.entity;

import com.sprint.team2.monew.domain.base.DeletableEntity;
import com.sprint.team2.monew.domain.base.UpdatableEntity;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends DeletableEntity {
    @Column(name = "source", nullable = false, length = 30)
    private String source;

    @Column(name = "source_url", unique = true, nullable = false, length = 255)
    private String sourceUrl;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "publish_date", nullable = false)
    private LocalDateTime publishDate;

    @Column(name = "summary", nullable = false, length = 100)
    private String summary;

    @Column(name = "comment_count", nullable = false)
    private long commentCount = 0L;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "interest_id")
    private Interest interest;
}
