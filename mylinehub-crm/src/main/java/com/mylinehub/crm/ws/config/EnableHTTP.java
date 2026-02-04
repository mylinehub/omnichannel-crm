package com.mylinehub.crm.ws.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class EnableHTTP {
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return (TomcatServletWebServerFactory factory) -> {
            // also listen on http
            final Connector connector = new Connector();
            connector.setPort(httpPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }

    @Value("${server.http.port}")
    private int httpPort = 8081;
}