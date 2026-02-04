package com.mylinehub.crm.data;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextProvider implements ApplicationContextAware {

    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextProvider.ctx = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (ctx == null) return null;
        return ctx.getBean(clazz);
    }
}
