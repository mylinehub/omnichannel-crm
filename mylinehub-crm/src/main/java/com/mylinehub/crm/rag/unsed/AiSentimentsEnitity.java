//package com.mylinehub.crm.rag.unsed;
//
//import lombok.*;
//import javax.persistence.*;
//
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.Instant;
//
//@Entity
//@Table(name = "aisentiment", indexes = {
//    @Index(name = "idx_aisentiment_chatHistoryId", columnList = "chatHistoryId"),
//    @Index(name = "idx_aisentiment_org", columnList = "organization")
//})
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class AiSentimentsEnitity{
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//    private Long chatHistoryId;
//    private String organization;
//    private String assistantName;
//    private String languageScript;
//    private String actualLanguage;
//    private String messageContext;
//    
//    private boolean callNow;
//    private boolean callSchedule;
// // Message Intent and Flags (Detected by LLM)
//    private boolean isGenericMessage;
//    private boolean isImageQuestion;
//    private boolean requiresVideoAnswer;
//    private boolean isPricingQuery;
//    private boolean wantsProductVideo;
//    private boolean wantsToTalkToAgent;
//    private boolean hasAskedFollowup;
//    private boolean hasAskedFutureUpdates;
//    private boolean isPersonalMessageToAnand;
//    private boolean isCategoryMessage;
//    private boolean isForgetPassword;
//    private boolean shouldRaiseTicket;
//    private boolean shouldBlockUser;
//    private boolean shouldConnectFurther;
//    private boolean shouldRedirect;
//    private boolean isFrustrated;
//    private boolean isLanguageAbsusive;
//    private boolean stopAIMessage;
//    private boolean doeschangeOfLanguageRequired;
//    
//    // Detected Human Behavior
//    private boolean isMultiPartMessage;
//    private boolean isHumanTypingPattern;
//    private boolean isCalculationRequired;
//    private boolean isFeedBackReqired;
//    private boolean doesItContainRequirment;
//    private boolean isSetOfRequirmentLooksComplete;
//    private boolean isCustomerAskingRefund;
//    private boolean isCustomerAskingReplacement;
//    private boolean isCustomerReferencingPreviousCustomerSupportTicket;
//    private boolean isCustomerAskingCreateCustomerSupportTicket;
//    //Tell them we store context of last 10 messages, ask them to resend previous context in case its not being answered now
//    private boolean isCustomerAskingReferPreviousMessage;
//    
//    @Column(updatable = false)
//	@CreationTimestamp
//	private Instant createdOn;
//	    
//	@UpdateTimestamp
//	private Instant lastUpdatedOn;
//}
