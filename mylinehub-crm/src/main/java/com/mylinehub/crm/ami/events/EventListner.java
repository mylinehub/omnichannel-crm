package com.mylinehub.crm.ami.events;

import java.util.Map;

import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.ManagerEvent;
import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.ami.autodialer.AutodialerReinitiateAndFunctionService;
import com.mylinehub.crm.ami.service.cdr.BridgeLeaveMemoryDataService;
import com.mylinehub.crm.ami.service.cdr.BridgedEnterMemoryDataService;
import com.mylinehub.crm.ami.service.cdr.CdrMemoryDataService;
import com.mylinehub.crm.ami.service.cdr.NewLineMemoryDataService;
import com.mylinehub.crm.data.CurrentConnections;
import com.mylinehub.crm.enums.ASTERISK_EVENTS;
import com.mylinehub.crm.utils.LoggerUtils;

public class EventListner implements ManagerEventListener {

    // Deep logs switch (TRUE for now as requested)
    private static final boolean DEEP_LOGS = true;

    private AutodialerReinitiateAndFunctionService autodialerReinitiateAndFunctionService;
    private NewLineMemoryDataService newLineMemoryDataService;
    private BridgedEnterMemoryDataService bridgedEnterMemoryDataService;
    private BridgeLeaveMemoryDataService bridgeLeaveMemoryDataService;
    private CdrMemoryDataService cdrMemoryDataService;

    String domain;
    String amiUser;
    String organization;

    public EventListner(String organization, String domain, String amiUser, ApplicationContext applicationContext) {

        this.organization = organization;
        this.domain = domain;
        this.amiUser = amiUser;

        this.newLineMemoryDataService = applicationContext.getBean(NewLineMemoryDataService.class);
        this.bridgedEnterMemoryDataService = applicationContext.getBean(BridgedEnterMemoryDataService.class);
        this.bridgeLeaveMemoryDataService = applicationContext.getBean(BridgeLeaveMemoryDataService.class);
        this.cdrMemoryDataService = applicationContext.getBean(CdrMemoryDataService.class);
        this.autodialerReinitiateAndFunctionService = applicationContext.getBean(AutodialerReinitiateAndFunctionService.class);
    }

    @Override
    public void onManagerEvent(ManagerEvent event) {

        try {
            if (event == null) {
                if (DEEP_LOGS) LoggerUtils.log.debug("EventListner: received null event");
                return;
            }
            
            CurrentConnections.markActivity(domain);


            Map<String, String> mapEvent = this.autodialerReinitiateAndFunctionService.eventToMap(event);

            if (DEEP_LOGS) {
                LoggerUtils.log.debug("EventListner: eventClass=" + event.getClass() + " mapEvent=" + mapEvent);
            }

            if (String.valueOf(event.getClass()).contains(ASTERISK_EVENTS.CdrEvent.name())) {

                LoggerUtils.log.debug("**************************  CdrEvent *****************************");
                if (DEEP_LOGS) {
                    System.out.println("CDR event : " + event);
                    System.out.println("CDR mapEvent : " + mapEvent);
                }

                try {
                    this.cdrMemoryDataService.insertCdrEventDataIntoCdrMemoryData(organization, mapEvent);
                } catch (Exception e) {
                    LoggerUtils.log.debug("CdrEvent insert exception: " + e.getMessage());
                    e.printStackTrace();
                }

            } else if (String.valueOf(event.getClass()).contains(ASTERISK_EVENTS.DeviceStateChangeEvent.name())) {

                LoggerUtils.log.debug("**************************  DeviceStateChangeEvent *****************************");
                this.autodialerReinitiateAndFunctionService.changeDeviceState(mapEvent);

            } else if (String.valueOf(event.getClass()).contains(ASTERISK_EVENTS.NewConnectedLineEvent.name())) {

                LoggerUtils.log.debug("*****************NewConnectedLineEvent****************");
                this.newLineMemoryDataService.insertNewLineDataIntoCdrMemoryData(organization, mapEvent);

            } else if (String.valueOf(event.getClass()).contains(ASTERISK_EVENTS.BridgeEnterEvent.name())) {

                LoggerUtils.log.debug("****************BridgeEntryEvent*****************");
                this.bridgedEnterMemoryDataService.insertBridgeEnterDataIntoCdrMemoryData(organization, mapEvent);

            } else if (String.valueOf(event.getClass()).contains(ASTERISK_EVENTS.BridgeLeaveEvent.name())) {

                LoggerUtils.log.debug("***************BridgeLeaveEvent******************");
                this.bridgeLeaveMemoryDataService.insertBridgeLeaveDataIntoCdrMemoryData(organization, mapEvent);

            } else {
                // ignore other events (same as before)
                if (DEEP_LOGS) {
                    LoggerUtils.log.debug("EventListner: ignored eventClass=" + event.getClass());
                }
            }

        } catch (Exception e) {
            LoggerUtils.log.debug("EventListner.onManagerEvent Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
