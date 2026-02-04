package com.mylinehub.crm.entity.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeBasicInfoDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String role;
    private String departmentName;
    private String phonenumber;
    private String extension;
    private String email;
    private String pesel;
    private Date birthdate;
    private byte[] iconImageData;
    private String imageType;
    private String state;
    private String presence;
    private String dotClass;
    private String channel;
    private Long sizeMediaUploadInMB;
}
