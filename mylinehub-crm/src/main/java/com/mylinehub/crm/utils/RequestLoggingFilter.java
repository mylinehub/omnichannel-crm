package com.mylinehub.crm.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

    	System.out.println("[RequestLoggingFilter : REQ] " + request.getMethod() + " " + request.getRequestURI()
        + " ct=" + request.getContentType()
        + " len=" + request.getContentLengthLong()
        + " from=" + request.getRemoteAddr());

        filterChain.doFilter(request, response);
    }
}
