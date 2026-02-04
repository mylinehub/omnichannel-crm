package com.mylinehub.crm.ami;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.response.ManagerResponse;

import com.mylinehub.crm.enums.ASTERISK_APPLICATIONS;

public class ManagerFunctionalityWrapper {
	
	ManagerStream stream = new ManagerStream();
	static List<Map<String,String>> allDynamicConference;
	
	 public ManagerResponse originateToPhone(String organization,String channelToCall, String callerID,String callingChannel, int priority,Long timeOut, boolean async, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  int min = 200;  
		  int max = 600; 
		  Map<String,String> currentSet = new HashMap<String,String>();
		  ManagerResponse response;
		  String application = ASTERISK_APPLICATIONS.Dial.name();
		  String id = organization+"-"+(int)(Math.random()*(max-min+1)+min);
		  String data = callingChannel;
		  response = stream.originateDataCall(organization, channelToCall, callerID, data, priority, timeOut, async, application, managerConnection);
		  
		  if(response.getMessage()=="success")
		  {
			  currentSet.put(id, channelToCall);
			  allDynamicConference.add(currentSet);
		  }
		  
		  return response;
		 
	    }
	 
	 public ManagerResponse createPublicConference(String organization,String channelToCall, String callerID, int priority,Long timeOut, boolean async, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  int min = 200;  
		  int max = 600; 
		  Map<String,String> currentSet = new HashMap<String,String>();
		  ManagerResponse response;
		  String application = ASTERISK_APPLICATIONS.ConfBridge.name();
		  String id = organization+"-"+(int)(Math.random()*(max-min+1)+min);
		  String data = id;
		  response = stream.originateDataCall(organization, channelToCall, callerID, data, priority, timeOut, async, application, managerConnection);
		  
		  if(response.getMessage()=="success")
		  {
			  currentSet.put(id, channelToCall);
			  allDynamicConference.add(currentSet);
		  }
		  
		  return response;
		 
	    }
	 
	 public ManagerResponse createCustomConference(String organization,String channelToCall, String callerID, int priority,Long timeOut, boolean async,String bridge, String userprofile,String menu, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  int min = 200;  
		  int max = 600; 
		  Map<String,String> currentSet = new HashMap<String,String>();
		  ManagerResponse response;
		  String application = ASTERISK_APPLICATIONS.ConfBridge.name();
		  String id = organization+"-"+(int)(Math.random()*(max-min+1)+min);
		  String data = id+","+bridge+","+userprofile+","+menu;
		  response = stream.originateDataCall(organization, channelToCall, callerID, data, priority, timeOut, async, application, managerConnection);
		  
		  if(response.getMessage()=="success")
		  {
			  currentSet.put(id, channelToCall);
			  allDynamicConference.add(currentSet);
		  }
		  
		  
		  return response;
		 
	    }
	 
	 public ManagerResponse listenQuietly(String organization,String callingChannel, String channelToCall, String callerID, int priority,Long timeOut, boolean async, ManagerConnection managerConnection) throws IOException, AuthenticationFailedException, TimeoutException
	    {
		  ManagerResponse response;
		  String application = ASTERISK_APPLICATIONS.ExtenSpy.name();
		  String data = channelToCall+"@"+organization+",4,o,q,S";
		  response = stream.originateDataCall(organization, callingChannel, callerID, data, priority, timeOut, async, application, managerConnection);
		  return response;
		 
	    }

}
