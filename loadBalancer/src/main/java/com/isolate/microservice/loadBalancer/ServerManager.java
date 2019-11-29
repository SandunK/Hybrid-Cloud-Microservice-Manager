package com.isolate.microservice.loadBalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
@Component
public class ServerManager {

    private ArrayList<InstanceManager> instanceManagers = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger("info");
    private EurekaInstanceFinder eurekaInstanceFinder;

    public ServerManager(){}

    @Autowired
    public ServerManager(EurekaInstanceFinder eurekaInstanceFinder, Environment env) {
        this.eurekaInstanceFinder=eurekaInstanceFinder;                     // Eureka finder for find available services from eureka registry
        try {
            int expireTime=Integer.parseInt(Objects.requireNonNull(env.getProperty("eureka.instance.leaseRenewalIntervalInSeconds")));
            int registerTime=Integer.parseInt(Objects.requireNonNull(env.getProperty("eureka.instance.leaseExpirationDurationInSeconds")));
            int waitingTime = expireTime+registerTime+5;                        // +5 for starting time of aws instance

            instanceManagers.add(new AwsServerManager(waitingTime));                       // Add new Aws manager into instance manager lst
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.error("Error resolving environment variables "+e);
        }
    }

    synchronized Map.Entry<String, Map> find(Map<String, Integer> pending, String serviceId) throws Exception {
        Map.Entry<String, Map> serverDetails = eurekaInstanceFinder.findEurekaInstance(pending, serviceId);
        if (serverDetails != null) {
            return serverDetails;                                               // find a upped server in eureka registry
        } else {
            for (InstanceManager instanceManager : this.instanceManagers) {     // loop through instance managers and find new instances if available
                return instanceManager.manageNewInstance(pending, serviceId, eurekaInstanceFinder);                         // otherwise up an new amazon instance if available
            }
        }
        LOGGER.error("Service Unavailable.");
        throw new Exception("Service Unavailable.");
    }

}
