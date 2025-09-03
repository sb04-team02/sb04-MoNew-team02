package com.sprint.team2.monew.domain.subscription.entity;

import com.sprint.team2.monew.domain.base.UpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends UpdatableEntity {
}
