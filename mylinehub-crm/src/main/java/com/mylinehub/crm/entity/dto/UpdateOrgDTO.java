package com.mylinehub.crm.entity.dto;

import java.util.List;

import com.mylinehub.crm.entity.Organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrgDTO {
		public Organization updates;
		public String organization;
		public MenuDto menuDto;
		public String businessId;
        public String token;
        public String firstName;
        public String lastName;
        public String phoneNumber;
        public String pan;
        public String email;
        public String address;
        public String natureOfBusiness;
        public Boolean updateMenu;
        public List<String> notificationsToHit;
        public String phonePrefix;
        public String gstin_status;
        public String legal_name;
        public String gstin;
        public String business_name;
        public String domain;
        
        private String phoneContext;
        private String costCalculation;
        private Double callingTotalAmountLoaded;
        private Integer callLimit;
        private Double totalWhatsAppMessagesAmount;
        private Integer whatsAppMessageLimit;
        private Boolean allowWhatsAppAutoAIMessage;
        private Boolean allowWhatsAppAutoMessage;
        private Boolean allowWhatsAppCampaignMessage;
        private Boolean ragSet;
        private String trunkNamesPrimary;
        private String trunkNamesSecondary;
        private Boolean useSecondaryAllotedLine;
        private String secondDomain;
        private String phoneTrunk;
        private String menuAccess;
        private Double allowedUploadInMB;
        private Double currentUploadInMB;;
        private Integer allowedEmbeddingConversion;
        private Integer consumedEmbeddingConversion;
        private Integer allowedUsers;
        private Boolean enableFileUpload;
        private Boolean enableEmployeeCreation;
        private Boolean enableCalling;
        private Boolean enableInternalMessaging;
        private Boolean enableWhatsAppMessaging;
        private String protocol;
        private Integer sipPort;
        private String sipPath;
        private String priLineType;
        private List<String> ariApplication;
        private List<String> ariApplicationDomain;
        private List<String> ariApplicationPort;
        private Integer aiCallChargeAmount;
        private String aiCallChargeType;
        private Boolean activated;
}
