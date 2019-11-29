package com.isolate.microservice.loadBalancer;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
public class ErrorFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return "error";
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
        ctx.getResponse().setContentType("Application/Json");       // set the type into application json
        ctx.setThrowable(null);                                     // forward error into "/error" endpoint. Spring will automatically forward
        return null;
    }
}
