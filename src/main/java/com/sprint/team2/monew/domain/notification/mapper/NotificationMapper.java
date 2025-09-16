package com.sprint.team2.monew.domain.notification.mapper;

import com.sprint.team2.monew.domain.notification.dto.response.NotificationDto;
import com.sprint.team2.monew.domain.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    
    NotificationDto toNotificationDto(Notification entity);
}
