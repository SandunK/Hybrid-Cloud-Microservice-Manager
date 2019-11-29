package com.isolate.microservice.loadBalancer;

import java.util.Map;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
public interface InstanceManager {
    void startNewInstance(String instanceId);
    Map.Entry<String, Map> manageNewInstance (Map<String, Integer> pending, String serviceId, EurekaInstanceFinder eurekaInstanceFinder) throws Exception;

}
