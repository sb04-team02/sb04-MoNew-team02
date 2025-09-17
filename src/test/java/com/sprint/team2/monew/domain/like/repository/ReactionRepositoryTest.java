package com.sprint.team2.monew.domain.like.repository;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(ReactionRepositoryTest.AuditingOnly.class)
@EnableJpaRepositories(basePackageClasses = ReactionRepository.class)
@EntityScan(basePackageClasses = {
        Reaction.class, Comment.class, Article.class, User.class, Interest.class
})
public class ReactionRepositoryTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class AuditingOnly {
    }

    @Autowired
    ReactionRepository reactionRepository;
    @Autowired
    TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Article article;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .email("u1@example.com")
                .password("pw")
                .nickname("u1")
                .build();
        user2 = User.builder()
                .email("u2@example.com")
                .password("pw")
                .nickname("u2")
                .build();

        article = Article.builder()
                .source(ArticleSource.NAVER)
                .sourceUrl("https://naver.com/some-news")
                .title("title")
                .publishDate(LocalDateTime.now())
                .summary("summary")
                .build();

        comment = Comment.builder()
                .article(article)
                .user(user1)
                .content("hello")
                .likeCount(0)
                .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(article);
        entityManager.persist(comment);
        entityManager.flush();
        entityManager.clear();
    }

    private Reaction persistReaction(User user, Comment comment) {
        Reaction reaction = Reaction.builder()
                .user(user)
                .comment(comment)
                .build();
        entityManager.persist(reaction);
        entityManager.flush();
        entityManager.clear();
        return reaction;
    }

    @Test
    @DisplayName("리액션 존재 여부 반환 테스트")
    void existsByUserIdAndCommentId() {
        boolean before = reactionRepository.existsByUser_IdAndComment_Id(user1.getId(), comment.getId());
        assertThat(before).isFalse();

        persistReaction(user1, comment);

        boolean after = reactionRepository.existsByUser_IdAndComment_Id(user1.getId(), comment.getId());
        assertThat(after).isTrue();
    }

    @Test
    @DisplayName("리액션을 Optional로 탐색 테스트")
    void findByUserIdAndCommentId() {
        Optional<Reaction> none = reactionRepository.findByUser_IdAndComment_Id(user1.getId(), comment.getId());
        assertThat(none).isEmpty();

        Reaction saved = persistReaction(user1, comment);

        Optional<Reaction> found = reactionRepository.findByUser_IdAndComment_Id(user1.getId(), comment.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUser().getId()).isEqualTo(user1.getId());
        assertThat(found.get().getComment().getId()).isEqualTo(comment.getId());
    }

    @Test
    @DisplayName("특정 유저의 특정 댓글 리액션 1건을 삭제하고 삭제 수를 반환한다")
    void deleteByUserIdAndCommentId() {
        persistReaction(user1, comment);
        persistReaction(user2, comment); // 다른 유저의 리액션도 하나 더

        int deleted = reactionRepository.deleteByUser_IdAndComment_Id(user1.getId(), comment.getId());
        entityManager.flush(); entityManager.clear();

        assertThat(deleted).isEqualTo(1);
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user1.getId(), comment.getId())).isFalse();
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user2.getId(), comment.getId())).isTrue();
    }

    @Test
    @DisplayName("해당 댓글의 모든 리액션을 삭제한다")
    void deleteByCommentId() {
        persistReaction(user1, comment);
        persistReaction(user2, comment);

        reactionRepository.deleteByComment_Id(comment.getId());
        entityManager.flush(); entityManager.clear();

        assertThat(reactionRepository.findAll()).isEmpty();
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user1.getId(), comment.getId())).isFalse();
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user2.getId(), comment.getId())).isFalse();
    }
}
