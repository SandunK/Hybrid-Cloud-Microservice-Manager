package com.example.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@RestController
public class endpoints {
    @Autowired
    Environment environment;

    @RequestMapping("/")
    public String health() {
        return "I am Ok";
    }

    @RequestMapping("whoami")
    public Map<String, Object> whoAmI() {
        System.out.println("Inside MyRestController::backend...");

        String serverPort = environment.getProperty("local.server.port");

        Map<String, Object> descriptor = new HashMap<>();
        descriptor.put("text", "Hello form Backend!!! " + " Host : localhost " + " :: Port : " + serverPort);
        descriptor.put("AvailableJobs", 1);
        descriptor.put("ServerCapabilityRating",1);
        return descriptor;
    }

    @RequestMapping("api/backtest")
    public String backtest() {
        return "inside backtest on port 9090";
    }

}
