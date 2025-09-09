package com.sprint.team2.monew.domain.interest.entity;

import com.sprint.team2.monew.domain.base.UpdatableEntity;
import com.sprint.team2.monew.domain.interest.dto.request.InterestRegisterRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.Objects;

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
    @Column(name = "keywords", columnDefinition = "VARCHAR(20)[]")
    private List<String> keywords;
    @Column(name="subscription_count")
    private long subscriberCount;

    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords = keywords;
        this.subscriberCount = 0;
    }

    public Interest(InterestRegisterRequest interestRegisterRequest) {
        this.name = interestRegisterRequest.name();
        this.keywords = interestRegisterRequest.keywords();
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Interest interest)) return false;
        return Objects.equals(getId(), interest.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
