package com.sprint.team2.monew.domain.userActivity.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import com.sprint.team2.monew.domain.article.dto.response.ArticleViewDto;
import com.sprint.team2.monew.domain.comment.dto.response.CommentActivityDto;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityCancelDto;
import com.sprint.team2.monew.domain.userActivity.dto.CommentActivityLikeDto;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import com.sprint.team2.monew.domain.userActivity.exception.UserActivityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/*
 * Article과 연관된 UserActivity의 articleView 배열에서 참조를 제거하기 위한 커스텀 레포지토리
 */

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserActivityRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    /**
     * Subscription 관련
     */
    public void addSubscription(UUID userId, SubscriptionDto subscriptionDto) {
        Query query = new Query(Criteria.where("_id").is(userId)); //parent
        Update update = new Update()
            .push("subscriptions")
            .atPosition(0)
            .slice(10)
            .each(subscriptionDto);

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
    }

    public void cancelSubscription(UUID userId, UUID subscriptionId) {
        Query query = new Query(Criteria.where("_id").is(userId)); // parent document
        Update update = new Update()
            .pull("subscriptions", query(Criteria.where("id").is(subscriptionId)));

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
    }

    // 모든 유저의 정보도 업데이트
    public void deleteSubscription(UUID interestId) {
        Query query = new Query(Criteria.where("subscriptions.interestId").is(interestId));
        Update update = new Update()
            .pull("subscriptions", query(Criteria.where("interestId").is(interestId)));

        UpdateResult result = mongoTemplate.updateMulti(query, update, UserActivity.class);
        log.info("[사용자 활동] 전체 구독 삭제 완료 - interestId = {}. {}명의 사용자에게서 삭제됨.",
            interestId, result.getModifiedCount());
    }

    // 모든 유저의 정보도 업데이트
    public void updateSubscriptionKeyword(UUID interestId, List<String> keywords) {
        Query query = new Query(Criteria.where("subscriptions.interestId").is(interestId));
        Update update = new Update()
            .set("subscriptions.$.interestKeywords", keywords);

        UpdateResult result = mongoTemplate.updateMulti(query, update, UserActivity.class);
        if (result.getModifiedCount() == 0) {
            log.warn("[사용자 활동] 구독 키워드 수정 실패 - 매칭되는 interestId가 없습니다. interestId = {}", interestId);
        }
    }

    /**
     * Comment 관련
     */
    public void addComment(CommentActivityDto commentActivityDto) {
        UUID userId = commentActivityDto.userId();
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update()
            .push("comments")
            .atPosition(0)
            .slice(10)
            .each(commentActivityDto);

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
    }

    public void updateComment(CommentActivityDto commentActivityDto) {
        UUID userId = commentActivityDto.userId();
        UUID commentId = commentActivityDto.id();
        Query query = new Query(Criteria.where("_id").is(userId)
            .and("comments.id").is(commentId));
        Update update = new Update()
            .set("comments.$", commentActivityDto);

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
    }

    public void deleteComment(CommentActivityDto commentActivityDto) {
        UUID userId = commentActivityDto.userId();
        UUID commentId = commentActivityDto.id();
        Query query = new Query(Criteria.where("_id").is(userId)); // parent document
        Update update = new Update() // for comments array (child)
            .pull("comments", query(Criteria.where("id").is(commentId)));

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
        if (result.getModifiedCount() == 0) {
            log.warn("[사용자 활동] 삭제할 댓글 ID {}를 활동 내역에서 찾지 못했습니다.", commentId);
        }
    }

    public void addCommentLike(CommentActivityLikeDto commentActivityDto) {
        UUID userId = commentActivityDto.commentUserId();
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update()
            .push("commentLikes")
            .atPosition(0)
            .slice(10)
            .each(commentActivityDto);

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
    }

    public void cancelCommentLike(CommentActivityCancelDto commentActivityDto) {
        UUID userId = commentActivityDto.commentUserId();
        UUID commentId = commentActivityDto.id();
        Query query = new Query(Criteria.where("_id").is(userId)); // parent document
        Update update = new Update() // for comments array (child)
            .pull("commentLikes", query(Criteria.where("id").is(commentId)));

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
        if (result.getModifiedCount() == 0) {
            log.warn("[사용자 활동] 삭제할 댓글 ID {}를 활동 내역에서 찾지 못했습니다.", commentId);
        }
    }

    /**
     * Article 관련
     */
    public ArticleViewDto findByArticleId(UUID userId, UUID articleId) {
        Query query = new Query(Criteria.where("_id").is(userId)
            .and("articleViews.articleId").is(articleId));
        query.fields().include("articleViews.$");
        UserActivity userActivity = mongoTemplate.findOne(query, UserActivity.class);
        if (userActivity != null && !userActivity.getArticleViews().isEmpty()) {
            return userActivity.getArticleViews().get(0);
        }
        return null; // Or throw an exception
    }

    public void addArticleView(ArticleViewDto articleViewDto) {
        UUID userId = articleViewDto.viewedBy();
        Query query = new Query(Criteria.where("_id").is(userId));
        Update update = new Update()
            .push("articleViews")
            .atPosition(0)
            .slice(10)
            .each(articleViewDto);

        UpdateResult result = mongoTemplate.updateFirst(query, update, UserActivity.class);
        if (result.getMatchedCount() == 0) {
            throw UserActivityNotFoundException.withId(userId);
        }
    }

    public boolean existsByArticleId(UUID articleId) {
        Query query = new Query(where("articleViews.articleId").is(articleId));
        return mongoTemplate.exists(query, UserActivity.class);
    }

    public void deleteByArticleId(UUID articleId) {
        Query query = new Query(where("articleViews.articleId").is(articleId));
        Update update = new Update().pull("articleViews", query(where("articleId").is(articleId)));
        mongoTemplate.updateMulti(query, update, UserActivity.class);
    }

}
