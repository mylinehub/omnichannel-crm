//package com.mylinehub.crm.entity;
//
//
//import java.time.Instant;
//import java.util.Date;
//
//import javax.persistence.CascadeType;
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Index;
//import javax.persistence.JoinColumn;
//import javax.persistence.OneToOne;
//import javax.persistence.SequenceGenerator;
//import javax.persistence.Table;
//
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.ToString;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@ToString
//@Entity
//@Table(name = "CALL_COST",
//indexes = {
//		  @Index(name = "CallingCost_id_Index", columnList = "id"),
//		  @Index(name = "CallingCost_Organization_Index", columnList = "organization"),
//		  @Index(name = "CallingCost_callcalculation_Index", columnList = "callcalculation"),
//		  @Index(name = "CallingCost_date_Index", columnList = "date"),
//		  @Index(name = "CallingCost_Call_DETAIL_ID_Index", columnList = "Call_DETAIL_ID"),
//		  @Index(name = "CallingCost_amount_Index", columnList = "amount"),
//		})
//public class CallingCost {
//
//    @Id
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "callcost_sequence"
//    )
//    @SequenceGenerator(
//            name="callcost_sequence",
//            sequenceName = "callcost_sequence",
//            allocationSize = 1,
//            initialValue = 100
//    )
//    @Column(nullable = false)
//    private Long id;
//    
//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
//    @JoinColumn(name = "Call_DETAIL_ID")
//    private CallDetail callDetail;
//    private String extension;
//    
//    private Double amount;
//    
//    // Calculation Type ::: Data
//    private String callcalculation;
//    private String organization;
//    private String remarks;
//    private Date date;
//    private Date dateEnd;
//    
//    @Column(updatable = false)
//    @CreationTimestamp
//    private Instant createdOn;
//    
//    @UpdateTimestamp
//    private Instant lastUpdatedOn;
//    
//    
//    CallingCost (CallDetail callDetail,Double amount,String callcalculation,String organization)
//    {
//    	this.callDetail = callDetail;
//    	this.amount=amount;
//    	this.callcalculation = callcalculation;
//    	this.organization = organization;
//    }
//    
//}