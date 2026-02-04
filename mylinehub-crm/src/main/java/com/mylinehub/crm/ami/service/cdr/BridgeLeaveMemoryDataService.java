package com.mylinehub.crm.ami.service.cdr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.data.CDRMemoryCollection;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.dto.CdrDTO;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.enums.BRIDGE_LEAVE_EVENT;
import com.mylinehub.crm.enums.DEVICE_STATES;

import lombok.AllArgsConstructor;

/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class BridgeLeaveMemoryDataService {

    private AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;
    
    private static final boolean ASYNC_SIDE_EFFECTS = true;
    private static final ExecutorService SIDE_EFFECT_EXEC =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "cdr-sidefx-bridgeleave");
                t.setDaemon(true);
                return t;
            });

    public boolean insertBridgeLeaveDataIntoCdrMemoryData(String organization, Map<String, String> mapEvent) {
        System.out.println("Inside insertBridgeLeaveDataIntoCdrMemoryData");
        boolean toReturn = true;

        try {
            if (mapEvent == null) return false;

            String linkedId = mapEvent.get(BRIDGE_LEAVE_EVENT.linkedid.name());
            String calleridnum = mapEvent.get(BRIDGE_LEAVE_EVENT.calleridnum.name());

            System.out.println("linkedId : " + linkedId);
            System.out.println("calleridnum : " + calleridnum);

            Map<String, CdrDTO> allValues = CDRMemoryCollection.workWithCDRInterimData(linkedId, null, "get-one");
            CdrDTO cdrDTO = null;
            if (allValues != null) cdrDTO = allValues.get(linkedId);

            if (cdrDTO == null) {
                System.out.println("This should not happen, this is Bridge enter event , should be after new line connected");
                return true;
            }

            System.out.println("CDR was not null");

            boolean isPhone = false;
            boolean isTrunkPhone = false;

            if (calleridnum != null) {
                System.out.println("calleridnum is not null");

                // lookup once
                String extension = getExtensionForPhone(calleridnum);
                System.out.println("After fetching extension");

                isPhone = (extension != null);

                if (cdrDTO.getTrunkNumber() != null) {
                    isTrunkPhone = cdrDTO.getTrunkNumber().equals(calleridnum);
                }

                System.out.println("After isTrunkPhone");

                if (cdrDTO.isPridictive() && isPhone) {
                    System.out.println("Call is pridictive and isPhone");
                    if (cdrDTO.getEmployee() != null) {
                        System.out.println("Pridictive call, employee is going into not in use state");
                        final Employee emp = cdrDTO.getEmployee();
                        runSideEffect(() ->
                                this.autodialerReinitiateAndFunctionService.changeEmployeeState(DEVICE_STATES.NOT_INUSE.name(), emp));
                    }
                }

                if (cdrDTO.isProgressive() && isTrunkPhone) {
                    System.out.println("Call is progressive and isTrunkPhone");
                    if (cdrDTO.getEmployee() != null) {
                        System.out.println("Progressive call, employee is going into not in use state");
                        final Employee emp = cdrDTO.getEmployee();
                        runSideEffect(() ->
                                this.autodialerReinitiateAndFunctionService.changeEmployeeState(DEVICE_STATES.NOT_INUSE.name(), emp));
                    }
                }

                System.out.println("Setting count of bridge leave event");
                cdrDTO.setNoOfBridgeLeave(cdrDTO.getNoOfBridgeLeave() + 1);

                System.out.println("Setting last update date");
                cdrDTO.setLastUpdated(new Date());

                System.out.println("Putting value in CDR");
                Map<String, CdrDTO> values = new HashMap<>();
                values.put(linkedId, cdrDTO);
                CDRMemoryCollection.workWithCDRInterimData(linkedId, values, "update");
            } else {
                System.out.println("Caller Id is null");
            }

        } catch (Exception e) {
            toReturn = false;
            e.printStackTrace();
        }

        return toReturn;
    }

    private void runSideEffect(Runnable r) {
        if (!ASYNC_SIDE_EFFECTS) {
            try { r.run(); } catch (Exception ignore) {}
            return;
        }
        try {
            SIDE_EFFECT_EXEC.submit(() -> {
                try { r.run(); } catch (Exception e) { e.printStackTrace(); }
            });
        } catch (Exception e) {
            try { r.run(); } catch (Exception ignore) {}
        }
    }

    private String getExtensionForPhone(String phone) {
        try {
            Map<String, String> one =
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(phone, null, "get-one");
            if (one != null) return one.get(phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
