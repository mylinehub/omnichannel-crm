package com.mylinehub.crm.security;

import com.mylinehub.crm.service.EmployeeService;
import com.mylinehub.crm.service.RefreshTokenService;
import com.mylinehub.crm.utils.RequestLoggingFilter;
import com.mylinehub.crm.security.jwt.JwtConfiguration;
import com.mylinehub.crm.security.jwt.JwtUsernameAndPasswordAuthorizationFilter;
import com.mylinehub.crm.security.jwt.JwtUsernameAndPasswordAuthenticationFilter;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true,
        securedEnabled = true,
        jsr250Enabled = true)
@Order(1)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final EmployeeService employeeService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final SecretKey secretKey;
    private final JwtConfiguration jwtConfiguration;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    private RequestLoggingFilter requestLoggingFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        System.out.println(">>> SecurityConfig: Starting configure(HttpSecurity)");

        JwtUsernameAndPasswordAuthenticationFilter authenticationFilter =
            new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfiguration, employeeService, refreshTokenService, secretKey, "");

        JwtUsernameAndPasswordAuthorizationFilter authorizationFilter =
            new JwtUsernameAndPasswordAuthorizationFilter(secretKey, jwtConfiguration);

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(management -> management
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Register RequestLoggingFilter BEFORE JwtUsernameAndPasswordAuthenticationFilter
            .addFilterBefore(requestLoggingFilter, JwtUsernameAndPasswordAuthenticationFilter.class)
            // Register JWT Authentication Filter
            .addFilter(authenticationFilter)
            // Register JWT Authorization Filter AFTER Authentication Filter
            .addFilterAfter(authorizationFilter, JwtUsernameAndPasswordAuthenticationFilter.class);

        System.out.println(">>> SecurityConfig: Filters registered successfully");
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        System.out.println(">>> SecurityConfig: Configuring DaoAuthenticationProvider");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(employeeService);
        return provider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println(">>> SecurityConfig: Configuring AuthenticationManagerBuilder");
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    CorsConfigurationSource corsConfigurationSource() {
        System.out.println(">>> SecurityConfig: Creating CorsConfigurationSource");
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowOrigins = Arrays.asList(
            "http://app.mylinehub.com", "http://app.mylinehub.com:8080", "http://app.mylinehub.com:8081",
            "https://app.mylinehub.com", "https://app.mylinehub.com:8080",
            "https://www.app.mylinehub.com", "https://www.app.mylinehub.com:8080", "http://localhost:4200"
        );
        configuration.setAllowedOrigins(allowOrigins);
        configuration.setAllowedMethods(Arrays.asList(CorsConfiguration.ALL));
        configuration.setAllowedHeaders(Arrays.asList(CorsConfiguration.ALL));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        System.out.println(">>> SecurityConfig: Configuring WebSecurity ignoring patterns");
        web.ignoring().antMatchers("/api/v1/whatsappwebhook/**");
        web.ignoring().antMatchers("/api/v1/organization-app/**");
    }
}
