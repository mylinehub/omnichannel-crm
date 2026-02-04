package com.mylinehub.crm.ws.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.mylinehub.crm.entity.Employee;
import com.mylinehub.crm.repository.EmployeeRepository;
import com.mylinehub.crm.utils.LoggerUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	    private final SecretKey secretKey;
		private final EmployeeRepository employeeRepository;

	    @Override
	    public void configureClientInboundChannel(ChannelRegistration registration) {
	    	
//	    	System.out.println("Class : WebSocketAuthenticationConfig :WebSocketMessageBrokerConfigurer");
//	    	System.out.println("configureClientInboundChannel");
	    	
	    	final String USER_NOT_FOUND_MSG =
	                "user with email %s not found";
	        
	        registration.interceptors(new ChannelInterceptor() {
	            @Override
	            public Message<?> preSend(Message<?> message, MessageChannel channel) {
	            	
	            	
//	            	System.out.println("preSend");
	            	
	            	
	                StompHeaderAccessor accessor =
	                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
	                
//	                System.out.println("after accessor");
	                
	                
	                LoggerUtils.log.info((String.valueOf(accessor.getCommand())));
	                
//	                System.out.println("after logging");
	                
	                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//	                	System.out.println("Inside if");
	                    List<String> authorization = accessor.getNativeHeader("auth");
//	                    System.out.println("after authrization");
	                    String token = authorization.get(0);
	                    
//	                    System.out.println("token");
//	                    System.out.println(token);
	                    
	                    //Jwt jwt = jwtDecoder.decode(accessToken);
	                    //JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
	                    //Authentication authentication = converter.convert(jwt);
	                    //accessor.setUser(authentication);

	                    try {

	                        Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

	                        Claims body = claimsJws.getBody();

	                        String username = body.getSubject();

//	                        System.out.println("before employee");
	                        
	                        Employee employee = employeeRepository.findByEmail(username)
	                                .orElseThrow(()-> new UsernameNotFoundException(
	                                        String.format(USER_NOT_FOUND_MSG, username)));
	                        
//	                        System.out.println("after employee");
	                       
	                        List<Map<String, String>> authorities = (List<Map<String, String>>) body.get("authorities");

	                        Set<SimpleGrantedAuthority> simpleGrantedAuthority = authorities.stream()
	                                .map(m -> new SimpleGrantedAuthority(m.get("authority")))
	                                .collect(Collectors.toSet());

	                        Authentication authentication = new UsernamePasswordAuthenticationToken(
	                                username,
	                                null,
	                                simpleGrantedAuthority
	                        );
	                        

	                       accessor.setUser(authentication);     
	   
	                    }
	                    catch (Exception e) {
	                        throw new IllegalStateException(String.format("Token %s can't be trusted", token));
	                    }
	                }
	                return message;
	            }
	        });
	    }
	    
    @Override
    public void configureMessageBroker(final MessageBrokerRegistry config) {
//    	System.out.println("Class : WebSocketConfig :WebSocketMessageBrokerConfigurer");
//    	System.out.println("configureMessageBroker");
        config.enableSimpleBroker("/event");
        config.setApplicationDestinationPrefixes("/mylinehub");
        
    }

    @Override
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
//    	System.out.println("registerStompEndpoints");
        registry.addEndpoint("/chat");
        registry.addEndpoint("/chat").withSockJS();
        
//        registry.addEndpoint("/chat").setAllowedOrigins("*");
//        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
        
        registry.addEndpoint("/chat").setAllowedOrigins("http://app.mylinehub.com","http://app.mylinehub.com:8080","http://app.mylinehub.com:8081","https://app.mylinehub.com","https://app.mylinehub.com:8080","https://www.app.mylinehub.com","https://www.app.mylinehub.com:8080","http://localhost:4200");
        registry.addEndpoint("/chat").setAllowedOrigins("http://app.mylinehub.com","http://app.mylinehub.com:8080","http://app.mylinehub.com:8081","https://app.mylinehub.com","https://app.mylinehub.com:8080","https://www.app.mylinehub.com","https://www.app.mylinehub.com:8080","http://localhost:4200").withSockJS();
   
    }
    
//    @Override
//    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
//        registration.setMessageSizeLimit(2048 * 2048);
//        registration.setSendBufferSizeLimit(2048 * 2048);
//        registration.setSendTimeLimit(2048 * 2048);
//    }

//    @Bean
//    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
//        ServletServerContainerFactoryBean factoryBean = new ServletServerContainerFactoryBean();
//        factoryBean.setMaxTextMessageBufferSize(2048 * 2048);
//        factoryBean.setMaxBinaryMessageBufferSize(2048 * 2048);
//        factoryBean.setMaxSessionIdleTimeout(2048L * 2048L);
//        factoryBean.setAsyncSendTimeout(2048L * 2048L);
//        return factoryBean;
//    }
    
    
    
    
//    @Override
//    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
//        registration.setMessageSizeLimit(2048 * 2048 * 20);
//        registration.setSendBufferSizeLimit(2048 * 2048 * 20);
//        registration.setSendTimeLimit(2048 * 2048);
//    }
//    
//    @Bean
//    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
//        ServletServerContainerFactoryBean factoryBean = new ServletServerContainerFactoryBean();
//        factoryBean.setMaxTextMessageBufferSize(2048 * 2048 * 20);
//        factoryBean.setMaxBinaryMessageBufferSize(2048 * 2048 * 20);
//        factoryBean.setMaxSessionIdleTimeout(2048L * 2048L);
//        factoryBean.setAsyncSendTimeout(2048L * 2048L);
//        return factoryBean;
//    }


}