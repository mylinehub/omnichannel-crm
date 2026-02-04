package com.mylinehub.crm.whatsapp.dto.general.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.contact.UrlDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferralDto {

	String source_url;
	String source_type;
	String source_id;
	String headline;
	String body;
	String media_type;
	String image_url;
	String video_url;
	String thumbnail_url;
	String ctwa_clid;
	WelcomeMessageDto welcome_message;
}
