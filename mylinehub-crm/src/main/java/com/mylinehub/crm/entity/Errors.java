package com.mylinehub.crm.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "ERRORS",
	   indexes = {
		  @Index(name = "Errors_Organization_Index", columnList = "organization")})
public class Errors {
	
	@Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "error_sequence"
    )
    @SequenceGenerator(
            name="error_sequence",
            sequenceName = "error_sequence",
            allocationSize = 1,
            initialValue = 100
    )
    @Column(nullable = false)
    private Long id;
	
	@Column(columnDefinition = "TEXT", nullable = true)
	String error;
	
	@Column(columnDefinition = "TEXT", nullable = true)
	String data;
	
	String errorClass;
	
	String functionality;
	
	Date createdDate;
	
	String organization;
	
}