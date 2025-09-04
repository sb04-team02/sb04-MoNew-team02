package com.sprint.team2.monew.domain.comment.service.basic;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.comment.mapper.CommentMapper;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.comment.service.CommentService;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Override
    @Transactional
    public CommentDto registerComment(CommentRegisterRequest request) {

        log.info("댓글 생성 시작: articleId={}, userId={}", request.articleId(), request.userId());
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> {
                    log.error("사용자가 존재하지 않음: userId={}", request.userId());
                    return new EntityNotFoundException("User not found: " + request.userId());
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
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
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
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("댓글을 찾을 수 없습니다: commentId={}", commentId);
                    return new EntityNotFoundException("Comment not found: " + commentId);
                });

        // 본인 댓글만 수정 가능
        if (requesterUserId != null && !comment.getUser().getId().equals(requesterUserId)) {
            log.error("본인의 댓글만 수정이 가능합니다: commentId={}, ownerId={}, requesterUserId={}",
                    commentId, comment.getUser().getId(), requesterUserId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다.");
        }

        //업데이트 시 좋아요 유지되는 구문은 좋아요 도메인 작성 후 생성

        // 검증: null/blank 금지 (공백만 있는 경우도 거부)
        if (request.content() == null || request.content().isBlank()) {
            log.error("댓글 수정 실패: 빈 댓글 (commentId={}, requesterUserId={})",
                    commentId, requesterUserId);
            throw new IllegalArgumentException("수정할 댓글 내용을 입력해주세요.");
        }

        comment.update(request.content());
        log.debug("댓글 수정 반영: commentId={}, length={}", commentId, request.content().length());

        CommentDto dto = commentMapper.toDto(comment, false);
        log.info("댓글 수정 성공: commentId={}", commentId);
        return dto;
    }
}
