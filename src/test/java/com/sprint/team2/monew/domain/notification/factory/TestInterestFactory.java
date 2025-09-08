package com.sprint.team2.monew.domain.notification.factory;

import com.sprint.team2.monew.domain.interest.entity.Interest;

import java.util.List;

public class TestInterestFactory {
    public static Interest createInterest() {
        Interest interest = new Interest();
        interest.setName("경제");
        interest.setKeywords(List.of("환율", "물가"));
        return interest;
    }
}
