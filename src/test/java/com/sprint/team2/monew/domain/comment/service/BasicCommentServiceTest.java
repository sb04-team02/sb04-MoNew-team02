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
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class BasicCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ReactionRepository reactionRepository;

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
    void createCommentSuccess() {
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
    void createCommentShouldFailWhenUserNotFound() {
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
    void createCommentShouldFailWhenArticleNotFound() {
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
    void createCommentShouldFailWhenContentIsBlank() {
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
    void updateCommentSuccess() {
        // given
        assertThat(comment.getUser().getId()).isEqualTo(ownerId);

        given(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .willReturn(Optional.of(comment));
        given(reactionRepository.existsByUserIdAndCommentId(ownerId, commentId))
                .willReturn(false);

        CommentDto expected = new CommentDto(
                commentId, getId(article), ownerId, owner.getNickname(),
                "수정된 내용", 0L, false, LocalDateTime.now()
        );
        given(commentMapper.toDto(any(Comment.class), eq(false)))
                .willReturn(expected);

        CommentUpdateRequest req = new CommentUpdateRequest("수정된 내용");

        // when
        CommentDto result = commentService.updateComment(commentId, ownerId, req);

        // then
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(comment.getContent()).isEqualTo("수정된 내용");

        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(reactionRepository).should().existsByUserIdAndCommentId(ownerId, commentId);
        then(commentMapper).should().toDto(any(Comment.class), eq(false));

        then(userRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("댓글이 존재하지 않아 수정 실패")
    void updateCommentShouldFailWhenCommentNotFound() {
        // given
        given(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("수정")))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessageContaining("댓글을 찾을 수 없습니다.");

        // verify
        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
        then(reactionRepository).shouldHaveNoInteractions();
        then(commentMapper).shouldHaveNoInteractions();
        then(userRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("권한 없음 - 본인 댓글 아님")
    void updateCommentShouldFailForNonOwner() {
        // given
        comment.setDeletedAt(null);
        given(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, otherUserId, new CommentUpdateRequest("수정")))
                .isInstanceOf(CommentForbiddenException.class)
                .hasMessageContaining("본인의 댓글만");

        // verify: 좋아요/매퍼는 호출되지 않음
        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
        then(reactionRepository).shouldHaveNoInteractions();
        then(commentMapper).shouldHaveNoInteractions();
        then(userRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("빈 내용으로 댓글 수정 실패")
    void updateCommentShouldFailWhenContentIsBlank() {
        // given
        comment.setDeletedAt(null);
        given(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("   ")))
                .isInstanceOf(CommentContentRequiredException.class)
                .hasMessageContaining("댓글 내용을 입력해주세요.");

        // verify: 내용 검증에서 예외 → 좋아요/매퍼 호출 안됨
        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
        then(reactionRepository).shouldHaveNoInteractions();
        then(commentMapper).shouldHaveNoInteractions();
        then(userRepository).shouldHaveNoInteractions();
        then(articleRepository).shouldHaveNoInteractions();
    }

    // ===== SoftDeleteComment =====

    @Test
    @DisplayName("논리 삭제 성공")
    void softDeleteCommentSuccess() {
        // given
        given(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.softDeleteById(commentId))
                .willReturn(1);

        // when & then
        assertThatCode(() -> commentService.softDeleteComment(commentId, ownerId))
                .doesNotThrowAnyException();

        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(commentRepository).should().softDeleteById(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 논리삭제-실패권한없음")
    void softDeleteCommentShouldFailWhenUserForbidden() {
        //given
        given(commentRepository.findByIdAndDeletedAtIsNull(commentId))
                .willReturn(Optional.of(comment));

        //when + then
        assertThatThrownBy(() -> commentService.softDeleteComment(commentId, otherUserId))
                .isInstanceOf(CommentForbiddenException.class);

        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 논리삭제 실패-댓글없음 혹은 이미삭제됨")
    void softDeleteCommentShouldFailWhenCommentNotFound() {
        //given
        given(commentRepository.findByIdAndDeletedAtIsNull(commentId)).willReturn(Optional.empty());

        //when + then
        assertThatThrownBy(() -> commentService.softDeleteComment(commentId, ownerId))
                .isInstanceOf(ContentNotFoundException.class);

        then(commentRepository).should().findByIdAndDeletedAtIsNull(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    // ===== HardDeleteComment =====
    @Test
    @DisplayName("물리 삭제 성공")
    void hardDeleteCommentSuccess() {
        //given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        willDoNothing().given(reactionRepository).deleteByCommentId(commentId);
        willDoNothing().given(commentRepository).delete(comment);
        //when + then
        assertThatCode(() -> commentService.hardDeleteComment(commentId, ownerId))
                .doesNotThrowAnyException();

        then(commentRepository).should().findById(commentId);
        then(reactionRepository).should().deleteByCommentId(commentId);
        then(commentRepository).should().delete(comment);
        then(commentRepository).shouldHaveNoMoreInteractions();
        then(reactionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("물리 삭제 실패-삭제 권한 없음")
    void hardDeleteCommentForbidden() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.hardDeleteComment(commentId, otherUserId))
                .isInstanceOf(CommentForbiddenException.class);

        then(commentRepository).should().findById(commentId);
        then(reactionRepository).should(never()).deleteByCommentId(any());
        then(commentRepository).should(never()).delete(any());
        then(commentRepository).shouldHaveNoMoreInteractions();
        then(reactionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("물리 삭제 실패-댓글이 존재하지 않음")
    void hardDeleteCommentNotFound() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.hardDeleteComment(commentId, ownerId))
                .isInstanceOf(ContentNotFoundException.class);

        then(commentRepository).should().findById(commentId);
        then(reactionRepository).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    // ===== 유틸: DeletableEntity의 id 주입/조회 =====
    private static void setId(Object entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
    private static UUID getId(Object entity) {
        return (UUID) ReflectionTestUtils.getField(entity, "id");
    }
}
