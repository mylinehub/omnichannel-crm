package com.mylinehub.voicebridge.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CrmPropertyInventoryDto {

    private Long id;

    private String pid;

    private String city;
    private String area;
    private String premiseName;

    private String unitNo;
    private String unitType;
    private Integer bhk;

    private Integer sqFt;

    private Integer washroom;
    private Integer balconies;

    private String floorNo;
    private Integer totalFloors;

    private String furnishedType;
    private String facing;

    private Integer propertyAge;
    private String propertyType;

    private Boolean rent;
    private Integer rentValue;

    private String tenant;

    private String nearby;

    private String callStatus;

    private String brokerage;

    private String propertyDescription1;
    private Boolean moreThanOneProperty;
    
}
