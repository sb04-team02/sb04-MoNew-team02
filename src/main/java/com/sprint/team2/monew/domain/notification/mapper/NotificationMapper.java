package com.sprint.team2.monew.domain.notification.mapper;

import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "id", target = "notificationId")
    NotificationDto toNotificationDto(Notification entity);
}
