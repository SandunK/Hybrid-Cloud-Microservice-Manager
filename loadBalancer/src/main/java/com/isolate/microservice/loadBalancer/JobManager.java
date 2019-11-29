package com.isolate.microservice.loadBalancer;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sandun Gunasekara
 * Created on 11/25/2019
 */
@Component
public class JobManager {
    private Map<String, Integer> pending = new HashMap<>();         // pending job map server id to pending job amount
    private Map<String, Cookie> registerCookie = new HashMap<>();   // server id to cookie which need to send set-cookie header
    private Map<String, String> serverIdToHost = new HashMap<>();   // server id by host url
    private final Object pendingLock = new Object();                // lock for pending map
    private final int cookieCacheSize = 50;
    private final Map<String, String> cookieMap = Collections.synchronizedMap(      // synchronized linked hash map of cookie(cookie to server id) which automatically delete unused entries
            new LinkedHashMap<String, String>(cookieCacheSize, 1, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > cookieCacheSize;
                }
            }
    );

    /**
     * give registered cookie for the key
     *
     * @param cookieKey - Server id to register the cookie
     * @return - cookie object
     */
    Cookie getRegisterCookie(String cookieKey) {
        return registerCookie.get(cookieKey);
    }

    /**
     * give registered cookie for the key
     *
     * @param cookieKey - server id that need to remove the cookie
     */
    void removeRegisterCookie(String cookieKey) {
        this.registerCookie.remove(cookieKey);
    }

    Map<String, Integer> getPending() {
        return pending;
    }

    /**
     * put new entry for register cookie
     *
     * @param serverId - server id that should register the cookie
     * @param cookie   - cookie object to register
     */
    void setRegisterCookie(String serverId, Cookie cookie) {
        this.registerCookie.put(serverId, cookie);
    }

    String getServerIdToHost(String host) {
        return this.serverIdToHost.get(host);
    }

    /**
     * put new entry for server host to id map
     *
     * @param serverUrl - server url
     * @param serverId  - server id that mapped into the url
     */
    void setServerIdToHost(String serverUrl, String serverId) {
        this.serverIdToHost.put(serverUrl, serverId);
    }

    Map<String, String> getCookieMap() {
        return cookieMap;
    }

    /**
     * put new entry for cookie map
     *
     * @param cookie    - cookie object
     * @param serverUrl - serverUrl that mapped into the cookie
     */
    void setCookieMap(String cookie, String serverUrl) {
        this.cookieMap.put(cookie, serverUrl);
    }

    /**
     * Increase pending job amount of server until server response received
     *
     * @param serverId - server id that job was submitted
     */
    void increasePending(String serverId) {
        synchronized (pendingLock) {
            if (pending.containsKey(serverId)) {
                pending.put(serverId, pending.get(serverId) + 1);
            } else {
                pending.put(serverId, 1);
            }
        }
    }

    /**
     * decrease pending jobs
     *
     * @param serverId - server id that job was submitted
     */
    void decreasePending(String serverId) {
        synchronized (pendingLock) {
            this.pending.put(serverId, this.pending.get(serverId) - 1);
        }
    }
}
