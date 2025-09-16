package com.sprint.team2.monew.domain.like.e2e;

import com.sprint.team2.monew.MonewApplication;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = MonewApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
public class ReactionE2ETest {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserRepository userRepository;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    ReactionRepository reactionRepository;

    private User user;
    private Article article;
    private Comment comment;

    @BeforeEach
    void setUp() {
        reactionRepository.deleteAll();
        commentRepository.deleteAll();
        userRepository.deleteAll();
        articleRepository.deleteAll();

        user = userRepository.save(
                User.builder()
                        .email("u1@example.com")
                        .password("pw")
                        .nickname("u1")
                        .build()
        );

        article = articleRepository.save(
                Article.builder()
                        .source(ArticleSource.NAVER)
                        .sourceUrl("https://naver.com/x")
                        .title("t")
                        .publishDate(LocalDateTime.now())
                        .summary("s")
                        .build()
        );

        comment = commentRepository.save(
                Comment.builder()
                        .article(article)
                        .user(user)
                        .content("hello")
                        .likeCount(0)
                        .build()
        );
    }

    @Test
    @DisplayName("E2E: 댓글 좋아요 생성 후 취소까지 성공 플로우")
    void likeThenUnlikeSuccessFlow() {
        long before = getLikeCount(comment.getId());
        assertThat(before).isZero();


        HttpHeaders likeHeaders = headersFor(user.getId());
        ResponseEntity<CommentLikeDto> likeRes = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.POST,
                new HttpEntity<>(null, likeHeaders),
                CommentLikeDto.class,
                comment.getId()
        );
        assertThat(likeRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(likeRes.getBody()).isNotNull();
        assertThat(likeRes.getBody().commentId()).isEqualTo(comment.getId());

        // 카운트 +1 확인
        long afterLike = getLikeCount(comment.getId());
        assertThat(afterLike).isEqualTo(1L);
        // reaction 존재 확인
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user.getId(), comment.getId())).isTrue();

        ResponseEntity<Void> unlikeRes = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.DELETE,
                new HttpEntity<>(null, likeHeaders),
                Void.class,
                comment.getId()
        );
        assertThat(unlikeRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 카운트 -1 확인
        long afterUnlike = getLikeCount(comment.getId());
        assertThat(afterUnlike).isZero();
        // reaction 제거 확인
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user.getId(), comment.getId())).isFalse();
    }

    @Test
    @DisplayName("E2E: 중복 좋아요 시 409(CONFLICT) 반환 및 카운트 유지")
    void likeDuplicateReturnsConflict() {
        HttpHeaders headers = headersFor(user.getId());

        // 최초 좋아요 성공
        ResponseEntity<CommentLikeDto> first = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                CommentLikeDto.class,
                comment.getId()
        );
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getLikeCount(comment.getId())).isEqualTo(1L);

        // 두 번째 좋아요 → 충돌(이미 좋아요)
        ResponseEntity<String> second = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                String.class,
                comment.getId()
        );
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(getLikeCount(comment.getId())).isEqualTo(1L); // 유지
    }

    @Test
    @DisplayName("E2E: 좋아요 없이 취소하면 404(NOT_FOUND)")
    void unlikeWithoutReactionReturnsNotFound() {
        HttpHeaders headers = headersFor(user.getId());

        // 현재 reaction 없음
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user.getId(), comment.getId())).isFalse();
        assertThat(getLikeCount(comment.getId())).isZero();

        ResponseEntity<String> res = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.DELETE,
                new HttpEntity<>(null, headers),
                String.class,
                comment.getId()
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getLikeCount(comment.getId())).isZero();
    }

    @Test
    @DisplayName("E2E: 존재하지 않는 사용자로 좋아요 시 404(NOT_FOUND)")
    void likeWithUnknownUserReturnsNotFound() {
        HttpHeaders headers = headersFor(UUID.randomUUID());

        ResponseEntity<String> res = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                String.class,
                comment.getId()
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getLikeCount(comment.getId())).isZero();
        assertThat(reactionRepository.existsByUser_IdAndComment_Id(user.getId(), comment.getId())).isFalse();
    }

    @Test
    @DisplayName("E2E: 존재하지 않는 댓글로 좋아요 시 404(NOT_FOUND)")
    void likeWithUnknownCommentReturnsNotFound() {
        HttpHeaders headers = headersFor(user.getId());
        UUID unknownCommentId = UUID.randomUUID();

        ResponseEntity<String> res = restTemplate.exchange(
                "/api/comments/{cid}/comment-likes",
                HttpMethod.POST,
                new HttpEntity<>(null, headers),
                String.class,
                unknownCommentId
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---------- 핼퍼 ----------
    private HttpHeaders headersFor(UUID requesterUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Monew-Request-User-ID", requesterUserId.toString());
        return headers;
    }

    private long getLikeCount(UUID commentId) {
        return Optional.ofNullable(commentRepository.findLikeCountById(commentId)).orElse(0L);
    }
}
