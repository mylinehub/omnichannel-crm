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
public class DateTimeDto {

	String fallback_value;
    String day_of_week;
    String year;
    String month;
    String day_of_month;
    String hour;
    String minute;
    String calendar;
}
