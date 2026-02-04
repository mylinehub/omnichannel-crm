package com.mylinehub.crm.ami;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.AbsoluteTimeoutAction;
import org.asteriskjava.manager.action.AbstractManagerAction;
import org.asteriskjava.manager.action.AgentsAction;
import org.asteriskjava.manager.action.AtxferAction;
import org.asteriskjava.manager.action.BridgeAction;
import org.asteriskjava.manager.action.ChangeMonitorAction;
import org.asteriskjava.manager.action.CommandAction;
import org.asteriskjava.manager.action.ConfbridgeKickAction;
import org.asteriskjava.manager.action.ConfbridgeListAction;
import org.asteriskjava.manager.action.ConfbridgeListRoomsAction;
import org.asteriskjava.manager.action.ConfbridgeLockAction;
import org.asteriskjava.manager.action.ConfbridgeMuteAction;
import org.asteriskjava.manager.action.ConfbridgeSetSingleVideoSrcAction;
import org.asteriskjava.manager.action.ConfbridgeStartRecordAction;
import org.asteriskjava.manager.action.ConfbridgeStopRecordAction;
import org.asteriskjava.manager.action.ConfbridgeUnlockAction;
import org.asteriskjava.manager.action.ConfbridgeUnmuteAction;
import org.asteriskjava.manager.action.ExtensionStateAction;
import org.asteriskjava.manager.action.GetVarAction;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.MonitorAction;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.action.ParkAction;
import org.asteriskjava.manager.action.PauseMonitorAction;
import org.asteriskjava.manager.action.RedirectAction;
import org.asteriskjava.manager.action.SendTextAction;
import org.asteriskjava.manager.action.StatusAction;
import org.asteriskjava.manager.action.StopMonitorAction;
import org.asteriskjava.manager.action.UnpauseMonitorAction;
import org.asteriskjava.manager.response.ManagerResponse;

public class ManagerStream {
	  

	 public ManagerResponse requestStateForAllAgents(String organization,Long timeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  AgentsAction stateForAgentsAction;
	      ManagerResponse agentResponse=null;
	        
		  try
		  {
			  stateForAgentsAction = new AgentsAction();
			  stateForAgentsAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
//			  agentResponse = managerConnection.sendAction(stateForAgentsAction, 10000);
			  agentResponse = sendAction(organization,managerConnection,stateForAgentsAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return agentResponse;
	        
	    }

	 public ManagerResponse statusForSpecificChannel(String organization,String channelID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  StatusAction statusAction;
	      ManagerResponse statusResponse=null;
	        
		  try
		  {
			  statusAction = new StatusAction(channelID);
			  statusAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          statusResponse = sendAction(organization,managerConnection,statusAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return statusResponse;
	        
	    }
	 
	 
	 public ManagerResponse sendAnonymousTextToChannel(String organization,String channelID, String message, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  SendTextAction sendTextAction;
	      ManagerResponse sendTextResponse=null;
	        
		  try
		  {
			  sendTextAction = new SendTextAction();
			  sendTextAction.setChannel(channelID);
			  sendTextAction.setMessage(message);
			  sendTextAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          sendTextResponse = sendAction(organization,managerConnection,sendTextAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return sendTextResponse;
	        
	    }
	 
	 public ManagerResponse bridgeTwoActiveCalls(String organization, String channelToCall, String callingChannel,Long timeOut, boolean tone, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  BridgeAction bridgeAction;
	      ManagerResponse bridgeResponse=null;
	        
		  try
		  {
			  bridgeAction = new BridgeAction();
		      bridgeAction.setChannel1(callingChannel);
			  bridgeAction.setChannel2(channelToCall);
		      bridgeAction.setTone(tone);
		      bridgeAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
		      bridgeResponse = sendAction(organization,managerConnection,bridgeAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			  //Something which is required
		  }
	        
	        return bridgeResponse;
	        
	    }
	 
	 
	 public ManagerResponse originateToDialDirect(
		        String organization,
		        String channelToCall,
		        String callerId,
		        String dialString,        // e.g. "PJSIP/9711761156@mylinehub"
		        int dialTimeoutSeconds,   // e.g. 60
		        Long timeOutMs,
		        boolean async,
		        ManagerConnection managerConnection
		) throws IOException, AuthenticationFailedException, TimeoutException {

		    OriginateAction a = new OriginateAction();
		    a.setAccount(organization);
		    a.setChannel(channelToCall);
		    a.setCallerId(callerId);

		    a.setApplication("Dial");
		    a.setData(dialString + "," + dialTimeoutSeconds);

		    a.setActionId(organization);
		    a.setTimeout(timeOutMs);
		    a.setAsync(async);

		    return sendAction(organization, managerConnection, a, timeOutMs);
		}

	 
	  public ManagerResponse originateCall(String organization, String channelToCall, String callerID, String context, String extensionToCall, int priority,Long timeOut, boolean async, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  OriginateAction originateAction;
	      ManagerResponse originateResponse=null;
	        
		  try
		  {
			  System.out.println("Manager Stream Originate Call");
			  
			  originateAction = new OriginateAction();
			  
			  System.out.println("organization : "+organization);
			  System.out.println("channelToCall : "+channelToCall);
			  System.out.println("callerID : "+callerID);
			  System.out.println("context : "+context);
			  System.out.println("extensionToCall : "+extensionToCall);
			  System.out.println("priority : "+priority);
			  System.out.println("timeOut : "+timeOut);
			  System.out.println("async : "+async);
			  
			  originateAction.setAccount(organization);
		      originateAction.setChannel(channelToCall);
		      originateAction.setCallerId(callerID);
		      originateAction.setContext(context);
		      originateAction.setExten(extensionToCall);
		      originateAction.setPriority(priority);
		      originateAction.setActionId(organization);
		      originateAction.setTimeout(timeOut);
		      originateAction.setAsync(async);
		      // send the action we defined and wait 10 seconds for a reply
		      originateResponse = sendAction(organization,managerConnection,originateAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			  //Something which is required
		  }
	        
	        return originateResponse;
	        
	    }
	  
	 
	  /**
	     * Originate a call directly into Asterisk Stasis app (ARI entry point).
	     *
	     * Asterisk dialplan app is: Stasis(appName[,args...])
	     * For AMI Originate: Application=Stasis, Data="appName,args"
	     */
	    public ManagerResponse originateToStasis(
	            String organization,
	            String channelToCall,
	            String callerId,
	            String stasisAppName,
	            String stasisArgsCsv,
	            Long timeOut,
	            boolean async,
	            ManagerConnection managerConnection
	    ) throws IOException, AuthenticationFailedException, TimeoutException {

	        // Data format: "appName,arg1,arg2"
	        final String data;
	        if (stasisArgsCsv == null || stasisArgsCsv.isBlank()) {
	            data = stasisAppName;
	        } else {
	            data = stasisAppName + "," + stasisArgsCsv;
	        }

	        // Reuse your existing originateDataCall
	        return originateDataCall(
	                organization,
	                channelToCall,
	                callerId,
	                data,
	                0,              // priority not used for application originate in your current method
	                timeOut,
	                async,
	                "Stasis",
	                managerConnection
	        );
	    }
	  
	  public ManagerResponse originateDataCall(String organization,String channelToCall, String callerID, String data, int priority,Long timeOut, boolean async, String application, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  OriginateAction originateAction;
	      ManagerResponse originateResponse=null;
	        
		  try
		  {
			  originateAction = new OriginateAction();
			  originateAction.setAccount(organization);
		      originateAction.setChannel(channelToCall);
		      originateAction.setCallerId(callerID);
		      originateAction.setData(data);// such as conferenceId
		      //originateAction.setPriority(priority);
		      originateAction.setActionId(organization);
		      originateAction.setTimeout(timeOut);
		      originateAction.setAsync(async);
		      originateAction.setApplication(application); // such as ConfBridge
		      // send the action we defined and wait 10 seconds for a reply
		      originateResponse = sendAction(organization,managerConnection,originateAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			  //Something which is required
		  }
	        
	        return originateResponse;
	        
	    }
	  
	  public ManagerResponse hungUpCall(String organization,String channelToHungUp, Integer cause,Long timeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  HangupAction HungUpAction;
	      ManagerResponse hungupResponse=null;
	        
		  try
		  {
			  HungUpAction = new HangupAction();
			  HungUpAction.setChannel(channelToHungUp);
		      HungUpAction.setCause(cause);
		      HungUpAction.setActionId(organization);

		      // send the action we defined and wait 10 seconds for a reply
		      hungupResponse = sendAction(organization,managerConnection,HungUpAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			 //Something is is always required
		  }
	        
	        return hungupResponse;
	        
	    }
	  
	  public ManagerResponse blindTransferCall(String organization,String channelFromTransfer, String callerID, String context, String extensionToTransfer, int priority,Long timeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  RedirectAction blindTransferAction;
	      ManagerResponse blindTransferResponse=null;
	        
		  try
		  {
			  blindTransferAction = new RedirectAction();
			  blindTransferAction.setChannel(channelFromTransfer);
		      blindTransferAction.setContext(context);
		      blindTransferAction.setExten(extensionToTransfer);
		      blindTransferAction.setPriority(priority);
		      blindTransferAction.setActionId(organization);

		      // send the action we defined and wait 10 seconds for a reply
	          blindTransferResponse = sendAction(organization,managerConnection,blindTransferAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			 //Something is is always required
		  }
	        
	        return blindTransferResponse;
	        
	    }
	  
	 
	  
	  public ManagerResponse attemptedTransferCall(String organization,String channelFromTransfer, String callerID, String context, String extensionToTransfer, int priority,Long timeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  AtxferAction attemptedTransferAction;
	      ManagerResponse attemptedTransferResponse=null;
	        
		  try
		  {
			  attemptedTransferAction = new AtxferAction();
			  attemptedTransferAction.setChannel(channelFromTransfer);
			  attemptedTransferAction.setContext(context);
			  attemptedTransferAction.setExten(extensionToTransfer);
			  attemptedTransferAction.setPriority(priority);
			  attemptedTransferAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          attemptedTransferResponse = sendAction(organization,managerConnection,attemptedTransferAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return attemptedTransferResponse;
	        
	    }
	  
	  
	  public ManagerResponse disconnectAfterXSeconds(String organization,String channelToHungUp, int seconds, Long timeout,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  AbsoluteTimeoutAction timeOutAction;
	      ManagerResponse timeOutResponse=null;
	        
		  try
		  {
			  timeOutAction = new AbsoluteTimeoutAction();
			  timeOutAction.setChannel(channelToHungUp);
			  timeOutAction.setTimeout(seconds);
			  timeOutAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
			  timeOutResponse = sendAction(organization,managerConnection,timeOutAction, timeout);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return timeOutResponse;
	        
	    }

	  
	  //ConfBridge Functions
	  
	  public ManagerResponse confbridgeListRooms(String organization, Long timeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeListRoomsAction confbridgeListRoomsAction;
	      ManagerResponse confbridgeListRoomsResponse=null;
	        
		  try
		  {
			  confbridgeListRoomsAction = new ConfbridgeListRoomsAction();
			  confbridgeListRoomsAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeListRoomsResponse = sendAction(organization,managerConnection,confbridgeListRoomsAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeListRoomsResponse;
	        
	    }
	  
	  public ManagerResponse confbridgeListMembers(String organization,String conferenceID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeListAction confbridgeListAction;
	      ManagerResponse confbridgeListResponse=null;
	        
		  try
		  {
			  confbridgeListAction = new ConfbridgeListAction();
			  confbridgeListAction.setConference(conferenceID);
			  confbridgeListAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeListResponse = sendAction(organization,managerConnection,confbridgeListAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeListResponse;
	        
	    }
	  
	  public ManagerResponse confbridgeLock(String organization,String conferenceID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeLockAction confbridgeLockAction;
	      ManagerResponse confbridgeLockResponse=null;
	        
		  try
		  {
			  confbridgeLockAction = new ConfbridgeLockAction();
			  confbridgeLockAction.setConference(conferenceID);
			  confbridgeLockAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeLockResponse = sendAction(organization,managerConnection,confbridgeLockAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeLockResponse;
	        
	    }
	  public ManagerResponse confbridgeUnlock(String organization,String conferenceID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeUnlockAction confbridgeUnlockAction;
	      ManagerResponse confbridgeUnlockResponse=null;
	        
		  try
		  {
			  confbridgeUnlockAction = new ConfbridgeUnlockAction();
			  confbridgeUnlockAction.setConference(conferenceID);
			  confbridgeUnlockAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeUnlockResponse = sendAction(organization,managerConnection,confbridgeUnlockAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeUnlockResponse;
	        
	    }
	  
	  public ManagerResponse confbridgeMuteMember(String organization,String conferenceID,String channelID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeMuteAction confbridgeMuteAction;
	      ManagerResponse confbridgeMuteResponse=null;
	        
		  try
		  {
			  confbridgeMuteAction = new ConfbridgeMuteAction();
			  confbridgeMuteAction.setConference(conferenceID);
			  confbridgeMuteAction.setChannel(channelID);
			  confbridgeMuteAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeMuteResponse = sendAction(organization,managerConnection,confbridgeMuteAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeMuteResponse;
	        
	    }
	  
	  public ManagerResponse confbridgeUnmuteMember(String organization,String conferenceID,String channelID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeUnmuteAction confbridgeUnmuteAction;
	      ManagerResponse confbridgeUnmuteResponse=null;
	        
		  try
		  {
			  confbridgeUnmuteAction = new ConfbridgeUnmuteAction();
			  confbridgeUnmuteAction.setConference(conferenceID);
			  confbridgeUnmuteAction.setChannel(channelID);
			  confbridgeUnmuteAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeUnmuteResponse = sendAction(organization,managerConnection,confbridgeUnmuteAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeUnmuteResponse;
	        
	    }
	  
	  public ManagerResponse confbridgeSetSingleVideoSrcMember(String organization,String conferenceID,String channelID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeSetSingleVideoSrcAction confbridgeSetSingleVideoSrcAction;
	      ManagerResponse confbridgeSetSingleVideoSrcResponse=null;
	        
		  try
		  {
			  confbridgeSetSingleVideoSrcAction = new ConfbridgeSetSingleVideoSrcAction();
			  confbridgeSetSingleVideoSrcAction.setConference(conferenceID);
			  confbridgeSetSingleVideoSrcAction.setChannel(channelID);
			  confbridgeSetSingleVideoSrcAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeSetSingleVideoSrcResponse = sendAction(organization,managerConnection,confbridgeSetSingleVideoSrcAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeSetSingleVideoSrcResponse;
	        
	    }
	  
	  
	  public ManagerResponse confbridgeStartRecord(String path,String organization,String conferenceID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {

		  ConfbridgeStartRecordAction confbridgeStartRecordAction;
	      ManagerResponse confbridgeStartRecordResponse=null;
	        
		  try
		  {
			  confbridgeStartRecordAction = new ConfbridgeStartRecordAction();
			  confbridgeStartRecordAction.setConference(conferenceID);
			  confbridgeStartRecordAction.setRecordFile(path);
			  confbridgeStartRecordAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeStartRecordResponse = sendAction(organization,managerConnection,confbridgeStartRecordAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeStartRecordResponse;
	        
	    }
	  
	  public ManagerResponse confbridgeStopRecord(String organization,String conferenceID, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeStopRecordAction confbridgeStopRecordAction;
	      ManagerResponse confbridgeStopRecordResponse=null;
	        
		  try
		  {
			  confbridgeStopRecordAction = new ConfbridgeStopRecordAction();
			  confbridgeStopRecordAction.setConference(conferenceID);
			  confbridgeStopRecordAction.setActionId(conferenceID);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          confbridgeStopRecordResponse = sendAction(organization,managerConnection,confbridgeStopRecordAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return confbridgeStopRecordResponse;
	        
	    }
	  
	  
	  public ManagerResponse confbridgeKickMember(String organization,String conferenceID,String channelToKick, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ConfbridgeKickAction action;
	      ManagerResponse response=null;
	        
		  try
		  {
			  action = new ConfbridgeKickAction();
			  action.setConference(conferenceID);
			  action.setChannel(channelToKick);
			  action.setActionId(conferenceID);
		      
		      // send the action we defined and wait 10 seconds for a reply
			  response = sendAction(organization,managerConnection,action, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return response;
	        
	    }
	  
	  
	  public ManagerResponse extensionState(String organization,String exten,String context, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ExtensionStateAction extensionStateAction;
	      ManagerResponse extensionStateResponse=null;
	        
		  try
		  {
			  extensionStateAction = new ExtensionStateAction(exten,context);
			  extensionStateAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          extensionStateResponse = sendAction(organization,managerConnection,extensionStateAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return extensionStateResponse;
	        
	    }
	  
	  
	  public ManagerResponse commandAction(String organization,String command, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  CommandAction commandAction;
	      ManagerResponse commandActionResponse=null;
	        
		  try
		  {
			  commandAction = new CommandAction(command);
			  commandAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
			  commandActionResponse = sendAction(organization,managerConnection,commandAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return commandActionResponse;
	        
	    }
	  
	  
	  public ManagerResponse getVarAction(String organization,String exten,String variable, Long timeOut,ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  GetVarAction getVarAction;
	      ManagerResponse getVarResponse=null;
	        
		  try
		  {
			  getVarAction = new GetVarAction(exten,variable);
			  getVarAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
			  getVarResponse = sendAction(organization,managerConnection,getVarAction, timeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        
	        return getVarResponse;
	        
	    }
	  
	  public ManagerResponse parkForTimeOut(String organization,String channelTosendText,String channelToPlayMusic, Integer parkTimeOut,Long actionTimeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ParkAction parkAction;
	      ManagerResponse parkResponse=null;
	        
		  try
		  {
			  parkAction = new ParkAction();
			  parkAction.setChannel(channelTosendText);
			  parkAction.setChannel2(channelToPlayMusic);
			  parkAction.setTimeout(parkTimeOut);
			  parkAction.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	          parkResponse = sendAction(organization,managerConnection,parkAction,actionTimeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        return parkResponse;  
	    }
	  
	  public ManagerResponse monitorAction(String organization,String channel, String format,Boolean mix,Long actionTimeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  MonitorAction action;
	      ManagerResponse response=null;
	      
	      try
		  {
	    	  action = new MonitorAction();
	    	  action.setChannel(channel);
	    	  action.setFormat(format);
	    	  action.setMix(mix);
	    	  action.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	    	  response = sendAction(organization,managerConnection,action,actionTimeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        return response;  
	    }
	  
	  public ManagerResponse unpauseMonitorAction(String organization,String channel,Long actionTimeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  UnpauseMonitorAction action;
	      ManagerResponse response=null;
	      
	      try
		  {
	    	  action = new UnpauseMonitorAction();
	    	  action.setChannel(channel);
	    	  action.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	    	  response = sendAction(organization,managerConnection,action,actionTimeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        return response; 
	    }
	  
	  
	  public ManagerResponse pauseMonitorAction(String organization,String channel,Long actionTimeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  PauseMonitorAction action;
	      ManagerResponse response=null;
	      
	      try
		  {
	    	  action = new PauseMonitorAction();
	    	  action.setChannel(channel);
	    	  action.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	    	  response = sendAction(organization,managerConnection,action,actionTimeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        return response; 
	    }
	  
	  public ManagerResponse stopMonitorAction(String organization,String channel,Long actionTimeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  StopMonitorAction action;
	      ManagerResponse response=null;
	      
	      try
		  {
	    	  action = new StopMonitorAction();
	    	  action.setChannel(channel);
	    	  action.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	    	  response = sendAction(organization,managerConnection,action,actionTimeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        return response; 
	    }
	  
	  public ManagerResponse changeMonitorAction(String organization,String channel,String file,Long actionTimeOut, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  
		  ChangeMonitorAction action;
	      ManagerResponse response=null;
	      
	      try
		  {
	    	  action = new ChangeMonitorAction();
	    	  action.setChannel(channel);
	    	  action.setFile(file);
	    	  action.setActionId(organization);
		      
		      // send the action we defined and wait 10 seconds for a reply
	    	  response = sendAction(organization,managerConnection,action,actionTimeOut);
		  }
		  catch(Exception e)
		  {
			  throw e;
		  }
		  finally
		  {
			//Something is is always required
		  }
	        return response; 
	    }
	  
	  
	  //Synchronization of send Action as per organization data
	  ManagerResponse sendAction(String currentOrganization, ManagerConnection managerConnection,AbstractManagerAction action, Long timeout ) throws IllegalArgumentException, IllegalStateException, IOException, TimeoutException
	  {	  
		  ManagerResponse response=managerConnection.sendAction(action,timeout);
		  return response;
	  }

}
