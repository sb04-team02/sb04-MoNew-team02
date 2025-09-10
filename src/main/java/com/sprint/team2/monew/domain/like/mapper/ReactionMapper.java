package com.sprint.team2.monew.domain.like.mapper;

import com.sprint.team2.monew.domain.like.dto.CommentLikeDto;
import com.sprint.team2.monew.domain.like.dto.response.CommentLikeActivityDto;
import com.sprint.team2.monew.domain.like.entity.Reaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReactionMapper {
    @Mapping(target = "likedBy", source = "reaction.user.id")
    @Mapping(target = "commentId", source = "reaction.comment.id")
    @Mapping(target = "articleId", source = "reaction.comment.article.id")
    @Mapping(target = "commentUserId", source = "reaction.comment.user.id")
    @Mapping(target = "commentUserNickname", source = "reaction.comment.user.nickname")
    @Mapping(target = "commentContent", source = "reaction.comment.content")
    @Mapping(target = "commentLikeCount", source = "reaction.comment.likeCount")
    @Mapping(target = "commentCreatedAt", source = "reaction.comment.createdAt")
    CommentLikeDto toDto(Reaction reaction);

    @Mapping(target = "likedBy",            source = "reaction.user.id")
    @Mapping(target = "commentId",          source = "reaction.comment.id")
    @Mapping(target = "articleId",          source = "reaction.comment.article.id")
    @Mapping(target = "commentUserId",      source = "reaction.comment.user.id")
    @Mapping(target = "commentUserNickname",source = "reaction.comment.user.nickname")
    @Mapping(target = "commentContent",     source = "reaction.comment.content")
    @Mapping(target = "commentLikeCount",   source = "updatedLikeCount")
    @Mapping(target = "commentCreatedAt",   source = "reaction.comment.createdAt")
    CommentLikeDto toDto(Reaction reaction, long updatedLikeCount);
}
