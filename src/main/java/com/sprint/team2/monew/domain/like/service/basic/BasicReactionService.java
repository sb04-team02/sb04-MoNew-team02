package com.sprint.team2.monew.domain.like.service.basic;

import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.like.exception.ReactionAlreadyExistsException;
import com.sprint.team2.monew.domain.like.mapper.ReactionMapper;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.like.service.ReactionService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicReactionService implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReactionMapper reactionMapper;

    @Override
    @Transactional
    public CommentLikeDto likeComment(UUID commentId, UUID requesterUserId) {
        log.info("댓글 좋아요 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);
        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> {
                    log.error("좋아요 실패: 사용자 없음 requesterUserId={}", requesterUserId);
                    return new UserNotFoundException();
                });

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("좋아요 실패: 댓글 없음 commentId={}", commentId);
                    return ContentNotFoundException.contentNotFoundException(commentId);
                });

        try {
            // 2) 중복 방지
            if (reactionRepository.existsByUserIdAndCommentId(user.getId(), comment.getId())) {
                log.error("좋아요 실패: 이미 좋아요됨 userId={}, commentId={}", user.getId(), comment.getId());
                throw ReactionAlreadyExistsException.reactionAlreadyExists(commentId, user.getId());
            }

            Reaction reaction = new Reaction();
            reaction.setUser(user);
            reaction.setComment(comment);

            //유니크 위반을 즉시 드러내기 위해 flush
            reactionRepository.saveAndFlush(reaction);

            // ★ 원자적 +1 (동시성 안전)
            commentRepository.incrementLikeCount(comment.getId());

            CommentLikeDto dto = reactionMapper.toDto(reaction);
            log.info("댓글 좋아요 성공: reactionId={}, commentId={}, likedBy={}", dto.id(), dto.commentId(), dto.likedBy());
            return dto;
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약(동시 더블클릭 등) 최종 방어
            log.error("좋아요 실패(유니크 위반): userId={}, commentId={}", user.getId(), comment.getId());
            throw ReactionAlreadyExistsException.reactionAlreadyExists(comment.getId(), user.getId());
        }
    }

    public void unlikeComment(UUID commentId, UUID requesterUserId) {

    }
}
