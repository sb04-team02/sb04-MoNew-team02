package com.sprint.team2.monew.domain.comment.service.basic;

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
import com.sprint.team2.monew.domain.comment.service.CommentService;
import com.sprint.team2.monew.domain.like.repository.ReactionRepository;
import com.sprint.team2.monew.domain.notification.entity.ResourceType;
import com.sprint.team2.monew.domain.notification.repository.NotificationRepository;
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentAddEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.commentEvent.CommentUpdateEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentMapper commentMapper;
    private final ReactionRepository reactionRepository;
    private final NotificationRepository notificationRepository;

    // User Activity 이벤트
    private final ApplicationEventPublisher publisher;

    @Override
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

        // article의 댓글 수 증가 반영
        articleRepository.increaseCommentCount(article.getId());

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new CommentAddEvent(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getArticle().getTitle(),
                user.getId(),
                user.getNickname(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.getCreatedAt()
        ));

        return result;
    }

    @Override
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
            likedByMe = reactionRepository.existsByUser_IdAndComment_Id(requesterUserId, commentId);
        }

        comment.update(request.content());
        log.debug("댓글 수정 반영: commentId={}, length={}", commentId, request.content().length());

        CommentDto dto = commentMapper.toDto(comment, likedByMe);
        log.info("댓글 수정 성공: commentId={}", commentId);

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new CommentUpdateEvent(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getArticle().getTitle(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getLikeCount(),
                comment.getCreatedAt()
        ));

        return dto;
    }

    @Override
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

        // article 관련 댓글 수 감소
        articleRepository.decreaseCommentCount(comment.getArticle().getId());

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new CommentDeleteEvent(
                comment.getId(),
                requesterUserId
        ));
    }

    @Override
    public void hardDeleteComment(UUID commentId, UUID requesterUserId) {
        log.info("댓글 물리 삭제 요청: commentId={}, requesterUserId={}", commentId, requesterUserId);

        //존재 확인 (삭제 상태와 무관하게)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("댓글 삭제 실패: 댓글 없음 또는 이미 삭제됨 commentId={}", commentId);
                    return new ContentNotFoundException();
                });

        //권한 체크: 작성자 본인만
        if (comment.getUser() == null || !comment.getUser().getId().equals(requesterUserId)) {
            log.error("댓글 물리 삭제 실패: 권한 없음 commentId={}, ownerId={}, requesterUserId={}",
                    commentId,
                    comment.getUser() == null ? null : comment.getUser().getId(),
                    requesterUserId);
            throw new CommentForbiddenException();
        }

        //연관 데이터 정리
        reactionRepository.deleteByComment_Id(commentId);
        notificationRepository.deleteByResourceTypeAndResourceId(ResourceType.COMMENT, commentId);
        log.info("댓글 연관 좋아요, 알림 삭제 완료: commentId={}", commentId);


        //댓글 자체 물리 삭제
        commentRepository.delete(comment);
        log.info("댓글 물리 삭제 완료: commentId={}", commentId);

        // ============== User Activity 이벤트 추가 ==============
        publisher.publishEvent(new CommentDeleteEvent(
                comment.getId(),
                requesterUserId
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseCommentDto getAllArticleComment(
            UUID articleId,
            UUID requesterUserId,
            String cursor,
            int size,
            LocalDateTime after,
            CommentSortType sortType,
            boolean asc
    ) {
        log.info("댓글 목록 조회 시작: articleId={}, sortType={}, asc={}, size={}",
                articleId, sortType, asc, size);

        if (sortType == null) sortType = CommentSortType.DATE;
        if (size <= 0 || size > 100) size = 20;

        if (!articleRepository.existsById(articleId)) {
            log.error("뉴스기사가 존재하지 않습니다.");
            throw new ArticleNotFoundException();
        }

        // === after 보정 및 플래그 ===
        final LocalDateTime afterDate = after;
        final boolean hasAfter = (afterDate != null);

        // === cursor 파싱 및 플래그 ===
        LocalDateTime cursorDate = null;
        Long cursorLike = null;

        if (cursor != null && !cursor.isBlank()) {
            try {
                if (sortType == CommentSortType.DATE) {
                    cursorDate = LocalDateTime.parse(cursor);
                } else { // LIKE_COUNT
                    String[] parts = cursor.split("\\|");
                    if (parts.length != 2) throw new IllegalArgumentException("좋아요 정렬에 대한 잘못된 커서 형식입니다");
                    cursorLike = Long.parseLong(parts[0]);
                    cursorDate = LocalDateTime.parse(parts[1]);
                }
            } catch (Exception e) {
                log.error("잘못된 커서 형식: cursor={}, sortType={}", cursor, sortType);
                throw new IllegalArgumentException("잘못된 커서 형식입니다.");
            }
        }
        final boolean hasCursor = (sortType == CommentSortType.DATE)
                ? (cursorDate != null)
                : (cursorLike != null && cursorDate != null);

        // === 정렬/페이지 설정 ===
        Sort.Direction direction = asc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = (sortType == CommentSortType.LIKE_COUNT)
                ? Sort.by(direction, "likeCount").and(Sort.by(direction, "createdAt"))
                : Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(0, size, sort);

        // === 조회 ===
        Slice<Comment> slice = (sortType == CommentSortType.DATE)
                ? commentRepository.findByArticle_IdWithDateCursor(
                /* :articleId */ articleId,
                /* :hasAfter  */ hasAfter,
                /* :afterDate */ afterDate,
                /* :hasCursor */ hasCursor,
                /* :cursorDate*/ cursorDate,
                /* :asc       */ asc,
                pageable
        )
                : commentRepository.findByArticle_IdWithLikeCountCursor(
                /* :articleId  */ articleId,
                /* :hasAfter   */ hasAfter,
                /* :afterDate  */ afterDate,
                /* :hasCursor  */ hasCursor,
                /* :cursorLike */ cursorLike,
                /* :cursorDate */ cursorDate,
                /* :asc        */ asc,
                pageable
        );

        // === 변환 ===
        List<Comment> comments = slice.getContent();
        boolean hasNext = slice.hasNext();

        List<CommentDto> commentDtos = comments.stream()
                .map(c -> {
                    boolean likedByMe = (requesterUserId != null)
                            && reactionRepository.existsByUser_IdAndComment_Id(requesterUserId, c.getId());
                    return commentMapper.toDto(c, likedByMe);
                })
                .toList();

        // === nextCursor / nextAfter ===
        String nextCursor = null;
        LocalDateTime nextAfter = null;
        if (hasNext && !comments.isEmpty()) {
            Comment last = comments.get(comments.size() - 1);
            nextAfter = last.getCreatedAt(); // DTO 필드명과 동일
            nextCursor = (sortType == CommentSortType.LIKE_COUNT)
                    ? last.getLikeCount() + "|" + last.getCreatedAt()
                    : last.getCreatedAt().toString();
        }

        long totalElements = commentRepository.countByArticle_IdAndNotDeleted(articleId);

        log.info("댓글 목록 조회 완료: articleId={}, 조회된 수={}, 전체 수={}, hasNext={}",
                articleId, commentDtos.size(), totalElements, hasNext);

        return new CursorPageResponseCommentDto(
                commentDtos,
                nextCursor,
                nextAfter,
                size,
                totalElements,
                hasNext
        );
    }

}
