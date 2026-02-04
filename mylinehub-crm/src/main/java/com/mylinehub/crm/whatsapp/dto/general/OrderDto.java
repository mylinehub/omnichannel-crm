package com.mylinehub.crm.whatsapp.dto.general;

import java.util.List;

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
public class OrderDto {
	String catalog_id;
	String text;
	List<ProductItemDto> product_items;
}
