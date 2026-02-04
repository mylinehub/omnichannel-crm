package com.mylinehub.voicebridge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmPropertyInventoryUpdateDto {
	
	//Section-1 (Premise)
    private String premiseName;
    private String city;
    private String nearby;
    
    //Purpose can be sale , rent etc ...
    private String purpose;
    
	//Depicts House parameters
    //Whats is the area of property ?
    private String area;
    //How many balconies in this property ?
    private Integer balconies;
    //Ask Number of rooms ?
    private Integer bhk;
    //One side / Two Side / Three side facing
    private String facing;
    //Total floors avaiable for rent
    private String floorNo;
    //Furnished / Semi Furnished etc.
    private String furnishedType;
    // How old is property ?
    private Integer propertyAge;
    // Enter property decripiotn as per communication with user. This will be fetched after LLM transcription in seperate call Ausio hould not focus on this.
    private String propertyDescription1;
    //What is property type
    private String propertyType;
    //Area in number if property ?
    private Integer sqFt;
    //Number of tenant it can occupy ?
    private String tenant;
    //Total floors
    private Integer totalFloors;
    private String unitNo;
    private String unitType;
    //Number of washroom in property
    private Integer washroom;
    
    //Monetary
    private Boolean rent;
    //What does user expects about the rent value
    private Integer rentValue;
    
    //What is expected brokrage , what are deal terms , mention here.
    private String brokerage;

    private Boolean available;
    
    // Does user has more than one property to do business. Lets ark it on that interpretation.. Do not tell user anything just hear from him.. Gaian this will be fille dlater by anither completion. Initial audio prompt should not worry on this.
    private Boolean moreThanOneProperty;
    
    //Below date is not to be filled by LLM. It will automatically be filled by java code.
    private Instant listedDate;
}
