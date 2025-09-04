package com.sprint.team2.monew.domain.comment.service.basic;

import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.dto.request.CommentUpdateRequest;
import com.sprint.team2.monew.domain.comment.mapper.CommentMapper;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.comment.service.CommentService;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

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
        var user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));

        var article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new EntityNotFoundException("Article not found: " + request.articleId()));

        var comment = commentMapper.toEntity(request);
        comment.setUser(user);
        comment.setArticle(article);

        // 내용 최소 검증(등록은 @NotBlank 이지만 안전망)
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        comment.setContent(comment.getContent().trim());

        var saved = commentRepository.save(comment);
        return commentMapper.toDto(saved, false); // 좋아요 도메인 미구현이므로 false
    }

    @Override
    @Transactional
    public CommentDto updateComment(UUID commentId, UUID requesterUserId, CommentUpdateRequest request) {
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found: " + commentId));

        // 본인 댓글만 수정 가능
        if (requesterUserId != null && !comment.getUser().getId().equals(requesterUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다.");
        }

        //업데이트 시 좋아요 유지되는 구문은 좋아요 API 작성 후 생성

        // MapStruct 부분 업데이트 (null/blank 방지)
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("수정할 댓글 내용을 입력해주세요.");
        }

        // 엔티티 메서드 사용
        comment.update(request.content().trim());

        // JPA dirty checking 으로 업데이트 반영
        return commentMapper.toDto(comment, false);
    }

}
