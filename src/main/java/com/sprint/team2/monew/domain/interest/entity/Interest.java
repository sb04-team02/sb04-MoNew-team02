package com.sprint.team2.monew.domain.interest.entity;

import com.sprint.team2.monew.domain.base.UpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

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
    }

    public void decreaseSubscriber() {
        this.subscriberCount--;
    }
}
