package com.mylinehub.crm.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.dto.EmployeeBasicInfoDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;

@Mapper(componentModel = "spring")
public interface EmployeeBasicInfoMapper {
	
	@Mapping(target = "id", source = "id")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "role", source = "userRole")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "departmentName", source = "department.departmentName")
    @Mapping(target = "extension", source = "extension")
    @Mapping(target = "phonenumber", source = "phonenumber")
	@Mapping(target = "sizeMediaUploadInMB", source = "sizeMediaUploadInMB")
	@Mapping(target = "imageType", source = "imageType")
	@Mapping(target = "iconImageData", source = "employee.iconImageData", qualifiedByName = "mapIconImage")
	@Mapping(target = "state", source = "employee.extension", qualifiedByName = "mapState")
	@Mapping(target = "presence", source = "employee.extension", qualifiedByName = "mapPresence")
	@Mapping(target = "dotClass", source = "employee.extension", qualifiedByName = "mapDotClass")
	EmployeeBasicInfoDTO mapEmployeeToBasicInfoDto(Employee employee);
    
	
	@Named("mapState") 
    default String mapState(String extension) throws IOException{
		String toReturn = null;
		
		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
		} 
		
		if(employeeDataAndStateDTO != null)
		{
			toReturn = employeeDataAndStateDTO.getExtensionState().get(0);
		}
		
		return toReturn;
	}
	
	@Named("mapPresence") 
    default String mapPresence(String extension) throws IOException{
		String toReturn = null;
		
		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
		} 
		
		if(employeeDataAndStateDTO !=null)
		{
			toReturn = employeeDataAndStateDTO.getExtensionState().get(1);
		}
		
		return toReturn;
	}
	
	@Named("mapDotClass") 
    default String mapDotClass(String extension) throws IOException{
		String toReturn = null;
		
		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(extension, null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO = null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(extension);
		} 
		
		if(employeeDataAndStateDTO !=null)
		{
			toReturn = employeeDataAndStateDTO.getExtensionState().get(2);
		}
		
		return toReturn;
	}
	
	
	
	@Named("mapIconImage") 
    default byte[] mapEmployeeIconImage(String iconImageData) throws IOException{
		
		//System.out.println("mapEmployeeIconImage : "+iconImageData);
		if(iconImageData != null)
		{
//			System.out.println("iconImageData is not null");
			try
			{
				byte[] image;		
				String uploadIconDirectory = iconImageData.substring(0,iconImageData.lastIndexOf("/"));
//				System.out.println("uploadIconDirectory : "+uploadIconDirectory);
				iconImageData = iconImageData.replace(uploadIconDirectory+"/", "");
//				System.out.println("iconImageData : "+iconImageData);
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
