package com.isolate.microservice.loadBalancer;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
@Component
public class EurekaInstanceFinder {
    private ServerHealthCheck healthCheck;

    private EurekaInstanceFinder(ServerHealthCheck healthCheck){this.healthCheck=healthCheck;}


    Map.Entry<String,Map> findEurekaInstance(Map<String, Integer> pending, String serviceId) {
        Map<String, Map> serverDetails = healthCheck.check(serviceId);                               // get active server map (server data to id)
        String selectedServerId;
        int totalAvailableJobs;
        for (Map.Entry<String, Map> entry : serverDetails.entrySet()) {                             // loop through active service instances
            int availableJobs = Integer.parseInt((String) entry.getValue().get("AvailableJobs"));

            selectedServerId = entry.getKey();

            if (pending.containsKey(selectedServerId)) {
                totalAvailableJobs = availableJobs + pending.get(selectedServerId);   // calculate total available jobs
            } else {
                totalAvailableJobs = availableJobs;
            }

            if (totalAvailableJobs >= 1) {                                            //check whether more jobs available
                return entry;
            }
        }
        return null;
    }
}
