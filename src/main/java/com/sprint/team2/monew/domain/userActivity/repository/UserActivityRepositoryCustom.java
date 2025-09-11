package com.sprint.team2.monew.domain.userActivity.repository;

import com.mongodb.BasicDBObject;
import com.sprint.team2.monew.domain.userActivity.entity.UserActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/*
 * Article 에서 물리 삭제 시,
 * 해당 Article과 연관된 UserActivity의 articleView 배열에서 참조를 제거하기 위한 커스텀 레포지토리
 */

@Repository
@RequiredArgsConstructor
public class UserActivityRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public void deleteByArticleId(UUID articleId) {
        Query query = new Query(Criteria.where("articleViews.articleId").is(articleId));
        Update update = new Update().pull("articleViews", new BasicDBObject("articleId", articleId));
        mongoTemplate.updateMulti(query, update, UserActivity.class);
    }
}
