package com.mylinehub.crm.ami.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.entity.CallDetail;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Organization;

import lombok.Data;

@Data
public class TransferCdrMainMemoryToBackupRunnable implements Runnable {

    @Override
    public void run() {

        System.out.println("TransferCdrMainMemoryToBackupRunnable");

        // Clear previous backup first (safe)
        System.out.println("Deleting previous backup data before transfer");
        DeleteCdrMemoryCollectionRunnable deleteCdrMemoryCollectionRunnable = new DeleteCdrMemoryCollectionRunnable();
        deleteCdrMemoryCollectionRunnable.run();

        // get() should return SNAPSHOT from CDRMemoryCollection
        System.out.println("Fetching current CDR Data");
        Map<String, CdrDTO> interimRecords = CDRMemoryCollection.workWithCDRInterimData(null, null, "get");

        if (interimRecords == null) {
            System.out.println("Transfer CDR Data : interimRecords is null");
            return;
        }

        System.out.println("Transfer CDR Data : interimRecords size before transfer : " + interimRecords.size());
        System.out.println("Starting loop to transfer CDR Records");

        for (Map.Entry<String, CdrDTO> entry : interimRecords.entrySet()) {

            String callId = entry.getKey();
            CdrDTO cdrDTO = entry.getValue();

            if (callId == null || cdrDTO == null) {
                continue;
            }

            Date lastUpdated = cdrDTO.getLastUpdated();
            if (lastUpdated == null) {
                // If lastUpdated is missing, treat as stale after 30 min from "now - 30"
                lastUpdated = new Date();
                cdrDTO.setLastUpdated(lastUpdated);
            }

            CallDetail callDetail = cdrDTO.getCallDetail();
            Map<String, String> mapEvent = cdrDTO.getMapEvent();
            Employee employee = cdrDTO.getEmployee();

            // If call detail or mapEvent missing → keep short TTL cleanup rule
            if (callDetail == null || mapEvent == null) {
                System.out.println("Transfer Data : CDR incomplete (callDetail/mapEvent null)");
                boolean oldEnough = isAtleastThirtyMinutesAgo(lastUpdated);
                if (oldEnough) {
                    Map<String, CdrDTO> deleteRecords = new HashMap<>();
                    deleteRecords.put(callId, cdrDTO);
                    CDRMemoryCollection.workWithCDRInterimData(null, deleteRecords, "delete");
                }
                continue;
            }

            System.out.println("Transfer Data : CDR complete");

            if (employee == null) {
                System.out.println("Employee is null. Need to define an employee for common cases");

                // Resolve organization
                Organization currentOrganization = null;
                Map<String, Organization> organizationMap =
                        OrganizationData.workWithAllOrganizationData(callDetail.getOrganization(), null, "get-one", null);

                if (organizationMap != null) {
                    currentOrganization = organizationMap.get(callDetail.getOrganization());
                }

                if (currentOrganization != null) {
                    System.out.println("Transfer CDR : Organization is not null");

                    boolean oldEnough = isAtleastThreeMinutesAgo(lastUpdated);
                    if (oldEnough) {
                        System.out.println("Transfer CDR : Record is old enough to move to backup (employee null case)");

                        // delete from interim
                        Map<String, CdrDTO> record = new HashMap<>();
                        record.put(callId, cdrDTO);
                        CDRMemoryCollection.workWithCDRInterimData(null, record, "delete");

                        // create placeholder employee
                        Employee organizationEmployee = new Employee();
                        organizationEmployee.setExtension("N/A");
                        organizationEmployee.setOrganization(currentOrganization.getOrganization());
                        organizationEmployee.setCostCalculation(currentOrganization.getCostCalculation());
                        organizationEmployee.setPhoneContext(currentOrganization.getPhoneContext());

                        cdrDTO.setEmployee(organizationEmployee);

                        // update backup
                        Map<String, CdrDTO> toBackup = new HashMap<>();
                        toBackup.put(callId, cdrDTO);
                        CDRMemoryCollection.workWithCDRBackupInterimData(null, toBackup, "update");
                    }
                } else {
                    System.out.println("No Organization found, hence deleting this CDR");
                    Map<String, CdrDTO> deleteRecords = new HashMap<>();
                    deleteRecords.put(callId, cdrDTO);
                    CDRMemoryCollection.workWithCDRInterimData(null, deleteRecords, "delete");
                }

            } else {
                // Normal path: employee not null → move after timeout
                System.out.println("Transfer Data : Employee not null");

                boolean oldEnough = isAtleastThreeMinutesAgo(lastUpdated);
                if (oldEnough) {
                    System.out.println("Result is true and can be moved to backup");

                    Map<String, CdrDTO> record = new HashMap<>();
                    record.put(callId, cdrDTO);

                    CDRMemoryCollection.workWithCDRInterimData(null, record, "delete");
                    CDRMemoryCollection.workWithCDRBackupInterimData(null, record, "update");
                }
            }
        }

        System.out.println("Transfer CDR Data : interimRecords size after transfer snapshot : " + interimRecords.size());
    }

    // Keeping your original semantics (you named it 3 minutes but used 4 minutes).
    // To avoid changing behavior, we keep 4 minutes exactly.
    private static boolean isAtleastThreeMinutesAgo(Date date) {
        if (date == null) return false;
        Instant instant = Instant.ofEpochMilli(date.getTime());
        Instant fourMinutesAgo = Instant.now().minus(Duration.ofMinutes(4));
        return instant.isBefore(fourMinutesAgo);
    }

    private static boolean isAtleastThirtyMinutesAgo(Date date) {
        if (date == null) return false;
        Instant instant = Instant.ofEpochMilli(date.getTime());
        Instant thirtyMinutesAgo = Instant.now().minus(Duration.ofMinutes(30));
        return instant.isBefore(thirtyMinutesAgo);
    }
}
