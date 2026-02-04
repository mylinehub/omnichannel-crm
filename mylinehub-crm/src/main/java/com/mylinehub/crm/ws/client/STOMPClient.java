package com.mylinehub.crm.ws.client;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.SecretKey;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.mylinehub.crm.utils.LoggerUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


@Component
@PropertySource("classpath:application.properties")
public class STOMPClient {

	public static List<WebSocketStompClient> stompClient = new ArrayList<WebSocketStompClient>();
	public static List<StompSessionHandler> sessionHandler = new ArrayList<StompSessionHandler>();
	public static List<StompSession> stompSession = new ArrayList<StompSession>();
	private static final AtomicInteger counter = new AtomicInteger(0);
    

    public static synchronized int getAndIncrement() {
    	return STOMPClient.counter.getAndIncrement();
    }
    
    public static synchronized void reset()
    {
    	STOMPClient.counter.set(0);
    }
    
	public void createConnection(ApplicationContext applicationContext, String loggerServerQueueUrl) throws InterruptedException, ExecutionException
	{
		
		List<String> allWebSocketEmployeeUsers = new ArrayList<String>();
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user1"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user2"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user3"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user4"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user5"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user6"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user7"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user8"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user9"));
		allWebSocketEmployeeUsers.add(applicationContext.getEnvironment().getProperty("spring.websocket.user10"));
		
		for(int i = 0; i < allWebSocketEmployeeUsers.size(); i++) {
			
//			    LoggerUtils.log.info("Initializing Client : "+i);
			    
			    boolean initializeStompClient = false;
			    //Initialize client
	     		if( i >= STOMPClient.stompClient.size())
	     		{
//	     			LoggerUtils.log.info("Inside more if : "+i);
	     			initializeStompClient = true;
	     		}
	     		else
	     		{
	     			if(!STOMPClient.stompClient.get(i).isRunning())
	     			{
//	     				LoggerUtils.log.info("Inside less & not running if : "+i);
	     				initializeStompClient = true;
	     			}
	     		}
	     		
	     		if(initializeStompClient) {
	     			WebSocketClient client = new StandardWebSocketClient();
	     			WebSocketStompClient stompClient = new WebSocketStompClient(client);
	     			stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	     		    STOMPClient.stompClient.add(i,stompClient);
	     		}
	     		

//	     		LoggerUtils.log.info("Initializing Handler : "+i);
	    		//Initializing Handler
	    		if(i >= STOMPClient.sessionHandler.size())
	    		{
	    			LoggerUtils.log.info("Inside Handler : "+i);
	    			StompSessionHandler sessionHandler = new MyStompSessionHandler();
	    			STOMPClient.sessionHandler.add(i,sessionHandler);
	    		}
	    		
	    		
	    		boolean initializeStompSession = false;
//	    		LoggerUtils.log.info("Initializing Session : "+i);
	    		//Initialize session
	    		if (i >= STOMPClient.stompSession.size())
	    		{
//	    			    LoggerUtils.log.info("Inside more Session : "+i);
//	    				System.out.println("Before connection");
	    				initializeStompSession = true;
	    		}
	    		else
	    		{
	    			if(!STOMPClient.stompSession.get(i).isConnected())
	    			{
//	    				    LoggerUtils.log.info("Inside less & not connected Session : "+i);
		    				//System.out.println("Before connection");
	    					initializeStompSession = true;
	    			}
	    		}
	    		
	    		if(initializeStompSession) {
	    			WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    				StompHeaders stompHeaders = new StompHeaders ();
    				
                    String key =  applicationContext.getEnvironment().getProperty("jwt.private-key");
                    
    			    SecretKey originalKey =Keys.hmacShaKeyFor(key.getBytes());
    			    		
    				Map<String,String> map = new HashMap<String, String>();
    				map.put("authority", "ADMIN");
    				
    				List<Map<String,String>> allAthorities = new ArrayList<Map<String,String>>();
    				
    				allAthorities.add(map);
    				
    				 String token = Jwts.builder()
    			                .setSubject(allWebSocketEmployeeUsers.get(i))
    			                .claim("authorities", allAthorities)
    			                .setIssuedAt(new Date())
    			                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusDays(365)))
    			                .signWith(originalKey)
    			                .compact();
    				
//    				 LoggerUtils.log.info((String.valueOf("CONNECTING USER  : "+token)));
    				 
//    				 System.out.println("After Token");
    				headers.add("Authorization", "Bearer "+token);
    				stompHeaders.add("auth", token);
    				STOMPClient.stompSession.add(i,STOMPClient.stompClient.get(i).connect(loggerServerQueueUrl, headers, stompHeaders, STOMPClient.sessionHandler.get(i)).get());
//    				System.out.println("After connection");
    				//System.out.println("After added line");
	    		}
       	
		}
	}
}
