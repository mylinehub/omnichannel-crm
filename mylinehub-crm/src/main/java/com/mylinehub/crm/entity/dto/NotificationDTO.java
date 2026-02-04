package com.mylinehub.crm.entity.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDTO {
	public Long id;
	public String forExtension;
	public String organization;
	public String alertType;
	public String notificationType;
	public String title;
	public String message;
	public Date creationDate;
	public boolean isDeleted;
}
