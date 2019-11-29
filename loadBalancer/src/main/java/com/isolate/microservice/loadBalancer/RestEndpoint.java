package com.isolate.microservice.loadBalancer;

import org.springframework.web.bind.annotation.*;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
@RestController
public class RestEndpoint {

    @ResponseBody
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return "<h2>Load balancer service initialized...</h2>";
    }

}
