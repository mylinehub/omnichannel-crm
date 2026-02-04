package com.mylinehub.crm.ws.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.simp.stomp.StompSession.Receiptable;

import com.mylinehub.crm.ami.TaskScheduler.RefreshBackEndConnectionRunnable;
import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.utils.LoggerUtils;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * This class is an implementation for <code>StompSessionHandlerAdapter</code>.
 * Once a connection is established, We subscribe to /topic/messages and 
 * send a sample message to server.
 * 
 * @author Anand Goel
 *
 */
public class MyStompSessionHandler extends StompSessionHandlerAdapter {
	
	private Logger logger = LogManager.getLogger(MyStompSessionHandler.class);
	public static Set<StompSession> sessions = new CopyOnWriteArraySet<>();
	public static ApplicationContext applicationContext = null;
	
	    @Override
	    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
//	        logger.info("New session established : " + session.getSessionId());
//	        logger.info("Server connection opened and added to list of all sessions");
	        sessions.add(session);
	        //session.subscribe("/event/host-tune-perform", this);
//	        session.subscribe("/event/host-tune-perform", this);
//	        session.subscribe("/event/201", this);
//	        logger.info("Subscribed to /event/host-tune-perform");
//	        sendMessage(session,"/mylinehub/sendevent", getSampleMessage());
	    }

	    @Override
	    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
	    	logger.error("Is Session Connected :", session.isConnected());
	    	logger.error("Got an exception", exception);
	    }

	    @Override
	    public Type getPayloadType(StompHeaders headers) {
	        return BotInputDTO.class;
	    }

	    @Override
	    public void handleFrame(StompHeaders headers, Object payload) {
	    	BotInputDTO msg = (BotInputDTO) payload;
	        logger.info("Received : " + msg.getMessage() + " from : " + msg.getOrganization()+"("+msg.getExtension()+")"+"  at : " +LocalTime.now());
	    }
		
		
		public static Receiptable sendMessage(String path, Object payload) {
			 
			 int supportUserSize = STOMPClient.stompSession.size();
			 LoggerUtils.log.debug("supportUserSize : "+supportUserSize);
		      int sessionNumber =  0;
		      if(supportUserSize != 0)
		      {
		    	  LoggerUtils.log.debug("First get and then increment sstomp client");
		    	  sessionNumber = STOMPClient.getAndIncrement(); 
		    	  if(sessionNumber>=supportUserSize)
		    	  {
		    		  LoggerUtils.log.debug("Resetting stomp client array number");
		    		  sessionNumber = 0;
		    		  STOMPClient.reset();
		    	  }
		    	  
		    	  StompSession stompSession = STOMPClient.stompSession.get(sessionNumber);
		    	  
		    	  LoggerUtils.log.debug("sessionNumber : "+sessionNumber);
		    	  
		    	  if (stompSession != null)
		    	  LoggerUtils.log.debug("stompSession.isConnected() : "+stompSession.isConnected());
		    	  
		   	   	  if (stompSession.isConnected()) {
		   	   		  
		   	   		  try {
		   	   		        LoggerUtils.log.debug("Sending stomp message");
					   	   	Receiptable receiptable = stompSession.send(path, payload);
							return receiptable;
		   	   		  }
		   	   		  catch(Exception e)
		   	   		  {
			   	   		 try {
			   	   			 reinitiateAfterRefreshConnection(path, payload, sessionNumber);
			   	   		  }
			   	   		  catch(Exception e1)
			   	   		  {
			   	   			  e1.printStackTrace();
			   	   		  }
		   	   		 
		   	   			  e.printStackTrace();
			   	   		  throw e;
		   	   		  }

			      }
		   	   	  else
		   	   	  {
		   	   		  try {
		   	   			  reinitiateAfterRefreshConnection(path, payload, sessionNumber);
		   	   		  }
		   	   		  catch(Exception e)
		   	   		  {
		   	   			  e.printStackTrace();
			   	   		  throw e;
		   	   		  }

		   	   	  }
		      }
		      else
		      {
		    	  LoggerUtils.log.debug("MySTOMPSessionHandler : No support users found to send event. This should not happen. Contact Admin.");
		      }
		      
		      return null;
		}

		public static Receiptable reinitiateAfterRefreshConnection(String path, Object payload, int sessionNumber) {
			Receiptable receiptable = null;
			ErrorRepository errorRepository = applicationContext.getBean(ErrorRepository.class);
	    	
			try {
				
	   	   		  
	   	   		LoggerUtils.log.debug("Refreshing backend connections in stomp session send data");
	   	   		  
				RefreshBackEndConnectionRunnable refreshBackEndConnectionRunnable = new RefreshBackEndConnectionRunnable();
	    		refreshBackEndConnectionRunnable.setErrorRepository(errorRepository);
	    		refreshBackEndConnectionRunnable.setApplicationContext(applicationContext);
	    		refreshBackEndConnectionRunnable.execute("STOMP");
	    		
	    		try {
	    			
	    			StompSession stompSession = STOMPClient.stompSession.get(sessionNumber);
			   	   	  if (stompSession.isConnected()) {
			    			receiptable = stompSession.send(path, payload);
							return receiptable;
			   	   	  }
			   	   	  else {
			   	   		  throw new Exception("STOMP connection now found");
			   	   	  }

	        	}
	        	catch(Exception e)
	        	{
	        		try {
		    			
		    			StompSession stompSession = STOMPClient.stompSession.get(sessionNumber);
				   	   	  if (stompSession.isConnected()) {
				    			receiptable = stompSession.send(path, payload);
								return receiptable;
				   	   	  }
				   	   	  else {
				   	   		  throw new Exception("STOMP connection now found");
				   	   	  }

		        	}
		        	catch(Exception e1)
		        	{
		        		e1.printStackTrace();
		        		sendMessage(path, payload);
		        		return receiptable;
		        	}
	        	}
			}
			catch(Exception e1)
			{
	    		e1.printStackTrace();
	    		throw new Error ("STOMP Connection not found");
			}
		}
}

