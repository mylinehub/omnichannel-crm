package com.mylinehub.shh;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.asteriskjava.manager.TimeoutException;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.mylinehub.crm.SpringBootApp;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.repository.ErrorRepository;

public class SshWrapper {
	
	// Base timeout constant (in seconds)
    private static final int BASE_TIMEOUT_SECONDS = 2;

    // Shared maps used across threads -> changed from HashMap to ConcurrentHashMap for thread safety
    public static Map<String, JSch> allJSch = new ConcurrentHashMap<String, JSch>(); // Changed to ConcurrentHashMap
    public static Map<String, Session> allSession = new ConcurrentHashMap<String, Session>(); // Changed to ConcurrentHashMap
    public static Map<String, Channel> allChannel = new ConcurrentHashMap<String, Channel>(); // Changed to ConcurrentHashMap

    // Separate non-fair ReentrantLocks for each map
    private static final ReentrantLock jschLock = new ReentrantLock(false);
    private static final ReentrantLock sessionLock = new ReentrantLock(false);
    private static final ReentrantLock channelLock = new ReentrantLock(false);

	boolean goToFinally = false;
	ApplicationContext context;
	
	//Synchronization of send Action as per organization data
	public Channel configureOrGetChannelUsingPassword(String organization, String password, String user, String host, ApplicationContext context)
	        throws IllegalArgumentException, IllegalStateException, IOException, TimeoutException {
//			  System.out.println("configureOrGetChannelUsingPassword");

	    ErrorRepository errorRepository = context.getBean(ErrorRepository.class);

	    this.context = context;
	    // Using enhanced for loop(for-each) for iteration
	    if (allChannel.containsKey(organization) || allSession.containsKey(organization) || allJSch.containsKey(organization)) {
//				  System.out.println("allChannel.containsKey(organization) is true");

	        while (true) {
	            long timeoutSeconds = BASE_TIMEOUT_SECONDS + channelLock.getQueueLength();
	            try {
	                if (channelLock.tryLock(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)) {
	                    try {
	                        if (allSession.get(organization).isConnected() && allChannel.get(organization).isConnected()) {
	                            return allChannel.get(organization);
	                        } else {
	                            throw new Exception("Session not connected. It will get connected further in code.");
	                        }
	                    } catch (Exception e) {
	                        Report.addError(e.getMessage(), "Ssh Connection", "SshWrapper",
	                                "configureOrGetChannelUsingPassword", organization, errorRepository);
	                        goToFinally = true;
	                    } finally {
	                        if (goToFinally) {
	                            allChannel.get(organization).disconnect();
	                            allSession.get(organization).disconnect();

	                            allChannel.remove(organization);
	                            allSession.remove(organization);
	                            allJSch.remove(organization);
	                        }
	                        channelLock.unlock();
	                    }
	                    break;
	                }
	            } catch (InterruptedException ie) {
	                Thread.currentThread().interrupt();
	            }
	            // Retry if lock not acquired in timeout
	        }
	    }

	    // Using enhanced for loop(for-each) for iteration
	    if (!allChannel.containsKey(organization)) {
//				  System.out.println("allChannel.containsKey(organization) is false");

	        while (true) {
	            long timeoutSeconds = BASE_TIMEOUT_SECONDS + channelLock.getQueueLength();
	            try {
	                if (channelLock.tryLock(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)) {
	                    try {
	                        //ManagerResponse response=managerConnection.sendAction(action,timeout);
//		   			         System.out.println("Creating JSch");
	                        JSch currentJSch = new JSch();

//		   			         System.out.println("Creating Session");
//		   			         System.out.println("Setting Jsch Proterties");
	                        Properties config = new Properties();
	                        config.put("StrictHostKeyChecking", "no");
	                        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");

//			   			 	 System.out.println("Creating Object");
	                        Session currentSession = currentJSch.getSession(user, host, 22);
	                        currentSession.setConfig(config);
	                        currentSession.setPassword(password);

//			   				 System.out.println("Calling Connect");

	                        currentSession.connect(); // here I got Exception....

//			   				 System.out.println("After SSH Connection connect using password");
//			   				 System.out.println("Session : " + String.valueOf(currentSession));

//		   			         System.out.println("Creating Channel");
	                        Channel currentChannel = getJSchChannel(currentSession, organization);

	                        allJSch.put(organization, currentJSch);
	                        allSession.put(organization, currentSession);
	                        allChannel.put(organization, currentChannel);

	                    } catch (Exception e) {
//		  	        	System.out.println("I got an exception");
	                        System.out.println(e.getMessage());
	                        e.printStackTrace();
	                        Report.addError(e.getMessage(), "Ssh Connection", "SshWrapper",
	                                "configureOrGetChannelUsingPassword", organization, errorRepository);
	                        goToFinally = true;
	                    } finally {
	                        if (goToFinally) {
	                            allChannel.get(organization).disconnect();
	                            allSession.get(organization).disconnect();

	                            allChannel.remove(organization);
	                            allSession.remove(organization);
	                            allJSch.remove(organization);
	                        }
	                        channelLock.unlock();
	                    }
	                    break;
	                }
	            } catch (InterruptedException ie) {
	                Thread.currentThread().interrupt();
	            }
	            // Retry if lock not acquired
	        }
	    }

	    return allChannel.get(organization);
	}

	 
	//Synchronization of send Action as per organization data
	public Channel configureOrGetChannelUsingPem(String organization, String fileName, String user, String host, ApplicationContext context)
	        throws IllegalArgumentException, IllegalStateException, IOException, TimeoutException {
	    ErrorRepository errorRepository = context.getBean(ErrorRepository.class);

	    this.context = context;
	    // Using enhanced for loop(for-each) for iteration
	    // Using enhanced for loop(for-each) for iteration
	    if (allChannel.containsKey(organization) || allSession.containsKey(organization) || allJSch.containsKey(organization)) {
	        while (true) {
	            long timeoutSeconds = BASE_TIMEOUT_SECONDS + channelLock.getQueueLength();
	            try {
	                if (channelLock.tryLock(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)) {
	                    try {
	                        if (allSession.get(organization).isConnected() && allChannel.get(organization).isConnected()) {
	                            return allChannel.get(organization);
	                        } else {
	                            throw new Exception("Session not connected. It will get connected further in code.");
	                        }
	                    } catch (Exception e) {
	                        Report.addError(e.getMessage(), "Ssh Connection", "SshWrapper",
	                                "configureOrGetChannelUsingPem", organization, errorRepository);
	                        goToFinally = true;
	                    } finally {
	                        if (goToFinally) {
	                            allChannel.get(organization).disconnect();
	                            allSession.get(organization).disconnect();

	                            allChannel.remove(organization);
	                            allSession.remove(organization);
	                            allJSch.remove(organization);
	                        }
	                        channelLock.unlock();
	                    }
	                    break; // Exit retry loop when done
	                }
	            } catch (InterruptedException ie) {
	                Thread.currentThread().interrupt();
	            }
	            // Retry if lock not acquired within timeout
	        }
	    }

	    if (!allChannel.containsKey(organization)) {
	        while (true) {
	            long timeoutSeconds = BASE_TIMEOUT_SECONDS + channelLock.getQueueLength();
	            try {
	                if (channelLock.tryLock(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS)) {
	                    try {
	                        JSch jsch = getConfigJSchForPEM(fileName);
	                        Session session = getJSchSessionUsingPEM(jsch, organization, user, host);
	                        Channel channel = getJSchChannel(session, organization);

	                        allJSch.put(organization, jsch);
	                        allSession.put(organization, session);
	                        allChannel.put(organization, channel);
	                        //System.out.println(channel);
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                        Report.addError(e.getMessage(), "Ssh Connection", "SshWrapper",
	                                "configureOrGetChannelUsingPem", organization, errorRepository);
	                        goToFinally = true;
	                    } finally {
	                        if (goToFinally) {
	                            allChannel.get(organization).disconnect();
	                            allSession.get(organization).disconnect();

	                            allChannel.remove(organization);
	                            allSession.remove(organization);
	                            allJSch.remove(organization);
	                        }
	                        channelLock.unlock();
	                    }
	                    break; // Exit retry loop when done
	                }
	            } catch (InterruptedException ie) {
	                Thread.currentThread().interrupt();
	            }
	            // Retry if lock not acquired within timeout
	        }
	    }

	    return allChannel.get(organization);
	}

	  
	JSch getConfigJSchForPEM(String fileName) throws JSchException
	{		
		JSch jsch = new JSch();
		
		//File fn = new File(path);
		
		//String pemFormat = addMarkers(key);
		//byte[] decodedBytes = Base64.getDecoder().decode(key);
		//jsch.addIdentity("FreePBX_SSH.pem", decodedBytes, null, null);
		

//		System.out.println("*******************************************************************");
//		System.out.println("*******************************************************************");
//		System.out.println("*******************************************************************");
//		System.out.println("*******************************************************************");
		//File fn = new File("./src/main/resources/"+fileName);
		
		/*File fn = null;
		try {
			fn = new ClassPathResource(fileName).getFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		ApplicationHome home = new ApplicationHome(SpringBootApp.class);
    	home.getDir();    // returns the folder where the jar is. This is what I wanted.
    	home.getSource(); // returns the jar absolute path.
//    	System.out.println(home);
//    	System.out.println(home.getDir());
//    	System.out.println(home.getSource());
		JSch.setConfig("StrictHostKeyChecking", "no");
		File fn = new File(home.getDir()+"/"+fileName);
		jsch.addIdentity(fn.getAbsolutePath());
		
		return jsch;	
	}
	

		
	Session getJSchSessionUsingPEM(JSch jsch,String organization, String user, String host) throws JSchException
	{
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
		//Session session = jsch.getSession("teleconnx", "www.host-tune-perform.com", 22);
		Session session = jsch.getSession(user, host, 22);
		session.setConfig(config);
		//session.setPassword(getPsw());
		session.connect(); // here I got Exception....
		return session;
	}
	
	
	ChannelSftp getJSchChannel(Session session,String organization) throws JSchException {

		ChannelSftp channel = (ChannelSftp)session.openChannel("sftp");
		channel.connect();
		
		return channel;
	}
	
	public Vector<ChannelSftp.LsEntry> getFolderList(ChannelSftp channel, String folder) throws SftpException
	{
		//channel.cd("/a/folder");
		
		
		
//		System.out.println(channel.getHome());
//		System.out.println("Changing to root");
		channel.cd("/");
//		System.out.println(channel.getHome());
//		System.out.println("Changing Folder");
//		System.out.println(folder);
		channel.cd(folder);
		
//		System.out.println("Fetching Entried");
		Vector<ChannelSftp.LsEntry> entries = channel.ls(folder);
		
//		System.out.println(entries);
		
//		System.out.println("Returning Entries");
		return entries;
	}
	
	public InputStream downloadFile(ChannelSftp channel, String folder,String fileNameInFtp) throws SftpException
	{
		channel.cd(folder);
		return channel.get(fileNameInFtp);
	}
	
	public OutputStream uploadFile(ChannelSftp channel, String folder,String src, String  dst, int mode) throws SftpException
	{
		//Upload Modes:
		//public static final int OVERWRITE=0;
		//public static final int RESUME=1;
		//public static final int APPEND=2;
		
		channel.cd(folder);
		return channel.put(dst);
	}

}
