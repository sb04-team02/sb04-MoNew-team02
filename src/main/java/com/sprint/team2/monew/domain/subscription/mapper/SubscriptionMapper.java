package com.sprint.team2.monew.domain.subscription.mapper;

import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "interestId", expression = "java(subscription.getInterest().getId())")
    @Mapping(target = "interestName", expression = "java(subscription.getInterest().getName())")
    @Mapping(target = "interestKeywords", expression = "java(subscription.getInterest().getKeywords())")
    @Mapping(target = "interestSubscriberCount", expression = "java(subscription.getInterest().getSubscriberCount())")
    SubscriptionDto toDto(Subscription subscription);
}
