package com.mylinehub.crm.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.mylinehub.crm.entity.Product;
import com.mylinehub.crm.entity.dto.CustomerProductInterestDTO;

@Mapper(componentModel = "spring")
public interface CustomerProductInterestMapper {
	
	
	    @Mapping(target = "name", source = "name")
	    @Mapping(target = "id", source = "id")
	    @Mapping(target = "productStringType", source = "productStringType")
	    @Mapping(target = "imageType", source = "imageType")
	    @Mapping(target = "imageByteData", source = "product.imageData", qualifiedByName = "mapImageData")
	    CustomerProductInterestDTO mapProductToCustomerProductInterestDto(Product product); 
	    
	    @Named("mapImageData") 
	    default byte[] mapEmployeeIconImage(String imageData) throws IOException{
			
			if(imageData != null)
			{
				try
				{
					byte[] image;		
					String uploadIconDirectory = imageData.substring(0,imageData.lastIndexOf("/"));
					imageData = imageData.replace(uploadIconDirectory+"/", "");
					image = getImage(uploadIconDirectory, imageData);
					
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
