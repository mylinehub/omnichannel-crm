package com.mylinehub.crm.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mylinehub.crm.entity.dto.EmployeeDTO;
import com.mylinehub.crm.service.EmployeeService;
import com.mylinehub.crm.service.RefreshTokenService;

import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

@AllArgsConstructor
public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtConfiguration jwtConfiguration;
    private final EmployeeService employeeService;
    private final RefreshTokenService refreshTokenService;
    private final SecretKey secretKey;
    private String email = "";
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        
//    	System.out.println("Request : "+ request.toString());
    	
    	try {
    		
    		if (CorsUtils.isPreFlightRequest(request)) {
    			response.setStatus(HttpServletResponse.SC_OK);
    			
    	    }
    		

    	    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
    	    response.setHeader("Access-Control-Allow-Credentials", "true");
    	    response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
    	    response.setHeader("Access-Control-Max-Age", "3600");
    	    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");

//    	    System.out.println("After response header");
    	    
            UsernameAndPasswordAuthenticationRequest authenticationRequest = new ObjectMapper()
                    .readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);

//            System.out.println("After response header");
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()
            );
            
            email = authenticationRequest.getUsername();
            
//            System.out.println("After rauthentication request");
            
            Authentication authenticate = authenticationManager.authenticate(authentication);
            
//            System.out.println("After rauthentication");
            
            return authenticate;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
//    	 System.out.println("Successful Authentication");
    	
    	Calendar cal = Calendar.getInstance(); // creates calendar
    	cal.setTime(new Date());
    	cal.add(Calendar.HOUR_OF_DAY, 20);
    	
    	
    	String token = Jwts.builder()
                .setSubject(authResult.getName())
                .claim("authorities", authResult.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(cal.getTime())
                .signWith(secretKey)
                .compact();


//        System.out.println("authenticationRequest");
        
        EmployeeDTO current = employeeService.findByEmail(authResult.getName());
        
        refreshTokenService.saveToken(authResult.getName(), token, cal.getTime());

//        System.out.println("current");
        
       // Authentication authenticate = authenticationManager.authenticate(authentication);
        
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        
        response.addHeader(jwtConfiguration.getAuthorizationHeader(), jwtConfiguration.getTokenPrefix() + token);
        response.setStatus(HttpServletResponse.SC_OK);
       // "	\"user\":\""+ow.writeValueAsString(current)+"\"\n" + 
        
        response.getWriter().write("{\n" + 
        		"  \"data\": {\n" + 
        		"    \"token\": \""+token+"\",\n" + 
        		"    \"user\":"+ow.writeValueAsString(current)+"\n" + 
        		"  }\n" + 
        		"}");
        response.getWriter().flush();
    }
}
