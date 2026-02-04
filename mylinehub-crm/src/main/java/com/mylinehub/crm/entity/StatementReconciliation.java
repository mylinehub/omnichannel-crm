package com.mylinehub.crm.entity;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.mylinehub.crm.entity.dto.StatementReconciliationLineDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "STATEMENT_RECONCILIATION",
indexes = {
		  @Index(name = "statement_reconciliation_Organization_Index", columnList = "organization"),
		  @Index(name = "statement_reconciliation_Name_Index", columnList = "name"),
		  @Index(name = "statement_reconciliation_Client_Index", columnList = "client"),
		  @Index(name = "statement_reconciliation_byExtension_Index", columnList = "byExtension"),
		})
public class StatementReconciliation {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "statement_reconciliation_sequence"
    )
    @SequenceGenerator(
            name="statement_reconciliation_sequence",
            sequenceName = "statement_reconciliation_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
    
    private  String name;
    
    private  String organization;
    
    @Column(unique = true)
    private  String byExtension;
    
    int rawFileCount;
    int comparedFileCount;
    int isFoundCount;
    int isExtraCount;
    int isMismatchCount;
    
    int rawUtrColumn;
    int rawAmountColumn;
    int rawCashFlowDirection;
    
    int compareUtrColumn;
    int compareAmountColumn;
    int compareCashFlowDirection;
    
    int noObservationFound;
    
    int numberOfParallelThreads;
    
    private  String rawSheetName;
    private  String compareSheetName;
    
    private  String client;
    private String reportHeading;
    private String bankName;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private List<StatementReconciliationLineDTO> observations;
    
    private String zipPath;
    private boolean complete;
    
    @Column(updatable = false)
    @CreationTimestamp
    private Instant createdOn;
    
    @UpdateTimestamp
    private Instant lastUpdatedOn;
    
    //To retrieve basic information from database.
    public StatementReconciliation(Long id, String name, String organization, String byExtension, String client, String reportHeading, String bankName, int noObservationFound, int numberOfParallelThreads, Instant createdOn) { 
      	this.id = id;
        this.name = name;
        this.organization = organization;
        this.byExtension = byExtension;
        this.client = client;
        this.reportHeading = reportHeading;
        this.bankName = bankName;
        this.noObservationFound = noObservationFound;
      	this.numberOfParallelThreads=numberOfParallelThreads;
      	this.createdOn= createdOn;
    }
    
}