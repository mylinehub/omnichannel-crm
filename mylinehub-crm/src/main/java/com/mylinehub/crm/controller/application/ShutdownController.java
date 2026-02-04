package com.mylinehub.crm.controller.application;

import static com.mylinehub.crm.controller.ApiMapping.APPLICATION_REST_URL;

import javax.crypto.SecretKey;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.AllArgsConstructor;

@RestController
@RequestMapping(produces="application/json", path = APPLICATION_REST_URL)
@AllArgsConstructor
@CrossOrigin(origins="*")
public class ShutdownController implements ApplicationContextAware {

    private ApplicationContext context;
    
    @PostMapping("/shutdownContext")
    public void shutdownContext() {
        ((ConfigurableApplicationContext) context).close();
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
        
    }
}
