package com.sprint.team2.monew.domain.article.controller;

import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.article.dto.response.ArticleRestoreResultDto;
import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.service.ArticleStorageService;
import com.sprint.team2.monew.domain.article.entity.ArticleDirection;
import com.sprint.team2.monew.domain.article.entity.ArticleOrderBy;
import com.sprint.team2.monew.domain.article.entity.ArticleSource;
import com.sprint.team2.monew.domain.article.service.ArticleService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleStorageService articleStorageService;

    @PostMapping("{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> postArticleView(@RequestHeader("Monew-Request-User-ID") UUID userId,
                                                          @PathVariable UUID articleId) {
        ArticleViewDto result = articleService.view(userId, articleId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponseArticleDto> getArticles(@RequestHeader("Monew-Request-User-ID") UUID userId,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) UUID interestId,
                                                                    @RequestParam(required = false) List<ArticleSource> sourceIn,
                                                                    @RequestParam(required = false) LocalDateTime publishedDateFrom,
                                                                    @RequestParam(required = false) LocalDateTime publishedDateTo,
                                                                    @RequestParam(defaultValue = "publishDate") ArticleOrderBy orderBy,
                                                                    @RequestParam(defaultValue = "ASC") ArticleDirection direction,
                                                                    @RequestParam(required = false) String cursor,
                                                                    @RequestParam(required = false) LocalDateTime after,
                                                                    @RequestParam(defaultValue = "50") int limit) {
        CursorPageResponseArticleDto result = articleService.read(
                userId, orderBy, direction, limit,
                keyword,
                interestId, sourceIn, publishedDateFrom, publishedDateTo,
                cursor, after
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/sources")
    public ResponseEntity<List<ArticleSource>> getSources() {
        List<ArticleSource> result = articleService.readSource();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/restore")
    public ResponseEntity<List<ArticleRestoreResultDto>> restoreArticles(@RequestParam("from") LocalDateTime from,
                                                                   @RequestParam("to") LocalDateTime to
    ) {
        LocalDate fromDate = from.toLocalDate();
        LocalDate toDate = to.toLocalDate();

        ArticleRestoreResultDto articleRestoreResultDto = articleStorageService.restoreArticle(fromDate, toDate);
        if (articleRestoreResultDto.restoredArticleCount() == 0) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(List.of(articleRestoreResultDto));
    }


    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("articleId") UUID articleId) {
        articleService.softDelete(articleId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{articleId}/hard")
    public ResponseEntity<Void> deleteHardArticle(@PathVariable("articleId") UUID articleId) {
        articleService.hardDelete(articleId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}