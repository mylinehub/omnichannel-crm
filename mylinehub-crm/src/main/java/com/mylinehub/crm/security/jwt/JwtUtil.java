package com.mylinehub.crm.security.jwt;

import java.security.Key;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtUtil {

    private final Key secretKey;

    public JwtUtil(Key secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isTokenExpired(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return false; // Token is valid (not expired)
        } catch (Exception e) {
            return true; // Token is expired
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
             return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            throw e;
        }
    }
}