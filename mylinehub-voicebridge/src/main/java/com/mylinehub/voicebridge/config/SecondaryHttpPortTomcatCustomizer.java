//package com.mylinehub.voicebridge.config;
//
//import org.apache.catalina.connector.Connector;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConditionalOnProperty(name = "server.http.port") // only if property present
//public class SecondaryHttpPortTomcatCustomizer
//        implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
//
//  @Value("${server.http.port}")
//  private int httpPort;
//
//  @Override
//  public void customize(TomcatServletWebServerFactory factory) {
//    Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
//    connector.setPort(httpPort);
//    connector.setScheme("http");
//    connector.setSecure(false);
//    factory.addAdditionalTomcatConnectors(connector);
//  }
//}
