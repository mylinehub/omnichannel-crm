package com.mylinehub.crm.entity.dto;


import javax.persistence.Lob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SshConnectionDTO {

	private Long id;
    private String phonecontext;
    private String organization;
    private String sshHostType;
    private String type;
    private String sshUser;
    private String password;
    private String authType;
    private String domain;  
    private String port;
    private String pemFileName;
    private String pemFileLocation;
    private String connectionString;
    private boolean active;
    private String privateKey;
    private String publicKey;
    private String extraKey;
    
}