package com.sprint.team2.monew.domain.article.controller;

import com.sprint.team2.monew.domain.article.dto.response.CursorPageResponseArticleDto;
import com.sprint.team2.monew.domain.article.service.ArticleService;
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

    @GetMapping
    // TODO: ENUM 사용
    public ResponseEntity<CursorPageResponseArticleDto> getArticles(@RequestHeader("Monew-Request-User-ID") UUID userId,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) UUID interestId,
                                                                    @RequestParam(required = false) List<String> sourceIn,
                                                                    @RequestParam(required = false) LocalDateTime publishedDateFrom,
                                                                    @RequestParam(required = false) LocalDateTime publishedDateTo,
                                                                    @RequestParam(defaultValue = "publishDate") String orderBy,
                                                                    @RequestParam(defaultValue = "ASC") String direction,
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
}
