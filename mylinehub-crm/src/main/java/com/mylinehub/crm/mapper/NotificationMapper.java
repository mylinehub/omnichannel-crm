package com.mylinehub.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.dto.NotificationDTO;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "forExtension", source = "forExtension")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "alertType", source = "alertType")
	@Mapping(target = "notificationType", source = "notificationType")
	@Mapping(target = "title", source = "title")
	@Mapping(target = "message", source = "message")
	@Mapping(target = "creationDate", source = "creationDate")
	@Mapping(target = "isDeleted", source = "isDeleted")
	NotificationDTO mapNotificationToDTO(Notification notification);
	
	@Mapping(target = "forExtension", source = "forExtension")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "alertType", source = "alertType")
	@Mapping(target = "notificationType", source = "notificationType")
	@Mapping(target = "title", source = "title")
	@Mapping(target = "message", source = "message")
	@Mapping(target = "creationDate", source = "creationDate")
	@Mapping(target = "isDeleted", source = "isDeleted")
	Notification mapDTOToNotification(NotificationDTO notificationDTO);
	
}
