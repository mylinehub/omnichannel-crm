package com.mylinehub.crm.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class MultipartImage implements MultipartFile {

	 private final byte[] imgContent;
	 private final String originalFileName;
	 private final String contentType;
	 private final String name;

     public MultipartImage(byte[] imgContent,String originalFileName,String contentType,String name) {
         this.imgContent = imgContent;
         this.originalFileName=originalFileName;
         this.contentType = contentType;
         this.name=name;
     }

     @Override
     public String getName() {
         // TODO - implementation depends on your requirements 
         return name;
     }

     @Override
     public String getOriginalFilename() {
         // TODO - implementation depends on your requirements
         return originalFileName;
     }

     @Override
     public String getContentType() {
         // TODO - implementation depends on your requirements
         return contentType;
     }

     @Override
     public boolean isEmpty() {
         return imgContent == null || imgContent.length == 0;
     }

     @Override
     public long getSize() {
         return imgContent.length;
     }

     @Override
     public byte[] getBytes() throws IOException {
         return imgContent;
     }

     @Override
     public InputStream getInputStream() throws IOException {
         return new ByteArrayInputStream(imgContent);
     }

     @Override
     public void transferTo(File dest) throws IOException, IllegalStateException { 
         new FileOutputStream(dest).write(imgContent);
     }

}