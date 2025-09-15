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
import com.sprint.team2.monew.domain.user.entity.User;
import com.sprint.team2.monew.domain.user.exception.UserNotFoundException;
import com.sprint.team2.monew.domain.user.repository.UserRepository;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
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
import java.util.ArrayList;
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
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.error("[Article] 존재하지 않는 뉴스 기사, articleId = {}", articleId);
                    return ArticleNotFoundException.withId(articleId);
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[User] 존재하지 않는 사용자, userId = {}", userId);
                    return UserNotFoundException.withId(userId);
                });

        UserActivity userActivity = userActivityRepository.findById(userId)
                .map(activity -> {
                    if (activity.getArticleViews() == null) activity.setArticleViews(new ArrayList<>());
                    if (activity.getComments() == null) activity.setComments(new ArrayList<>());
                    if (activity.getCommentLikes() == null) activity.setCommentLikes(new ArrayList<>());
                    if (activity.getSubscriptions() == null) activity.setSubscriptions(new ArrayList<>());
                    return activity;
                })
                .orElseGet(() -> {
                    log.info("[UserActivity] 활동내역이 없어서 새로 생성, userId = {}", userId);
                    UserActivity newActivity = new UserActivity(user.getId(), user.getEmail(), user.getNickname());
                    return userActivityRepository.save(newActivity);
                });

        boolean alreadyViewed = userActivityRepositoryCustom.existsByArticleId(articleId);

        ArticleViewDto dto;
        if (!alreadyViewed) {
            dto = new ArticleViewDto(
                    UUID.randomUUID(),
                    userId,
                    LocalDateTime.now(),
                    article.getId(),
                    article.getSource().name(),
                    article.getSourceUrl(),
                    article.getTitle(),
                    article.getPublishDate(),
                    article.getSummary(),
                    0L,
                    article.getViewCount() + 1
            );

            userActivity.getArticleViews().add(dto);
            userActivityRepository.save(userActivity);

            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
        } else {
            dto = userActivity.getArticleViews().stream()
                    .filter(articleView -> articleView.articleId().equals(articleId))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.error("[UserActivity] 조회 기록 없음, userActivityId = {}", userActivity.getId());
                        return ArticleNotFoundException.withId(userActivity.getId());
                    });
        }

        long commentCount = commentRepository.countByArticleId(articleId);

        return new ArticleViewDto(
                UUID.randomUUID(),
                userId,
                dto.createdAt(),
                article.getId(),
                article.getSource().name(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                commentCount,
                article.getViewCount()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseArticleDto read(UUID userId, ArticleOrderBy orderBy, ArticleDirection direction, int limit,
                                             String keyword,
                                             UUID interestId, List<ArticleSource> sourceIn, LocalDateTime publishedDateFrom, LocalDateTime publishedDateTo,
                                             String cursor, LocalDateTime after) {

        if (limit <= 0) {
            log.error("[Article] 커서 페이지 크기는 0보다 커야 함, limit = {}", limit);
            throw InvalidParameterException.invalidParameter();
        }

        if (orderBy != ArticleOrderBy.publishDate &&
                orderBy != ArticleOrderBy.commentCount &&
                orderBy != ArticleOrderBy.viewCount) {
            log.error("[Article] 정렬 속성에는 publishDate, commentCount, viewCount만 가능, 정렬 속성 = {}", orderBy);
            throw InvalidParameterException.invalidParameter();
        }

        List<Article> articles = articleRepositoryCustom.searchArticles(
                keyword, interestId, sourceIn, publishedDateFrom, publishedDateTo,
                orderBy, direction,
                cursor, after, limit
        );

        boolean hasNext = articles.size() > limit;
        if (hasNext) {
            articles = articles.subList(0, limit);
        }

        log.debug("[Article] 조회한 결과 조회, userId = {}, keyword = {}, interestId = {}, size = {}, hasNext = {}, nextCursor = {}",
                userId, keyword, interestId, articles.size(), hasNext,
                !articles.isEmpty() ? (orderBy == ArticleOrderBy.publishDate ? articles.get(articles.size() - 1).getPublishDate() : articles.get(articles.size() - 1).getCommentCount()) : null);

        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (!articles.isEmpty()) {
            Article last = articles.get(articles.size() - 1);
            switch (orderBy) {
                case commentCount -> nextCursor = String.valueOf(last.getCommentCount());
                case viewCount -> nextCursor = String.valueOf(last.getViewCount());
                default -> nextCursor = last.getPublishDate().toString();
            }

            nextAfter = last.getCreatedAt();
        }

        long totalElements = articleRepositoryCustom.countArticles(
                keyword, interestId, sourceIn, publishedDateFrom, publishedDateTo
        );

        return new CursorPageResponseArticleDto(
                articles.stream().map(articleMapper::toArticleDto).toList(),
                nextCursor,
                nextAfter,
                articles.size(),
                totalElements,
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
    }

    @Override
    public void hardDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> {
                    log.error("[Article] 물리 삭제 실패, 존재하지 않는 뉴스 기사, id = {}", articleId);
                    return ArticleNotFoundException.withId(articleId);
                });

        userActivityRepositoryCustom.deleteByArticleId(articleId);

        articleRepository.delete(article);
        log.info("[Article] 물리 삭제 성공");
    }

}
