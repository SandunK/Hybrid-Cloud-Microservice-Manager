package com.isolate.microservice.loadBalancer;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 *
 * InstanceManager type class which handle aws instances
 */

public class AwsServerManager implements InstanceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("info");
    private int waitingTime;
    AwsServerManager(int waitingTime) {
        super();
        this.waitingTime=waitingTime;
    }


    public void startNewInstance(String instanceId) {
        LOGGER.info("Amazon server start Initialized for instance " + instanceId);
        try {
            final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new ProfileCredentialsProvider("loadBalancer"))
                    .withRegion("me-south-1").build();                                  // provide aws credentials using credential profile

            StartInstancesRequest request = new StartInstancesRequest()             // start request
                    .withInstanceIds(instanceId);

            ec2.startInstances(request);
            LOGGER.info("Instance is starting " + instanceId);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error occurred " + e);
        }

    }

    public Map.Entry<String, Map> manageNewInstance(Map<String, Integer> pending, String serviceId, EurekaInstanceFinder eurekaInstanceFinder) throws Exception {
        LOGGER.info("Checking AWS instances ...");
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(new ProfileCredentialsProvider("loadBalancer"))
                .withRegion("me-south-1").build();
        boolean done = false;
        final List<String> instanceStatus = Collections.singletonList("80"); // 64 - stopping, 80 - stopped
        DescribeInstancesRequest request = new DescribeInstancesRequest().withFilters(new Filter().withName("tag-key").withValues("version1"),
                new Filter().withName("instance-state-code").withValues(instanceStatus));               // request for getting instance details with a tag
        while (!done) {
            try {
                DescribeInstancesResult response = ec2.describeInstances(request);
                for (Reservation reservation : response.getReservations()) {                // loop through the instances
                    List<Instance> instances = reservation.getInstances();
                    if (!instances.isEmpty()) {
                        Instance instance = instances.get(0);
                        String instanceId = instance.getInstanceId();
                        startNewInstance(instanceId);                                       // call start instance function
                        LOGGER.info("Waiting until service starting ");
                        Thread.sleep(waitingTime*1000);                               // wait till service initiate
                        LOGGER.info("Checking service status on "+instanceId);

                        for (int i = 0; i < 20; i++) {
                            Map.Entry<String,Map> serverDetails = eurekaInstanceFinder.findEurekaInstance(pending, serviceId);
                            if(serverDetails != null){                                      // check whether service available
                                LOGGER.info(serviceId+" Service started on instance "+instanceId);
                                return serverDetails;
                            }else{
                                Thread.sleep(5_000);                                  // wait some time before next check
                            }
                        }
                        LOGGER.error("Service not available in started instance " + instanceId);
                    }
                }

                request.setNextToken(response.getNextToken());

                if (response.getNextToken() == null) {
                    done = true;
                }
            } catch (Exception e) {
                done = true;
                e.printStackTrace();
                LOGGER.error("Error Occurred " + e);
            }

        }
        LOGGER.error("Service Unavailable.");
        throw new Exception("Service Unavailable.");
    }
}
