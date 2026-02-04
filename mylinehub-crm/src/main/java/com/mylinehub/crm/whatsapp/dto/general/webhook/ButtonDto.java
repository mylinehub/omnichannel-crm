package com.mylinehub.crm.whatsapp.dto.general.webhook;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ButtonDto {

	String type;
	String title;
	String id;
	String payload;
	String text;
}
