package com.mylinehub.crm.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import com.mylinehub.crm.rag.util.FlexibleMapDeserializer;
import com.mylinehub.crm.rag.util.FlexibleStringListDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiInterfaceOutputDto {

	@JsonDeserialize(using = FlexibleMapDeserializer.class)
	private Map<String, String> files; 
	private boolean customerAskingCreateCustomerSupportTicket; //Requirment-Set 1 property
	private String productOrServiceName;
	private String customerEmailId;
	private String complaint;
	private boolean customerAskingAboutPreviousCustomerSupportTicket; //Requirment-Set 1 property
	private boolean customerAskingAboutNewScheduleCall; //Requirment-Set 1 property
	private String scheduleDateTime;
	private boolean customerAskingAboutCurrentScheduleCall; //Requirment-Set 1 property
	private String typeOfProduct;
	private String intent;
    private boolean stopAIMessage;
    @JsonDeserialize(using = FlexibleStringListDeserializer.class)
    private List<String> llmResponse;
    
}
