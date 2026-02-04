package com.mylinehub.crm.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeToCampaignDTO {

	public Long id;
	public Long campaignid;
	public String campaignName;
    public Long employeeid;
    public String organization;
    private String firstName;
    private String email;
    private String phonenumber;
	public String lastConnectedCustomerPhone;
	public  int lastCustomerNumber;
}
