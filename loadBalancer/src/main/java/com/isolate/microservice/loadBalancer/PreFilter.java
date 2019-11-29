package com.isolate.microservice.loadBalancer;

import com.netflix.zuul.ZuulFilter;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
public class PreFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return "pre";
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
        return null;
    }
}