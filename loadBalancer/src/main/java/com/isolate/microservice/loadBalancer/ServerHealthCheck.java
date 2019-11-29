package com.isolate.microservice.loadBalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 *
 * Server availability and health check
 */
@Service
public class ServerHealthCheck {

    private DiscoveryClient discoveryClient;
    private static final Logger LOGGER = LoggerFactory.getLogger("info");

    @Autowired
    public ServerHealthCheck(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    private RestTemplate restTemplate = new RestTemplate();

    Map<String, Map> check(String serviceId) {
        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory());
        SimpleClientHttpRequestFactory rf = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        rf.setConnectTimeout(30000);

        List<ServiceInstance> instances = this.discoveryClient.getInstances(serviceId);     // get all instances of service id
        Map<String, Map> serverMap = new HashMap<>();                                    // available servers of current check
        ArrayList<String> instanceIp = new ArrayList<>();
        instances.forEach((instance)-> instanceIp.add(instance.getUri().toString()));
        LOGGER.info("Services registered with Eureka with service id "+serviceId+" : "+instanceIp);

        for (ServiceInstance serviceInstance : instances) {
            LOGGER.info("Evaluating availability of service on instance "+serviceInstance.getUri());
            Map<String, Object> serverData = new HashMap<>();
            String url = serviceInstance.getUri() + "/whoami";   // send request to health check endpoint

            LOGGER.info("Checking service " + url);
            String serverId = serviceInstance.getInstanceId();
            try {
                Map result = restTemplate.getForObject(url, Map.class);
                assert result != null;
                int availableJobs = Integer.parseInt((String) result.get("AvailableJobs"));

                serverData.put("Url", serviceInstance.getUri());     // if available add to the list
                serverData.put("AvailableJobs", String.valueOf(availableJobs));
                serverData.put("ServerId", serverId);

                serverMap.put(serverId, serverData);

                LOGGER.info(String.valueOf(result));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }
        return serverMap;                                     // return server map
    }
}
