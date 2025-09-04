package com.sprint.team2.monew.domain.comment.mapper;

import com.sprint.team2.monew.domain.article.entity.Article;
import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import com.sprint.team2.monew.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    // likedByMe 여부를 서비스에서 계산해 함께 주입
    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "articleId", source = "comment.article.id")
    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "userNickname", source = "comment.user.nickname")
    @Mapping(target = "content", source = "comment.content")
    @Mapping(target = "likeCount", source = "comment.likeCount")
    @Mapping(target = "likedByMe", expression = "java(likedByMe)")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    CommentDto toDto(Comment comment, boolean likedByMe);

    // 기본값: likedByMe = false
    default CommentDto toDto(Comment comment) {
        return toDto(comment, false);
    }

    // 등록 요청 -> 엔티티 (연관관계는 서비스에서 세팅)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "content", source = "content")
    @Mapping(target = "likeCount", ignore = true) // 엔티티 기본값(0L) 사용
    Comment toEntity(CommentRegisterRequest request);
}
