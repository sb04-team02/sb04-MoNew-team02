package com.sprint.team2.monew.domain.comment.service;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.exception.CommentContentRequiredException;
import com.sprint.team2.monew.domain.comment.exception.CommentForbiddenException;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.comment.mapper.CommentMapper;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.comment.service.basic.BasicCommentService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("댓글 생성 성공")
    void createComment_Success() {
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
    @DisplayName("사용자가 존재하지 않아 댓글 생성 실패")
    void createComment_Fail_UserNotFound() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentRegisterRequest request = new CommentRegisterRequest(articleId, userId, "내용");

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.registerComment(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자 정보가 없습니다.");
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(articleRepository, commentRepository, commentMapper);
    }

    @Test
    @DisplayName("뉴스기사가 존재하지 않아 댓글 생성 실패")
    void createComment_Fail_ArticleNotFound() {
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
    @DisplayName("빈 내용으로 인해 댓글 생성 실패")
    void createComment_Fail_BlankContent() {
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
                .isInstanceOf(CommentContentRequiredException.class)
                .hasMessageContaining("댓글 내용을 입력해주세요");
        verifyNoInteractions(commentRepository); // 저장 시도 없어야 함
    }

    // ===== updateComment =====

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
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
    @DisplayName("댓글이 존재하지 않아 수정 실패")
    void updateComment_Fail_CommentNotFound() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("수정")))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("권한 없음 - 본인 댓글 아님")
    void updateComment_Fail_Forbidden_NotOwner() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, otherUserId, new CommentUpdateRequest("수정")))
                .isInstanceOf(CommentForbiddenException.class)
                .hasMessageContaining("본인의 댓글만");
    }

    @Test
    @DisplayName("빈 내용으로 댓글 수정 실패")
    void updateComment_Fail_BlankContent() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("   ")))
                .isInstanceOf(CommentContentRequiredException.class)
                .hasMessageContaining("댓글 내용을 입력해주세요.");
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
