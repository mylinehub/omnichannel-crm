package com.mylinehub.crm.security.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@AllArgsConstructor
public class JwtKey {
    private final JwtConfiguration jwtConfiguration;

    @Bean
    public SecretKey secretKey() {
    	
//    	System.out.println("secretKey");
    	
        return Keys.hmacShaKeyFor(jwtConfiguration.getKey().getBytes());
    }
}
