package com.sprint.team2.monew.domain.article.service.basic;

import com.sprint.team2.monew.domain.article.collect.NaverApiCollector;
import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.article.entity.ArticleDirection;
import com.sprint.team2.monew.domain.article.entity.ArticleOrderBy;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.exception.ArticleCollectFailedException;
import com.sprint.team2.monew.domain.article.exception.ArticleNotFoundException;
import com.sprint.team2.monew.domain.article.exception.ArticleSaveFailedException;
import com.sprint.team2.monew.domain.article.exception.InvalidParameterException;
import com.sprint.team2.monew.domain.article.mapper.ArticleMapper;
import com.sprint.team2.monew.domain.article.repository.ArticleRepository;
import com.sprint.team2.monew.domain.article.repository.ArticleRepositoryCustom;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import com.sprint.team2.monew.domain.comment.repository.CommentRepository;
import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.exception.InterestNotFoundException;
import com.sprint.team2.monew.domain.interest.repository.InterestRepository;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleDeleteEvent;
import com.sprint.team2.monew.domain.userActivity.events.articleEvent.ArticleViewEvent;
import com.sprint.team2.monew.domain.userActivity.exception.UserActivityNotFoundException;
import com.sprint.team2.monew.domain.userActivity.mapper.UserActivityMapper;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepository;
import com.sprint.team2.monew.domain.userActivity.repository.UserActivityRepositoryCustom;
import com.sprint.team2.monew.domain.notification.event.InterestArticleRegisteredEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Builder
@Transactional
public class BasicArticleService implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleRepository articleRepository;
    private final ArticleRepositoryCustom articleRepositoryCustom;
    private final NaverApiCollector naverApiCollector;

    private final InterestRepository interestRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final UserActivityRepository userActivityRepository;
    private final UserActivityRepositoryCustom userActivityRepositoryCustom;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    private final UserActivityMapper userActivityMapper;

    @Override
    public void saveByInterest(UUID interestId) {

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> InterestNotFoundException.notFound(interestId));
        for (String keyword : interest.getKeywords()) {
            List<ArticleDto> articles;
            try {
                articles = naverApiCollector.collect(keyword);
            } catch (RuntimeException e) {
                log.error("[Article] keyword({}) 수집 실패", keyword, e);
                throw ArticleCollectFailedException.withKeyword(keyword);
            }

            for (ArticleDto dto : articles) {
                if (!articleRepository.existsBySourceUrlAndDeletedAtIsNull(dto.sourceUrl())) {
                    Article articleEntity = articleMapper.toEntity(dto);
                    try {
                        articleRepository.save(articleEntity);
                        log.info("[Article] {}에서 keyword({}) 저장 성공: {} - {}",
                                dto.source(), keyword, dto.title(), dto.sourceUrl());

                        applicationEventPublisher.publishEvent(new InterestArticleRegisteredEvent(
                                interestId,
                                articleEntity.getId()
                        ));

                    } catch (Exception e) {
                        log.error("[Article] {}에서 keyword({}) 저장 실패: {} - {}",
                                dto.source(), keyword, dto.title(), dto.sourceUrl(), e);
                        throw ArticleSaveFailedException.articleSaveFailed();
                    }
                } else {
                    log.debug("[Article] {}에서 keyword({}) 저장 실패(중복 기사): {} - {}",
                            dto.source(), keyword, dto.title(), dto.sourceUrl());
                }
            }
            log.info("[Article] keyword({})로 뉴스 수집 완료, total = {}", keyword, articles.size());
        }
    }

    @Override
    public ArticleViewDto view(UUID userId, UUID articleId) {
        log.info("[Article] 기사 뷰 등록 시작, userId: {}, articleId: {}", userId, articleId);
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.error("[Article] 존재하지 않는 뉴스 기사, articleId = {}", articleId);
                    return ArticleNotFoundException.withId(articleId);
                });

        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[User] 존재하지 않는 사용자, userId = {}", userId);
                    return UserNotFoundException.withId(userId);
                });

        boolean alreadyViewed = hasUserViewedArticle(userId, articleId);

        LocalDateTime viewedDate;
        long articleCommentCount = 0L;
        long articleViewCount = article.getViewCount();

        if (!alreadyViewed) {
            viewedDate = LocalDateTime.now();
            articleViewCount += 1;
            article.setViewCount(articleViewCount);

            articleRepository.save(article);
        } else {
            viewedDate = userActivityRepositoryCustom.findByArticleId(userId ,articleId).createdAt();
        }

        articleCommentCount = commentRepository.countByArticle_Id(articleId);

        log.info("[Article] 기사 뷰 등록 성공");
        ArticleViewDto articleViewDto = new ArticleViewDto(
                UUID.randomUUID(),
                userId,
                viewedDate,
                article.getId(),
                article.getSource().name(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                articleCommentCount,
                articleViewCount
        );

        // publish event
        applicationEventPublisher.publishEvent(userActivityMapper.toArticleViewEvent(articleViewDto));

        return articleViewDto;
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto read(UUID userId,
                                             ArticleOrderBy orderBy,
                                             ArticleDirection direction,
                                             int limit,
                                             String keyword,
                                             UUID interestId,
                                             List<ArticleSource> sourceIn,
                                             LocalDateTime publishedDateFrom,
                                             LocalDateTime publishedDateTo,
                                             String cursor,
                                             LocalDateTime after) {

        log.info("[Article] 뉴스 기사 목록 조회 시작");
        if (keyword == null || keyword.isBlank() || cursor == null && after == null) {
            cursor = null;
            after = null;
            limit = 50;
        }

        if (orderBy != ArticleOrderBy.publishDate &&
                orderBy != ArticleOrderBy.commentCount &&
                orderBy != ArticleOrderBy.viewCount) {
            log.error("[Article] 정렬 속성에는 publishDate, commentCount, viewCount만 가능, 정렬 속성 = {}", orderBy);
            throw InvalidParameterException.invalidParameter();
        }

        List<Article> articles = articleRepositoryCustom.searchArticles(
                keyword, interestId, sourceIn, publishedDateFrom, publishedDateTo,
                orderBy, direction, cursor, after, limit
        );

        boolean hasNext = articles.size() > limit;

        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            Article last = articles.get(limit);

            switch (orderBy) {
                case commentCount -> {
                    nextCursor = String.valueOf(last.getCommentCount());
                    nextAfter = last.getCreatedAt();

                }
                case viewCount -> {
                    nextCursor = String.valueOf(last.getViewCount());
                    nextAfter = last.getCreatedAt();
                }
                default -> {
                    nextAfter = last.getCreatedAt();
                    nextCursor = last.getPublishDate().toString();
                }
            }
            articles = articles.subList(0, limit);
        }

        List<ArticleDto> content = articles.stream()
                .map(article -> new ArticleDto(
                        article.getId(),
                        article.getSource().name(),
                        article.getSourceUrl(),
                        article.getTitle(),
                        article.getPublishDate(),
                        article.getSummary(),
                        commentRepository.countByArticle_Id(article.getId()),
                        article.getViewCount(),
                        hasUserViewedArticle(userId, article.getId())
                ))
                .toList();

        log.info("[Article] 뉴스 기사 목록 조회 성공");

        return new CursorPageResponseArticleDto(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                articleRepositoryCustom.countArticles(keyword, interestId, sourceIn, publishedDateFrom, publishedDateTo),
                hasNext
        );
    }

    @Override
    public List<ArticleSource> readSource() {

        return List.of(ArticleSource.values());
    }

    @Override
    public void softDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.error("[Article] 논리 삭제 실패, 존재하지 않는 뉴스 기사, id = {}", articleId);
                    return ArticleNotFoundException.withId(articleId);
                });

        article.setDeletedAt(LocalDateTime.now());
        log.info("[Article] 논리 삭제 성공, articleId = {}, deletedAt = {}", articleId, article.getDeletedAt());

        applicationEventPublisher.publishEvent(new ArticleDeleteEvent(
            articleId
        ));
    }

    @Override
    public void hardDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.error("[Article] 물리 삭제 실패, 존재하지 않는 뉴스 기사, id = {}", articleId);
                    return ArticleNotFoundException.withId(articleId);
                });

        applicationEventPublisher.publishEvent(new ArticleDeleteEvent(
            articleId
        ));

        articleRepository.delete(article);
        log.info("[Article] 물리 삭제 성공");
    }

    // 공통 메서드
    public boolean hasUserViewedArticle(UUID userId, UUID articleId) {
        return userActivityRepository.findById(userId)
                .map(userActivity -> userActivity.getArticleViews().stream()
                        .anyMatch(view -> view.articleId().equals(articleId)))
                .orElse(false);
    }
}
