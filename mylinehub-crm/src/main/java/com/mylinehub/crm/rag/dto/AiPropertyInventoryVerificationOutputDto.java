package com.mylinehub.crm.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import com.mylinehub.crm.rag.util.FlexibleMapDeserializer;
import com.mylinehub.crm.rag.util.FlexibleStringListDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiPropertyInventoryVerificationOutputDto {

	//Store this complete output in english
	//All if available are already in input 'customer'. It iss json having lot of cutomer details. 
	//And inside it propertyInventory. This bot is to fetch only below or verify already exiitng information for below
    //As per information being by customer keep filling below.. You get it everytime with new compeletion request
	//So your input from user define whats already filled and whats pending 
	//Langauge rules for below are english only... Fill below json in english 
	private String purpose;
    private String premiseName;
    private String propertyType;
    private boolean rent;
    private Long rentValue;
    private Integer bhk;
    private String furnishedType;
    private Integer sqFt;
    private String city;
    private String nearby;
    private String area;
    private Integer propertyAge;
    private String unitType;
    private String tenant;
    private String facing;
    private Integer totalFloors;
    private String brokerage;
    private Integer balconies;
    private Integer washroom;
    private String unitNo;
    private String floorNo;
    private String propertyDescription;
    private Boolean available;
    private Boolean moreThanOneProperty;
    
    
    //Langauge rules for below are english only... Fill below json in english 
    //If not avaiable in put , not valid , or else phone number , ask for name. do not ask firstname and last anme separately
    //LLM while generating value in json should bifurcate name into firstname and last name.
    //Ask only if we do not have valid in inout customer information. No need to confirm in case it valid
    private String customerFirstName;
    //Langauge rules for below are english only... Fill below json in english 
    //Ask only if we do not have valid in inout customer information. Never ask for last Name.
    private String customerLastName;
    
    //Verfy important variable. If input is complete and uer says he he told everything. Do not continue.
    //If we have doubt and user is skeptical / creas confusion.
    //In this case mark it false.
    //Mark it true only when use is clear to stop , 100% confirmation --> single word like stop does not mean stop. It should be more than 3 words in customerConvertedMessageInput in input user prompt
    private Boolean shouldAIExitBecauseInformationIsCompleteOrVerifiedFull;
    
    
    //Langauge siwse this is very important .. It should be in same langauge as per customerOriginalMessageInput , in inout prompt from user. It is strict.
    //At maximum produce at max only 2 messages, not more than thats. Its  scrict rule.. one what's done(what info we have) .. one whats pending (whats not filled) as per output json  /// If everyghing is filled just one message  (to verify) // Or else one messgae saying Listing will be prepared and team will get in touch shortly. 
    //If you see from previous chats of user that user have already told and confirmation something do not include it in next verification list
    //messageResponseHistoryFromUser tell what user responded previousy. It's a list keep checking that
    //Out from LLM will be multiple string, One having information about what's already told and need confirmation
    //Second what is completely blank and need new input
    //For already known tell customer that we already know it .. But need your confirmation. 
    @JsonDeserialize(using = FlexibleStringListDeserializer.class)
    private List<String> llmResponse;
    
}
