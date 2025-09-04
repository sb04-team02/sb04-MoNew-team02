package com.sprint.team2.monew.domain.comment.service;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.mapper.CommentMapper;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.comment.service.basic.BasicCommentService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private BasicCommentService commentService;

    private UUID commentId;
    private UUID ownerId;
    private UUID otherUserId;

    private Article article;
    private User owner;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        // Article / User 는 @Builder 사용 (protected 기본 생성자 회피)
        article = Article.builder()
                .source("nyt")
                .sourceUrl("https://example.com/" + UUID.randomUUID())
                .title("뉴스 제목")
                .publishDate(LocalDateTime.now())
                .summary("요약")
                .commentCount(0L)
                .viewCount(0L)
                .build();
        setId(article, UUID.randomUUID());

        owner = User.builder()
                .email("owner@example.com")
                .password("password")
                .nickname("작성자")
                .build();
        setId(owner, ownerId);

        // Comment는 기본 생성자 가능
        comment = new Comment();
        setId(comment, commentId);
        comment.setArticle(article);
        comment.setUser(owner);
        comment.setContent("원래 내용");
        comment.setLikeCount(0L);
    }

    // ===== registerComment =====

    @Test
    void 댓글_생성_성공_저장_및_매핑() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRegisterRequest request =
                new CommentRegisterRequest(articleId, userId, "새 댓글 내용");

        User user = User.builder().email("user@example.com").password("pw").nickname("nick").build();
        Article article = Article.builder()
                .source("naver")
                .sourceUrl("https://n/" + UUID.randomUUID())
                .title("제목")
                .publishDate(LocalDateTime.now())
                .summary("요약")
                .commentCount(0L)
                .viewCount(0L)
                .build();

        Comment mapped = new Comment();
        mapped.setContent("새 댓글 내용");

        Comment saved = mapped;

        CommentDto expected = new CommentDto(
                UUID.randomUUID(), articleId, userId, "nick",
                "새 댓글 내용", 0L, false, LocalDateTime.now()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
        given(commentMapper.toEntity(request)).willReturn(mapped);
        given(commentRepository.save(mapped)).willReturn(saved);
        given(commentMapper.toDto(saved, false)).willReturn(expected);

        // when
        CommentDto result = commentService.registerComment(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("새 댓글 내용");
    }

    @Test
    void 댓글_생성_실패_사용자없음() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "내용");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.registerComment(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(articleRepository, commentRepository, commentMapper);
    }

    @Test
    void 댓글_생성_실패_게시글없음() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "내용");

        given(userRepository.findById(userId)).willReturn(Optional.of(owner));
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.registerComment(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Article not found");
        verify(userRepository).findById(userId);
        verify(articleRepository).findById(articleId);
        verifyNoMoreInteractions(userRepository, articleRepository);
        verifyNoInteractions(commentRepository, commentMapper);
    }

    @Test
    void 댓글_생성_실패_빈내용() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "   ");

        Comment mapped = new Comment();
        mapped.setContent("   ");

        given(userRepository.findById(userId)).willReturn(Optional.of(owner));
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
        given(commentMapper.toEntity(request)).willReturn(mapped);

        // when & then
        assertThatThrownBy(() -> commentService.registerComment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("댓글 내용을 입력해주세요");
        verifyNoInteractions(commentRepository); // 저장 시도 없어야 함
    }

    // ===== updateComment =====

    @Test
    void 댓글_수정_성공() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentDto expected = new CommentDto(
                commentId, getId(article), ownerId, owner.getNickname(),
                "수정된 내용", 0L, false, LocalDateTime.now()
        );
        given(commentMapper.toDto(any(Comment.class), eq(false))).willReturn(expected);

        // when
        CommentUpdateRequest req = new CommentUpdateRequest("수정된 내용");
        CommentDto result = commentService.updateComment(commentId, ownerId, req);

        // then
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(comment.getContent()).isEqualTo("수정된 내용");
        verify(commentRepository).findById(commentId);
        verify(commentMapper).toDto(any(Comment.class), eq(false));
        verifyNoInteractions(userRepository, articleRepository);
    }

    @Test
    void 댓글_수정_실패_댓글없음() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("수정")))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Comment not found");
    }

    @Test
    void 댓글_수정_실패_권한없음_본인아님() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, otherUserId, new CommentUpdateRequest("수정")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("본인의 댓글만");
    }

    @Test
    void 댓글_수정_실패_빈내용() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("   ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정할 댓글 내용을 입력해주세요");
        verify(commentRepository).findById(commentId);
        verifyNoMoreInteractions(commentRepository);
        verifyNoInteractions(commentMapper);
    }

    // ===== 유틸: DeletableEntity의 id 주입/조회 =====
    private static void setId(Object entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
    private static UUID getId(Object entity) {
        return (UUID) ReflectionTestUtils.getField(entity, "id");
    }
}
