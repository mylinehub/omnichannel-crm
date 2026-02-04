package com.mylinehub.crm.ami;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.*;
import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.ami.events.EventListner;
import com.mylinehub.crm.data.CurrentConnections;
import com.mylinehub.crm.utils.LoggerUtils;

public class ConnectionStream {

    /**
     * Create (or reuse) a ManagerConnection for a domain.
     *
     * Requirements you asked:
     * - If domain AMI already exists AND is usable => just return it (no new factory/connection).
     * - Else create new connection, attach listener, keep all deregister/register lines, store in CurrentConnections.
     *
     * NOTE: we don't call login() here (same as your old flow). Caller does login().
     * @throws TimeoutException 
     * @throws AuthenticationFailedException 
     * @throws IOException 
     * @throws IllegalStateException 
     */
    public ManagerConnection createConnection(String organization, String domain, String user, String password, ApplicationContext context) throws IllegalStateException, IOException, AuthenticationFailedException, TimeoutException {
        // 1) Verify-only path: if already exists and looks usable, return it.
        ManagerConnection existing = CurrentConnections.getConnectionIfUsable(domain);
        if (existing != null) {
        	System.out.println("Returning existing AMI manager connected client");
            return existing;
        }
        else {
        	System.out.println("Creating new AMI manager");
        }
        
        // 2) Create new factory/connection
        ManagerConnectionFactory factory = new ManagerConnectionFactory(domain, user, password);
        LoggerUtils.log.debug("Creating manager connection factory");
        ManagerConnection managerConnection = factory.createManagerConnection();

        // 3) Add listener
        EventListner eventListner = new EventListner(organization, domain, user, context);
        managerConnection.addEventListener(eventListner);
        
        // 4) Keep ALL your deregister/register lines (unchanged)
        // -----------------------------------------------
        managerConnection.deregisterEventClass(BridgeDestroyEvent.class);
        managerConnection.deregisterEventClass(MixMonitorStartEvent.class);
        managerConnection.deregisterEventClass(MessageWaitingEvent.class);
        managerConnection.deregisterEventClass(AbstractAgentEvent.class);
        managerConnection.deregisterEventClass(AbstractBridgeEvent.class);
        managerConnection.deregisterEventClass(AbstractChannelEvent.class);
        managerConnection.deregisterEventClass(AbstractChannelStateEvent.class);
        managerConnection.deregisterEventClass(AbstractChannelTalkingEvent.class);
        managerConnection.deregisterEventClass(AbstractConfbridgeEvent.class);
        managerConnection.deregisterEventClass(AbstractFaxEvent.class);
        managerConnection.deregisterEventClass(AbstractHoldEvent.class);
        managerConnection.deregisterEventClass(AbstractMeetMeEvent.class);
        managerConnection.deregisterEventClass(AbstractMixMonitorEvent.class);
        managerConnection.deregisterEventClass(AbstractMonitorEvent.class);
        managerConnection.deregisterEventClass(AbstractParkedCallEvent.class);
        managerConnection.deregisterEventClass(AbstractQueueMemberEvent.class);
        managerConnection.deregisterEventClass(AbstractRtcpEvent.class);
        managerConnection.deregisterEventClass(AbstractRtpStatEvent.class);
        managerConnection.deregisterEventClass(AbstractUnParkedEvent.class);
        managerConnection.deregisterEventClass(AgentCallbackLoginEvent.class);
        managerConnection.deregisterEventClass(AgentCallbackLogoffEvent.class);
        managerConnection.deregisterEventClass(AgentCalledEvent.class);
        managerConnection.deregisterEventClass(AgentCompleteEvent.class);
        managerConnection.deregisterEventClass(AgentConnectEvent.class);
        managerConnection.deregisterEventClass(AgentDumpEvent.class);
        managerConnection.deregisterEventClass(AgentLoginEvent.class);
        managerConnection.deregisterEventClass(AgentLogoffEvent.class);
        managerConnection.deregisterEventClass(AgentRingNoAnswerEvent.class);
        managerConnection.deregisterEventClass(AgentsCompleteEvent.class);
        managerConnection.deregisterEventClass(AgentsEvent.class);
        managerConnection.deregisterEventClass(AgiExecEndEvent.class);
        managerConnection.deregisterEventClass(AgiExecEvent.class);
        managerConnection.deregisterEventClass(AgiExecStartEvent.class);
        managerConnection.deregisterEventClass(AlarmClearEvent.class);
        managerConnection.deregisterEventClass(AlarmEvent.class);
        managerConnection.deregisterEventClass(AntennaLevelEvent.class);
        managerConnection.deregisterEventClass(AorDetail.class);
        managerConnection.deregisterEventClass(AsyncAgiEndEvent.class);
        managerConnection.deregisterEventClass(AsyncAgiEvent.class);
        managerConnection.deregisterEventClass(AsyncAgiExecEvent.class);
        managerConnection.deregisterEventClass(AsyncAgiStartEvent.class);
        managerConnection.deregisterEventClass(AttendedTransferEvent.class);
        managerConnection.deregisterEventClass(AuthDetail.class);
        managerConnection.deregisterEventClass(BlindTransferEvent.class);
        managerConnection.deregisterEventClass(BridgeCreateEvent.class);
        managerConnection.deregisterEventClass(BridgeCreateEvent.class);
        managerConnection.deregisterEventClass(BridgeEnterEvent.class);
        managerConnection.deregisterEventClass(BridgeEvent.class);
        managerConnection.deregisterEventClass(BridgeExecEvent.class);
        managerConnection.deregisterEventClass(BridgeExecEvent.class);
        managerConnection.deregisterEventClass(BridgeMergeEvent.class);
        // managerConnection.deregisterEventClass(CdrEvent.class);
        managerConnection.deregisterEventClass(CelEvent.class);
        managerConnection.deregisterEventClass(ChallengeResponseFailedEvent.class);
        managerConnection.deregisterEventClass(ChallengeSentEvent.class);
        managerConnection.deregisterEventClass(ChannelReloadEvent.class);
        managerConnection.deregisterEventClass(ChannelTalkingStartEvent.class);
        managerConnection.deregisterEventClass(ChannelTalkingStopEvent.class);
        managerConnection.deregisterEventClass(ChannelUpdateEvent.class);
        managerConnection.deregisterEventClass(ChanSpyStartEvent.class);
        managerConnection.deregisterEventClass(ChanSpyStopEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeEndEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeJoinEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeLeaveEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeListCompleteEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeListEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeListRoomsCompleteEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeListRoomsEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeStartEvent.class);
        managerConnection.deregisterEventClass(ConfbridgeTalkingEvent.class);
        managerConnection.deregisterEventClass(ConnectEvent.class);
        managerConnection.deregisterEventClass(ContactList.class);
        managerConnection.deregisterEventClass(ContactListComplete.class);
        managerConnection.deregisterEventClass(ContactStatusDetail.class);
        managerConnection.deregisterEventClass(ContactStatusEvent.class);
        managerConnection.deregisterEventClass(CoreShowChannelEvent.class);
        managerConnection.deregisterEventClass(CoreShowChannelsCompleteEvent.class);
        managerConnection.deregisterEventClass(DAHDIChannelEvent.class);
        managerConnection.deregisterEventClass(DahdiShowChannelsCompleteEvent.class);
        managerConnection.deregisterEventClass(DahdiShowChannelsEvent.class);
        managerConnection.deregisterEventClass(DbGetResponseEvent.class);
        // managerConnection.deregisterEventClass(DeviceStateChangeEvent.class);
        managerConnection.deregisterEventClass(DialBeginEvent.class);
        managerConnection.deregisterEventClass(DialEndEvent.class);
        managerConnection.deregisterEventClass(DialEvent.class);
        managerConnection.deregisterEventClass(DialStateEvent.class);
        managerConnection.deregisterEventClass(DialStateEvent.class);
        managerConnection.deregisterEventClass(DndStateEvent.class);
        managerConnection.deregisterEventClass(DongleCallStateChangeEvent.class);
        managerConnection.deregisterEventClass(DongleCENDEvent.class);
        managerConnection.deregisterEventClass(DongleDeviceEntryEvent.class);
        managerConnection.deregisterEventClass(DongleNewCMGREvent.class);
        managerConnection.deregisterEventClass(DongleNewSMSBase64Event.class);
        managerConnection.deregisterEventClass(DongleNewSMSEvent.class);
        managerConnection.deregisterEventClass(DongleShowDevicesCompleteEvent.class);
        managerConnection.deregisterEventClass(DongleStatusEvent.class);
        managerConnection.deregisterEventClass(DtmfBeginEvent.class);
        managerConnection.deregisterEventClass(DtmfEndEvent.class);
        managerConnection.deregisterEventClass(DtmfEvent.class);
        managerConnection.deregisterEventClass(EndpointDetail.class);
        managerConnection.deregisterEventClass(EndpointDetailComplete.class);
        managerConnection.deregisterEventClass(EndpointList.class);
        managerConnection.deregisterEventClass(EndpointListComplete.class);
        managerConnection.deregisterEventClass(EndpointListComplete.class);
        managerConnection.deregisterEventClass(ExtensionStatusEvent.class);
        managerConnection.deregisterEventClass(FaxDocumentStatusEvent.class);
        managerConnection.deregisterEventClass(FaxReceivedEvent.class);
        managerConnection.deregisterEventClass(FaxStatusEvent.class);
        managerConnection.deregisterEventClass(FaxStatusEvent.class);
        managerConnection.deregisterEventClass(HangupEvent.class);
        managerConnection.deregisterEventClass(HangupRequestEvent.class);
        managerConnection.deregisterEventClass(HoldedCallEvent.class);
        managerConnection.deregisterEventClass(HoldEvent.class);
        managerConnection.deregisterEventClass(InvalidAccountId.class);
        managerConnection.deregisterEventClass(InvalidPasswordEvent.class);
        managerConnection.deregisterEventClass(InvalidPasswordEvent.class);
        managerConnection.deregisterEventClass(JitterBufStatsEvent.class);
        managerConnection.deregisterEventClass(JoinEvent.class);
        managerConnection.deregisterEventClass(LeaveEvent.class);
        managerConnection.deregisterEventClass(LeaveEvent.class);
        managerConnection.deregisterEventClass(ListDialplanEvent.class);
        managerConnection.deregisterEventClass(LocalBridgeEvent.class);
        managerConnection.deregisterEventClass(LocalOptimizationBeginEvent.class);
        managerConnection.deregisterEventClass(LocalOptimizationEndEvent.class);
        managerConnection.deregisterEventClass(LogChannelEvent.class);
        managerConnection.deregisterEventClass(ManagerEvent.class);
        managerConnection.deregisterEventClass(ManagerEvent.class);
        managerConnection.deregisterEventClass(MeetMeEndEvent.class);
        managerConnection.deregisterEventClass(MeetMeEndEvent.class);
        managerConnection.deregisterEventClass(MeetMeLeaveEvent.class);
        managerConnection.deregisterEventClass(MeetMeLeaveEvent.class);
        // managerConnection.deregisterEventClass(MeetMeStopTalkingEvent.class);
        managerConnection.deregisterEventClass(MeetMeTalkingEvent.class);
        managerConnection.deregisterEventClass(MeetMeTalkingRequestEvent.class);
        managerConnection.deregisterEventClass(MeetMeTalkingRequestEvent.class);
        managerConnection.deregisterEventClass(MeetMeTalkingRequestEvent.class);
        managerConnection.deregisterEventClass(MeetMeTalkingRequestEvent.class);
        managerConnection.deregisterEventClass(ModuleLoadReportEvent.class);
        managerConnection.deregisterEventClass(MonitorStartEvent.class);
        managerConnection.deregisterEventClass(MonitorStartEvent.class);
        managerConnection.deregisterEventClass(MonitorStartEvent.class);
        managerConnection.deregisterEventClass(MusicOnHoldStartEvent.class);
        managerConnection.deregisterEventClass(MusicOnHoldStopEvent.class);
        managerConnection.deregisterEventClass(NewAccountCodeEvent.class);
        managerConnection.deregisterEventClass(NewCallerIdEvent.class);
        managerConnection.deregisterEventClass(NewChannelEvent.class);
        managerConnection.deregisterEventClass(NewConnectedLineEvent.class);
        managerConnection.deregisterEventClass(NewExtenEvent.class);
        managerConnection.deregisterEventClass(NewStateEvent.class);
        // managerConnection.deregisterEventClass(OriginateFailureEvent.class);
        managerConnection.deregisterEventClass(OriginateResponseEvent.class);
        // managerConnection.deregisterEventClass(OriginateSuccessEvent.class);
        managerConnection.deregisterEventClass(ParkedCallEvent.class);
        managerConnection.deregisterEventClass(ParkedCallGiveUpEvent.class);
        managerConnection.deregisterEventClass(ParkedCallsCompleteEvent.class);
        managerConnection.deregisterEventClass(ParkedCallTimeOutEvent.class);
        managerConnection.deregisterEventClass(ParkedCallTimeOutEvent.class);
        managerConnection.deregisterEventClass(PeerEntryEvent.class);
        managerConnection.deregisterEventClass(PeerlistCompleteEvent.class);
        managerConnection.deregisterEventClass(PeersEvent.class);
        managerConnection.deregisterEventClass(PeerStatusEvent.class);
        managerConnection.deregisterEventClass(PeerStatusEvent.class);
        managerConnection.deregisterEventClass(PeerStatusEvent.class);
        managerConnection.deregisterEventClass(ProtocolIdentifierReceivedEvent.class);
        managerConnection.deregisterEventClass(QueueCallerAbandonEvent.class);
        managerConnection.deregisterEventClass(QueueCallerJoinEvent.class);
        managerConnection.deregisterEventClass(QueueCallerLeaveEvent.class);
        managerConnection.deregisterEventClass(QueueEntryEvent.class);
        managerConnection.deregisterEventClass(QueueEvent.class);
        managerConnection.deregisterEventClass(QueueMemberAddedEvent.class);
        managerConnection.deregisterEventClass(QueueMemberEvent.class);
        managerConnection.deregisterEventClass(QueueMemberPausedEvent.class);
        managerConnection.deregisterEventClass(QueueMemberPauseEvent.class);
        managerConnection.deregisterEventClass(QueueMemberPenaltyEvent.class);
        managerConnection.deregisterEventClass(QueueMemberRemovedEvent.class);
        managerConnection.deregisterEventClass(QueueMemberStatusEvent.class);
        managerConnection.deregisterEventClass(QueueParamsEvent.class);
        managerConnection.deregisterEventClass(QueueStatusCompleteEvent.class);
        managerConnection.deregisterEventClass(QueueSummaryCompleteEvent.class);
        managerConnection.deregisterEventClass(QueueSummaryEvent.class);
        managerConnection.deregisterEventClass(ReceiveFaxEvent.class);
        managerConnection.deregisterEventClass(RegistrationsCompleteEvent.class);
        managerConnection.deregisterEventClass(RegistryEntryEvent.class);
        managerConnection.deregisterEventClass(RegistryEvent.class);
        managerConnection.deregisterEventClass(ReloadEvent.class);
        managerConnection.deregisterEventClass(RenameEvent.class);
        managerConnection.deregisterEventClass(RequestBadFormatEvent.class);
        managerConnection.deregisterEventClass(ResponseEvent.class);
        managerConnection.deregisterEventClass(RtcpReceivedEvent.class);
        managerConnection.deregisterEventClass(RtcpSentEvent.class);
        managerConnection.deregisterEventClass(RtpReceiverStatEvent.class);
        managerConnection.deregisterEventClass(RtpSenderStatEvent.class);
        managerConnection.deregisterEventClass(SendFaxEvent.class);
        managerConnection.deregisterEventClass(SendFaxStatusEvent.class);
        managerConnection.deregisterEventClass(ShowDialplanCompleteEvent.class);
        managerConnection.deregisterEventClass(ShutdownEvent.class);
        managerConnection.deregisterEventClass(SkypeAccountStatusEvent.class);
        managerConnection.deregisterEventClass(SkypeBuddyEntryEvent.class);
        managerConnection.deregisterEventClass(SkypeBuddyListCompleteEvent.class);
        managerConnection.deregisterEventClass(SkypeBuddyStatusEvent.class);
        managerConnection.deregisterEventClass(SkypeChatMessageEvent.class);
        managerConnection.deregisterEventClass(SkypeLicenseEvent.class);
        managerConnection.deregisterEventClass(SkypeLicenseListCompleteEvent.class);
        managerConnection.deregisterEventClass(SoftHangupRequestEvent.class);
        managerConnection.deregisterEventClass(SoftHangupRequestEvent.class);
        managerConnection.deregisterEventClass(StatusEvent.class);
        managerConnection.deregisterEventClass(SuccessfulAuthEvent.class);
        managerConnection.deregisterEventClass(T38FaxStatusEvent.class);
        managerConnection.deregisterEventClass(TransferEvent.class);
        managerConnection.deregisterEventClass(TransportDetail.class);
        managerConnection.deregisterEventClass(UnholdEvent.class);
        // managerConnection.deregisterEventClass(UnlinkEvent.class);
        managerConnection.deregisterEventClass(UnparkedCallEvent.class);
        managerConnection.deregisterEventClass(UnpausedEvent.class);
        managerConnection.deregisterEventClass(UserEvent.class);
        managerConnection.deregisterEventClass(VarSetEvent.class);
        managerConnection.deregisterEventClass(VoicemailUserEntryCompleteEvent.class);
        managerConnection.deregisterEventClass(VoicemailUserEntryEvent.class);
        managerConnection.deregisterEventClass(ZapShowChannelsCompleteEvent.class);
        managerConnection.deregisterEventClass(ZapShowChannelsEvent.class);
        managerConnection.deregisterEventClass(FullyBootedEvent.class);
        managerConnection.deregisterEventClass(ConnectEvent.class);

        managerConnection.registerUserEventClass(CdrEvent.class);
        managerConnection.registerUserEventClass(DeviceStateChangeEvent.class);
        managerConnection.registerUserEventClass(NewConnectedLineEvent.class);
        managerConnection.registerUserEventClass(BridgeLeaveEvent.class);
        managerConnection.registerUserEventClass(BridgeEnterEvent.class);
        // -----------------------------------------------

        // 5) Store domain -> (factory, connection, listener)
        CurrentConnections.upsert(domain, factory, managerConnection, eventListner);

        login(managerConnection);
        return managerConnection;
    }

    /**
     * Verify-only getter:
     * - returns usable connection if present, otherwise null.
     * - (No looping lists, since CurrentConnections is domain-keyed now)
     */
    public ManagerConnection getConnection(String domain, String secondDomain, boolean isSecondLine) {
        LoggerUtils.log.debug("ManagerConnection-->getConnection");
        String d = isSecondLine ? secondDomain : domain;
        return CurrentConnections.getConnectionIfUsable(d);
    }

    /**
     * Login helper:
     * - If login fails, remove entry from CurrentConnections so next refresh will create clean connection.
     */
    public void login(ManagerConnection managerConnection)
            throws IllegalStateException, IOException, AuthenticationFailedException, TimeoutException {
        try {
            managerConnection.login();
        } catch (Exception e) {
            // Remove from map on login failure (best-effort)
            try {
                String host = null;
                try {
                    host = managerConnection.getHostname();
                } catch (Exception ignore) {}
                if (host != null && !host.trim().isEmpty()) {
                    CurrentConnections.remove(host);
                }
            } catch (Exception ignore) {}
            throw e;
        }
    }

    public void logout(ManagerConnection managerConnection) {
        if (managerConnection == null) return;

        try {
            // Remove from registry as well so system can recreate later
            try {
                String host = managerConnection.getHostname();
                if (host != null && !host.trim().isEmpty()) {
                    CurrentConnections.remove(host);
                }
            } catch (Exception ignore) {}

            managerConnection.logoff();
        } catch (Exception e) {
            LoggerUtils.log.error("AMI logoff failed msg={}", String.valueOf(e.getMessage()), e);
        }
    }
}
