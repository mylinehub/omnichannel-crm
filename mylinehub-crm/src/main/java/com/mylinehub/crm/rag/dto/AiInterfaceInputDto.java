package com.mylinehub.crm.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mylinehub.crm.entity.Customers;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiInterfaceInputDto {

	private String customerOriginalMessageInput;
    private String customerConvertedMessageInput;
    private List<String> messageResponseHistoryFromUser;
    private List<String> messageResponseHistoryFromLLM;
    private List<String> ragResponse;
    private boolean allTimeFirstMessage;
    private boolean sessionFirstMessage;
    private Customers customer;
    private String calculationLogic;
    private String videoLinkData;
    private String customerEmail;

}
