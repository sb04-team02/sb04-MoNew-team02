package com.sprint.team2.monew.domain.like.service.basic;

import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.exception.ContentNotFoundException;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import com.sprint.team2.monew.domain.like.exception.ReactionAlreadyExistsException;
import com.sprint.team2.monew.domain.like.exception.ReactionNotFoundException;
import com.sprint.team2.monew.domain.like.mapper.ReactionMapper;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.like.service.ReactionService;
import com.sprint.team2.monew.domain.notification.event.CommentLikedEvent;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentLikeCancelEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicReactionService implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ReactionMapper reactionMapper;

    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public CommentLikeDto likeComment(UUID commentId, UUID requesterUserId) {
        log.info("댓글 좋아요 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);
        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> {
                    log.error("좋아요 실패: 사용자 없음 requesterUserId={}", requesterUserId);
                    return new UserNotFoundException();
                });

        Comment comment = commentRepository.findWithArticleAndUserById(commentId)
                .orElseThrow(() -> {
                    log.error("좋아요 실패: 댓글 없음 commentId={}", commentId);
                    return ContentNotFoundException.contentNotFoundException(commentId);
                });

        try {
            //중복 방지
            if (reactionRepository.existsByUser_IdAndComment_Id(user.getId(), comment.getId())) {
                log.error("좋아요 실패: 이미 좋아요됨 userId={}, commentId={}", user.getId(), comment.getId());
                throw ReactionAlreadyExistsException.reactionAlreadyExists(commentId, user.getId());
            }

            Reaction reaction = new Reaction();
            reaction.setUser(user);
            reaction.setComment(comment);

            //유니크 위반을 즉시 드러내기 위해 flush
            reactionRepository.saveAndFlush(reaction);

            // 좋아요 생성 시 알림 이벤트 발행
            applicationEventPublisher.publishEvent(new CommentLikedEvent(
                    commentId,
                    comment.getUser().getId()
            ));

            //원자적 +1 (동시성 안전)
            int updated = commentRepository.incrementLikeCount(comment.getId());
            if (updated == 0) {
                throw ContentNotFoundException.contentNotFoundException(commentId);
            }

            //증가 후 카운트 재조회
            long newLikeCount = Optional.ofNullable(
                    commentRepository.findLikeCountById(comment.getId())
            ).orElse(0L);

            Comment freshComment = commentRepository.findWithArticleAndUserById(comment.getId())
                    .orElseThrow(() -> ContentNotFoundException.contentNotFoundException(commentId));
            reaction.setComment(freshComment);

            CommentLikeDto dto = reactionMapper.toDto(reaction, newLikeCount);
            log.info("댓글 좋아요 성공: reactionId={}, commentId={}, likedBy={}", dto.id(), dto.commentId(), dto.likedBy());

            // User Activity 이벤트
            applicationEventPublisher.publishEvent(new CommentLikeAddEvent(
                reaction.getId(),
                reaction.getCreatedAt(),
                comment.getId(),
                comment.getArticle().getId(),
                comment.getArticle().getTitle(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                newLikeCount,
                comment.getCreatedAt()
            ));

            return dto;
        } catch (DataIntegrityViolationException e) {
            // 유니크 제약(동시 더블클릭 등) 최종 방어
            log.error("좋아요 실패(유니크 위반): userId={}, commentId={}", user.getId(), comment.getId());
            throw ReactionAlreadyExistsException.reactionAlreadyExists(comment.getId(), user.getId());
        }
    }

    @Override
    public void unlikeComment(UUID commentId, UUID requesterUserId) {
        log.info("댓글 좋아요 취소 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);

        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> {
                    log.error("좋아요 취소 실패 : 사용자가 존재하지 않음. requesterUserId={}", requesterUserId);
                    return UserNotFoundException.withId(requesterUserId);
                });

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("좋아요 취소 실패 : 댓글이 존재하지 않음 commentId={}", commentId);
                    return ContentNotFoundException.contentNotFoundException(commentId);
                });

        boolean exists = reactionRepository.existsByUser_IdAndComment_Id(user.getId(), comment.getId());
        if (!exists) {
            log.error("좋아요 취소 실패: 기존 Reaction 없음 userId={}, commentId={}", user.getId(), comment.getId());
            throw ReactionNotFoundException.forUnlike(comment.getId(), user.getId());
        }

        int deleted = reactionRepository.deleteByUser_IdAndComment_Id(user.getId(), comment.getId());
        if (deleted > 0) {
            int affected = commentRepository.decrementLikeCount(comment.getId());
            if (affected == 0) {
                log.warn("decrementLikeCount 미적용(이미 0이었을 가능성): commentId={}", comment.getId());
            }
            log.info("댓글 좋아요 취소 성공: userId={}, commentId={}", user.getId(), comment.getId());
            long newLikeCount = commentRepository.findLikeCountById(comment.getId());

            // User Activity 이벤트
            applicationEventPublisher.publishEvent(new CommentLikeCancelEvent(
                commentId,
                comment.getUser().getId(), // author
                newLikeCount
            ));

        } else {
            log.info("좋아요 취소: 기존 Reaction 없음 -> 멱등 처리 userId={}, commentId={}", user.getId(), comment.getId());
        }
    }
}
