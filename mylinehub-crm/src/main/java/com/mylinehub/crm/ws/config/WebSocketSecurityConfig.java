package com.mylinehub.crm.ws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//    	System.out.println("Class : WebSocketSecurityConfig :AbstractSecurityWebSocketMessageBrokerConfigurer");
//    	System.out.println("configureInbound");
        messages.anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
//    	System.out.println("sameOriginDisabled");
        return true;
    }

}
