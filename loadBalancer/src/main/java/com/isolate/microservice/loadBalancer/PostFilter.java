package com.isolate.microservice.loadBalancer;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
public class PostFilter extends ZuulFilter {
    @Autowired
    JobManager jobManager;

    private static final Logger LOGGER = LoggerFactory.getLogger("info");


    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletResponse response = ctx.getResponse();                  // get response from current context

        String host = ctx.getRouteHost().toString();
        String serverId = jobManager.getServerIdToHost(host);       // get server id of current host

        Cookie cookie = jobManager.getRegisterCookie(serverId);           // get cookie from register cookie if available
        if (cookie != null) {
            response.addCookie(cookie);
            LOGGER.info("added set-cookie header into the response header with the cookie " + cookie.getValue());
            jobManager.removeRegisterCookie(serverId);                     // remove server from register cookie
            LOGGER.info("removed cookie from set-cookie map " + cookie.getValue());
            try {
                jobManager.decreasePending(serverId);
                LOGGER.info("Server Job pending decremented for " + serverId);
            } catch (Exception e) {
                LOGGER.error("Error updating 'Pending Map' " + e);
                ZuulException zuulException = new ZuulException("Invalid Request", 500, e.getMessage());
                throw new ZuulRuntimeException(zuulException);
            }
        }
        LOGGER.info("Response status from backend " + response.getStatus());
        LOGGER.info("Response was sent back to " + host);
        return null;
    }
}