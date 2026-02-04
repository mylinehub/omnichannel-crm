package com.mylinehub.crm.mapper;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

	@Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "userRole")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "salary", source = "salary")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "phoneTrunk", source = "phoneTrunk")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "extension", source = "extension")
    @Mapping(target = "extensionpassword", source = "extensionpassword")
    @Mapping(target = "protocol", source = "protocol")
    @Mapping(target = "domain", source = "domain")
	@Mapping(target = "secondDomain", source = "secondDomain")
    @Mapping(target = "isEnabled", source = "isEnabled")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "totalparkedchannels", source = "totalparkedchannels")
    @Mapping(target = "parkedchannel1", source = "parkedchannel1")
    @Mapping(target = "parkedchannel2", source = "parkedchannel2")
    @Mapping(target = "parkedchannel3", source = "parkedchannel3")
    @Mapping(target = "parkedchannel4", source = "parkedchannel4")
    @Mapping(target = "parkedchannel5", source = "parkedchannel5")
    @Mapping(target = "parkedchannel6", source = "parkedchannel6")
    @Mapping(target = "parkedchannel7", source = "parkedchannel7")
    @Mapping(target = "parkedchannel8", source = "parkedchannel8")
    @Mapping(target = "parkedchannel9", source = "parkedchannel9")
    @Mapping(target = "parkedchannel10", source = "parkedchannel10")
    @Mapping(target = "timezone", source = "timezone.displayName")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "callonnumber", source = "callonnumber")
	@Mapping(target = "useSecondaryAllotedLine", source = "useSecondaryAllotedLine")
    @Mapping(target = "phonenumber", source = "phonenumber")
    @Mapping(target = "transfer_phone_1", source = "transfer_phone_1")
    @Mapping(target = "transfer_phone_2", source = "transfer_phone_2")
    @Mapping(target = "birthdate", source = "birthdate")
    @Mapping(target = "provider1", source = "provider1")
	@Mapping(target = "allotednumber1", source = "allotednumber1")
	@Mapping(target = "provider2", source = "provider2")
	@Mapping(target = "allotednumber2", source = "allotednumber2")
	@Mapping(target = "costCalculation", source = "costCalculation")
	@Mapping(target = "amount", source = "amount")
	@Mapping(target = "imageName", source = "imageName")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "imageData", source = "imageData")
	@Mapping(target = "iconImageData", source = "iconImageData")
	@Mapping(target = "governmentDocument1Data", source = "governmentDocument1Data")
	@Mapping(target = "governmentDocumentID1", source = "governmentDocumentID1")
	@Mapping(target = "governmentDocument2Data", source = "governmentDocument2Data")
	@Mapping(target = "governmentDocumentID2", source = "governmentDocumentID2")
	@Mapping(target = "sizeMediaUploadInMB", source = "sizeMediaUploadInMB")
	@Mapping(target = "pesel", source = "pesel")
	@Mapping(target = "sex", source = "sex")
	@Mapping(target = "uiTheme", source = "uiTheme")
	@Mapping(target = "autoAnswer", source = "autoAnswer")
	@Mapping(target = "autoConference", source = "autoConference")
	@Mapping(target = "autoVideo", source = "autoVideo")
	@Mapping(target = "micDevice", source = "micDevice")
	@Mapping(target = "speakerDevice", source = "speakerDevice")
	@Mapping(target = "videoDevice", source = "videoDevice")
	@Mapping(target = "videoOrientation", source = "videoOrientation")
	@Mapping(target = "videoQuality", source = "videoQuality")
	@Mapping(target = "videoFrameRate", source = "videoFrameRate")
	@Mapping(target = "autoGainControl", source = "autoGainControl")
	@Mapping(target = "echoCancellation", source = "echoCancellation")
	@Mapping(target = "noiseSupression", source = "noiseSupression")
	@Mapping(target = "sipPort", source = "sipPort")
	@Mapping(target = "sipPath", source = "sipPath")
	@Mapping(target = "recordAllCalls", source = "recordAllCalls")
	@Mapping(target = "doNotDisturb", source = "doNotDisturb")
	@Mapping(target = "startVideoFullScreen", source = "startVideoFullScreen")
	@Mapping(target = "intercomPolicy", source = "intercomPolicy")
	@Mapping(target = "freeDialOption", source = "freeDialOption")
	@Mapping(target = "textDictateOption", source = "textDictateOption")
	@Mapping(target = "textMessagingOption", source = "textMessagingOption")
	@Mapping(target = "confExtension", source = "confExtension")
	@Mapping(target = "extensionPrefix", source = "extensionPrefix")
	@Mapping(target = "confExtensionPrefix", source = "confExtensionPrefix")
	@Mapping(target = "callWaiting", source = "callWaiting")
	@Mapping(target = "notificationDot", source = "notificationDot")
	@Mapping(target = "doc1ImageType", source = "doc1ImageType")
	@Mapping(target = "doc2ImageType", source = "doc2ImageType")
	@Mapping(target = "iconImageByteData", source = "employee.iconImageData", qualifiedByName = "mapIconImage")
	@Mapping(target = "lastConnectedCustomerPhone", source = "lastConnectedCustomerPhone")
	@Mapping(target = "allowedToSwitchOffWhatsAppAI", source = "allowedToSwitchOffWhatsAppAI")
    EmployeeDTO mapEmployeeToDto(Employee employee);
    
    
    
	@Mapping(target = "provider1", source = "provider1")
	@Mapping(target = "allotednumber1", source = "allotednumber1")
	@Mapping(target = "provider2", source = "provider2")
	@Mapping(target = "allotednumber2", source = "allotednumber2")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "userRole", source = "role")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "salary", source = "salary")
    @Mapping(target = "department.id", source = "departmentId")
    @Mapping(target = "department.departmentName", source = "departmentName")
    @Mapping(target = "phoneContext", source = "phoneContext")
	@Mapping(target = "phoneTrunk", source = "phoneTrunk")
    @Mapping(target = "organization", source = "organization")
    @Mapping(target = "extension", source = "extension")
    @Mapping(target = "extensionpassword", source = "extensionpassword")
    @Mapping(target = "protocol", source = "protocol")
    @Mapping(target = "domain", source = "domain")
	@Mapping(target = "secondDomain", source = "secondDomain")
    @Mapping(target = "totalparkedchannels", source = "totalparkedchannels")
    @Mapping(target = "parkedchannel1", source = "parkedchannel1")
    @Mapping(target = "parkedchannel2", source = "parkedchannel2")
    @Mapping(target = "parkedchannel3", source = "parkedchannel3")
    @Mapping(target = "parkedchannel4", source = "parkedchannel4")
    @Mapping(target = "parkedchannel5", source = "parkedchannel5")
    @Mapping(target = "parkedchannel6", source = "parkedchannel6")
    @Mapping(target = "parkedchannel7", source = "parkedchannel7")
    @Mapping(target = "parkedchannel8", source = "parkedchannel8")
    @Mapping(target = "parkedchannel9", source = "parkedchannel9")
    @Mapping(target = "parkedchannel10", source = "parkedchannel10")
    @Mapping(target = "timezone", expression = "java(TimeZone.getTimeZone(employeeDTO.timezone))")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "callonnumber", source = "callonnumber")
	@Mapping(target = "useSecondaryAllotedLine", source = "useSecondaryAllotedLine")
    @Mapping(target = "phonenumber", source = "phonenumber")
    @Mapping(target = "transfer_phone_1", source = "transfer_phone_1")
    @Mapping(target = "transfer_phone_2", source = "transfer_phone_2")
    @Mapping(target = "isEnabled", source = "isEnabled")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "birthdate", source = "birthdate")
    @Mapping(target = "costCalculation", source = "costCalculation")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "imageName", source = "imageName")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "imageData", source = "imageData")
	@Mapping(target = "iconImageData", source = "iconImageData")
	@Mapping(target = "governmentDocument1Data", source = "governmentDocument1Data")
	@Mapping(target = "governmentDocumentID1", source = "governmentDocumentID1")
	@Mapping(target = "governmentDocument2Data", source = "governmentDocument2Data")
	@Mapping(target = "governmentDocumentID2", source = "governmentDocumentID2")
	@Mapping(target = "sizeMediaUploadInMB", source = "sizeMediaUploadInMB")
	@Mapping(target = "pesel", source = "pesel")
	@Mapping(target = "sex", source = "sex")
	@Mapping(target = "autoConference", source = "autoConference")
	@Mapping(target = "autoVideo", source = "autoVideo")
	@Mapping(target = "micDevice", source = "micDevice")
	@Mapping(target = "speakerDevice", source = "speakerDevice")
	@Mapping(target = "videoDevice", source = "videoDevice")
	@Mapping(target = "videoOrientation", source = "videoOrientation")
	@Mapping(target = "videoQuality", source = "videoQuality")
	@Mapping(target = "videoFrameRate", source = "videoFrameRate")
	@Mapping(target = "autoGainControl", source = "autoGainControl")
	@Mapping(target = "echoCancellation", source = "echoCancellation")
	@Mapping(target = "noiseSupression", source = "noiseSupression")
	@Mapping(target = "sipPort", source = "sipPort")
	@Mapping(target = "sipPath", source = "sipPath")
	@Mapping(target = "recordAllCalls", source = "recordAllCalls")
	@Mapping(target = "doNotDisturb", source = "doNotDisturb")
	@Mapping(target = "startVideoFullScreen", source = "startVideoFullScreen")
	@Mapping(target = "intercomPolicy", source = "intercomPolicy")
	@Mapping(target = "freeDialOption", source = "freeDialOption")
	@Mapping(target = "textDictateOption", source = "textDictateOption")
	@Mapping(target = "textMessagingOption", source = "textMessagingOption")
	@Mapping(target = "confExtension", source = "confExtension")
	@Mapping(target = "extensionPrefix", source = "extensionPrefix")
	@Mapping(target = "confExtensionPrefix", source = "confExtensionPrefix")
	@Mapping(target = "callWaiting", source = "callWaiting")
	@Mapping(target = "notificationDot", source = "notificationDot")
	@Mapping(target = "doc1ImageType", source = "doc1ImageType")
	@Mapping(target = "doc2ImageType", source = "doc2ImageType")
	@Mapping(target = "lastConnectedCustomerPhone", source = "lastConnectedCustomerPhone")
	@Mapping(target = "allowedToSwitchOffWhatsAppAI", source = "allowedToSwitchOffWhatsAppAI")
    Employee mapDtoToEmployee(EmployeeDTO employeeDTO);
	
	
	@Named("mapIconImage") 
    default byte[] mapEmployeeIconImage(String iconImageData) throws IOException{
		
//		System.out.println("mapEmployeeIconImage");
		if(iconImageData != null)
		{
//			System.out.println("iconImageData is not null");
			try
			{
				byte[] image;		
				
				String uploadIconDirectory = iconImageData.substring(0,iconImageData.lastIndexOf("/"));
				iconImageData = iconImageData.replace(uploadIconDirectory+"/", "");
				image = getImage(uploadIconDirectory, iconImageData);
				
		        return image;
			}
			catch(Exception e)
			{
				return null;
			}
			
		}
		else
		{
//			System.out.println("iconImageData is null");
			return null;
		}
    }
	
	// To view an image
	default byte[] getImage(String imageDirectory, String imageName) throws IOException {
        Path imagePath = Path.of(imageDirectory, imageName);

        if (Files.exists(imagePath)) {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return imageBytes;
        } else {
            return null; // Handle missing images
        }
    }

}
