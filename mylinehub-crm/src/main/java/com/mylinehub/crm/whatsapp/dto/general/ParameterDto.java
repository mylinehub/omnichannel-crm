package com.mylinehub.crm.whatsapp.dto.general;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.whatsapp.dto.general.webhook.ButtonDto;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterDto {

	String id;
	String type;
	String parameter_name;
	
	//type 'text'
	TextDto text;
	//type 'currency'
	CurrencyDto currency;
	//type 'datetime'
    DateTimeDto dateTime;
    //type 'any kind of media'
	MediaDto media;
	
	//Extra parameters	
	String languageCode;
	String sub_type;
	int index;

}
