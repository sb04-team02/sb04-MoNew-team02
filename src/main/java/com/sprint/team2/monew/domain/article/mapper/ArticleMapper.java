package com.sprint.team2.monew.domain.article.mapper;

import com.sprint.team2.monew.domain.article.dto.response.ArticleDto;
import com.sprint.team2.monew.domain.article.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(target = "viewedByMe", constant = "false")
    ArticleDto toArticleDto(Article article);

    @Mapping(target = "id", expression = "java(UUID.randomUUID())")
    @Mapping(target = "source", constant = "NAVER")
    @Mapping(target = "sourceUrl", source = "link")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "publishDate", expression = "java(LocalDateTime.now())")
    @Mapping(target = "summary", source = "description")
    @Mapping(target = "commentCount", constant = "0L")
    @Mapping(target = "viewCount", constant = "0L")
    @Mapping(target = "viewedByMe", constant = "false")
    ArticleDto toArticleDto(Map<String, String> items);

    @Mapping(target = "interest", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Article toEntity(ArticleDto articleDto);
}
