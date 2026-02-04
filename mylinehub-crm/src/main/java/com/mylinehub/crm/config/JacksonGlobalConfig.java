package com.mylinehub.crm.config;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class JacksonGlobalConfig {

    private static final Logger log = LoggerFactory.getLogger(JacksonGlobalConfig.class);

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Don't fail on unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Log unknown fields instead of throwing error
        mapper.addHandler(new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt,
                                                 JsonParser p,
                                                 JsonDeserializer<?> deserializer,
                                                 Object beanOrClass,
                                                 String propertyName) throws IOException {
            	
            	System.out.println("******************************************************");
            	System.out.println("**********UNKOWN JSON PROPERTY********************");
            	System.out.println("******************************************************");
                System.out.println("[JSON] Unknown property "+propertyName+"in "+ beanOrClass.getClass().getSimpleName());
             	System.out.println("******************************************************");

                return true; // skip it safely
            }
        });

        return mapper;
    }
}
