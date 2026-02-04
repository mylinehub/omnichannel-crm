package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.dto.BotInputDTO;
import com.mylinehub.crm.entity.dto.BulkUploadEmployeeDto;
import com.mylinehub.crm.entity.dto.EmployeeBasicInfoDTO;
import com.mylinehub.crm.entity.dto.EmployeeDTO;
import com.mylinehub.crm.entity.dto.EmployeeDataAndStateDTO;
import com.mylinehub.crm.enums.USER_ROLE;
import com.mylinehub.crm.exports.pdf.ExportEmployeeToPDF;
import com.mylinehub.crm.exports.excel.BulkUploadEmployeeToDatabase;
import com.mylinehub.crm.exports.excel.ExportEmployeeToXLSX;
import com.mylinehub.crm.mapper.EmployeeBasicInfoMapper;
import com.mylinehub.crm.mapper.EmployeeMapper;
import com.mylinehub.crm.report.Report;
import com.mylinehub.crm.security.Registration.token.ConfirmationToken;
import com.mylinehub.crm.security.Registration.token.ConfirmationTokenService;
import com.mylinehub.crm.security.email.EmailBuilder;
import com.mylinehub.crm.security.email.EmailService;
import com.mylinehub.crm.whatsapp.api.cloud.wrapper.messages.OkHttpSendTemplateMessageClient;
import com.mylinehub.crm.whatsapp.data.WhatsAppMemoryData;
import com.mylinehub.crm.whatsapp.entity.WhatsAppPhoneNumberTemplates;
import com.mylinehub.crm.whatsapp.enums.MESSAGING_PRODUCT;
import com.mylinehub.crm.whatsapp.requests.WhatsAppTemplateVariableRequest;
import com.mylinehub.crm.ws.client.MyStompSessionHandler;

import io.jsonwebtoken.Jwts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.mylinehub.crm.data.EmployeeDataAndState;
import com.mylinehub.crm.data.OrganizationData;
import com.mylinehub.crm.data.StartedCampaignData;
import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.Notification;
import com.mylinehub.crm.entity.Organization;
import com.mylinehub.crm.entity.RefreshToken;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.ErrorRepository;
import com.mylinehub.crm.repository.NotificationRepository;
import com.mylinehub.crm.repository.RefreshTokenRepository;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;



/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class EmployeeService implements UserDetailsService, CurrentTimeInterface {

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;
    private Environment env;
    private final EmployeeMapper employeeMapper;
    private final EmployeeBasicInfoMapper employeeBasicInfoMapper;
    private final DepartmentService departmentService;
    private final ErrorRepository errorRepository;
    private final NotificationRepository notificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecretKey secretKey;
    private final FileService fileService;
    private final ApplicationContext applicationContext;
    private final OkHttpSendTemplateMessageClient okHttpSendTemplateMessageClient;
	private final AdminService adminService;
	
    private static final String USER_NOT_FOUND_MSG =
            "user with email %s not found";

    
    /**
     * The task of the method is to send welcome email to user.
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendWelcomeEmail(String email,String organization) {
    	Boolean toReturn = false;
    	Employee searchEmployees= getByEmailAndOrganization(email,organization);
		
		if(searchEmployees!=null)
		{
			toReturn = true;
			//System.out.println("Inside sendWelcomeEmail service. Search employee is not null");
			
			//{{login_url}}
			//{{username}}
			//{{start_date}}
			//{{access}}
			//{{senderName}}
			//{{support_email}}
			//{{password}}
			//{{action_url}}
			String body = EmailBuilder.buildWelcomeEmail();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");  
			LocalDateTime now = LocalDateTime.now();   
			String supportEmail = env.getProperty("spring.parentorginization.support.email");
			body= body.replace("{{name}}", searchEmployees.getFirstName()); 
			body= body.replace("{{login_url}}", "https://app.mylinehub.com");
			body= body.replace("{{username}}", searchEmployees.getUsername());
			body= body.replace("{{start_date}}", dtf.format(now).toString());
			body= body.replace("{{access}}", "Admin Access");
			//body= body.replace("{{senderName}}", "");
			body= body.replace("{{support_email}}", supportEmail);
			//body= body.replace("{{password}}", searchEmployees.getPassword());
			
    		emailService.send(email, body,"** Welcome To MylineHub CRM **",supportEmail);
    		return toReturn;
		}
		else {
			//System.out.println("Inside sendWelcomeEmail service. Search employee is null. We will not send welcome email.");
		}
        return toReturn;
    }

    
    /**
     * The task of the method is to send initial passwors email
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendPasswordEmail(String email,String password,String organization) {
    	Boolean toReturn = false;
    	Employee searchEmployees= getByEmailAndOrganization(email,organization);
		
		if(searchEmployees!=null)
		{
			toReturn = true;
			//System.out.println("search employee is not null Inside sendPasswordEmail service");
			String supportEmail = env.getProperty("spring.parentorginization.support.email");

			//{{email}}
			//{{support_url}}
			//{{name}}
			//{{password}}
			String body = EmailBuilder.buildPasswordEmail();  
			body= body.replace("{{name}}", searchEmployees.getFirstName()); 
			body= body.replace("{{email}}", email);
			body= body.replace("{{password}}", password);
			body= body.replace("{{support_email}}", supportEmail);

    		emailService.send(email, body,"** Password For MylineHub Account **",supportEmail);
    		return toReturn;
		}
		else {
			//System.out.println("search employee is null Inside. Not sending password email");
		}
        return toReturn;
    }

    /**
     * The task of the method is to sent password reset email
     * @param email email of the user
     * @return enable user account
     */
    public Boolean sendPasswordResetEmail(String email,String password,String organization) {
    	Boolean toReturn = false;
    	Employee searchEmployees= getByEmailAndOrganization(email,organization);
		
		if(searchEmployees!=null)
		{
			toReturn = true;
			////System.out.println("Inside calling service");
			String supportEmail = env.getProperty("spring.parentorginization.support.email");

			//{{email}}
			//{{support_url}}
			//{{name}}
			//{{password}}
			String body = EmailBuilder.buildPasswordResetEmail(); 
			body= body.replace("{{name}}", searchEmployees.getFirstName()); 
			body= body.replace("{{email}}", email);
			body= body.replace("{{password}}", password);
			body= body.replace("{{support_email}}", supportEmail);

    		emailService.send(email, body,"** Password Reset For MylineHub Account **",supportEmail);
    		return toReturn;
		}
        return toReturn;
    }

    
    /**
     * @param email email of the user
     * @return user found via email
     * @throws UsernameNotFoundException if user does not exist in database
     */
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
    	
//    	//System.out.println("Class : Employee :loadUserByUsername");
    	
    	
        return employeeRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, email)));
    }

    /**
     * The method checks if the user with the given e-mail already exists in the database,
     * if it exists, it throws an exception. However, if the user does not exist in the database,
     * the method adds the user and creates a token that is needfor confirmation.
     * @param employee requestbody of the employee to be saved
     * @throws IllegalStateException if email already exists in the database
     *
     * @return token needed for enable account
     * @throws Exception 
     */
    public String signUpUser(Employee employee) throws Exception{
        boolean userExists = employeeRepository.findByEmail(employee.getEmail()).isPresent();
        if (userExists){
            //TODO: IF USER NOT CONFIRMED, SEND EMAIL AGAIN
            throw new IllegalStateException(
                    String.format("Email %s already taken", employee.getEmail()));
        }

        String encodedPassword = passwordEncoder.encode(employee.getPassword());
        employee.setPassword(encodedPassword);
        employeeRepository.save(employee);
        
        List<Employee> currentEmployees = new ArrayList<Employee>();
    	currentEmployees.add(employee);
    	try {
			sendEmployeeNotifications("create", currentEmployees);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                employee);

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public String googleLogin(String googleToken) throws Exception {
       String toReturn = "";
    	
    	try 
    	{
    		String CLIENT_ID = env.getProperty("spring.security.oauth2.client.registration.google.clientId");
//    		String clientSecret = env.getProperty("spring.security.oauth2.client.registration.google.clientSecret");
    		
//    		//System.out.println("CLIENT_ID: " + CLIENT_ID);
    		
    		HttpTransport httpTransport = new NetHttpTransport();
    		final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    		
//    		//System.out.println("Before Verifier");
    		
    		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
    			    // Specify the CLIENT_ID of the app that accesses the backend:
    			    .setAudience(Collections.singletonList(CLIENT_ID))
    			    // Or, if multiple clients access the backend:
    			    //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
    			    .build();
    		
//    		//System.out.println("Verifier: "+verifier);
    		
//    	    Resource resource = new ClassPathResource("/src/main/resources/google_secret.json");
//    	    InputStream in = resource.getInputStream();
//    	    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
//    	            JSON_FACTORY,
//    	            new InputStreamReader(in)
//    	    );
//    	    Collection<String> scopes = Collections.singleton(
//    	            DriveScopes.DRIVE
//    	    	
//    	    );
//
//    	    File file = new File("/src/main/resources/google_secret.json");
//    	    DataStoreFactory dataStore = new FileDataStoreFactory(file);
//    	    
//    	    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
//								    	    		.Builder(httpTransport,JSON_FACTORY,clientSecrets,scopes)
//								    	            .setAccessType("offline")
//								    	            .setDataStoreFactory(dataStore)
//								    	            .build();
    	    
//    	    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

    		GoogleIdToken idToken = verifier.verify(googleToken);
    		
//    		//System.out.println("idToken: "+idToken);
    		
    		if (idToken != null) {
    			//Do nothing
//    			//System.out.println("idToken is not null");
    		} else {
//    		  //System.out.println("Invalid ID token.");
    		  throw new Exception("Invalid google access token.");
    		}

    		Payload payload = idToken.getPayload();
//    		//System.out.println("Payload extracted");
    		
  		    // Print user identifier
  		    String userId = payload.getSubject();
//  		    //System.out.println("User ID: " + userId);

  		    // Get profile information from payload
  		    final String email = payload.getEmail();
//  		    //System.out.println("email: "+email);
  		    final boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
//  		    //System.out.println("emailVerified: " + emailVerified);
  		  
//  		    final String name = (String) payload.get("name");
//  		    final String pictureUrl = (String) payload.get("picture");
//  		    final String locale = (String) payload.get("locale");
//  		    final String familyName = (String) payload.get("family_name");
//  		    final String givenName = (String) payload.get("given_name");
	        
  		    if(!emailVerified)
  		    {
  		    	throw new Exception("This email is not verified as per google.");
  		    }
  		    
	    	EmployeeDTO employee = findByEmail(email);
	    	
	    	
        	if(employee != null)
        	{	

//        		//System.out.println("Employee not null");
        		
        		RefreshToken refreshToken  = refreshTokenRepository.findByEmail(employee.getEmail());
        		
        		Map<String,String> map = new HashMap<String, String>();
				map.put("authority", employee.getRole());
				
//				//System.out.println("Employee user role: " + employee.getRole());
				
				List<Map<String,String>> allAthorities = new ArrayList<Map<String,String>>();
				
				allAthorities.add(map);

				Calendar cal = Calendar.getInstance(); // creates calendar
		    	cal.setTime(new Date());
		    	cal.add(Calendar.HOUR_OF_DAY, 20);
				
            	String newToken = Jwts.builder()
                        .setSubject(employee.getEmail())
                        .claim("authorities", allAthorities)
                        .setIssuedAt(new Date())
                        .setExpiration(cal.getTime())
                        .signWith(secretKey)
                        .compact();
        		
            	 ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                 
            	 toReturn = "{\n" + 
                 		"  \"data\": {\n" + 
                 		"    \"token\": \""+newToken+"\",\n" + 
                 		"    \"user\":"+ow.writeValueAsString(employee)+"\n" + 
                 		"  }\n" + 
                 		"}";
        		
        		if(refreshToken != null)
        		{
        			refreshToken.setToken(newToken);
            		refreshToken.setExpiryDate(cal.getTime());
            		refreshTokenRepository.save(refreshToken);
        		}
        		else
        		{
        			refreshToken = new RefreshToken();
        			refreshToken.setToken(newToken);
            		refreshToken.setExpiryDate(cal.getTime());
            		refreshTokenRepository.save(refreshToken);
        		}

        	}
        	else
        	{
        		throw new Exception("Employee with such email does not exist in our directory.");
        	}
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
    	
    	return toReturn;
    }
    
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int enableUserOnOrganization(String email,String organization) {
    	int toReturn = employeeRepository.enableUserByOrganization(email,organization);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	
		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
        return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserUiThemeByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserUiThemeByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateNotificationDotStatusByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateNotificationDotStatusByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserAllowedToSwitchOffWhatsAppAIByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserAllowedToSwitchOffWhatsAppAIByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserRecordAllCallsByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserRecordAllCallsByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserDoNotDisturbByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserDoNotDisturbrByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserStartVideoFullScreenByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserStartVideoFullScreenByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserCallWaitingByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserCallWaitingByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserIntercomPolicyByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserIntercomPolicyByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserFreeDialOptionByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserFreeDialOptionByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserTextDictationByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserTextDictationByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserTextMessagingByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserTextMessagingByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserAutoAnswerByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserAutoAnswerByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserAutoConferenceByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserAutoConferenceByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserAutoVideoByOrganization(String email,String organization,Boolean value) {
    	int toReturn =  employeeRepository.updateUserAutoVideoByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserMicDeviceByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserMicDeviceByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserSpeakerDeviceByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserSpeakerDeviceByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserVideoDeviceByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserVideoDeviceByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserVideoOrientationByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserVideoOrientationByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserVideoQualityByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserVideoQualityByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserVideoFrameRateByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserVideoFrameRateByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserAutoGainControlByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserAutoGainControlByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserEchoCancellationByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserEchoCancellationByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateUserNoiseSupressionByOrganization(String email,String organization,String value) {
    	int toReturn =  employeeRepository.updateUserNoiseSupressionByOrganization(email,organization,value);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int disableUserOnOrganization(String email,String organization) {
    	int toReturn =  employeeRepository.disableUserByOrganization(email,organization);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		
    	 return toReturn;
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int enableUseAllotedSecondLineByOrganization(String email,String organization) {
    	 int toReturn =  employeeRepository.enableUseAllotedSecondLineByOrganization(email,organization);
    	 
    	 Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	 Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
 		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
 		if(allEmployeeDataAndState != null)
 		{
 			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
 		} 
 		employeeDataAndStateDTO.setEmployee(currentEmployee);
 		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
    	 
    	 return toReturn;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int disableUseAllotedSecondLineByOrganization(String email,String organization) {
    	int toReturn =  employeeRepository.disableUseAllotedSecondLineByOrganization(email,organization);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		 
    	 return toReturn;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int enableUserCallOnMobileByOrganization(String email,String organization) {
    	int toReturn =  employeeRepository.enableUserCallOnMobileByOrganization(email,organization);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		 
    	 return toReturn;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int disableUserCallOnMobileByOrganization(String email,String organization) {
    	int toReturn =  employeeRepository.disableUserCallOnMobileByOrganization(email,organization);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email, organization);
    	Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
		if(allEmployeeDataAndState != null)
		{
			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
		} 
		employeeDataAndStateDTO.setEmployee(currentEmployee);
		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
		 
    	 return toReturn;
    }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateWebPassword(String password, String email,String organization) {
        
    	////System.out.println(passwordEncoder.encode(password));
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email,organization);
    	if(currentEmployee==null)
    	{
    		////System.out.println("Current Employee Is Null");
    		return 0;
    	}
    	else
    	{
    		
    		try
    		{
    			currentEmployee.setPassword(passwordEncoder.encode(password));
    			currentEmployee = employeeRepository.save(currentEmployee);
    			
    			Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
    			EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
    			if(allEmployeeDataAndState != null)
    			{
    				employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
    			} 
    			employeeDataAndStateDTO.setEmployee(currentEmployee);
    			EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
    			
    			List<Employee> currentEmployees = new ArrayList<Employee>();
            	currentEmployees.add(currentEmployee);
            	try {
					sendEmployeeNotifications("update-web-password", currentEmployees);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			return 1;
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			//System.out.println("Exception while updating employee");
    			return 0;
    		}
    	}
     }

    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public int updateExtentionPassword(String password,String email,String organization) {

    	////System.out.println(email);
    	////System.out.println(organization);
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email,organization);
    	if(currentEmployee==null)
    	{
    		////System.out.println("Current Employee Is Null");
    		return 0;
    	}
    	else
    	{
    		
    		try
    		{
    			//System.out.println("Setting up Extension Password");
    			currentEmployee.setExtensionpassword(password);
    			currentEmployee = employeeRepository.save(currentEmployee);
    			
    			Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
    			EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
    			if(allEmployeeDataAndState != null)
    			{
    				employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
    			} 
    			employeeDataAndStateDTO.setEmployee(currentEmployee);
    			EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
    			
    			List<Employee> currentEmployees = new ArrayList<Employee>();
            	currentEmployees.add(currentEmployee);
            	try {
					sendEmployeeNotifications("update-extension-password", currentEmployees);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			return 1;
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			//System.out.println("Exception while updating employee");
    			return 0;
    		}
    	}
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     */
    public boolean updateEmployeeByOrganization(EmployeeDTO employeeDetails,String oldEmail) {
    	
    	//System.out.println("I am in updateEmployeeByOrganization");
    	
    	//System.out.println("Old Email :"+employeeDetails.getEmail());
    	
    	//System.out.println("Email :"+employeeDetails.getEmail());
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(oldEmail,employeeDetails.getOrganization());
        
    	Employee newEmployee = null;
    	if(oldEmail.equals(employeeDetails.getEmail())) {
    		
    	}
    	else
    	{
    		newEmployee =  employeeRepository.findByEmailAndOrganization(employeeDetails.getEmail(),employeeDetails.getOrganization());
    	}
        
    	////System.out.println("After finding");
    	
    	if(currentEmployee==null)
    	{
    		////System.out.println("Current Employee Is Null");
    		return false;
    	}
    	else
    	{
    		
    		if(newEmployee == null)
    		{
    			try
        		{

        			String oldPhoneNumber = currentEmployee.getPhonenumber();
        					
            		////System.out.println("User Role : "+userRole);
            		if(USER_ROLE.ADMIN.name().equalsIgnoreCase(employeeDetails.getRole()))
              	  {
            			////System.out.println("User Role -IF-ELSE- Admin");
            			currentEmployee.setUserRole(USER_ROLE.ADMIN);
              	  }
              	  else if(USER_ROLE.EMPLOYEE.name().equalsIgnoreCase(employeeDetails.getRole())) 
              	  {
              		////System.out.println("User Role -IF-ELSE- EMPLOYEE");
              		currentEmployee.setUserRole(USER_ROLE.EMPLOYEE);
              	  }
              	  else if(USER_ROLE.MANAGER.name().equalsIgnoreCase(employeeDetails.getRole()))  
              	  {
              		  
              		////System.out.println("User Role -IF-ELSE- MANAGER");
              		currentEmployee.setUserRole(USER_ROLE.MANAGER);
              		 
              	  }
            		
            		
            		currentEmployee.setFirstName(employeeDetails.getFirstName());
            		currentEmployee.setLastName(employeeDetails.getLastName());
            		currentEmployee.getDepartment().setId(employeeDetails.getDepartmentId());
            		currentEmployee.setSalary(employeeDetails.getSalary());
            		currentEmployee.setEmail(employeeDetails.getEmail());
            		currentEmployee.setPhoneContext(employeeDetails.getPhoneContext());
            		currentEmployee.setPhoneTrunk(employeeDetails.getPhoneTrunk());
            		currentEmployee.setDomain(employeeDetails.getDomain());
            		currentEmployee.setSecondDomain(employeeDetails.getSecondDomain());
            		currentEmployee.setExtension(employeeDetails.getExtension());
//            		currentEmployee.setExtensionpassword(employeeDetails.getExtensionpassword());
            		currentEmployee.setTimezone(TimeZone.getTimeZone(employeeDetails.getTimezone()));
            		currentEmployee.setType(employeeDetails.getType());
            		currentEmployee.setCallonnumber(employeeDetails.isCallonnumber());
            		currentEmployee.setPhonenumber(employeeDetails.getPhonenumber());
            		currentEmployee.setTransfer_phone_1(employeeDetails.getTransfer_phone_1());
            		currentEmployee.setTransfer_phone_2(employeeDetails.getTransfer_phone_2());
            		currentEmployee.setBirthdate(employeeDetails.getBirthdate());
            		currentEmployee.setCostCalculation(employeeDetails.getCostCalculation());
            		currentEmployee.setAmount(employeeDetails.getAmount());
            		
            		currentEmployee.setProvider1(employeeDetails.getProvider1());
            		currentEmployee.setAllotednumber1(employeeDetails.getAllotednumber1());
            		currentEmployee.setProvider2(employeeDetails.getProvider2());
            		currentEmployee.setAllotednumber2(employeeDetails.getAllotednumber2());
            		
//            		currentEmployee.setImageName(employeeDetails.getImageName());
//            		currentEmployee.setImageType(employeeDetails.getImageType());
//            		currentEmployee.setImageData(employeeDetails.getImageData());
            		
//            		currentEmployee.setGovernmentDocument1Data(employeeDetails.getGovernmentDocument1Data());
//            		currentEmployee.setGovernmentDocument2Data(employeeDetails.getGovernmentDocument2Data());
            		currentEmployee.setGovernmentDocumentID1(employeeDetails.getGovernmentDocumentID1());
            		currentEmployee.setGovernmentDocumentID2(employeeDetails.getGovernmentDocumentID2());
            		
                    currentEmployee.setRecordAllCalls(employeeDetails.isRecordAllCalls());
            		currentEmployee.setIntercomPolicy(employeeDetails.isIntercomPolicy());
            		currentEmployee.setFreeDialOption(employeeDetails.isFreeDialOption());
            		currentEmployee.setTextDictateOption(employeeDetails.isTextDictateOption());
            		currentEmployee.setTextMessagingOption(employeeDetails.isTextMessagingOption());
            		currentEmployee.setConfExtension(employeeDetails.getConfExtension());
            		
//            		currentEmployee.setAutoAnswer(employeeDetails.isAutoAnswer());
//            		currentEmployee.setAutoConference(employeeDetails.isAutoConference());
//            		currentEmployee.setAutoVideo(employeeDetails.isAutoVideo());
//            		currentEmployee.setMicDevice(employeeDetails.getMicDevice());
//            		currentEmployee.setSpeakerDevice(employeeDetails.getSpeakerDevice());
//            		currentEmployee.setVideoDevice(employeeDetails.getVideoDevice());
//            		currentEmployee.setVideoOrientation(employeeDetails.getVideoOrientation());
//            		currentEmployee.setVideoQuality(employeeDetails.getVideoQuality());
//            		currentEmployee.setVideoFrameRate(employeeDetails.getVideoFrameRate());
//            		currentEmployee.setAutoGainControl(employeeDetails.getAutoGainControl());
//            		currentEmployee.setEchoCancellation(employeeDetails.getEchoCancellation());
//            		currentEmployee.setNoiseSupression(employeeDetails.getNoiseSupression());
            		
            		currentEmployee = employeeRepository.save(currentEmployee);
            		
            		
            		//Update employee data in static variables:
            		
            		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
            		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
            		if(allEmployeeDataAndState != null)
            		{
            			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
            		} 
            		employeeDataAndStateDTO.setEmployee(currentEmployee);
            		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
            		
            		
            		EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(oldPhoneNumber, null, "delete");
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(currentEmployee.getPhonenumber(), currentEmployee.getExtension(), "update");

            		StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(oldPhoneNumber, null, "delete");
            		StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(currentEmployee.getPhonenumber(), currentEmployee.getExtension(), "update");
            		
                	List<Employee> currentEmployees = new ArrayList<Employee>();
                	currentEmployees.add(currentEmployee);
                	try {
    					sendEmployeeNotifications("update", currentEmployees);
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                	
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace();
        			//System.out.println("Exception while updating employee");
        			return false;
        		}
    		}
    		else
    		{
    			return false;
    		}
    		
    	}
    	
        return true;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean updateSelfByOrganization(EmployeeDTO employeeDetails,String oldEmail) throws Exception {
    	
    	////System.out.println("I am in updateEmployeeByOrganization");
    	
    	////System.out.println("Email :"+employeeDetails.getEmail());
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(oldEmail,employeeDetails.getOrganization());
        
    	////System.out.println("After finding");
    	
    	Employee newEmployee = null;
    	if(oldEmail.equals(employeeDetails.getEmail())) {
    		
    	}
    	else
    	{
    		newEmployee =  employeeRepository.findByEmailAndOrganization(employeeDetails.getEmail(),employeeDetails.getOrganization());
    	}
    	
    	if(currentEmployee==null)
    	{
    		////System.out.println("Current Employee Is Null");
    		return false;
    	}
    	else
    	{
    		
    		if(newEmployee == null)
    		{
    			try
        		{

        			String oldPhoneNumber = currentEmployee.getPhonenumber();
        			
            		currentEmployee.setFirstName(employeeDetails.getFirstName());
            		currentEmployee.setLastName(employeeDetails.getLastName());
            		currentEmployee.getDepartment().setId(employeeDetails.getDepartmentId());
            		currentEmployee.setSalary(employeeDetails.getSalary());
            		currentEmployee.setEmail(employeeDetails.getEmail());
            		currentEmployee.setPhoneContext(employeeDetails.getPhoneContext());
            		currentEmployee.setPhoneTrunk(employeeDetails.getPhoneTrunk());
            		currentEmployee.setDomain(employeeDetails.getDomain());
            		currentEmployee.setSecondDomain(employeeDetails.getSecondDomain());
            		currentEmployee.setExtension(employeeDetails.getExtension());
//            		currentEmployee.setExtensionpassword(employeeDetails.getExtensionpassword());
            		currentEmployee.setTimezone(TimeZone.getTimeZone(employeeDetails.getTimezone()));
            		currentEmployee.setType(employeeDetails.getType());
            		currentEmployee.setCallonnumber(employeeDetails.isCallonnumber());
            		currentEmployee.setPhonenumber(employeeDetails.getPhonenumber());
            		currentEmployee.setTransfer_phone_1(employeeDetails.getTransfer_phone_1());
            		currentEmployee.setTransfer_phone_2(employeeDetails.getTransfer_phone_2());
            		currentEmployee.setBirthdate(employeeDetails.getBirthdate());
            		currentEmployee.setCostCalculation(employeeDetails.getCostCalculation());
            		currentEmployee.setAmount(employeeDetails.getAmount());
            		
            		currentEmployee.setProvider1(employeeDetails.getProvider1());
            		currentEmployee.setAllotednumber1(employeeDetails.getAllotednumber1());
            		currentEmployee.setProvider2(employeeDetails.getProvider2());
            		currentEmployee.setAllotednumber2(employeeDetails.getAllotednumber2());
            		
//            		currentEmployee.setImageName(employeeDetails.getImageName());
//            		currentEmployee.setImageType(employeeDetails.getImageType());
//            		currentEmployee.setImageData(employeeDetails.getImageData());
            		
//            		currentEmployee.setGovernmentDocument1Data(employeeDetails.getGovernmentDocument1Data());
//            		currentEmployee.setGovernmentDocument2Data(employeeDetails.getGovernmentDocument2Data());
            		currentEmployee.setGovernmentDocumentID1(employeeDetails.getGovernmentDocumentID1());
            		currentEmployee.setGovernmentDocumentID2(employeeDetails.getGovernmentDocumentID2());
            		
            		currentEmployee.setRecordAllCalls(employeeDetails.isRecordAllCalls());
             		currentEmployee.setIntercomPolicy(employeeDetails.isIntercomPolicy());
             		currentEmployee.setFreeDialOption(employeeDetails.isFreeDialOption());
             		currentEmployee.setTextDictateOption(employeeDetails.isTextDictateOption());
             		currentEmployee.setTextMessagingOption(employeeDetails.isTextMessagingOption());
             		currentEmployee.setConfExtension(employeeDetails.getConfExtension());
             		
//            		currentEmployee.setAutoAnswer(employeeDetails.isAutoAnswer());
//            		currentEmployee.setAutoConference(employeeDetails.isAutoConference());
//            		currentEmployee.setAutoVideo(employeeDetails.isAutoVideo());
//            		currentEmployee.setMicDevice(employeeDetails.getMicDevice());
//            		currentEmployee.setSpeakerDevice(employeeDetails.getSpeakerDevice());
//            		currentEmployee.setVideoDevice(employeeDetails.getVideoDevice());
//            		currentEmployee.setVideoOrientation(employeeDetails.getVideoOrientation());
//            		currentEmployee.setVideoQuality(employeeDetails.getVideoQuality());
//            		currentEmployee.setVideoFrameRate(employeeDetails.getVideoFrameRate());
//            		currentEmployee.setAutoGainControl(employeeDetails.getAutoGainControl());
//            		currentEmployee.setEchoCancellation(employeeDetails.getEchoCancellation());
//            		currentEmployee.setNoiseSupression(employeeDetails.getNoiseSupression());
            		
            		currentEmployee = employeeRepository.save(currentEmployee);
            		
            		
            		//Update employee data in static variables:
            		
            		Map<String,EmployeeDataAndStateDTO> allEmployeeDataAndState = EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "get-one");
            		EmployeeDataAndStateDTO employeeDataAndStateDTO= null;
            		if(allEmployeeDataAndState != null)
            		{
            			employeeDataAndStateDTO = allEmployeeDataAndState.get(currentEmployee.getExtension());
            		} 
            		employeeDataAndStateDTO.setEmployee(currentEmployee);
            		EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), employeeDataAndStateDTO, "update");
            		
                    
 
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(oldPhoneNumber, null, "delete");
                    EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(currentEmployee.getPhonenumber(), currentEmployee.getExtension(), "update");

            		StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(oldPhoneNumber, null, "delete");
            		StartedCampaignData.workOnAllActivePhoneAndTheirExtensions(currentEmployee.getPhonenumber(), currentEmployee.getExtension(), "update");
            		
            		List<Employee> currentEmployees = new ArrayList<Employee>();
                	currentEmployees.add(currentEmployee);
                	try {
    					sendEmployeeNotifications("update", currentEmployees);
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
        		}
        		catch(Exception e)
        		{
        			e.printStackTrace();
        			//System.out.println("Exception while updating employee");
        			return false;
        		}
        		
    		}
    		else
    		{
    			return false;
    		}
    		
    	}

    	
        return true;
    }
    
    
    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean createEmployeeByOrganization(EmployeeDTO employeeDetails) throws Exception {
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(employeeDetails.getEmail(),employeeDetails.getOrganization());
    	
    	if(currentEmployee==null)
    	{
    		OrganizationService.lastUsedRegistrationExtension= 0;
    		
            currentEmployee = employeeMapper.mapDtoToEmployee(employeeDetails);
            currentEmployee = BulkUploadEmployeeToDatabase.addEmployeeDefault(currentEmployee);
            currentEmployee = employeeRepository.save(currentEmployee);
    		BulkUploadEmployeeDto bulkUploadEmployeeDto = new BulkUploadEmployeeDto();
    		bulkUploadEmployeeDto.setActualPassword(currentEmployee.getPassword());
    		currentEmployee.setPassword(passwordEncoder.encode(currentEmployee.getPassword()));
    		bulkUploadEmployeeDto.setEmployee(currentEmployee);
    		newUserCreationAfterTriggers(bulkUploadEmployeeDto);
    	}
    	else
    	{
    		
    		
    		return false;
    	}
    
    	
        return true;
    }

    /**
     * The task of the method is enable user in the database after confirming the account
     * @param email email of the user
     * @return enable user account
     * @throws Exception 
     */
    public boolean deleteEmployeeByEmailAndOrganization(Employee employee,String email, String organization) throws Exception {
    	
    	Employee currentEmployee = employeeRepository.findByEmailAndOrganization(email,organization);
    	
    	if(currentEmployee==null)
    	{
    		return false;
    	}
    	else
    	{   
    		
    		String uploadDoc2OriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadDoc2OriginalDirectory");
    		String uploadDoc1OriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadDoc1OriginalDirectory");
    		String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadEmployeeOriginalDirectory");
            String uploadIconDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadEmployeeIconDirectory");
            
        	Employee current = employeeRepository.findByEmailAndOrganization(email, organization);
        	
        	if(current.getImageData() != null || current.getImageData() != "")
        	{
        		try {
    			
    				String name = current.getGovernmentDocument2Data();
            		name = name.replace(uploadDoc2OriginalDirectory+"/", "");
            		fileService.deleteFile(employee.getOrganization(),uploadDoc2OriginalDirectory, name);
        		}
        		catch(Exception e)
        		{
        			//Donot do anything
//        			e.printStackTrace();
        		}
        		
        		
        		
        		try {
    				String name = current.getGovernmentDocument1Data();
            		name = name.replace(uploadDoc1OriginalDirectory+"/", "");
            		fileService.deleteFile(employee.getOrganization(),uploadDoc1OriginalDirectory, name);
        		}
        		catch(Exception e)
        		{
        			//Donot do anything
//        			e.printStackTrace();
        		}
        		
        		
        		try {
        			
        			String name = current.getImageData();
            		name = name.replace(uploadOriginalDirectory+"/", "");
            		fileService.deleteFile(employee.getOrganization(),uploadOriginalDirectory, name);
        		}
        		catch(Exception e)
        		{
        			//Donot do anything
//        			e.printStackTrace();
        		}
				
				
    			
        		
        		try {
        			
        			String name = current.getIconImageData();
            		name = name.replace(uploadIconDirectory+"/", "");
            		fileService.deleteFile(employee.getOrganization(),uploadIconDirectory, name);
        		}
        		catch(Exception e)
        		{
        			//Donot do anything
//        			e.printStackTrace();
        		}
        		
        	}
    		
    		employeeRepository.delete(currentEmployee);
            EmployeeDataAndState.workOnAllEmployeeDataAndState(currentEmployee.getExtension(), null, "delete");
            EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(currentEmployee.getExtension(), null, "delete");
    	}
    	
        return true;
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByEmailAndOrganization(String email,String organization ){
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByEmailAndOrganization(email,organization));
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public Employee getByEmailAndOrganization(String email,String organization ){
        return employeeRepository.findByEmailAndOrganization(email,organization);
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO findByEmail(String email ){
        return  employeeMapper.mapEmployeeToDto(employeeRepository.findByEmail(email).get());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByExtensionAndOrganization(String extension,String organization ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByExtensionAndOrganization(extension,organization));
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public Employee getByExtensionAndOrganization(String extension,String organization ){
    	
        return employeeRepository.findByExtensionAndOrganization(extension,organization);
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByAllotednumber1(String allotednumber1 ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByAllotednumber1(allotednumber1));
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByAllotednumber2(String allotednumber2 ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByAllotednumber2(allotednumber2));
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByPhonenumber(String phonenumber ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByPhonenumber(phonenumber));
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByAllotednumber1Containing(String allotednumber1 ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByAllotednumber1Containing(allotednumber1));
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByAllotednumber2Containing(String allotednumber2 ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByAllotednumber2Containing(allotednumber2));
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByPhonenumberContaining(String phonenumber ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByPhonenumberContaining(phonenumber));
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByExtension(String extension ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByExtension(extension));
    }

    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public EmployeeDTO getEmployeeByPhonenumberAndOrganization(String phonenumber,String organization ){
    	
        return employeeMapper.mapEmployeeToDto(employeeRepository.findByPhonenumberAndOrganization(phonenumber,organization));
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeDTO> getAllEmployeesOnPhoneContextAndOrganization(String phoneContext, String organization){
        return employeeRepository.findAllByPhoneContextAndOrganization(phoneContext,organization)
                .stream()
                .map(employeeMapper::mapEmployeeToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeDTO> getAllEmployeesOnSexAndOrganization(String sex, String organization){
        return employeeRepository.findAllBySexAndOrganization(sex,organization)
                .stream()
                .map(employeeMapper::mapEmployeeToDto)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeDTO> findAllBycostCalculationAndOrganization(String costCalculation, String organization){
        return employeeRepository.findAllBycostCalculationAndOrganization(costCalculation,organization)
                .stream()
                .map(employeeMapper::mapEmployeeToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeDTO> getAllEmployeesOnUserRoleAndOrganization(USER_ROLE userRole, String organization){
        return employeeRepository.findAllByUserRoleAndOrganization(userRole,organization)
                .stream()
                .map(employeeMapper::mapEmployeeToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeDTO> getAllEmployeesOnIsEnabledAndOrganization(Boolean isEnabled, String organization){
        return employeeRepository.findAllByIsEnabledAndOrganization(isEnabled,organization)
                .stream()
                .map(employeeMapper::mapEmployeeToDto)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<Employee> findAllEmployeesOnIsEnabledAndOrganization(Boolean isEnabled, String organization){
        return employeeRepository.findAllByIsEnabledAndOrganization(isEnabled,organization);
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeBasicInfoDTO> getAllEmployeesBasicInfoByOrganization(String organization,Employee employee){
        return employeeRepository.findAllBasicByOrganizationWithIconImage(organization,employee.getExtension())
                .stream()
                .map(employeeBasicInfoMapper::mapEmployeeToBasicInfoDto)
                .collect(Collectors.toList());
    }
    
    
    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<EmployeeDTO> getAllEmployeesByOrganization(String organization){
        return employeeRepository.findAllByOrganization(organization)
                .stream()
                .map(employeeMapper::mapEmployeeToDto)
                .collect(Collectors.toList());
    }
    

    /**
     * The method is to retrieve all employee from the database and display them.
     *
     * After downloading all the data about the employee,
     * the data is mapped to dto which will display only those needed
     * @return list of all employees with specification of data in EmployeeDTO
     */
    
    public List<Employee> getFullDetailOfAllEmployeesByOrganization(String organization){
        return employeeRepository.findFullDetailOfAllByOrganization(organization);
    }

    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=employees_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Employee> employeeList = employeeRepository.findAll();

        ExportEmployeeToXLSX exporter = new ExportEmployeeToXLSX(employeeList);
        exporter.export(response);
    }

    
    /**
     * The purpose of the method is to set the details of the
     * excel file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToExcelOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=employees_" + getCurrentDateTime() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Employee> employeeList = employeeRepository.findAllByOrganization(organization);

        ExportEmployeeToXLSX exporter = new ExportEmployeeToXLSX(employeeList);
        exporter.export(response);
    }
    
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=employees_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Employee> listEmployees = employeeRepository.findAll();

        ExportEmployeeToPDF exporter = new ExportEmployeeToPDF(listEmployees);
        exporter.export(response);
    }
    
    
    /**
     * The purpose of the method is to set the details of the
     * pdf file that will be exported for download and then download it.
     * @param response response to determine the details of the file
     * @throws IOException if incorrect data is sent to the file
     */
    public void exportToPDFOnOrganization(String organization,HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=employees_" + getCurrentDateTime() + ".pdf";
        response.setHeader(headerKey, headerValue);

        List<Employee> listEmployees = employeeRepository.findAllByOrganization(organization);
        ExportEmployeeToPDF exporter = new ExportEmployeeToPDF(listEmployees);
        
        //System.out.println(response);
        
        exporter.export(response);
    }
    
    
    
    public void uploadEmployeeUsingExcel(MultipartFile file,String email,String organization) throws Exception {
        try {
        	
        	OrganizationService.lastUsedRegistrationExtension= 0;
        	////System.out.println("I am inside try of uoload Employee Service");
          List<BulkUploadEmployeeDto> employees = new BulkUploadEmployeeToDatabase().excelToEmployees(this,departmentService,file.getInputStream(),email,organization,passwordEncoder,errorRepository);
         
          List<Employee> employeesAll = new ArrayList<>();
          for(int i = 0;i<employees.size(); i++){
        	  employeesAll.add(employees.get(i).getEmployee());
          } 
          
          employeesAll = employeeRepository.saveAll(employeesAll);

          try
  		 {
        	//send email to each employee added
        	  employees.stream().forEach(
        	            (employee) -> {
        	            	newUserCreationAfterTriggers(employee);
        	            });

  		}
  		catch(Exception e)
  		{
  			 Report.addError("Bulk Upload", e.getMessage(),"Employee Service", "Cannot send email while bulk upload employee",organization,errorRepository);	
					
  		}
          
        } catch (IOException e) {
        	////System.out.println("I am inside catch of Upload service");
          throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
      }
    
    public boolean newUserCreationAfterTriggers(BulkUploadEmployeeDto employee)
    {
    	boolean toReturn = false;
    	
    	//System.out.println("****************** newUserCreationAfterTriggers ******************");
    	
    	try {
    		
    		try {
        		//System.out.println("Send Welcome Email");
        		sendWelcomeEmail(employee.getEmployee().getEmail(),employee.getEmployee().getOrganization());
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}
    		
  			try {
  	    		//System.out.println("Send Password Email");
  	  			sendPasswordEmail(employee.getEmployee().getEmail(),employee.getActualPassword(),employee.getEmployee().getOrganization());
    		}
    		catch(Exception e) {
    			e.printStackTrace();
    		}

  			//System.out.println("Save Employee To Memory");
  			setNewEmployeeToMemoryData(employee.getEmployee()); 	
  			//System.out.println("Send Employee Notification");
        	List<Employee> currentEmployees = new ArrayList<Employee>();
        	currentEmployees.add(employee.getEmployee());
        	
        	
        	try {
				sendEmployeeNotifications("create", currentEmployees);
				//addedtofbproject
				sendEmployeeNotifications("addedtofbproject", currentEmployees);
				//whatsappforsupport
				sendEmployeeNotifications("whatsappforsupport", currentEmployees);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	//System.out.println("Send Whats App Messages");
        	
        	try {
        		//Send whats app trigger for employees
        		//System.out.println("****************** Send Onbarding Whats App Message ******************");
        		String onboardingTemplate = env.getProperty("spring.template.onboarding");
        		sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(employee,onboardingTemplate,null,"0","url");
   			 
        	}catch (Exception e) {
				// TODO Auto-generated catch block
        		//System.out.println("Exception while sending whats app message to employee for onboarding");
				e.printStackTrace();
			}
        	

        	try {
        		
        		//System.out.println("****************** Send Password Whatsapp Message ******************");
        		String passcodeTemplate = env.getProperty("spring.template.passwordrecovery");
        		sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(employee,passcodeTemplate,null,"0","url");
   			 
        	}catch (Exception e) {
				// TODO Auto-generated catch block
        		//System.out.println("Exception while sending whats app message to employee for passcode");
				e.printStackTrace();
			}
        	
        	
        	try {
        		//System.out.println("****************** Send Email to Admin ******************");
        		//Send Admin Trigger for email and whats app
        		adminService.sendNewOrgEmailtoParent(employee.getEmployee().getOrganization(), employee.getEmployee().getFirstName()+"-"+employee.getEmployee().getLastName(), employee.getEmployee().getPhonenumber());
   			 
        	}catch (Exception e) {
				// TODO Auto-generated catch block
        		//System.out.println("Exception while sending email to admin");
				e.printStackTrace();
			}
        	
        	try {
        		//System.out.println("****************** Send Whats App to Admin ******************");
        		//Send Admin Trigger for email and whats app
                adminService.sendEmployeeOnboardingWhatsAppMessageToParent(employee);
   			 
        	}catch (Exception e) {
				// TODO Auto-generated catch block
        		//System.out.println("Exception while sending whats app message to admin");
				e.printStackTrace();
			}
    		
        	
			toReturn = true;
    	}
    	catch(Exception  e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return toReturn;
    }

    
    public boolean sendEmployeeSpecificWhatsAppMessageAsPerTemplateName(BulkUploadEmployeeDto employee,String templateName,String amount, String buttonIndex,String buttonSub_type) throws Exception {
    	boolean toReturn = false;
    	try {
    		//System.out.println("Send Employee Specific WhatsApp Message As Per Template Name");
    		
    		WhatsAppTemplateVariableRequest whatsAppTemplateVariableRequest = new WhatsAppTemplateVariableRequest();
    		whatsAppTemplateVariableRequest.setEmployee(employee.getEmployee());
    		whatsAppTemplateVariableRequest.setName(employee.getEmployee().getFirstName()+" "+employee.getEmployee().getLastName());
    		whatsAppTemplateVariableRequest.setEmail(employee.getEmployee().getEmail());
    		whatsAppTemplateVariableRequest.setCode(employee.getActualPassword());
    		whatsAppTemplateVariableRequest.setAmount(amount);
    		whatsAppTemplateVariableRequest.setReason(employee.getReason());
    		whatsAppTemplateVariableRequest.setParentorg(employee.getParentOrg());
    		whatsAppTemplateVariableRequest.setIndex(buttonIndex);
    		whatsAppTemplateVariableRequest.setSub_type(buttonSub_type);
    		
    		Map<String,Organization> organizationMap = OrganizationData.workWithAllOrganizationData(employee.getEmployee().getOrganization(),null,"get-one",null);
    		
    		if(organizationMap == null || organizationMap.size() == 0) {
    			throw new Exception("Organization not found for employee having org : "+ employee.getEmployee().getOrganization());
    		}
    		
    		Organization Organization = organizationMap.get(employee.getEmployee().getOrganization());
    		whatsAppTemplateVariableRequest.setOrganization(Organization);
    		
    		//System.out.println("Fetching parent phone number from env file");
    		String phone = env.getProperty("spring.whatsapp.phone");
    		
    		//System.out.println("Fetching parent templates from memory as per phone number");
    		//Add detail to memory Data
    		Map<String,List<WhatsAppPhoneNumberTemplates>> allTemplatesMap = WhatsAppMemoryData.workWithWhatsAppPhoneNumbersTemplates(phone,null,"get-one");
    		
    		//System.out.println("Total templates fetched : "+allTemplatesMap.size());
    		
    		if(allTemplatesMap == null || allTemplatesMap.size() == 0) {
    				throw new Exception("No template found for parent. Please connect with support.");
    		}
    		
    		List<WhatsAppPhoneNumberTemplates> allTemplates = allTemplatesMap.get(phone);

    				
    		allTemplates.forEach((element)->{
    			if(element.getTemplateName().equalsIgnoreCase(templateName)) {
    				try {
    					
    					//System.out.println("Sending whats app message for template : "+ element.getTemplateName());
    	    			whatsAppTemplateVariableRequest.setWhatsAppPhoneNumberTemplate(element);
    	    			JSONObject jsonObject = okHttpSendTemplateMessageClient.sendMessage(MESSAGING_PRODUCT.whatsapp.name(),employee.getEmployee().getPhonenumber(), whatsAppTemplateVariableRequest, whatsAppTemplateVariableRequest.getWhatsAppPhoneNumberTemplate().getLanguageCode(),  element.getWhatsAppPhoneNumber().getWhatsAppProject().getApiVersion(), element.getWhatsAppPhoneNumber().getPhoneNumberID(), element.getWhatsAppPhoneNumber().getWhatsAppProject().getAccessToken());
    	    			
    	    			//System.out.println("*****************************************************************");
    					//System.out.println("Response Data From Whats App");
    					//System.out.println("*****************************************************************");
    	    			//System.out.println("Response : "+jsonObject);
    				
    				} catch (Exception e) {
						// TODO Auto-generated catch block
						//System.out.println("Exception while sending password reset message");
						e.printStackTrace();
					}
            		
    			}
    		});
    		toReturn = true;
    	}
    	catch(Exception  e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return toReturn;
    }
    
    
    public boolean setNewEmployeeToMemoryData(Employee employee)
    {
    	//System.out.println("****************** setNewEmployeeToMemoryData ******************");
    	
    	boolean toReturn = false;
    	
    	try {
    		String state = "terminated";
  			String presence = "danger";
  			String dotClass = "dotOffline";
  			List<String> combinedValue = new ArrayList<String>();
  			combinedValue.add(state);
  			combinedValue.add(presence);
  			combinedValue.add(dotClass);
  			
  			EmployeeDataAndStateDTO current = new EmployeeDataAndStateDTO();
			current.setEmployee(employee);
			current.setMemberState(combinedValue);
			current.setExtensionState(combinedValue);
			
			EmployeeDataAndState.workOnAllEmployeeDataAndState(employee.getExtension(), current, "update");

			if((employee.getPhonenumber() != null) && (employee.getPhonenumber() != ""))
			{
				EmployeeDataAndState.workOnAllEmployeePhoneAndExtension(employee.getPhonenumber(),employee.getExtension(), "update");
			}
			
			toReturn = true;
    	}
    	catch(Exception  e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return toReturn;
    }
    
    public boolean uploadProfilePicByEmailAndOrganization(Employee employee,MultipartFile image,String email,String organization) throws Exception {
    	String uploadOriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadEmployeeOriginalDirectory");
        String uploadIconDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadEmployeeIconDirectory");
        
    	Employee current = employeeRepository.findByEmailAndOrganization(email, organization);
    	
    	if(current.getImageData() != null || current.getImageData() != "")
    	{
    		try {
    			String name = current.getImageData();
        		name = name.replace(uploadOriginalDirectory+"/", "");
        		fileService.deleteFile(employee.getOrganization(),uploadOriginalDirectory, name);
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
//    			e.printStackTrace();
    		}
    		
    	}
    	
    	try {
    		String imagesLocation = fileService.saveFileToStorage(employee.getOrganization(),uploadOriginalDirectory, image);
        	current.setImageData(uploadOriginalDirectory+"/"+imagesLocation);
        	current.setImageType(image.getContentType());
        	current.setImageName(image.getOriginalFilename());
        	current.setImageSize(image.getSize());
        	employeeRepository.save(current);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	
    	if(current.getIconImageData() != null || current.getIconImageData() != "")
    	{
    		try {
    			String name = current.getIconImageData();
        		name = name.replace(uploadIconDirectory+"/", "");
        		fileService.deleteFile(employee.getOrganization(),uploadIconDirectory, name);
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
//    			e.printStackTrace();
    		}
    		
    	}
    	
    	try {
    		
    		BufferedImage originalImage = ImageIO.read(image.getInputStream());
    		BufferedImage resizedImage = new BufferedImage(127, 127, BufferedImage.TYPE_INT_RGB);
    	    Graphics2D graphics2D = resizedImage.createGraphics();
    	    graphics2D.drawImage(originalImage, 0, 0, 127, 127, null);
    	    graphics2D.dispose();
    	    
    	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	    ImageIO.write( resizedImage, "jpg", baos );
    	    baos.flush();

    	    MultipartFile newImageFile = new MultipartImage(baos.toByteArray(),image.getOriginalFilename(),image.getContentType(),image.getName());
    	    String imagesLocation = fileService.saveFileToStorage(employee.getOrganization(),uploadIconDirectory, newImageFile);
        	current.setIconImageData(uploadIconDirectory+"/"+imagesLocation);
        	current.setIconImageSize(newImageFile.getSize());
        	employeeRepository.save(current);
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return true;
    }
    
    public boolean uploadDocOneByEmailAndOrganization(Employee employee,MultipartFile image,String email,String organization) throws Exception {
    	String uploadDoc1OriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadDoc1OriginalDirectory");
    	Employee current = employeeRepository.findByEmailAndOrganization(email, organization);
    	if(current.getImageData() != null || current.getImageData() != "")
    	{
    		try {
    			String name = current.getGovernmentDocument1Data();
        		name = name.replace(uploadDoc1OriginalDirectory+"/", "");
        		fileService.deleteFile(employee.getOrganization(),uploadDoc1OriginalDirectory, name);
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
    		}
    		
    	}
    	
    	try {
    		String imagesLocation = fileService.saveFileToStorage(employee.getOrganization(),uploadDoc1OriginalDirectory, image);
        	current.setGovernmentDocument1Data(uploadDoc1OriginalDirectory+"/"+imagesLocation);
        	current.setDoc1ImageType(image.getContentType());
        	current.setDoc1ImageSize(image.getSize());
        	employeeRepository.save(current);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return true;
    }
    
    public boolean uploadDocTwoByEmailAndOrganization(Employee employee,MultipartFile image,String email,String organization) throws Exception {
    	String uploadDoc2OriginalDirectory = applicationContext.getEnvironment().getProperty("spring.websocket.uploadDoc2OriginalDirectory");
    	Employee current = employeeRepository.findByEmailAndOrganization(email, organization);
    	if(current.getImageData() != null || current.getImageData() != "")
    	{
    		try {
    			String name = current.getGovernmentDocument2Data();
        		name = name.replace(uploadDoc2OriginalDirectory+"/", "");
        		fileService.deleteFile(employee.getOrganization(),uploadDoc2OriginalDirectory, name);
    		}
    		catch(Exception e)
    		{
    			//Donot do anything
//    			e.printStackTrace();
    		}
    		
    	}
    	
    	try {
    		String imagesLocation = fileService.saveFileToStorage(employee.getOrganization(),uploadDoc2OriginalDirectory, image);
        	current.setGovernmentDocument2Data(uploadDoc2OriginalDirectory+"/"+imagesLocation);
        	current.setDoc2ImageType(image.getContentType());
        	current.setDoc2ImageSize(image.getSize());
        	employeeRepository.save(current);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		throw e;
    	}
    	
    	return true;
    }
    
    
    /**
     * The purpose of the method is to send notifications
     */
    public void sendEmployeeNotifications(String type, List<Employee> employees) throws Exception {
        
    	ObjectMapper mapper = new ObjectMapper();
		List<Notification> allNotifications = new ArrayList<Notification>();
		employees.forEach(
				(employee) -> {
			    	   /*
					    * 
					    * Example on how to set up notification
					    * 
					    *  { alertType: 'alert-success', title: 'Well done!' , message: 'You successfullyread this important.'},
						  { alertType: 'alert-info', title: 'Heads up!', message: 'This alert needs your attention, but it\'s not super important.' },
						  { alertType: 'alert-warning', title: 'Warning!', message: 'Better check yourself, you\'re not looking too good.' },
						  { alertType: 'alert-danger', title: 'Oh snap!', message: 'Change a few things up and try submitting again.' },
						  { alertType: 'alert-primary', title: 'Good Work!', message: 'You completed the training number 23450 well.' },

					    */
			    	BotInputDTO msg;
					Notification notification;
			    	switch(type)
			    	{
			    	
			    	   case "whatsappforsupport": 
			    		   
			    		   notification = new Notification();
			    		   notification.setCreationDate(new Date());
			    		   notification.setAlertType("alert-info");
			    		   notification.setForExtension(employee.getExtension());
			    		   notification.setMessage("Whats-App +919625048379 when required.");
			    		   notification.setNotificationType("employee");
			    		   notification.setOrganization(employee.getOrganization());
			    		   notification.setTitle("Connect Support!");
			    		   
			    		   msg = new BotInputDTO();
				    	   msg.setDomain(employee.getDomain());
				    	   msg.setExtension(employee.getExtension());
				    	   msg.setFormat("json");
				    	   try {
							msg.setMessage(mapper.writeValueAsString(notification));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	   msg.setMessagetype("notification");
				    	   msg.setOrganization(employee.getOrganization());
					    	try {
						       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
						    }
						    catch(Exception e)
						    {
							   e.printStackTrace();
						    }
			    		   allNotifications.add(notification);
			    		      
			    	   break;
			    	   
			    	   case "addedtofbproject": 
			    		   
			    		   notification = new Notification();
			    		   notification.setCreationDate(new Date());
			    		   notification.setAlertType("alert-success");
			    		   notification.setForExtension(employee.getExtension());
			    		   notification.setMessage("You have been added to mylinehub whats app advantages.");
			    		   notification.setNotificationType("employee");
			    		   notification.setOrganization(employee.getOrganization());
			    		   notification.setTitle("Congo!");
			    		   
			    		   msg = new BotInputDTO();
				    	   msg.setDomain(employee.getDomain());
				    	   msg.setExtension(employee.getExtension());
				    	   msg.setFormat("json");
				    	   try {
							msg.setMessage(mapper.writeValueAsString(notification));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	   msg.setMessagetype("notification");
				    	   msg.setOrganization(employee.getOrganization());
					    	try {
						       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
						    }
						    catch(Exception e)
						    {
							   e.printStackTrace();
						    }
			    		   allNotifications.add(notification);
			    		      
			    	   break;
			    	  
			    	   case "create": 
			    		   
			    		   notification = new Notification();
			    		   notification.setCreationDate(new Date());
			    		   notification.setAlertType("alert-success");
			    		   notification.setForExtension(employee.getExtension());
			    		   notification.setMessage("We welcome you onboard. Contact our tech team for challanges. We shall be happy to help");
			    		   notification.setNotificationType("employee");
			    		   notification.setOrganization(employee.getOrganization());
			    		   notification.setTitle("Yuu-Huu!");
			    		   
			    		   msg = new BotInputDTO();
				    	   msg.setDomain(employee.getDomain());
				    	   msg.setExtension(employee.getExtension());
				    	   msg.setFormat("json");
				    	   try {
							msg.setMessage(mapper.writeValueAsString(notification));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	   msg.setMessagetype("notification");
				    	   msg.setOrganization(employee.getOrganization());
					    	try {
						       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
						    }
						    catch(Exception e)
						    {
							   e.printStackTrace();
						    }
			    		   allNotifications.add(notification);
			    		      
			    	   break;
			    	  
			    	   
			    	   	case "update-extension-password": 
			    		   
			    		   notification = new Notification();
			    		   notification.setCreationDate(new Date());
			    		   notification.setAlertType("alert-info");
			    		   notification.setForExtension(employee.getExtension());
			    		   notification.setMessage("Your extension password has been updated");
			    		   notification.setNotificationType("employee");
			    		   notification.setOrganization(employee.getOrganization());
			    		   notification.setTitle("Done!");
			    		   
			    		   msg = new BotInputDTO();
				    	   msg.setDomain(employee.getDomain());
				    	   msg.setExtension(employee.getExtension());
				    	   msg.setFormat("json");
				    	   try {
							msg.setMessage(mapper.writeValueAsString(notification));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	   msg.setMessagetype("notification");
				    	   msg.setOrganization(employee.getOrganization());
					    	try {
						       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
						    }
						    catch(Exception e)
						    {
							   e.printStackTrace();
						    }
			    		   allNotifications.add(notification); 
			    		   break;
			    		   
			    	   	case "update-web-password": 
			    		   
			    		   notification = new Notification();
			    		   notification.setCreationDate(new Date());
			    		   notification.setAlertType("alert-info");
			    		   notification.setForExtension(employee.getExtension());
			    		   notification.setMessage("Your web password has been updated");
			    		   notification.setNotificationType("employee");
			    		   notification.setOrganization(employee.getOrganization());
			    		   notification.setTitle("Done!");
			    		   
			    		   msg = new BotInputDTO();
				    	   msg.setDomain(employee.getDomain());
				    	   msg.setExtension(employee.getExtension());
				    	   msg.setFormat("json");
				    	   try {
							msg.setMessage(mapper.writeValueAsString(notification));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	   msg.setMessagetype("notification");
				    	   msg.setOrganization(employee.getOrganization());
					    	try {
						       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
						    }
						    catch(Exception e)
						    {
							   e.printStackTrace();
						    }
			    		   allNotifications.add(notification); 
			    		   break;

			    		   
			    	   case "update": 
			    		   
			    		   notification = new Notification();
			    		   notification.setCreationDate(new Date());
			    		   notification.setAlertType("alert-info");
			    		   notification.setForExtension(employee.getExtension());
			    		   notification.setMessage("Your employement information has been updated");
			    		   notification.setNotificationType("employee");
			    		   notification.setOrganization(employee.getOrganization());
			    		   notification.setTitle("Done!");
			    		   
			    		   msg = new BotInputDTO();
				    	   msg.setDomain(employee.getDomain());
				    	   msg.setExtension(employee.getExtension());
				    	   msg.setFormat("json");
				    	   try {
							msg.setMessage(mapper.writeValueAsString(notification));
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				    	   msg.setMessagetype("notification");
				    	   msg.setOrganization(employee.getOrganization());
					    	try {
						       	  MyStompSessionHandler.sendMessage("/mylinehub/sendcalldetails", msg);
						    }
						    catch(Exception e)
						    {
							   e.printStackTrace();
						    }
			    		   allNotifications.add(notification); 
			    		   break;
			    		   
				    	   default:
				    	   break;
			    	}
			    	
   	            });
    	
    	notificationRepository.saveAll(allNotifications);	
    }
    

}
