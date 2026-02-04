package com.mylinehub.crm.TaskScheduler;

import java.util.Map;

import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.OrganizationWorkingDTO;
import com.mylinehub.crm.entity.Organization;

import lombok.Data;
import org.springframework.context.ApplicationContext;

@Data
public class RecalculateOrganizationStorageRunnable   implements Runnable{

	ApplicationContext applicationContext;
	String jobId;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("SaveOrganizationDataRunnable");
		Map<String,Organization> allOrganizations = OrganizationData.workWithAllOrganizationData(null, null, "get",null);
		
		if(allOrganizations!=null)
		{
			System.out.println("Organization not null. Recalculating its file storage values.");
			for (Map.Entry<String,Organization> entry : allOrganizations.entrySet())  
			{ 
				System.out.println("Recalculating For Org : "+ entry.getKey());
				  OrganizationWorkingDTO organizationWorkingDTO = new OrganizationWorkingDTO();
	 			  organizationWorkingDTO.setApplicationContext(applicationContext);
	 			  OrganizationData.workWithAllOrganizationData(entry.getKey(),null,"recalculate-storage",organizationWorkingDTO);
				
			}
		}
	}

}
