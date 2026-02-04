package com.mylinehub.crm.data.dto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultiPartFileDTO implements MultipartFile {
	
	public byte[] input;
	public String fileName;
	public String originalFilename;
	public String mime_type;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	@Override
	public String getOriginalFilename() {
		// TODO Auto-generated method stub
		return originalFilename;
	}
	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return mime_type;
	}
	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return input == null || input.length == 0;
	}
	@Override
	public byte[] getBytes() throws IOException {
		// TODO Auto-generated method stub
		return input;
	}
	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream(input);
	}
	@Override
    public void transferTo(File destination) throws IOException, IllegalStateException {
        try(FileOutputStream fos = new FileOutputStream(destination)) {
            fos.write(input);
        }
    }
	@Override
	public long getSize() {
		// TODO Auto-generated method stub
		return input.length;
	}
	
}
