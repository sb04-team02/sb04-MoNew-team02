package com.sprint.team2.monew.domain.subscription.mapper;

import com.sprint.team2.monew.domain.interest.entity.Interest;
import com.sprint.team2.monew.domain.interest.mapper.InterestMapper;
import com.sprint.team2.monew.domain.subscription.dto.SubscriptionDto;
import com.sprint.team2.monew.domain.subscription.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptoinMapper {
    @Mapping(target = "interestId", expression = "java(subscription.getInterest().getId())")
    @Mapping(target = "interestName", expression = "java(subscription.getInterest().getName())")
    @Mapping(target = "interestKeywords", expression = "java(subscription.getInterest().getKeywords())")
    SubscriptionDto toDto(Subscription subscription);
}
