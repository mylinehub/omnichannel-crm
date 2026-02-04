package com.mylinehub.crm.gst.service;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.gst.data.GSTVerificationEngineData;
import com.mylinehub.crm.gst.data.dto.GstVerificationEngineDataParameterDto;
import com.mylinehub.crm.gst.entity.GstVerificationEngine;
import com.mylinehub.crm.gst.repository.GstVerificationEngineRepository;


import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class GstVerificationEngineService{
	
	  /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final GstVerificationEngineRepository gstVerificationEngineRepository;
    
    public GstVerificationEngine updateGstVerificationEngineData(GstVerificationEngine details) {
    	
    	GstVerificationEngine fromDatabase = null;
    	
    	try {
        	  fromDatabase = gstVerificationEngineRepository.getGstVerificationEngineByEngineName(details.getEngineName());
        	
        	  if(fromDatabase != null) {
        		
        		fromDatabase.setClientId(details.getClientId());
        		fromDatabase.setCientSecret(details.getCientSecret());
        		fromDatabase.setValidityInHours(details.getValidityInHours());
        		fromDatabase.setActive(details.isActive());
        		
        		GstVerificationEngineDataParameterDto gstVerificationEngineDataParameterDto = new GstVerificationEngineDataParameterDto();
        		gstVerificationEngineDataParameterDto.setEngineName(details.getEngineName());
        		gstVerificationEngineDataParameterDto.setDetails(details);
        		
        		if(details.isActive())
        		gstVerificationEngineDataParameterDto.setAction("update");
        		else
        		gstVerificationEngineDataParameterDto.setAction("delete");
        		
        		GSTVerificationEngineData.workWithGstVerificationData(gstVerificationEngineDataParameterDto);
        		
        	}
        	else {
        		throw new Exception("Engine not present. Unable to update");
        	}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	} 	
    	
    	return fromDatabase;
    }
}
