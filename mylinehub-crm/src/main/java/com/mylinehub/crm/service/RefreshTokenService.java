package com.mylinehub.crm.service;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.entity.RefreshToken;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.repository.RefreshTokenRepository;
import com.mylinehub.crm.security.jwt.JwtConfiguration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



/**
 * @author Anand Goel
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class RefreshTokenService implements  CurrentTimeInterface {

    /**
     * were injected by the constructor using the lombok @AllArgsContrustor annotation
     */
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfiguration jwtConfiguration;
    private final SecretKey secretKey;
    private static final String USER_NOT_FOUND_MSG =
            "user with email %s not found";
    
    
    public boolean saveToken(String email, String token, Date expiryDate)
    {
    	Employee employee  = employeeRepository.findByEmail(email)
        .orElseThrow(()-> new UsernameNotFoundException(
                        String.format(USER_NOT_FOUND_MSG, email)));
    	
    	
    	RefreshToken refreshToken  = refreshTokenRepository.findByEmail(employee.getEmail());
    	
    	if(refreshToken == null)
    	{
    		refreshToken = new RefreshToken();
    		refreshToken.setEmail(email);
    		refreshToken.setToken(token);
    		refreshToken.setExpiryDate(expiryDate);
    		refreshTokenRepository.save(refreshToken);
    	}
    	else
    	{
    		refreshToken.setToken(token);
    		refreshToken.setExpiryDate(expiryDate);
    		refreshTokenRepository.save(refreshToken);
    	}
    	
		return true;
    }
    

    public String refreshTokenOfEmployee(String oldToken) throws Exception
    {
    	String toReturn = "";
    	
    	try 
    	{
    		oldToken = oldToken.replace(jwtConfiguration.getTokenPrefix(), "");
        	//System.out.println(oldToken);
			
			Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(oldToken);

	        Claims body = claimsJws.getBody();

	        String username = body.getSubject();
	        
	    	Employee employee = employeeRepository.findByEmail(username)
	                 .orElseThrow(()-> new UsernameNotFoundException(
	                         String.format(USER_NOT_FOUND_MSG, username)));
        	
	    	
	    	
        	if(employee != null)
        	{	

        		RefreshToken refreshToken  = refreshTokenRepository.findByEmail(employee.getEmail());
            	
            	if(refreshToken == null)
            	{
            		throw new Exception("This user has never logged in before.");
            	}
            	else
            	{
            		if(refreshToken.getToken().equals(oldToken))
            		{
            			Map<String,String> map = new HashMap<String, String>();
        				map.put("authority", employee.getUserRole().name());
        				
        				List<Map<String,String>> allAthorities = new ArrayList<Map<String,String>>();
        				
        				allAthorities.add(map);

        				Calendar cal = Calendar.getInstance(); // creates calendar
        		    	cal.setTime(new Date());
        		    	cal.add(Calendar.HOUR_OF_DAY, 2);
        				
                    	String newToken = Jwts.builder()
                                .setSubject(employee.getEmail())
                                .claim("authorities", allAthorities)
                                .setIssuedAt(new Date())
                                .setExpiration(cal.getTime())
                                .signWith(secretKey)
                                .compact();
                		
                		toReturn = newToken;
                		
            			refreshToken.setToken(newToken);
                		refreshToken.setExpiryDate(cal.getTime());
                		refreshTokenRepository.save(refreshToken);
            		}
            		else
            		{
            			throw new Exception("This token is expired and cannot be refreshed.");
            		}
            		
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

}