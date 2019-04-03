package com.callumcarmicheal.wframe.web;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.apache.log4j.Logger;

import net.jodah.expiringmap.*;

/**
 * A session that expires after 4 hours of inactivity
 */
public class SessionList {
    protected final static Logger logger = Logger.getLogger(SessionList.class);
    protected final static ExpirationPolicy POLICY = ExpirationPolicy.ACCESSED;
    protected final static int EXPIRATION_TIME = 4;
    protected final static TimeUnit EXPIRATION_UNIT = TimeUnit.HOURS;
    protected final static int EXPIRATION_SECONDS = (60 * 60 * 4); // 4 hours
    protected final static int SESSION_KEY_SIZE = 64;

    // Cookie safe characters
    private static final String AB = "abdefghijklmnqrstuvxyzABDEFGHIJKLMNQRSTUVXYZ0123456789!#$%()*+-./@^_{}~";
    private static SecureRandom Rand = new SecureRandom();

    /* ---- SETTINGS ---- */
	public String COOKIE_HEADER = "WFRAME_SESSION";
    /* ---- SETTINGS ---- */

    /**
     * List of sessions
     */
    ExpiringMap<String, Session> sessions;

    /** 
     * Create a new session list
     */
    public SessionList() { 
        sessions = ExpiringMap.builder().variableExpiration().build();
    }

    /**
     * Get or create a session
     * @param sessionKey        The session key
     * @param forceCreateNew    If the session exists overwrite it with a new instance
     * @return                  The created session
     */
    public Session getOrCreate(String sessionKey, boolean forceCreateNew) {
        // Create new session if does not exists or replace
        if (!this.exists(sessionKey) || forceCreateNew) {
            // Create a new session with a new key if its invalid
            if (!validateKey(sessionKey))
                return create();

            Session session = new Session(sessionKey);
            sessions.put(sessionKey, session, POLICY, EXPIRATION_TIME, EXPIRATION_UNIT);    
            return session;
        }

        // Return the session
        return sessions.get(sessionKey);
    }

    /**
     * Check if a session key exists
     * @param sessionKey    The session key
     * @return              If the session exists
     */
    public boolean exists(String sessionKey) {
        return sessions.keySet().contains(sessionKey);
    }

    /**
     * Get a session by a session key
     * @param sessionKey    The session key
     * @return              The session object, null if not found
     */
    public Session get(String sessionKey) {
        if (!validateKey(sessionKey))
            return null;
        
        if (exists(sessionKey))
            return sessions.get(sessionKey);
        return null;
    }

    private boolean validateKey(String sessionKey) {
        return sessionKey.length() == SESSION_KEY_SIZE;
    }
    
    /**
     * Attempt to create a new session (with a random Session Key)
     * @return  The session object, null if unable to generate a unique session
     */
    public Session create() {
        // Attempt to generate a random session 5 times then fail
        final int MAX_ATTEMPTS = 5; 

        // Loop until we have outdone our max attempts
        for(int x = 0; x < MAX_ATTEMPTS; x++) {
            String sessionKey = randomString(SESSION_KEY_SIZE); // SESSION_KEY_SIZE characters

            // The session key exists
            if (exists(sessionKey))
                continue;

            // Create the new session
            Session session = new Session(sessionKey);
            sessions.put(sessionKey, session, POLICY, EXPIRATION_TIME, EXPIRATION_UNIT);    
            return session;
        }

        // Failed to generate new session key
        return null;
    }

    private String randomString(int len){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ ) 
           sb.append( AB.charAt( Rand.nextInt(AB.length()) ) );
        return sb.toString();
     }

     public int getExpirationSeconds() {
         return EXPIRATION_SECONDS;
     }
     
    public ExpiringMap<String, Session> getSessions() {
        return this.sessions;
    }
}