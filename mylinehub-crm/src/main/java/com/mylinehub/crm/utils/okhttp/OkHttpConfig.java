package com.mylinehub.crm.utils.okhttp;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.moczul.ok2curl.CurlInterceptor;
import com.mylinehub.crm.utils.OkHttpLoggerUtils;

@Configuration
public class OkHttpConfig {

    @Bean
    public OkHttpLoggerUtils okHttpLoggerUtils() {
        return new OkHttpLoggerUtils();
    }

    @Bean
    public ConnectionPool okHttpConnectionPool() {
        return new ConnectionPool(20, 5, TimeUnit.MINUTES);
    }

    @Bean
    public Dispatcher okHttpDispatcher() {
        Dispatcher d = new Dispatcher();
        d.setMaxRequests(200);
        d.setMaxRequestsPerHost(100);
        return d;
    }

    @Bean
    public OkHttpClient okHttpClient(
            OkHttpLoggerUtils okHttpLoggerUtils,
            ConnectionPool connectionPool,
            Dispatcher dispatcher
    ) {
        CurlInterceptor curlInterceptor = new CurlInterceptor(okHttpLoggerUtils);

        return new OkHttpClient.Builder()
                .addInterceptor(curlInterceptor)
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)

                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(80, TimeUnit.SECONDS)
                .readTimeout(80, TimeUnit.SECONDS)

                // IMPORTANT:
                .callTimeout(120, TimeUnit.SECONDS)      // hard cap for whole call
                .pingInterval(20, TimeUnit.SECONDS)      // keep HTTP/2 alive

                .retryOnConnectionFailure(true)
                .build();
    }

}
