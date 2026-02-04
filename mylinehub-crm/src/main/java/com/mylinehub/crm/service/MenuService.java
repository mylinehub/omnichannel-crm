package com.mylinehub.crm.service;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.mylinehub.crm.entity.dto.MenuDto;
import com.mylinehub.crm.enums.FLATTEN_MENU;
import com.mylinehub.crm.utils.LoggerUtils;

import lombok.AllArgsConstructor;


/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class MenuService {

	
	public JSONArray generateMenu(JSONArray menu, MenuDto menuDto) {
		
		try {
			
			
			if(menuDto.getWhatsAppModule()!=null) {
				menu = generateWhatsAppModuleMenu(menu);
				Map<String,List<String>> subModuleMap = menuDto.getWhatsAppModule();
				
				for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
					LoggerUtils.log.debug("Mapping Values");
		           LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		            
		            String keyString = entry.getKey();
		            List<String> childrenList = entry.getValue();
							            
					 if(keyString.equals(FLATTEN_MENU.whatsappdeliveryreport.name())) {
						 menu = includeDeliveryReportSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.whatsappfacebookproject.name())) {
						 menu = includeFacebookProjectSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.whatsappnumber.name())) {
						 menu = includeWhatsAppInstalledSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.whatsappchat.name())) {
						 menu = includeWhatsAppChatSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.mediastorage.name())) {
						 menu = includeMediaStorageSubModuleMenu(menu,childrenList);
					 }
		        }
			}

			
			if(menuDto.getCampaignModule()!=null) {
				menu = generateCampaignModuleMenu(menu);
				Map<String,List<String>> subModuleMap = menuDto.getCampaignModule();
				
				for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
					System.out.println("Mapping Values");
		           LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		            
		            String keyString = entry.getKey();
		            List<String> childrenList = entry.getValue();
							            
					 if(keyString.equals(FLATTEN_MENU.campaign.name())) {
						 menu = includeCampaignSubModuleMenu(menu,childrenList);
					 }
		        }
			}
			
			if(menuDto.getCallingModule()!=null) {
				
				menu = generateCallingModuleMenu(menu);
				
				Map<String,List<String>> subModuleMap = menuDto.getCallingModule();
				
				for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
					System.out.println("Mapping Values");
		           LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		            
		            String keyString = entry.getKey();
		            List<String> childrenList = entry.getValue();
							            
					 if(keyString.equals(FLATTEN_MENU.calldetail.name())) {
						 menu = includeCallDetailsSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.cost.name())) {
						 menu = includeCallingCostSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.conference.name())) {
						 menu = includeConferenceSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.ivr.name())) {
						 menu = includeIvrSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.queue.name())) {
						 menu = includeQueueSubModuleMenu(menu,childrenList);
					 }
		        }
			}

			if (menuDto.getFranchiseManagementModule() != null) {

			    menu = generateFranchiseManagementModuleMenu(menu);

			    Map<String, List<String>> subModuleMap = menuDto.getFranchiseManagementModule();

			    for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
			        LoggerUtils.log.debug("Mapping Values");
			        LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());

			        String keyString = entry.getKey();
			        List<String> childrenList = entry.getValue();

			        if (keyString.equals(FLATTEN_MENU.franchisemanagement.name())) {
			            menu = includeFranchiseManagementSubModuleMenu(menu, childrenList);
			        }
			    }
			}

			

			if (menuDto.getPropertyManagementModule() != null) {

			    menu = generatePropertyManagementModuleMenu(menu);

			    Map<String, List<String>> subModuleMap = menuDto.getPropertyManagementModule();

			    for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
			        LoggerUtils.log.debug("Mapping Values");
			        LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());

			        String keyString = entry.getKey();
			        List<String> childrenList = entry.getValue();

			        if (keyString.equals(FLATTEN_MENU.propertyinventory.name())) {
			            menu = includePropertyInventorySubModuleMenu(menu, childrenList);
			        }
			    }
			}
			
			
			if(menuDto.getOrganizationModule()!=null) {
				
				menu = generateOrganizationModuleMenu(menu);
				Map<String,List<String>> subModuleMap = menuDto.getOrganizationModule();
				
				for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
					System.out.println("Mapping Values");
		           LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		            
		            String keyString = entry.getKey();
		            List<String> childrenList = entry.getValue();
							            
					 if(keyString.equals(FLATTEN_MENU.employee.name())) {
						 menu = includeEmployeeSubModuleMenu(menu,childrenList);
					 }
					 
					 else if(keyString.equals(FLATTEN_MENU.filestore.name())) {
						 menu = includeFileStoreSubModuleMenu(menu,childrenList);
					 }
					 
					 else if(keyString.equals(FLATTEN_MENU.absenteeism.name())) {
						 menu = includeAbsenteeismSubModuleMenu(menu,childrenList);
					 }
					 
					 else if(keyString.equals(FLATTEN_MENU.department.name())) {
						 menu = includeDepartmentSubModuleMenu(menu,childrenList);
					 }					
					 
					 else if(keyString.equals(FLATTEN_MENU.customer.name())) {
						 menu = includeCustomerSubModuleMenu(menu,childrenList);
					 }
					 
					 else if(keyString.equals(FLATTEN_MENU.product.name())) {
						 menu = includeProductSubModuleMenu(menu,childrenList);
					 }
					 
					 else if(keyString.equals(FLATTEN_MENU.purchase.name())) {
						 menu = includePurchaseSubModuleMenu(menu,childrenList);
					 }
					 
					 else if(keyString.equals(FLATTEN_MENU.supplier.name())) {
						 menu = includeSupplierSubModuleMenu(menu,childrenList);
					 }
		        }

				
			}
			
			
			if(menuDto.getIssueTrackingModule()!=null) {
				menu = generateIssueTrackingModuleMenuTop(menu);
				Map<String,List<String>> subModuleMap = menuDto.getIssueTrackingModule();
				
				for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
					System.out.println("Mapping Values");
		           LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		            
		            String keyString = entry.getKey();
		            List<String> childrenList = entry.getValue();
							            
					 if(keyString.equals(FLATTEN_MENU.error.name())) {
						 menu = includeErrorSubModuleMenu(menu,childrenList);
					 }
					 else if(keyString.equals(FLATTEN_MENU.log.name())) {
						 menu = includeLogsSubModuleMenu(menu,childrenList);
					 }
		        }
				
			}
			
			if(menuDto.getSettingModule()!=null) {
				menu = generateSettingModuleMenuTop(menu);
				
				Map<String,List<String>> subModuleMap = menuDto.getSettingModule();
				
				for (Map.Entry<String, List<String>> entry : subModuleMap.entrySet()) {
					System.out.println("Mapping Values");
		           LoggerUtils.log.debug("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		            
		            String keyString = entry.getKey();
		            List<String> childrenList = entry.getValue();
		            
		            if(keyString.equals(FLATTEN_MENU.auth.name())) {
		            	menu = includeAuthSubModuleMenu(menu,childrenList);	
		            }
		            
		            else if(keyString.equals(FLATTEN_MENU.ssh.name())) {
						 menu = includeSSHSubModuleMenu(menu,childrenList);          	
				     }
							            
		            else  if(keyString.equals(FLATTEN_MENU.sip.name())) {
						 menu = includeSIPSubModuleMenu(menu,childrenList);
					 }
					 
		            else  if(keyString.equals(FLATTEN_MENU.ami.name())) {
						 menu = includeAMISubModuleMenu(menu,childrenList);
					 }

		        }
			}
			
			return menu;
			
		}catch (Exception e) {
			// TODO: handle exception
		}
		
	
		
		return menu; 
		
	}

	
	public JSONArray generateWhatsAppModuleMenu(JSONArray menu) {
		
	        // Whats-App Group
	        JSONObject whatsAppGroup = new JSONObject();
	        whatsAppGroup.put("title", "Whats-App Module");
	        whatsAppGroup.put("group", true);
	        menu.put(whatsAppGroup);	
	        
	        return menu;
	}
	
	public JSONArray includeDeliveryReportSubModuleMenu(JSONArray menu,List<String> entry) {
        // Delivery Report
        JSONObject deliveryReport = new JSONObject();
        deliveryReport.put("title", "Delivery Report");
        deliveryReport.put("icon", "message-square-outline");
        JSONArray deliveryReportChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.whatsappdeliveryreportdetails.name())) {
               JSONObject dashboard = new JSONObject();
               dashboard.put("title", "Dashboard");
               dashboard.put("link", "/pages/whatsapp-report/report");
               deliveryReportChildren.put(dashboard);
       	   }
          }

        deliveryReport.put("children", deliveryReportChildren);
        menu.put(deliveryReport);
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;  
	}
	
	public JSONArray includeFacebookProjectSubModuleMenu(JSONArray menu,List<String> entry) {

        // Facebook Project
        JSONObject facebookProject = new JSONObject();
        facebookProject.put("title", "Facebook Project");
        facebookProject.put("icon", "cube-outline");
        JSONArray facebookProjectChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.whatsappfacebookprojectdetails.name())) {
               JSONObject details = new JSONObject();
               details.put("title", "Details");
               details.put("link", "/pages/whatsapp-project/details");
               facebookProjectChildren.put(details);
       	   }
          }

        facebookProject.put("children", facebookProjectChildren);
        menu.put(facebookProject);
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;  
	}
	
	public JSONArray includeWhatsAppInstalledSubModuleMenu(JSONArray menu,List<String> entry) {
	    // Installed Numbers
        JSONObject installedNumbers = new JSONObject();
        installedNumbers.put("title", "Installed Numbers");
        installedNumbers.put("icon", "phone-outline");
        JSONArray installedNumbersChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.whatsappnumberdetails.name())) {
               JSONObject numberDetails = new JSONObject();
               numberDetails.put("title", "Details");
               numberDetails.put("link", "/pages/whatsapp-number/details");
               installedNumbersChildren.put(numberDetails);
       	   }
          }

        installedNumbers.put("children", installedNumbersChildren);
        menu.put(installedNumbers);
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;  

	}
	
	public JSONArray includeWhatsAppChatSubModuleMenu(JSONArray menu,List<String> entry) {
		
        // WhatsApp Chat
        JSONObject whatsAppChat = new JSONObject();
        whatsAppChat.put("title", "What'sApp Chat");
        whatsAppChat.put("icon", "message-circle-outline");
        JSONArray whatsAppChatChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.whatsappchatdetails.name())) {
               JSONObject messageConsole = new JSONObject();
               messageConsole.put("title", "Message Console");
               messageConsole.put("link", "/pages/whatsapp-chat/chat");
               whatsAppChatChildren.put(messageConsole);
       	   }
          }

        whatsAppChat.put("children", whatsAppChatChildren);
        menu.put(whatsAppChat);
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;  
	}
	
	public JSONArray includeMediaStorageSubModuleMenu(JSONArray menu,List<String> entry) {

        // Media Storage
        JSONObject mediaStorage = new JSONObject();
        mediaStorage.put("title", "Media Storage");
        mediaStorage.put("icon", "briefcase-outline");
        JSONArray mediaStorageChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
      	   if(entry.get(i).equals(FLATTEN_MENU.mediastoragedetails.name())) {
      	        JSONObject allMedia = new JSONObject();
      	        allMedia.put("title", "All-Media");
      	        allMedia.put("link", "/pages/file-storage/all-whatsapp-files");
      	        mediaStorageChildren.put(allMedia);
      	   }
         }
        
        mediaStorage.put("children", mediaStorageChildren);
        menu.put(mediaStorage);
        
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;    
	}

	

	public JSONArray generateCampaignModuleMenu(JSONArray menu) {

        // Calling Modules Group
        JSONObject callingModulesGroup = new JSONObject();
        callingModulesGroup.put("title", "Campaign Module");
        callingModulesGroup.put("group", true);
        menu.put(callingModulesGroup);
        
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;
	
	}
	
	public JSONArray includeCampaignSubModuleMenu(JSONArray menu,List<String> entry) {

        // Campaign
        JSONObject campaign = new JSONObject();
        campaign.put("title", "Campaign");
        campaign.put("icon", "pantone-outline");
        JSONArray campaignChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
        	   if(entry.get(i).equals(FLATTEN_MENU.campaigndetails.name())) {
        	        JSONObject campaignDetails = new JSONObject();
        	        campaignDetails.put("title", "Create-Details");
        	        campaignDetails.put("link", "/pages/campaign/all-campaigns");
        	        campaignChildren.put(campaignDetails);
        	   }
        	   if(entry.get(i).equals(FLATTEN_MENU.campaignrunhistory.name())) {
       	        JSONObject campaignDetails = new JSONObject();
       	        campaignDetails.put("title", "Run-Details");
       	        campaignDetails.put("link", "/pages/campaign/run-history");
       	        campaignChildren.put(campaignDetails);
        	   }
           }
        
        campaign.put("children", campaignChildren);
        menu.put(campaign);
        
        return menu;
	}
	
	public JSONArray generateCallingModuleMenu(JSONArray menu) {

        // Calling Modules Group
        JSONObject callingModulesGroup = new JSONObject();
        callingModulesGroup.put("title", "Calling Module");
        callingModulesGroup.put("group", true);
        menu.put(callingModulesGroup);
        
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;
	
	}

	public JSONArray includeCallDetailsSubModuleMenu(JSONArray menu,List<String> entry) {
        // Call Details
        JSONObject callDetails = new JSONObject();
        callDetails.put("title", "Call Details");
        callDetails.put("icon", "phone-call-outline");
        JSONArray callDetailsChildren = new JSONArray();
        
        for(int i = 0; i<entry.size();i++) {
      	   if(entry.get(i).equals(FLATTEN_MENU.calldetaildasboard.name())) {
      	        JSONObject callDashboard = new JSONObject();
      	        callDashboard.put("title", "Dashboard");
      	        callDashboard.put("link", "/pages/call-detail/call-dashboard");
      	        callDetailsChildren.put(callDashboard);
      	   }
      	   else if(entry.get(i).equals(FLATTEN_MENU.calldetailall.name())) {
	      		 JSONObject searchTerminal = new JSONObject();
	             searchTerminal.put("title", "Search Terminal");
	             searchTerminal.put("link", "/pages/call-detail/all-calls");
	             callDetailsChildren.put(searchTerminal);
     	   }
         }

        callDetails.put("children", callDetailsChildren);
        menu.put(callDetails);
        
        return menu;
	}
	
	public JSONArray includeCallingCostSubModuleMenu(JSONArray menu,List<String> entry) {
        // Calling Cost
        JSONObject callingCost = new JSONObject();
        callingCost.put("title", "Calling Cost");
        callingCost.put("icon", "trending-up-outline");
        JSONArray callingCostChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
     	   if(entry.get(i).equals(FLATTEN_MENU.costdetails.name())) {
     	        JSONObject trackAll = new JSONObject();
     	        trackAll.put("title", "Track All");
     	        trackAll.put("link", "/pages/calling-cost/all-costs");
     	        callingCostChildren.put(trackAll);
     	   }
        }
        callingCost.put("children", callingCostChildren);
        menu.put(callingCost);
        
        return menu;
		}
	
	
	public JSONArray includeConferenceSubModuleMenu(JSONArray menu,List<String> entry) {
		  // Conference
        JSONObject conference = new JSONObject();
        conference.put("title", "Conference");
        conference.put("icon", "people-outline");
        JSONArray conferenceChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
        	   if(entry.get(i).equals(FLATTEN_MENU.conferencedetails.name())) {
        	        JSONObject conferenceDetails = new JSONObject();
        	        conferenceDetails.put("title", "Details");
        	        conferenceDetails.put("link", "/pages/conference/all-conferences");
        	        conferenceChildren.put(conferenceDetails);
        	   }
           }
         
        conference.put("children", conferenceChildren);
        menu.put(conference);
        
        return menu;
	}
	
	public JSONArray includeIvrSubModuleMenu(JSONArray menu,List<String> entry) {

        // IVR
        JSONObject ivr = new JSONObject();
        ivr.put("title", "IVR");
        ivr.put("icon", "shake-outline");
        JSONArray ivrChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
        	   if(entry.get(i).equals(FLATTEN_MENU.ivrdetails.name())) {
        	        JSONObject ivrDetails = new JSONObject();
        	        ivrDetails.put("title", "Details");
        	        ivrDetails.put("link", "/pages/ivr/all-ivrs");
        	        ivrChildren.put(ivrDetails);
        	   }
           }

        ivr.put("children", ivrChildren);
        menu.put(ivr);
        
        return menu;

	}
	
	public JSONArray includeQueueSubModuleMenu(JSONArray menu,List<String> entry) {
        // Queue
        JSONObject queue = new JSONObject();
        queue.put("title", "Queue");
        queue.put("icon", "more-horizontal-outline");
        JSONArray queueChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
        	   if(entry.get(i).equals(FLATTEN_MENU.queuedetails.name())) {
        	       JSONObject queueDetails = new JSONObject();
        	       queueDetails.put("title", "Details");
        	       queueDetails.put("link", "/pages/queue/all-queues");
        	       queueChildren.put(queueDetails);
        	   }
           }
        
        queue.put("children", queueChildren);
        menu.put(queue);
        
        return menu;
	}
	

	public JSONArray generateOrganizationModuleMenu(JSONArray menu) {
		

        // Organization Module Group
        JSONObject organizationModuleGroup = new JSONObject();
        organizationModuleGroup.put("title", "Organization Module");
        organizationModuleGroup.put("group", true);
        menu.put(organizationModuleGroup);

        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;
        
	
	}

   public JSONArray includeEmployeeSubModuleMenu(JSONArray menu,List<String> entry) {
	   
       // Employee
       JSONObject employee = new JSONObject();
       employee.put("title", "Employee");
       employee.put("icon", "person-outline");
       JSONArray employeeChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.employeeprofile.name())) {
       	       JSONObject profile = new JSONObject();
       	       profile.put("title", "Profile");
       	       profile.put("link", "/pages/employee/profile");
       	       employeeChildren.put(profile);
       	   }
       	   else if(entry.get(i).equals(FLATTEN_MENU.employeecallhistory.name())) {
	       		  JSONObject callHistory = new JSONObject();
	              callHistory.put("title", "Call History");
	              callHistory.put("link", "/pages/employee/employee-call-history");
	              employeeChildren.put(callHistory);
           	   }
       	   else if(entry.get(i).equals(FLATTEN_MENU.employeedetails.name())) {
	       	       JSONObject allEmployees = new JSONObject();
	       	       allEmployees.put("title", "All Employees");
	       	       allEmployees.put("link", "/pages/employee/all-employees");
	       	       employeeChildren.put(allEmployees);
           	   }
       	   else if(entry.get(i).equals(FLATTEN_MENU.employeemonitor.name())) {
	       		   // Monitor Employees
	       		   JSONObject monitorEmployees = new JSONObject();
	       		   monitorEmployees.put("title", "Monitor Employees");
	       		   monitorEmployees.put("link", "/pages/employee/monitor-employees");
	       		   employeeChildren.put(monitorEmployees);
           	   }
          }
	   employee.put("children", employeeChildren);
	   menu.put(employee);
	   
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includeFileStoreSubModuleMenu(JSONArray menu,List<String> entry) {

       // File Storage
       JSONObject fileStorage = new JSONObject();
       fileStorage.put("title", "File Storage");
       fileStorage.put("icon", "briefcase-outline");
       JSONArray fileStorageChildren = new JSONArray();
       
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.filestoredetails.name())) {
       	      JSONObject allFiles = new JSONObject();
       	      allFiles.put("title", "All Files");
       	      allFiles.put("link", "/pages/file-storage/all-files");
       	      fileStorageChildren.put(allFiles);
       	   }
          }
       
       fileStorage.put("children", fileStorageChildren);
       menu.put(fileStorage);
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includeAbsenteeismSubModuleMenu(JSONArray menu,List<String> entry) {
		 
		 
	   // Absenteeism
       JSONObject absenteeism = new JSONObject();
       absenteeism.put("title", "Absenteeism");
       absenteeism.put("icon", "close-square-outline");
       JSONArray absenteeismChildren = new JSONArray();
       
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.myabsenteeism.name())) {
       		  JSONObject myAbsenteeism = new JSONObject();
              myAbsenteeism.put("title", "My Absenteeism");
              myAbsenteeism.put("link", "/pages/absenteeism/my-absenteeism");
              absenteeismChildren.put(myAbsenteeism);
       	   }
       	   else if(entry.get(i).equals(FLATTEN_MENU.allabsenteeism.name())) {
	       		JSONObject allAbsenteeism = new JSONObject();
	            allAbsenteeism.put("title", "All Absenteeism");
	            allAbsenteeism.put("link", "/pages/absenteeism/all-absenteeism");
	            absenteeismChildren.put(allAbsenteeism);
       	   }
          }
       
       absenteeism.put("children", absenteeismChildren);
       menu.put(absenteeism);
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includeDepartmentSubModuleMenu(JSONArray menu,List<String> entry) {

       // Department
       JSONObject department = new JSONObject();
       department.put("title", "Department");
       department.put("icon", "smartphone-outline");
       JSONArray departmentChildren = new JSONArray();
       
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.departmentdetails.name())) {
       	       JSONObject departmentDetails = new JSONObject();
       	       departmentDetails.put("title", "Details");
       	       departmentDetails.put("link", "/pages/department/all-departments");
       	       departmentChildren.put(departmentDetails);
       	   }
          }

       department.put("children", departmentChildren);
       menu.put(department);

	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includeCustomerSubModuleMenu(JSONArray menu,List<String> entry) {
       // Customer
       JSONObject customer = new JSONObject();
       customer.put("title", "Customer");
       customer.put("icon", "person-add-outline");
       JSONArray customerChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.customerautodialpreview.name())) {
       		  JSONObject autodialPreview = new JSONObject();
              autodialPreview.put("title", "Autodial Preview");
              autodialPreview.put("link", "/pages/customer/preview-customers");
              customerChildren.put(autodialPreview);
       	   }
       	   else if(entry.get(i).equals(FLATTEN_MENU.customerdetails.name())) {
       		JSONObject allCustomers = new JSONObject();
            allCustomers.put("title", "All Customers");
            allCustomers.put("link", "/pages/customer/all-customers");
            customerChildren.put(allCustomers);
       	   }
          }

   	   
  	   customer.put("children", customerChildren);
       menu.put(customer);
       
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includeProductSubModuleMenu(JSONArray menu,List<String> entry) {
		 
       // Product
       JSONObject product = new JSONObject();
       product.put("title", "Product");
       product.put("icon", "archive-outline");
       JSONArray productChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.productdetails.name())) {
       	       JSONObject productDetails = new JSONObject();
       	       productDetails.put("title", "Details");
       	       productDetails.put("link", "/pages/product/all-products");
       	       productChildren.put(productDetails);
       	   }
          }

   	   
	   product.put("children", productChildren);
       menu.put(product);
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includePurchaseSubModuleMenu(JSONArray menu,List<String> entry) {
		 
	   // Purchase
       JSONObject purchase = new JSONObject();
       purchase.put("title", "Purchase");
       purchase.put("icon", "clipboard-outline");
       JSONArray purchaseChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.purchasedetails.name())) {
       	       JSONObject purchaseDetails = new JSONObject();
       	       purchaseDetails.put("title", "Details");
       	       purchaseDetails.put("link", "/pages/purchase/all-purchases");
       	       purchaseChildren.put(purchaseDetails);
       	   }
          }

   	   
	   purchase.put("children", purchaseChildren);
       menu.put(purchase);
       
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }
   
   public JSONArray includeSupplierSubModuleMenu(JSONArray menu,List<String> entry) {
       
       // Supplier
       JSONObject supplier = new JSONObject();
       supplier.put("title", "Supplier");
       supplier.put("icon", "credit-card-outline");
       JSONArray supplierChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.supplierdetails.name())) {
       	       JSONObject supplierDetails = new JSONObject();
       	       supplierDetails.put("title", "Details");
       	       supplierDetails.put("link", "/pages/supplier/all-suppliers");
       	       supplierChildren.put(supplierDetails);
       	   }
          }
       
   	   
	   supplier.put("children", supplierChildren);
       menu.put(supplier);
       
	   // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
	   return menu;
		 
   }

   
   public JSONArray generateIssueTrackingModuleMenuTop(JSONArray menu) {
    	
        // Issue Tracking Group
        JSONObject issueTrackingGroup = new JSONObject();
        issueTrackingGroup.put("title", "Issue Tracking Module");
        issueTrackingGroup.put("group", true);
        menu.put(issueTrackingGroup);
        
        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;
	
   }

   public JSONArray includeErrorSubModuleMenu(JSONArray menu,List<String> entry) {

        // Errors
        JSONObject errors = new JSONObject();
        errors.put("title", "Errors");
        errors.put("icon", "slash-outline");
        JSONArray errorsChildren = new JSONArray();
        for(int i = 0; i<entry.size();i++) {
       	   if(entry.get(i).equals(FLATTEN_MENU.errordetails.name())) {
               JSONObject errorDetails = new JSONObject();
               errorDetails.put("title", "Details");
               errorDetails.put("link", "/pages/error/all-errors");
               errorsChildren.put(errorDetails);
       	   }
          }

    	   
        errors.put("children", errorsChildren);
        menu.put(errors);

        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;
	
 	}

   public JSONArray includeLogsSubModuleMenu(JSONArray menu,List<String> entry) {

     // Logs
     JSONObject logs = new JSONObject();
     logs.put("title", "Logs");
     logs.put("icon", "save-outline");
     JSONArray logsChildren = new JSONArray();
     for(int i = 0; i<entry.size();i++) {
  	   if(entry.get(i).equals(FLATTEN_MENU.logdetails.name())) {
  	     JSONObject logDetails = new JSONObject();
  	     logDetails.put("title", "Details");
  	     logDetails.put("link", "/pages/log/all-logs");
  	     logsChildren.put(logDetails);
  	   }
     }
     
	 logs.put("children", logsChildren);
     menu.put(logs);


     // Final JSON structure
    LoggerUtils.log.debug(menu.toString());
     
     return menu;
	
 	}


   
   public JSONArray generateSettingModuleMenuTop(JSONArray menu) {
    	
        // Settings Group
        JSONObject settingsGroup = new JSONObject();
        settingsGroup.put("title", "Settings Module");
        settingsGroup.put("group", true);
        menu.put(settingsGroup);

        // Final JSON structure
       LoggerUtils.log.debug(menu.toString());
        
        return menu;       
   }
 

   public JSONArray includeAMISubModuleMenu(JSONArray menu,List<String> entry) {

       // AMI Connections
       JSONObject amiConnections = new JSONObject();
       amiConnections.put("title", "AMI CONNECTIONS");
       amiConnections.put("icon", "globe-2-outline");
       JSONArray amiConnectionsChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
    	   if(entry.get(i).equals(FLATTEN_MENU.sipregistries.name())) {
    	       JSONObject registries = new JSONObject();
    	       registries.put("title", "Registries");
    	       registries.put("link", "/pages/ami-connection/registries");
    	       amiConnectionsChildren.put(registries);
    	   }
       }
       
       amiConnections.put("children", amiConnectionsChildren);
       menu.put(amiConnections);

       // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
       
       return menu; 
	
   }

   
   public JSONArray includeSIPSubModuleMenu(JSONArray menu,List<String> entry) {

       // SIP Providers
       JSONObject sipProviders = new JSONObject();
       sipProviders.put("title", "SIP PROVIDERS");
       sipProviders.put("icon", "funnel-outline");
       JSONArray sipProvidersChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
    	   if(entry.get(i).equals(FLATTEN_MENU.sipregistries.name())) {
    	       JSONObject sipRegistries = new JSONObject();
    	       sipRegistries.put("title", "Registries");
    	       sipRegistries.put("link", "/pages/sip-provider/registries");
    	       sipProvidersChildren.put(sipRegistries);
    	   }
       }
       
       sipProviders.put("children", sipProvidersChildren);
       menu.put(sipProviders);

       // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
       
       return menu;
       
   }

   
   public JSONArray includeSSHSubModuleMenu(JSONArray menu,List<String> entry) {
   	
       // SSH Connections
       JSONObject sshConnections = new JSONObject();
       sshConnections.put("title", "SSH CONNECTIONS");
       sshConnections.put("icon", "globe-outline");
       JSONArray sshConnectionsChildren = new JSONArray();
       for(int i = 0; i<entry.size();i++) {
    	   if(entry.get(i).equals(FLATTEN_MENU.sshregistries.name())) {
    	       JSONObject sshRegistries = new JSONObject();
    	       sshRegistries.put("title", "Registries");
    	       sshRegistries.put("link", "/pages/ssh-connection/registries");
    	       sshConnectionsChildren.put(sshRegistries);
    	   }
       }
       
       sshConnections.put("children", sshConnectionsChildren);
       menu.put(sshConnections);

       // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
       
       return menu;
	
   }
 
   
   public JSONArray includeAuthSubModuleMenu(JSONArray menu,List<String> entry) {
       
       // Auth
       JSONObject auth = new JSONObject();
       auth.put("title", "Auth");
       auth.put("icon", "lock-outline");
       JSONArray authChildren = new JSONArray();
       
       for(int i = 0; i<entry.size();i++) {
    	   if(entry.get(i).equals(FLATTEN_MENU.authresetpassword.name())) {
        	   JSONObject resetPassword = new JSONObject();
               resetPassword.put("title", "Reset Password");
               resetPassword.put("link", "/pages/reset-password/reset");
               authChildren.put(resetPassword);
    	   }
       }

       auth.put("children", authChildren);
       menu.put(auth);

       // Final JSON structure
      LoggerUtils.log.debug(menu.toString());
       
       return menu;
       
   }
   
   public JSONArray generatePropertyManagementModuleMenu(JSONArray menu) {

	    // Property Management Group
	    JSONObject group = new JSONObject();
	    group.put("title", "Property Management");
	    group.put("group", true);
	    menu.put(group);

	    LoggerUtils.log.debug(menu.toString());
	    return menu;
	}

	public JSONArray includePropertyInventorySubModuleMenu(JSONArray menu, List<String> entry) {

	    // Inventory (parent item)
	    JSONObject inventory = new JSONObject();
	    inventory.put("title", "Inventory");
	    inventory.put("icon", "home-outline"); // you can change later

	    JSONArray children = new JSONArray();

	    for (int i = 0; i < entry.size(); i++) {
	        if (entry.get(i).equals(FLATTEN_MENU.propertyinventorylisting.name())) {
	            JSONObject listing = new JSONObject();
	            listing.put("title", "Inventory Listing");
	            listing.put("link", "/pages/property-management/inventory-listing"); // must match Angular route
	            children.put(listing);
	        }
	    }

	    inventory.put("children", children);
	    menu.put(inventory);

	    LoggerUtils.log.debug(menu.toString());
	    return menu;
	}
	
	public JSONArray generateFranchiseManagementModuleMenu(JSONArray menu) {

	    // Franchise Management Group
	    JSONObject group = new JSONObject();
	    group.put("title", "Franchise Management");
	    group.put("group", true);
	    menu.put(group);

	    LoggerUtils.log.debug(menu.toString());
	    return menu;
	}

	public JSONArray includeFranchiseManagementSubModuleMenu(JSONArray menu, List<String> entry) {

	    JSONObject franchise = new JSONObject();
	    franchise.put("title", "Franchise");
	    franchise.put("icon", "people-outline"); // change icon if you want

	    JSONArray children = new JSONArray();

	    for (int i = 0; i < entry.size(); i++) {
	        if (entry.get(i).equals(FLATTEN_MENU.franchisecalllisting.name())) {
	            JSONObject listing = new JSONObject();
	            listing.put("title", "Call Listing");
	            listing.put("link", "/pages/franchise-management/franchise-call-listing");
	            children.put(listing);
	        }
	    }

	    franchise.put("children", children);
	    menu.put(franchise);

	    LoggerUtils.log.debug(menu.toString());
	    return menu;
	}



}
