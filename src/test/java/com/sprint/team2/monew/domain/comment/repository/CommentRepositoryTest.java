package com.sprint.team2.monew.domain.comment.repository;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = CommentRepositoryTest.SliceConfig.class)
public class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TestEntityManager tem;

    private User user;
    private Article article;

    @TestConfiguration
    @EnableJpaAuditing
    @EnableJpaRepositories(basePackageClasses = CommentRepository.class)
    @EntityScan(basePackageClasses = { Comment.class, Article.class, User.class, Interest.class })
    static class SliceConfig {}

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .password("pw")
                .nickname("nick")
                .build();

        // TODO: Article 필수 필드에 맞게 수정
        article = Article.builder()
                .source(ArticleSource.NAVER)
                .sourceUrl("naver.com")
                .title("title")
                .publishDate(LocalDateTime.now())
                .summary("content")
                .build();

        tem.persist(user);
        tem.persist(article);
        tem.flush();
        tem.clear();
    }

    // ---------- 헬퍼 ----------
    private Comment newComment(String text, long likeCount) {
        // TODO: Comment 필드명/생성자에 맞게 수정 (content / body 등)
        return Comment.builder()
                .article(article)
                .user(user)
                .content(text)
                .likeCount(likeCount)
                .build();
    }

    private Comment persistAndReturn(Comment c) {
        tem.persist(c);
        tem.flush();
        tem.clear();
        return c;
    }

    private void smallDelay() {
        try {
            Thread.sleep(15); // createdAt 차이를 위해 약간의 지연
        } catch (InterruptedException ignored) {}
    }

    @Test
    @DisplayName("incrementLikeCount: like_count 1 증가 & 조회로 검증")
    void incrementLikeCountUpdatesAndReads() {
        //given
        Comment c = persistAndReturn(newComment("hello", 0));

        //when
        int updated = commentRepository.incrementLikeCount(c.getId());
        tem.flush(); tem.clear();
        Long like = commentRepository.findLikeCountById(c.getId());

        //then
        assertThat(updated).isEqualTo(1);
        assertThat(like).isEqualTo(1L);
    }

    @Test
    @DisplayName("decrementLikeCount: 0에서 감소해도 음수로 내려가지 않음")
    void decrementLikeCountNotBelowZero() {
        //given
        Comment c0 = persistAndReturn(newComment("zero", 0));
        Comment c2 = persistAndReturn(newComment("two", 2));

        //when
        int u1 = commentRepository.decrementLikeCount(c0.getId());
        tem.flush(); tem.clear();
        int u2 = commentRepository.decrementLikeCount(c2.getId());
        tem.flush(); tem.clear();

        //then
        Long like0 = commentRepository.findLikeCountById(c0.getId());
        assertThat(u1).isEqualTo(1);
        assertThat(like0).isEqualTo(0L);
        Long like1 = commentRepository.findLikeCountById(c2.getId());
        assertThat(u2).isEqualTo(1);
        assertThat(like1).isEqualTo(1L);
    }

    @Test
    @DisplayName("findWithArticleAndUserById: fetch join으로 연관엔티티 로딩")
    void findWithArticleAndUserByIdFetches() {
        //given
        Comment c = persistAndReturn(newComment("fetch", 0));

        //when
        Optional<Comment> loaded = commentRepository.findWithArticleAndUserById(c.getId());

        //then
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getArticle()).isNotNull();
        assertThat(loaded.get().getUser()).isNotNull();
        assertThat(loaded.get().getArticle().getId()).isEqualTo(article.getId());
        assertThat(loaded.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("softDeleteById: 삭제 플래그 세팅 & NotDeleted 조회에서 제외")
    void softDeleteByIdMarksDeletedAndFilteredOut() {
        //given
        Comment c = persistAndReturn(newComment("to delete", 0));
        long before = commentRepository.countByArticle_IdAndNotDeleted(article.getId());

        //when
        int updated = commentRepository.softDeleteById(c.getId());
        tem.flush(); tem.clear();

        //then
        assertThat(updated).isEqualTo(1);
        assertThat(commentRepository.findByIdAndDeletedAtIsNull(c.getId())).isEmpty();

        long after = commentRepository.countByArticle_IdAndNotDeleted(article.getId());
        assertThat(after).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("findByArticle_IdWithDateCursor: 날짜 커서 + ASC 정렬 Slice")
    void findByArticleIdWithDateCursorDateCursorAsc() {
        //given
        Comment c1 = persistAndReturn(newComment("c1", 0)); smallDelay();
        Comment c2 = persistAndReturn(newComment("c2", 0)); smallDelay();
        Comment c3 = persistAndReturn(newComment("c3", 0)); smallDelay();
        Comment c4 = persistAndReturn(newComment("c4", 0)); smallDelay();
        Comment c5 = persistAndReturn(newComment("c5", 0));

        // 첫 페이지 (ASC, after/cursor 없음)
        Pageable page2Asc = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "createdAt"));
        //when(page:1)
        Slice<Comment> slice1 = commentRepository.findByArticle_IdWithDateCursor(
                article.getId(),
                false, null,
                false, null,
                true,
                page2Asc
        );

        //then(page:1)
        assertThat(slice1.getContent()).hasSize(2);
        LocalDateTime lastCreated = slice1.getContent().get(1).getCreatedAt();

        // 다음 페이지: cursorDate = 직전 페이지 마지막 요소의 createdAt
        //when(page:2)
        Slice<Comment> slice2 = commentRepository.findByArticle_IdWithDateCursor(
                article.getId(),
                false, null,
                true, lastCreated,
                true,
                page2Asc
        );

        //then(page:2)
        assertThat(slice2.getContent()).hasSize(2);
        assertThat(slice2.hasNext()).isTrue(); // 아직 남음(c5)

        // 세 번째 페이지
        LocalDateTime last2 = slice2.getContent().get(1).getCreatedAt();
        //when(page:3)
        Slice<Comment> slice3 = commentRepository.findByArticle_IdWithDateCursor(
                article.getId(),
                false, null,
                true, last2,
                true,
                page2Asc
        );
        //then(page:3)
        assertThat(slice3.getContent()).hasSize(1);
        assertThat(slice3.hasNext()).isFalse();
    }

    @Test
    @DisplayName("findByArticle_IdWithLikeCountCursor: likeCount 커서 + DESC 정렬 Slice (동점시 createdAt)")
    void findByArticleIdWithLikeCountCursorLikeCursorDesc() {
        //given
        Comment a = persistAndReturn(newComment("a", 0)); smallDelay();
        Comment b = persistAndReturn(newComment("b", 1)); smallDelay();
        Comment c = persistAndReturn(newComment("c", 1)); smallDelay();
        Comment d = persistAndReturn(newComment("d", 2));

        // 정렬: likeCount DESC, createdAt DESC
        Pageable page2Desc = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "likeCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt")));

        //when(page:1)
        // 첫 페이지 (cursor 없음)
        Slice<Comment> s1 = commentRepository.findByArticle_IdWithLikeCountCursor(
                article.getId(),
                false, null,
                false, null, null,
                false,
                page2Desc
        );

        //then(page:1)
        assertThat(s1.getContent()).hasSize(2);
        long firstLike = s1.getContent().get(0).getLikeCount();
        long secondLike = s1.getContent().get(1).getLikeCount();
        assertThat(firstLike).isGreaterThanOrEqualTo(secondLike);

        //when(page:2)
        // cursor: 직전 페이지 마지막 요소의 (likeCount, createdAt)
        Comment last = s1.getContent().get(1);
        Slice<Comment> s2 = commentRepository.findByArticle_IdWithLikeCountCursor(
                article.getId(),
                false, null,
                true, last.getLikeCount(), last.getCreatedAt(),
                false,
                page2Desc
        );

        //then(page:2)
        assertThat(s2.getContent()).hasSize(2);
        // 남은 요소들의 likeCount가 cursor 이하(동점이면 createdAt < cursorDate)인지 대략 검증
        if (!s2.getContent().isEmpty()) {
            Comment top = s2.getContent().get(0);

            long topLike = top.getLikeCount();
            long lastLike = last.getLikeCount();

            assertThat(topLike).isLessThanOrEqualTo(lastLike);
            if (topLike == lastLike) {
                assertThat(top.getCreatedAt()).isBefore(last.getCreatedAt());
            }
        }
    }
}
