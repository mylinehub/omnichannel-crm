package com.mylinehub.crm.TaskScheduler;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.dto.BulkUploadEmployeeDto;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.service.AdminService;
import com.mylinehub.crm.service.EmployeeService;
import com.mylinehub.crm.service.OrganizationService;
import com.mylinehub.crm.utils.DateService;

import lombok.Data;

@Data
public class LowAmountNotificationRunnable implements Runnable {

    private ApplicationContext applicationContext;
    String jobId;

    @Override
    public void run() {
        System.out.println("Inside LowAmountNotificationRunnable, jobId: " + jobId);

        EmployeeService employeeService = applicationContext.getBean(EmployeeService.class);
        EmployeeRepository employeeRepository = applicationContext.getBean(EmployeeRepository.class);
        OrganizationService organizationService = applicationContext.getBean(OrganizationService.class);
        Environment env = applicationContext.getBean(Environment.class);
        AdminService adminService = applicationContext.getBean(AdminService.class);
        DateService dateService = applicationContext.getBean(DateService.class);

        try {
            System.out.println("Fetching all organizations...");
            Map<String, Organization> allOrgMap = OrganizationData.workWithAllOrganizationData(null, null, "get", null);

            if (allOrgMap != null && !allOrgMap.isEmpty()) {
                System.out.println("Total organizations fetched: " + allOrgMap.size());

                for (Map.Entry<String, Organization> entry : allOrgMap.entrySet()) {
                    Organization organization = entry.getValue();

                    if (organization.isActivated()) {
                        System.out.println("Organization is activated: " + organization.getOrganization());
                        System.out.println("TotalWhatsAppMessagesAmount: " + organization.getTotalWhatsAppMessagesAmount());
                        System.out.println("TotalWhatsAppMessagesAmountSpend: " + organization.getTotalWhatsAppMessagesAmountSpend());

                        long balanceLeft = (long) (organization.getTotalWhatsAppMessagesAmount() - organization.getTotalWhatsAppMessagesAmountSpend());

                        if (balanceLeft < 50L) {
                            System.out.println("Organization eligible for low amount notification: " + organization.getOrganization());
                            // Prepare to send low balance WhatsApp and email notifications

                            Employee admin = new Employee();
                            admin.setEmail(organization.getEmail());
                            admin.setFirstName(organization.getOrganization());
                            admin.setLastName("");

                            String rechargerequiredTemplate = env.getProperty("spring.template.rechargerequired");
                            if (rechargerequiredTemplate != null) {
                                System.out.println("Warning: rechargerequiredTemplate is not null");
                                BulkUploadEmployeeDto bulkUploadEmployeeDto = new BulkUploadEmployeeDto();
                                bulkUploadEmployeeDto.setActualPassword("");
                                bulkUploadEmployeeDto.setEmployee(admin);

                                System.out.println("Sending WhatsApp low balance message...");
                                employeeService.sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(bulkUploadEmployeeDto, rechargerequiredTemplate, String.valueOf(organization.getTotalWhatsAppMessagesAmount()), "0", "url");
                                System.out.println("Sending low fund email...");
                                adminService.sendLowFundEmail(organization.getOrganization(), organization.getEmail());
                            }

                           
                        }

                        if (dateService.isXDaysAgo(45, organization.getLastRechargedOn())) {
                            System.out.println("More than 45 days since last recharge for organization: " + organization.getOrganization());
                            System.out.println("Deactivating organization and its employees...");

                            organization.setActivated(false);
                            organizationService.update(organization);

                            List<Employee> allEmployees = employeeService.findAllEmployeesOnIsEnabledAndOrganization(true, organization.getOrganization());

                            System.out.println("Employees count to deactivate: " + (allEmployees == null ? 0 : allEmployees.size()));

                            if (allEmployees != null && !allEmployees.isEmpty()) {
                                for (int i = 0; i < allEmployees.size(); i++) {
                                    Employee current = allEmployees.get(i);
                                    current.setIsEnabled(false);
                                    allEmployees.set(i, current);
                                    EmployeeDataAndState.workOnAllEmployeeDataAndState(current.getExtension(), null, "delete");
                                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(current.getPhonenumber(), current.getExtension(), "delete");
                                }
                                employeeRepository.saveAll(allEmployees);
                                System.out.println("Employees deactivated and saved.");
                            }

                            System.out.println("Sending deactivation notifications to organization...");

                            Employee organizationAdmin = new Employee();
                            organizationAdmin.setEmail(organization.getEmail());
                            organizationAdmin.setFirstName(organization.getOrganization());
                            organizationAdmin.setPhonenumber(organization.getPhoneNumber());
                            organizationAdmin.setOrganization(organization.getOrganization());
                            organizationAdmin.setLastName("");

                            String accountDeactivatedTemplate = env.getProperty("spring.template.accountDeactivated");
                            if (accountDeactivatedTemplate == null) {
                                System.out.println("Warning: accountDeactivatedTemplate is null");
                                accountDeactivatedTemplate = "defaultDeactivatedTemplate";
                            }

                            String parentOrg = env.getProperty("spring.parentorginization");
                            String parentPhone = env.getProperty("spring.parentorginization.phone");
                            String parentEmail = env.getProperty("spring.parentorginization.email");

                            BulkUploadEmployeeDto bulkUploadEmployeeDto = new BulkUploadEmployeeDto();
                            bulkUploadEmployeeDto.setActualPassword("");
                            bulkUploadEmployeeDto.setEmployee(organizationAdmin);
                            bulkUploadEmployeeDto.setParentOrg(parentOrg);
                            bulkUploadEmployeeDto.setReason("low balance from past 30 days. Kindly contact support.");

                            System.out.println("Sending WhatsApp account deactivated message to organization admin...");
                            employeeService.sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(bulkUploadEmployeeDto, accountDeactivatedTemplate, String.valueOf(organization.getTotalWhatsAppMessagesAmount()), "0", "url");
                            System.out.println("Sending account deactivated email to organization admin...");
                            adminService.sendAccountDeactivatedEmail(organization.getOrganization(), organization.getEmail());

                            System.out.println("Sending deactivation notification to mylinehub admin...");

                            // Notify product admin as well
                            organizationAdmin.setPhonenumber(parentPhone);
                            organizationAdmin.setEmail(parentEmail);
                            bulkUploadEmployeeDto.setEmployee(organizationAdmin);

                            employeeService.sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(bulkUploadEmployeeDto, accountDeactivatedTemplate, String.valueOf(organization.getTotalWhatsAppMessagesAmount()), "0", "url");
                            adminService.sendAccountDeactivatedEmail(organization.getOrganization(), parentEmail);

                            System.out.println("Deactivation notifications sent successfully.");
                        }
                    } else {
                        System.out.println("Organization is NOT activated: " + organization.getOrganization());
                    }
                }
            } else {
                System.out.println("No organizations found.");
            }
        } catch (Exception e) {
            System.out.println("Exception in LowAmountNotificationRunnable:");
            e.printStackTrace();
        }
        System.out.println("LowAmountNotificationRunnable completed, jobId: " + jobId);
    }
}
