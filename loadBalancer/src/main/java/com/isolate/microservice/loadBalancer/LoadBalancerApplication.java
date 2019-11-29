package com.isolate.microservice.loadBalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
@SpringBootApplication
@EnableEurekaClient
@EnableScheduling
@EnableZuulProxy

public class LoadBalancerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoadBalancerApplication.class, args);
    }

    @Bean
    public PreFilter preFilter() {
        return new PreFilter();
    }

    @Bean
    public PostFilter postFilter() {
        return new PostFilter();
    }

    @Bean
    public ErrorFilter errorFilter() {
        return new ErrorFilter();
    }

    @Bean
    public RouteFilter routeFilter() {
        return new RouteFilter();
    }

}
