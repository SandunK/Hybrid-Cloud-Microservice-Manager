package com.isolate.microservice.loadBalancer;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
public class RouteFilter extends ZuulFilter {

    @Autowired
    private ServerManager serverManager;

    @Autowired
    private JobManager jobManager;

    private static final Logger LOGGER = LoggerFactory.getLogger("info");

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    private void route(RequestContext context) {
        try {
            String serviceId = context.getRequest().getHeader("serviceId");   // get service id for the job
            if (serviceId == null) {
                serviceId = "alpha";
            }

            Map.Entry<String, Map> serverIdToServerData = this.serverManager.find(jobManager.getPending(), serviceId);  // find a free server to submit the job
            String serverId = (String) serverIdToServerData.getValue().get("ServerId");
            String serverUrl = serverIdToServerData.getValue().get("Url").toString();
            jobManager.setServerIdToHost(serverUrl, serverId);
            LOGGER.info("Routed new request into the url " + serverUrl);
            context.setRouteHost(new URL(serverUrl));                               // set selected server url to route

            jobManager.increasePending(serverId);                                         // increase pending list
            LOGGER.info("Server pending job count incremented for " + serverId);

            Cookie cookie = new Cookie("AWSALB", Long.toString(System.currentTimeMillis()));   // create a cookie
            jobManager.setCookieMap(cookie.getValue(), serverUrl);
            LOGGER.info("Cookie added into the map. " + cookie.getValue());
            jobManager.setRegisterCookie(serverId, cookie);
            LOGGER.info("Cookie added to the set-cookie map cookie-value = " + cookie.getValue());
        } catch (Exception e) {
            LOGGER.error("Routing Error " + e);
            ZuulException zuulException = new ZuulException("Invalid Request", 503, e.getMessage());
            throw new ZuulRuntimeException(zuulException);
        }
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();                      // get request data from current context
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultDate = new Date(currentTime);                            // get current time

        LOGGER.info("Received a routing request to the path " + request.getServletPath());
        LOGGER.info("Routing started at " + sdf.format(resultDate));
        Cookie[] cookieLst = request.getCookies();                          // get request cookies

        if (cookieLst != null) {                                            // if cookie was set
            String cookieValue = cookieLst[0].getValue();

            if (jobManager.getCookieMap().containsKey(cookieValue)) {                       // if set find the bound url
                String serverPath = jobManager.getCookieMap().get(cookieValue);
                try {
                    ctx.setRouteHost(new URL(serverPath));                  // forward to the same server
                    LOGGER.info("Routed the request into the " + serverPath + " with existing cookie " + cookieValue);
                } catch (MalformedURLException e) {
                    LOGGER.error("Error setting route host " + e);
                }
            } else {
                this.route(ctx);                // route to a new server if cookie invalid
            }
        } else {
            this.route(ctx);                   // route to a new server if cookie not available
        }

        return null;
    }
}
