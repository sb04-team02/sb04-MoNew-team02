package com.sprint.team2.monew.domain.comment.service;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.exception.ArticleNotFoundException;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.dto.response.CursorPageResponseCommentDto;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.entity.CommentSortType;
import com.sprint.team2.monew.domain.comment.exception.CommentContentRequiredException;
import com.sprint.team2.monew.domain.comment.exception.CommentForbiddenException;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.comment.mapper.CommentMapper;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.comment.service.basic.BasicCommentService;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.sprint.team2.monew.domain.article.entity.ArticleSource.NAVER;
import static org.assertj.core.api.Assertions.*;
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
    private ReactionRepository reactionRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private CommentMapper commentMapper;

    @Captor
    ArgumentCaptor<PageRequest> pageRequestCaptor;

    @InjectMocks
    private BasicCommentService commentService;

    // User Activity 용 Publisher
    @Mock
    private ApplicationEventPublisher publisher;


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
                .source(NAVER)
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
                .source(NAVER)
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
        given(reactionRepository.existsByUser_IdAndComment_Id(ownerId, commentId))
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
        then(reactionRepository).should().existsByUser_IdAndComment_Id(ownerId, commentId);
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
        willDoNothing().given(reactionRepository).deleteByComment_Id(commentId);
        willDoNothing().given(commentRepository).delete(comment);
        //when + then
        assertThatCode(() -> commentService.hardDeleteComment(commentId, ownerId))
                .doesNotThrowAnyException();

        then(commentRepository).should().findById(commentId);
        then(reactionRepository).should().deleteByComment_Id(commentId);
        then(notificationRepository).should().deleteByResourceTypeAndResourceId(ResourceType.COMMENT, commentId);
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
        then(reactionRepository).should(never()).deleteByComment_Id(any());
        then(notificationRepository).should(never()).deleteByResourceTypeAndResourceId(any(ResourceType.class), any(UUID.class));
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
        then(notificationRepository).shouldHaveNoInteractions();
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    // ===== ArticleComment List =====
    @Test
    @DisplayName("DATE 정렬(내림차순) 성공: nextCursor=마지막 createdAt, likedByMe 반영")
    void getAllArticleCommentWithDateSortSuccess() {
        //given
        UUID articleId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        int size = 3;
        boolean asc = false;

        LocalDateTime after = null;
        LocalDateTime afterDate = null;

        LocalDateTime t3 = LocalDateTime.parse("2025-09-10T10:10:00");

        Comment c1 = mock(Comment.class);
        Comment c2 = mock(Comment.class);
        Comment c3 = mock(Comment.class);

        UUID c1Id = UUID.randomUUID();
        UUID c2Id = UUID.randomUUID();
        UUID c3Id = UUID.randomUUID();

        given(c1.getId()).willReturn(c1Id);
        given(c2.getId()).willReturn(c2Id);
        given(c3.getId()).willReturn(c3Id);

        given(c3.getCreatedAt()).willReturn(t3);

        given(articleRepository.existsById(articleId)).willReturn(true);

        Slice<Comment> slice = new SliceImpl<>(
                List.of(c1, c2, c3),
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")),
                true // hasNext
        );
        given(commentRepository.findByArticle_IdWithDateCursor(eq(articleId), eq(false), isNull(), eq(false), isNull(), eq(asc), any(Pageable.class)))
                .willReturn(slice);

        // likedByMe: true, false, true
        given(reactionRepository.existsByUser_IdAndComment_Id(requesterId, c1Id)).willReturn(true);
        given(reactionRepository.existsByUser_IdAndComment_Id(requesterId, c2Id)).willReturn(false);
        given(reactionRepository.existsByUser_IdAndComment_Id(requesterId, c3Id)).willReturn(true);

        CommentDto dto1 = mock(CommentDto.class);
        CommentDto dto2 = mock(CommentDto.class);
        CommentDto dto3 = mock(CommentDto.class);

        given(commentMapper.toDto(c1, true)).willReturn(dto1);
        given(commentMapper.toDto(c2, false)).willReturn(dto2);
        given(commentMapper.toDto(c3, true)).willReturn(dto3);

        given(commentRepository.countByArticle_IdAndNotDeleted(articleId)).willReturn(42L);

        //when
        CursorPageResponseCommentDto result = commentService.getAllArticleComment(
                articleId, requesterId, null, size, after,null, asc);

        //then
        assertThat(result).isNotNull();
        assertThat(result.content()).containsExactly(dto1, dto2, dto3);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalElements()).isEqualTo(42L);
        assertThat(result.nextCursor()).isEqualTo(t3.toString());

        then(articleRepository).should().existsById(articleId);
        then(commentRepository).should()
                .findByArticle_IdWithDateCursor(eq(articleId), eq(false), isNull(), eq(false), isNull(), eq(false), any(Pageable.class));
        then(reactionRepository).should(times(3))
                .existsByUser_IdAndComment_Id(eq(requesterId), any(UUID.class));
        then(commentMapper).should().toDto(c1, true);
        then(commentMapper).should().toDto(c2, false);
        then(commentMapper).should().toDto(c3, true);
        then(commentRepository).should().countByArticle_IdAndNotDeleted(articleId);
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("LIKE_COUNT 정렬(내림차순) 성공")
    void getAllArticleCommentWithLikeCountSortSuccess() {
        //given
        UUID articleId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        int size = 2;
        boolean asc = false;

        LocalDateTime after = null;
        LocalDateTime afterDate = null;

        LocalDateTime t1 = LocalDateTime.parse("2025-09-10T09:00:00");
        LocalDateTime t2 = LocalDateTime.parse("2025-09-10T09:01:00");

        Comment a = mock(Comment.class);
        Comment b = mock(Comment.class);

        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();

        given(a.getId()).willReturn(aId);
        given(b.getId()).willReturn(bId);

        given(b.getCreatedAt()).willReturn(t2);

        given(b.getLikeCount()).willReturn(10L); // 동률 → createdAt 보조 정렬

        given(articleRepository.existsById(articleId)).willReturn(true);

        Slice<Comment> slice = new SliceImpl<>(
                List.of(a, b),
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "likeCount")
                        .and(Sort.by(Sort.Direction.DESC, "createdAt"))),
                true
        );
        given(commentRepository.findByArticle_IdWithLikeCountCursor(
                eq(articleId), eq(false), isNull(), eq(false), isNull(), isNull(), eq(asc), any(Pageable.class)))
                .willReturn(slice);

        given(reactionRepository.existsByUser_IdAndComment_Id(requesterId, aId)).willReturn(false);
        given(reactionRepository.existsByUser_IdAndComment_Id(requesterId, bId)).willReturn(true);

        CommentDto dtoA = mock(CommentDto.class);
        CommentDto dtoB = mock(CommentDto.class);

        given(commentMapper.toDto(a, false)).willReturn(dtoA);
        given(commentMapper.toDto(b, true)).willReturn(dtoB);

        given(commentRepository.countByArticle_IdAndNotDeleted(articleId)).willReturn(7L);

        //when
        CursorPageResponseCommentDto result = commentService.getAllArticleComment(
                articleId, requesterId, null, size, after, CommentSortType.LIKE_COUNT, asc);

        //then
        assertThat(result.content()).containsExactly(dtoA, dtoB);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalElements()).isEqualTo(7L);
        assertThat(result.nextCursor()).isEqualTo("10|" + t2);

        then(commentRepository).should().findByArticle_IdWithLikeCountCursor(
                eq(articleId), eq(false), isNull(), eq(false), isNull(), isNull(), eq(false), any(Pageable.class));
    }

    @Test
    @DisplayName("requesterUserId가 null이면 likedByMe 조회 호출 안 함")
    void requesterNullShouldNotQueryLikedByMe() {
        //given
        UUID articleId = UUID.randomUUID();
        int size = 2;

        LocalDateTime after = null;
        LocalDateTime afterDate = null;

        Comment c1 = mock(Comment.class);
        Comment c2 = mock(Comment.class);

        UUID c1Id = UUID.randomUUID();
        UUID c2Id = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        given(articleRepository.existsById(articleId)).willReturn(true);

        Slice<Comment> slice = new SliceImpl<>(
                List.of(c1, c2),
                PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")),
                false
        );
        given(commentRepository.findByArticle_IdWithDateCursor(eq(articleId), eq(false), isNull(), eq(false), isNull(), eq(false), any(Pageable.class)))
                .willReturn(slice);

        CommentDto dto1 = mock(CommentDto.class);
        CommentDto dto2 = mock(CommentDto.class);
        given(commentMapper.toDto(eq(c1), anyBoolean())).willReturn(dto1);
        given(commentMapper.toDto(eq(c2), anyBoolean())).willReturn(dto2);

        given(commentRepository.countByArticle_IdAndNotDeleted(articleId)).willReturn(2L);

        //when
        CursorPageResponseCommentDto result = commentService.getAllArticleComment(
                articleId, null, null, size, after, CommentSortType.DATE, false);

        //then
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursor()).isNull();
        assertThat(result.content()).containsExactly(dto1, dto2);

        then(reactionRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("잘못된 커서 포맷(DATE): IllegalArgumentException")
    void invalidCursorDateSortThrowsIllegalArgument() {
        //given
        UUID articleId = UUID.randomUUID();
        given(articleRepository.existsById(articleId)).willReturn(true);
        String badCursor = "NOT_A_DATE";

        //when + then
        assertThatThrownBy(() ->
                commentService.getAllArticleComment(articleId, UUID.randomUUID(), badCursor, 10, null, CommentSortType.DATE, false)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 커서 형식");

        then(commentRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("잘못된 커서 포맷(LIKE_COUNT): IllegalArgumentException")
    void invalidCursorLikeCountSortThrowsIllegalArgument() {
        //given
        UUID articleId = UUID.randomUUID();
        given(articleRepository.existsById(articleId)).willReturn(true);
        String badCursor = "notNumber|notDate";

        //when + then
        assertThatThrownBy(() ->
                commentService.getAllArticleComment(articleId, UUID.randomUUID(), badCursor, 10, null, CommentSortType.LIKE_COUNT, false)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 커서 형식");

        then(commentRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("기사 없음: ArticleNotFoundException")
    void articleNotFoundThrowsException() {
        //given
        UUID articleId = UUID.randomUUID();
        given(articleRepository.existsById(articleId)).willReturn(false);

        //when + then
        assertThatThrownBy(() ->
                commentService.getAllArticleComment(articleId, UUID.randomUUID(), null, 10, null, CommentSortType.DATE, false)
        ).isInstanceOf(ArticleNotFoundException.class);

        then(commentRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("size 보정(0 또는 100 초과 → 20) 및 Pageable 반영 확인")
    void sizeNormalizationAndPageableVerification() {
        //given
        UUID articleId = UUID.randomUUID();
        given(articleRepository.existsById(articleId)).willReturn(true);

        int requestedSize = 0; // → 20으로 보정
        Slice<Comment> emptySlice = new SliceImpl<>(
                List.of(),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")),
                false
        );

        given(commentRepository.findByArticle_IdWithDateCursor(eq(articleId), eq(false), isNull(), eq(false), isNull(), eq(false), any(Pageable.class)))
                .willReturn(emptySlice);
        given(commentRepository.countByArticle_IdAndNotDeleted(articleId)).willReturn(0L);

        //when
        commentService.getAllArticleComment(articleId, UUID.randomUUID(), null, requestedSize, null, CommentSortType.DATE, false);

        //then
        then(commentRepository).should().findByArticle_IdWithDateCursor(eq(articleId), eq(false), isNull(), eq(false), isNull(), eq(false), pageRequestCaptor.capture());
        PageRequest pr = pageRequestCaptor.getValue();
        assertThat(pr.getPageSize()).isEqualTo(20);
        assertThat(pr.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // ===== 유틸: DeletableEntity의 id 주입/조회 =====
    private static void setId(Object entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
    private static UUID getId(Object entity) {
        return (UUID) ReflectionTestUtils.getField(entity, "id");
    }
}
