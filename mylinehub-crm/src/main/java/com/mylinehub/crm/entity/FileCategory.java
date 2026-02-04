package com.mylinehub.crm.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "FILECATEGORY",
indexes = {
		  @Index(name = "FileCategory_extension_Index", columnList = "extension"),
		  @Index(name = "FileCategory_businessType_Index", columnList = "businessType")
		},uniqueConstraints = { @UniqueConstraint(columnNames = { "extension", "name" }) })
public class FileCategory {
	    @Id
	    @GeneratedValue(
	            strategy = GenerationType.SEQUENCE,
	            generator = "filecategory_sequence"
	    )
	    @SequenceGenerator(
	            name="filecategory_sequence",
	            sequenceName = "filecategory_sequence",
	            allocationSize = 1,
	            initialValue = 100
	    )
	    @Column(name = "FILECATEGORY_ID",nullable = false)
	    private Long id;
	    
	    private String domain;
	    private String organization;
	    private String extension;
	    
	    private Long whatsAppPhoneID;
	    
	    private String name;
	    private String businessType;
	    private String iconImageType;
	    @Column(columnDefinition = "varchar(500)")
	    private String iconImageData;
	    
	    @Column(columnDefinition = "bigint default 0")
	    private Long iconImageSize;
	    
	    @Column(columnDefinition = "boolean default true")
	    private boolean root;
}
