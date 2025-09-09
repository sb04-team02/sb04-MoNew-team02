package com.sprint.team2.monew.domain.comment.service.basic;

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
import com.sprint.team2.monew.domain.comment.service.CommentService;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentMapper commentMapper;
    private final ReactionRepository reactionRepository;

    @Override
    @Transactional
    public CommentDto registerComment(CommentRegisterRequest request) {

        log.info("댓글 생성 시작: articleId={}, userId={}", request.articleId(), request.userId());
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.error("사용자가 존재하지 않음: userId={}", request.userId());
                    return new UserNotFoundException();
                });

        Article article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> {
                    log.error("뉴스기사가 존재하지 않음: articleId={}", request.articleId());
                    return new EntityNotFoundException("Article not found: " + request.articleId());
                });

        Comment comment = commentMapper.toEntity(request);
        comment.setUser(user);
        comment.setArticle(article);

        // 내용 최소 검증(등록은 @NotBlank 이지만 안전하게 하기 위해)
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            log.error("댓글 생성 실패: 빈 댓글 (articleId={}, userId={})",
                    request.articleId(), request.userId());
            throw CommentContentRequiredException.commentContentRequiredForCreate(request.articleId(), request.userId());
        }
        comment.setContent(comment.getContent().trim());

        Comment saved = commentRepository.save(comment);
        log.info("댓글 생성 성공: commentId={}, articleId={}, userId={}",
                saved.getId(), request.articleId(), request.userId());
        CommentDto result = commentMapper.toDto(saved, false);// 좋아요 도메인 미구현이므로 false
        return result;
    }

    @Override
    @Transactional
    public CommentDto updateComment(UUID commentId, UUID requesterUserId, CommentUpdateRequest request) {
        log.info("댓글 수정 시작: commentId={}, requesterUserId={}", commentId, requesterUserId);

        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> {
                    log.error("댓글을 찾을 수 없거나 이미 삭제되었습니다: commentId={}", commentId);
                    return new ContentNotFoundException();
                });

        // 본인 댓글만 수정 가능
        if (requesterUserId != null && !comment.getUser().getId().equals(requesterUserId)) {
            log.error("본인의 댓글만 수정이 가능합니다: commentId={}, ownerId={}, requesterUserId={}",
                    commentId, comment.getUser().getId(), requesterUserId);
            throw new CommentForbiddenException();
        }

        // 검증: null/blank 금지 (공백만 있는 경우도 거부)
        if (request.content() == null || request.content().isBlank()) {
            log.error("댓글 수정 실패: 빈 댓글 (commentId={}, requesterUserId={})",
                    commentId, requesterUserId);
            throw CommentContentRequiredException.commentContentRequiredForUpdate(commentId);
        }

        //좋아요 유지 구문
        boolean likedByMe = false;
        if (requesterUserId != null) {
            likedByMe = reactionRepository.existsByUserIdAndCommentId(requesterUserId, commentId);
        }

        comment.update(request.content());
        log.debug("댓글 수정 반영: commentId={}, length={}", commentId, request.content().length());

        CommentDto dto = commentMapper.toDto(comment, likedByMe);
        log.info("댓글 수정 성공: commentId={}", commentId);
        return dto;
    }

    @Override
    @Transactional
    public void softDeleteComment(UUID commentId, UUID requesterUserId) {
        log.info("댓글 논리 삭제 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);

        //활성 댓글만 조회
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> {
                    log.error("댓글 삭제 실패: 댓글 없음 또는 이미 삭제됨 commentId={}", commentId);
                    return new ContentNotFoundException();
                });

        //권한 체크 : 작성한 본인만 삭제 가능
        if (comment.getUser() == null || !comment.getUser().getId().equals(requesterUserId)) {
            log.error("댓글 삭제 실패: 권한 없음 commentId={}, ownerId={}, requesterUserId={}", commentId,
                    comment.getUser() == null ? null : comment.getUser().getId(),
                    requesterUserId);
            throw new CommentForbiddenException();
        }

        //원자적 soft delete
        int softed = commentRepository.softDeleteById(commentId);
        if (softed == 0) {
            throw new ContentNotFoundException();
        }

        log.info("댓글 논리 삭제 성공: commentId={}, requesterUserId={}", commentId, requesterUserId);
    }

    @Override
    @Transactional
    public void hardDeleteComment(UUID commentId, UUID requesterUserId) {
        log.info("댓글 물리 삭제 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);

        //존재 확인 (삭제 상태와 무관하게)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(ContentNotFoundException::new);

        //권한 체크: 작성자 본인만
        if (comment.getUser() == null || !comment.getUser().getId().equals(requesterUserId)) {
            log.error("댓글 물리 삭제 실패: 권한 없음 commentId={}, ownerId={}, requesterUserId={}",
                    commentId,
                    comment.getUser() == null ? null : comment.getUser().getId(),
                    requesterUserId);
            throw new CommentForbiddenException();
        }

        //연관 데이터 정리
        reactionRepository.deleteByCommentId(commentId);
        log.info("댓글 연관 좋아요 삭제 완료: commentId={}", commentId);

        //댓글 자체 물리 삭제
        commentRepository.delete(comment);
        log.info("댓글 물리 삭제 완료: commentId={}", commentId);
    }
}
