package com.sprint.team2.monew.domain.interest.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.team2.monew.domain.interest.dto.request.CursorPageRequestInterestDto;
import com.sprint.team2.monew.domain.interest.dto.response.InterestQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sprint.team2.monew.domain.interest.entity.QInterest.interest;
import static com.sprint.team2.monew.domain.subscription.entity.QSubscription.subscription;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public Page<InterestQueryDto> findAllPage(CursorPageRequestInterestDto request, UUID userId) {
        OrderSpecifier[] orderSpecifiers = createOrderSpecifier(request.orderBy(), request.direction());

        JPAQuery query = jpaQueryFactory
                .select(
                        Projections.constructor(
                                InterestQueryDto.class,
                                interest.id.as("id"),
                                interest.name.as("name"),
                                interest.keywords.as("keywords"),
                                interest.subscriberCount.as("subscriberCount"),
                                subscription.user.id.eq(userId).as("subscribedByMe"),
                                interest.createdAt.as("createdAt")
                        )
                )
                .from(interest)
                .leftJoin(subscription).on(subscription.interest.id.eq(interest.id))
                .where(partialMatch(request.keyword()))
                .orderBy(orderSpecifiers)
                .limit(request.limit());

        if (StringUtils.hasText(request.cursor())) {
            query.where(createCursorAfter(request.orderBy(), request.direction(), request.cursor(), request.after()));
        }
        List<InterestQueryDto> content = query.fetch();

        Long totalElements = jpaQueryFactory.select(interest.count())
                .from(interest)
                .where(partialMatch(request.keyword()))
                .fetchFirst();

        return new PageImpl<>(content, PageRequest.of(0,request.limit(), Sort.Direction.valueOf(request.direction()),request.orderBy()), totalElements);
    }

    private OrderSpecifier[] createOrderSpecifier(String orderBy, String direction) {
        List<OrderSpecifier> orderSpecifier = new ArrayList<>();
        Order directionOrder = direction.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC;

        if (orderBy.equalsIgnoreCase("subscriberCount")) {
            orderSpecifier.add(new OrderSpecifier(directionOrder, interest.subscriberCount));
        } else {
            orderSpecifier.add(new OrderSpecifier(directionOrder, interest.name));
        }
        orderSpecifier.add(new OrderSpecifier(directionOrder, interest.createdAt));
        return orderSpecifier.toArray(new OrderSpecifier[orderSpecifier.size()]);
    }

    private BooleanBuilder createCursorAfter(String orderBy, String direction, String cursor, LocalDateTime after) {
        BooleanBuilder predicate = new BooleanBuilder();
        Order directionOrder = direction.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC;

        if (orderBy.equalsIgnoreCase("name")) {
            if (directionOrder == Order.ASC) {
                predicate.and(
                        interest.name.gt(cursor)
                                .or(StringUtils.hasText(String.valueOf(after))
                                        ? interest.name.eq(cursor).and(interest.createdAt.gt(after))
                                        : interest.name.eq(cursor))
                );
            } else {
                predicate.and(
                        interest.name.lt(cursor)
                                .or(StringUtils.hasText(String.valueOf(after))
                                        ? interest.name.eq(cursor).and(interest.createdAt.lt(after))
                                        : interest.name.eq(cursor))
                );
            }
        } else {  // subscriberCount
            long cursorValue = Long.parseLong(cursor);
            if (directionOrder == Order.ASC) {
                predicate.and(
                        interest.subscriberCount.gt(cursorValue)
                                .or(after != null
                                        ? interest.subscriberCount.eq(cursorValue).and(interest.createdAt.gt(after))
                                        : interest.subscriberCount.eq(cursorValue))
                );
            } else {
                predicate.and(
                        interest.subscriberCount.lt(cursorValue)
                                .or(after != null
                                        ? interest.subscriberCount.eq(cursorValue).and(interest.createdAt.lt(after))
                                        : interest.subscriberCount.eq(cursorValue))
                );
            }
        }

        return predicate;
    }

    // 부분일치 검색
    private BooleanBuilder partialMatch(String keyword) {
        BooleanBuilder predicate = new BooleanBuilder();

        predicate.or(interest.name.containsIgnoreCase(keyword));
        predicate.or(Expressions.stringTemplate(
                "array_to_string({0}, ',')", interest.keywords
        ).containsIgnoreCase(keyword));
        return predicate;
    }
}
