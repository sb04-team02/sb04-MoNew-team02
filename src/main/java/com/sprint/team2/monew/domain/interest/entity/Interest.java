package com.sprint.team2.monew.domain.interest.entity;

import com.sprint.team2.monew.domain.base.UpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "interests")
@NoArgsConstructor
@AllArgsConstructor
public class Interest extends UpdatableEntity {
    @Column(name = "name", length = 50, nullable = false)
    private String name;
    @Size(min = 1, max = 10)
    @Column(nullable = false, name = "keywords")
    private List<String> keywords;
    @Column(name="subscription_count")
    private long subscriberCount;

    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords = keywords;
        this.subscriberCount = 0;
    }

    public void increaseSubscriber() {
        this.subscriberCount++;
        log.debug("[관심사] 구독자 증가");
    }

    public void decreaseSubscriber() {
        this.subscriberCount--;
        log.debug("[관심사] 구독자 감소");
    }
}
