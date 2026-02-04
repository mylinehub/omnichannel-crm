package com.mylinehub.crm.entity;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Table(name = "refreshtoken",indexes = {@Index(name = "email_Employee_Index", columnList = "email")})
public class RefreshToken {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SequenceGenerator(
            name = "refresh_token_sequence",
            sequenceName = "refresh_token_sequence",
            allocationSize = 1)
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "refresh_token_sequence")
    @Column(name = "id")
    private Long id;

	@Column(columnDefinition = "TEXT")
    private String token;

    private Date expiryDate;

    @Column(unique = true,nullable=false)
    private String email;	
   
}

