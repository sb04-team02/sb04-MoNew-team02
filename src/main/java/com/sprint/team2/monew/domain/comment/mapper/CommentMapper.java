package com.sprint.team2.monew.domain.comment.mapper;

import com.sprint.team2.monew.domain.comment.dto.CommentDto;
import com.sprint.team2.monew.domain.comment.dto.request.CommentRegisterRequest;
import com.sprint.team2.monew.domain.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    // Entity -> DTO
    @Mapping(target = "articleId", source = "comment.article.id")
    @Mapping(target = "userId", source = "comment.user.id")
    @Mapping(target = "userNickname", source = "comment.user.nickname")
    @Mapping(target = "likedByMe",   source = "likedByMe")
    CommentDto toDto(Comment comment, boolean likedByMe);

    // 기본값: likedByMe = false
    default CommentDto toDto(Comment comment) {
        return toDto(comment, false);
    }

    // 등록 요청 -> 엔티티
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "likeCount", ignore = true) // 엔티티 기본값(0L) 사용
    Comment toEntity(CommentRegisterRequest request);
}
