package com.mylinehub.crm.entity.dto;

import java.util.Date;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private Double salary;
    private Long departmentId;
    
    private String email;
    private String departmentName;
    private String phoneContext;
    private String phoneTrunk;
    private String organization;
    private String extensionpassword;
    private String protocol;
    private String domain;
    private String secondDomain;
    private String extensionPrefix;
    private String extension;
    private String confExtensionPrefix;
    private String confExtension;  
    private int totalparkedchannels;
    private String parkedchannel1;
    private String parkedchannel2;
    private String parkedchannel3;
    private String parkedchannel4;
    private String parkedchannel5;
    private String parkedchannel6;
    private String parkedchannel7;
    private String parkedchannel8;
    private String parkedchannel9;
    private String parkedchannel10;
    public String timezone;
    private String type;
    private boolean callonnumber;
    private boolean useSecondaryAllotedLine;
    private Boolean isEnabled;
    private Date birthdate;
    private String password;
    private String phonenumber;
    private String provider1;
    private String allotednumber1;
    private String provider2;
    private String allotednumber2;
    private String transfer_phone_1;
    private String transfer_phone_2;
    private String costCalculation;
    private String amount;
    
    private String imageName;
    private String imageType;
    private String imageData;
    private String iconImageData;
    private byte[] iconImageByteData;
    
    private String doc1ImageType;
    private String governmentDocument1Data;
    private String governmentDocumentID1;
    
    private String doc2ImageType;
    private String governmentDocument2Data;
    private String governmentDocumentID2;
    
    private Long sizeMediaUploadInMB;
    
    private String pesel;
    private String sex;
    private Boolean isLocked ;
    
    private String uiTheme;
    private boolean autoAnswer; 
    private boolean autoConference; 
    private boolean autoVideo; 
    private String micDevice; 
    private String speakerDevice; 
    private String videoDevice; 
    private String videoOrientation; 
    private String videoQuality; 
    private String videoFrameRate; 
    private String autoGainControl; 
    private String echoCancellation;
    private String noiseSupression; 
    private int sipPort;
    private String sipPath;
    private boolean recordAllCalls;
    private boolean doNotDisturb;
    private boolean startVideoFullScreen;
    private boolean callWaiting;
    private boolean intercomPolicy;
    private boolean freeDialOption;
    private boolean textDictateOption;
    private boolean textMessagingOption; 
    private boolean notificationDot; 
    public String lastConnectedCustomerPhone;
    private boolean allowedToSwitchOffWhatsAppAI;
}
