package com.sprint.team2.monew.comment.service;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.mapper.CommentMapper;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.comment.service.basic.BasicCommentService;
import com.sprint.team2.monew.domain.interest.entity.Interest;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

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

        // 🔹 빌더 사용(Protected 생성자 회피)
        article = Article.builder()
                .source("nyt")
                .sourceUrl("https://example.com/" + UUID.randomUUID()) // unique
                .title("뉴스 제목")
                .publishDate(LocalDateTime.now())
                .summary("요약")
                .commentCount(0L)
                .viewCount(0L)
                .build();
        setId(article, UUID.randomUUID());

        owner = User.builder()
                .email("owner@example.com")
                .password("password") // 테스트값
                .nickname("작성자")
                .build();
        setId(owner, ownerId);

        // Comment는 public 생성자 가능(이미 @NoArgsConstructor public)
        comment = new Comment();
        setId(comment, commentId);
        comment.setArticle(article);
        comment.setUser(owner);
        comment.setContent("원래 내용");
        comment.setLikeCount(0L);
    }

    private static void setId(Object entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
    private static UUID getId(Object entity) {
        return (UUID) ReflectionTestUtils.getField(entity, "id");
    }

    @Test
    void 댓글_생성_성공() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentRegisterRequest request = new CommentRegisterRequest(
                articleId,
                userId,
                "새로운 댓글 내용"
        );

        User user = new User("example@example.com", "test1234", "testnick");
        Article article = new Article("네이버 뉴스",
                "http://news.naver.com/xxx",
                "테스트 제목",
                LocalDateTime.now(),
                "요약",
                0L,
                0L,
                new Interest("IT", List.of("AI", "개발", "테크")));
        Comment comment = new Comment(user, article, "새로운 댓글 내용", 0L);
        Comment savedComment = new Comment(user, article, "새로운 댓글 내용", 0L);
        CommentDto expectedDto = new CommentDto(
                savedComment.getId(),
                articleId,
                userId,
                user.getNickname(),   // User 엔티티에 닉네임 있다고 가정
                savedComment.getContent(),
                savedComment.getLikeCount(),
                false,                // 요청자가 좋아요 누른 상태 아님
                savedComment.getCreatedAt()
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
        given(commentMapper.toEntity(request)).willReturn(comment);
        given(commentRepository.save(comment)).willReturn(savedComment);
        given(commentMapper.toDto(savedComment, false)).willReturn(expectedDto);

        // when
        CommentDto result = commentService.registerComment(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("새로운 댓글 내용");
        assertThat(result.likeCount()).isZero();
    }

    @Test
    void 댓글_생성_실패_사용자없음() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentRegisterRequest request = new CommentRegisterRequest(
                articleId,
                userId,
                "내용"
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.registerComment(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void 댓글_생성_실패_게시글없음() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentRegisterRequest request = new CommentRegisterRequest(
                articleId,
                userId,
                "내용"
        );

        User user = new User("example@example.com", "test1234", "testnick");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.registerComment(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Article not found");
    }

    @Test
    void 댓글_수정_성공_본인댓글_내용_trim적용() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        CommentDto expected = new CommentDto(
                commentId,
                article.getId(),
                ownerId,
                owner.getNickname(),
                "수정된 내용",     // <- 기대 content
                0L,
                false,
                LocalDateTime.now()
        );

        // toDto 서명이 프로젝트에 따라 1-파라미터/2-파라미터 다를 수 있어 둘 다 lenient 스텁
        lenient().when(commentMapper.toDto(any(Comment.class))).thenReturn(expected);
        lenient().when(commentMapper.toDto(any(Comment.class), anyBoolean())).thenReturn(expected);

        // when
        var req = new CommentUpdateRequest("   수정된 내용   ");
        CommentDto result = commentService.updateComment(commentId, ownerId, req);

        // then
        assertThat(comment.getContent()).isEqualTo("수정된 내용");  // 엔티티가 실제로 바뀌었는가
        assertThat(result.content()).isEqualTo("수정된 내용");      // 응답도 일치하는가

        verifyNoInteractions(userRepository, articleRepository); // update 경로에서 불리지 않음이 자연스러움
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
    void 댓글_수정_실패_content_null() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정할 댓글 내용을 입력해주세요");
    }

    @Test
    void 댓글_수정_실패_content_blank() {
        // given
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() ->
                commentService.updateComment(commentId, ownerId, new CommentUpdateRequest("   ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수정할 댓글 내용을 입력해주세요");
    }
}
