package com.sprint.team2.monew.domain.like.service;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.like.mapper.ReactionMapper;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.like.service.basic.BasicReactionService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BasicReactionServiceTest {
    @Mock
    ReactionRepository reactionRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ReactionMapper reactionMapper;

    @InjectMocks
    BasicReactionService basicReactionService;

    private UUID commentId;
    private UUID requesterUserId;
    private User user;
    private Article article;
    private Comment comment;

    private static void setId(Object entity, UUID id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        requesterUserId = UUID.randomUUID();

        user = User.builder()
                .email("tester@example.com")
                .password("pw")
                .nickname("tester")
                .build();
        setId(user, requesterUserId);

        comment = Comment.builder()
                .user(user)
                .article(article)
                .content("내용")
                .build();
        setId(comment, commentId);
    }

    @Test
    @DisplayName("좋아요 성공 - 좋아요 정보 저장 및 카운트 +1")
    void likeCommentSuccess() {
        //given
        UUID commentId = UUID.randomUUID();
        UUID requesterUserId = UUID.randomUUID();

        User user = User.builder().email("user@example.com").password("pw").nickname("nick").build();
        Comment comment = new Comment();
        comment.setContent("댓글");
        comment.setLikeCount(0L);

        Reaction saved = new Reaction();
        CommentLikeDto commentLikeDto = new CommentLikeDto(
                UUID.randomUUID(), requesterUserId, LocalDateTime.now(),
                commentId, UUID.randomUUID(),
                UUID.randomUUID(), "nick", "댓글", 1L, LocalDateTime.now()
        );

        given(userRepository.findById(requesterUserId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(reactionRepository.existsByUserIdAndCommentId(any(), any())).willReturn(false);
        given(reactionRepository.save(any(Reaction.class))).willReturn(saved);
        given(reactionMapper.toDto(saved)).willReturn(commentLikeDto);

        // when
        CommentLikeDto result = basicReactionService.likeComment(commentId, requesterUserId);

        // then
        assertThat(result.commentId()).isEqualTo(commentId);
        assertThat(result.likedBy()).isEqualTo(requesterUserId);
        assertThat(comment.getLikeCount()).isEqualTo(1L); // 증가 확인

        verify(userRepository).findById(requesterUserId);
        verify(commentRepository).findById(commentId);
        verify(reactionRepository).existsByUserIdAndCommentId(any(), any());
        verify(reactionRepository).save(any(Reaction.class));
        verify(reactionMapper).toDto(saved);
        verifyNoMoreInteractions(userRepository, commentRepository, reactionRepository, reactionMapper);
    }

    @Test
    @DisplayName("좋아요 실패 - 댓글 정보 없음")
    void likeCommentFailCommentNotFound() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User user = User.builder().email("u@e.com").password("pw").nickname("nick").build();

        given(userRepository.findById(requesterId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> basicReactionService.likeComment(commentId, requesterId))
                .isInstanceOf(ContentNotFoundException.class);

        verify(commentRepository).findById(commentId);
        verifyNoInteractions(reactionRepository, reactionMapper);
    }

    @Test
    @DisplayName("댓글 좋아요 취소 성공")
    void likeCommentCancelSuccess() {
        //given
        Reaction reaction = Reaction.builder()
                .user(user)
                .comment(comment)
                .build();
        setId(reaction, UUID.randomUUID());

        given(userRepository.findById(requesterUserId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(reactionRepository.findByUserIdAndCommentId(user.getId(), comment.getId())).willReturn(Optional.of(reaction));
        given(reactionRepository.deleteByUserIdAndCommentId(user.getId(), comment.getId())).willReturn(1);
        given(commentRepository.decrementLikeCount(comment.getId())).willReturn(1);

        //when
        basicReactionService.unlikeComment(commentId, requesterUserId);

        //then
        then(userRepository).should().findById(requesterUserId);
        then(commentRepository).should().findById(commentId);
        then(reactionRepository).should().findByUserIdAndCommentId(user.getId(), comment.getId());
        then(reactionRepository).should().deleteByUserIdAndCommentId(user.getId(), comment.getId());
        then(commentRepository).should().decrementLikeCount(comment.getId());
        then(userRepository).shouldHaveNoMoreInteractions();
        then(commentRepository).shouldHaveNoMoreInteractions();
        then(reactionRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("댓글 좋아요 취소 실패 - 사용자가 존재하지 않음")
    void likeCommentCancelFailUserNotFound() {
        //given
        given(userRepository.findById(requesterUserId)).willReturn(Optional.empty());

        //when + then
        assertThrows(UserNotFoundException.class,
                () -> basicReactionService.unlikeComment(commentId, requesterUserId));

        then(userRepository).should().findById(requesterUserId);
        then(commentRepository).shouldHaveNoInteractions();
        then(reactionRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("댓글 좋아요 취소 실패 - 댓글이 존재하지 않음")
    void likeCommentCancelFailCommentNotFound() {
        //given
        given(userRepository.findById(requesterUserId)).willReturn(Optional.of(user));
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        //when + then
        assertThrows(ContentNotFoundException.class,
                () -> basicReactionService.unlikeComment(commentId, requesterUserId));

        then(userRepository).should().findById(requesterUserId);
        then(commentRepository).should().findById(commentId);
        then(reactionRepository).shouldHaveNoInteractions();
    }
}
