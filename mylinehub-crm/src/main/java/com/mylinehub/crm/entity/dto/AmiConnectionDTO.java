package com.mylinehub.crm.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmiConnectionDTO {

	private Long id;
    public String domain;
    public int port;
    public String amiuser;
    public String password;
    public String phonecontext;
    public String organization;
    public boolean isactive;
}
