package com.mylinehub.crm.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mylinehub.crm.entity.FileCategory;
import com.mylinehub.crm.entity.dto.FileCategoryDTO;

@Mapper(componentModel = "spring")
public interface FileCategoryMapper {

	@Mapping(target = "id", source = "id")
	@Mapping(target = "extension", source = "extension")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "businessType", source = "businessType")
	@Mapping(target = "iconImageType", source = "iconImageType")
	@Mapping(target = "iconImageData", source = "iconImageData")
	@Mapping(target = "iconImageByteData", source = "fileCategory.iconImageData", qualifiedByName = "mapIconImage")
	FileCategoryDTO mapFileCategoryToDTO(FileCategory fileCategory);
	
	@Mapping(target = "id", source = "id")
	@Mapping(target = "extension", source = "extension")
	@Mapping(target = "domain", source = "domain")
	@Mapping(target = "organization", source = "organization")
	@Mapping(target = "name", source = "name")
	@Mapping(target = "businessType", source = "businessType")
	@Mapping(target = "iconImageType", source = "iconImageType")
	@Mapping(target = "iconImageData", source = "iconImageData")
	FileCategory mapDTOToFileCategory(FileCategoryDTO fileCategory);
	
	@Named("mapIconImage") 
    default byte[] mapFileCategoryIconImage(String iconImageData) throws IOException{
		
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


