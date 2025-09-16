package com.sprint.team2.monew.domain.comment.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CommentE2ETest {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    ObjectMapper om;

    @Autowired
    UserRepository userRepository;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    CommentRepository commentRepository;

    @MockitoBean
    ReactionRepository reactionRepository;
    @MockitoBean
    NotificationRepository notificationRepository;

    User user;
    Article article;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.builder()
                        .email("user+" + UUID.randomUUID() + "@ex.com")
                        .password("pw")
                        .nickname("nick")
                        .build()
        );
        article = articleRepository.save(
                Article.builder()
                        .source(ArticleSource.NAVER)
                        .sourceUrl("https://naver.com/" + UUID.randomUUID())
                        .title("title")
                        .publishDate(LocalDateTime.now())
                        .summary("summary")
                        .build()
        );

        // 목록 변환 시 likedByMe 계산: 전부 false로
        when(reactionRepository.existsByUser_IdAndComment_Id(any(), any()))
                .thenReturn(false);
        // 삭제 시 사이드이펙트는 no-op
        doNothing().when(reactionRepository).deleteByComment_Id(any());
        doNothing().when(notificationRepository)
                .deleteByResourceTypeAndResourceId(any(), any());
    }

    private void sleepTiny() {
        try { Thread.sleep(20); } catch (InterruptedException ignored) {}
    }

    // ------------------------ CRUD 플로우 ------------------------
    @Test
    @DisplayName("E2E: POST → PATCH → DELETE(soft) → DELETE(hard)")
    void createUpdateSoftDeleteHardDelete() throws Exception {
        // 1) 생성
        CommentRegisterRequest register = new CommentRegisterRequest(
                article.getId(), user.getId(), " hello "
        );
        ResponseEntity<CommentDto> postRes = restTemplate.postForEntity(
                "/api/comments",
                json(register),
                CommentDto.class
        );
        assertThat(postRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        CommentDto created = postRes.getBody();
        assertThat(created).isNotNull();
        UUID commentId = created.id();
        assertThat(created.content()).isEqualTo("hello");
        assertThat(commentRepository.findByIdAndDeletedAtIsNull(commentId)).isPresent();

        // 2) 수정(본인)
        HttpHeaders patchHeaders = new HttpHeaders();
        patchHeaders.setContentType(MediaType.APPLICATION_JSON);
        patchHeaders.set("Monew-Request-User-ID", user.getId().toString());
        CommentUpdateRequest update = new CommentUpdateRequest("updated content");
        ResponseEntity<CommentDto> patchRes = restTemplate.exchange(
                "/api/comments/{id}",
                HttpMethod.PATCH,
                new HttpEntity<>(update, patchHeaders),
                CommentDto.class,
                commentId
        );
        assertThat(patchRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchRes.getBody()).isNotNull();
        assertThat(patchRes.getBody().content()).isEqualTo("updated content");

        // 3) 소프트 삭제
        HttpHeaders delHeaders = new HttpHeaders();
        delHeaders.set("Monew-Request-User-ID", user.getId().toString());
        ResponseEntity<Void> softDelRes = restTemplate.exchange(
                "/api/comments/{id}",
                HttpMethod.DELETE,
                new HttpEntity<>(delHeaders),
                Void.class,
                commentId
        );
        assertThat(softDelRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(commentRepository.findByIdAndDeletedAtIsNull(commentId)).isEmpty();

        // 4) 하드 삭제(새로 하나 만든 뒤)
        CommentRegisterRequest register2 = new CommentRegisterRequest(
                article.getId(), user.getId(), "bye"
        );
        ResponseEntity<CommentDto> post2 = restTemplate.postForEntity(
                "/api/comments", json(register2), CommentDto.class
        );
        UUID toHardDelete = post2.getBody().id();

        HttpHeaders hardDelHeaders = new HttpHeaders();
        hardDelHeaders.set("Monew-Request-User-ID", user.getId().toString());
        ResponseEntity<Void> hardDelRes = restTemplate.exchange(
                "/api/comments/{id}/hard",
                HttpMethod.DELETE,
                new HttpEntity<>(hardDelHeaders),
                Void.class,
                java.util.Map.of("id", toHardDelete.toString())
        );
        assertThat(hardDelRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(commentRepository.findById(toHardDelete)).isEmpty();

        verify(reactionRepository).deleteByComment_Id(eq(toHardDelete));
        verify(notificationRepository)
                .deleteByResourceTypeAndResourceId(eq(ResourceType.COMMENT), eq(toHardDelete));
    }

    // ------------------------ 목록/커서(LIKE DESC) ------------------------
    @Test
    @DisplayName("E2E: GET /api/comments - likeCount DESC & 커서 페이지네이션")
    void listLikeCountDescWithCursor() {
        Comment a = commentRepository.save(Comment.builder().article(article).user(user).content("a").likeCount(0).build());
        sleepTiny();
        Comment b = commentRepository.save(Comment.builder().article(article).user(user).content("b").likeCount(1).build());
        sleepTiny();
        Comment c = commentRepository.save(Comment.builder().article(article).user(user).content("c").likeCount(1).build());
        sleepTiny();
        Comment d = commentRepository.save(Comment.builder().article(article).user(user).content("d").likeCount(2).build());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Monew-Request-User-ID", user.getId().toString());

        // 1페이지: d(2), c(1 최신)
        ResponseEntity<CursorPageResponseCommentDto> p1 = restTemplate.exchange(
                "/api/comments?articleId={aid}&orderBy=likeCount&direction=DESC&limit=2",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CursorPageResponseCommentDto.class,
                article.getId()
        );
        assertThat(p1.getStatusCode()).isEqualTo(HttpStatus.OK);
        CursorPageResponseCommentDto body1 = p1.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.content()).hasSize(2);
        assertThat(body1.content().get(0).content()).isEqualTo("d");
        assertThat(body1.content().get(1).content()).isEqualTo("c");
        assertThat(body1.hasNext()).isTrue();
        assertThat(body1.nextCursor()).isNotBlank();

        // 2페이지: b(1), a(0)
        ResponseEntity<CursorPageResponseCommentDto> p2 = restTemplate.exchange(
                "/api/comments?articleId={aid}&orderBy=likeCount&direction=DESC&limit=2&cursor={cursor}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CursorPageResponseCommentDto.class,
                article.getId(),
                body1.nextCursor()
        );
        assertThat(p2.getStatusCode()).isEqualTo(HttpStatus.OK);
        CursorPageResponseCommentDto body2 = p2.getBody();
        assertThat(body2).isNotNull();
        assertThat(body2.content()).hasSize(2);
        assertThat(body2.content().get(0).content()).isEqualTo("b");
        assertThat(body2.content().get(1).content()).isEqualTo("a");
        assertThat(body2.hasNext()).isFalse();
        assertThat(body2.totalElements()).isEqualTo(4L);
    }

    // ------------------------ 목록/커서(DATE ASC) ------------------------
    @Test
    @DisplayName("E2E: GET /api/comments - date ASC & 커서 페이지네이션")
    void listDateAscWithCursor() {
        commentRepository.save(Comment.builder().article(article).user(user).content("c1").build());
        sleepTiny();
        commentRepository.save(Comment.builder().article(article).user(user).content("c2").build());
        sleepTiny();
        commentRepository.save(Comment.builder().article(article).user(user).content("c3").build());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Monew-Request-User-ID", user.getId().toString());

        ResponseEntity<CursorPageResponseCommentDto> p1 = restTemplate.exchange(
                "/api/comments?articleId={aid}&orderBy=date&direction=ASC&limit=2",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CursorPageResponseCommentDto.class,
                article.getId()
        );
        assertThat(p1.getStatusCode()).isEqualTo(HttpStatus.OK);
        CursorPageResponseCommentDto body1 = p1.getBody();
        assertThat(body1).isNotNull();
        assertThat(body1.content()).hasSize(2);
        assertThat(body1.content().get(0).content()).isEqualTo("c1");
        assertThat(body1.content().get(1).content()).isEqualTo("c2");
        assertThat(body1.hasNext()).isTrue();
        assertThat(body1.nextCursor()).isNotBlank();

        ResponseEntity<CursorPageResponseCommentDto> p2 = restTemplate.exchange(
                "/api/comments?articleId={aid}&orderBy=date&direction=ASC&limit=2&cursor={cursor}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CursorPageResponseCommentDto.class,
                article.getId(),
                body1.nextCursor()
        );
        CursorPageResponseCommentDto body2 = p2.getBody();
        assertThat(body2).isNotNull();
        assertThat(body2.content()).hasSize(1);
        assertThat(body2.content().get(0).content()).isEqualTo("c3");
        assertThat(body2.hasNext()).isFalse();
        assertThat(body2.totalElements()).isEqualTo(3L);
    }

    // ------------------------ 유틸 ------------------------
    private HttpEntity<String> json(Object body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(om.writeValueAsString(body), headers);
    }
}
