package com.mylinehub.crm.requests;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageRequest {
	
	Long id;
	String extension;
	MultipartFile pic;
	String email;
	String organization;

}
