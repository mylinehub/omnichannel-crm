package com.mylinehub.crm.security.jwt;

import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class JwtUsernameAndPasswordAuthorizationFilter extends OncePerRequestFilter {
    private final SecretKey secretKey;
    private final JwtConfiguration jwtConfiguration;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

//    	System.out.println("Class : JwtUsernameAndPasswordAuthorizationFilter :OncePerRequestFilter");
    	
        String authorizationHeader = httpServletRequest.getHeader(jwtConfiguration.getAuthorizationHeader());
        if (Strings.isNullOrEmpty(authorizationHeader) || !authorizationHeader.startsWith(jwtConfiguration.getTokenPrefix())){
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

//        System.out.println("After authorization header");
    	
        
        
        String token = authorizationHeader.replace(jwtConfiguration.getTokenPrefix(), "");

//        System.out.println("Token fetched");
        
        try {
//            Jws<Claims> claimsJws = Jwts.parser()
//                    .setSigningKey(secretKey)
//                    .parseClaimsJws(token);
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

            //System.out.println("After claims");
            
            Claims body = claimsJws.getBody();
            String username = body.getSubject();
            
//            System.out.println("body");
//            System.out.println(body);
//            System.out.println("email");
//            System.out.println(username);
//            System.out.println("authorities");
//            System.out.println(body.get("authorities"));
            
            List<Map<String, String>> authorities = (List<Map<String, String>>) body.get("authorities");

//            System.out.println("After athorities");
            
            Set<SimpleGrantedAuthority> simpleGrantedAuthority = authorities.stream()
                    .map(m -> new SimpleGrantedAuthority(m.get("authority")))
                    .collect(Collectors.toSet());

//            System.out.println("Fetched Authorities");
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    simpleGrantedAuthority
            );

//            System.out.println("Authentication Token Build");
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
//            System.out.println("Authentication Security added to context");
            
        } catch (JwtException e) {
            throw new IllegalStateException(String.format("Token %s can't be trusted", token));
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
