package com.callumcarmicheal.wframe.web;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Session implements Serializable {
    private static final long serialVersionUID = 13421235453234L;
    protected HashMap<String, Serializable> sessionValues = new HashMap<>();
    protected HashMap<String, Boolean> flashedSessionValues = new HashMap<>();
    private String sessionKey = null;

    /**
     * Create a new session object with the key specified. 
     * @param key
     */
    public Session(String key) {
        this.sessionKey = key;
    }

    /**
     * Check if a key is the same type of
     * @param <T>
     * @param key   The value inside the session
     * @param type  The type to check the value against
     * @return      If the types are the same
     */
    public <T extends Serializable> boolean isType(String key, Class<T> type) {
        if (this.sessionValues.containsKey(key))
            return false;

        Serializable value = this.sessionValues.get(key);
        return value.getClass().isInstance(type);
    }

    /**
     * Get a value from the session, if it does not exist set it's default value
     * @param <T>
     * @param key           The session key
     * @param defaultValue  The default value if key does not exist
     * @return              The value in session
     */
    public <T extends Serializable> T get(String key, T defaultValue) {
        if (!this.sessionValues.containsKey(key)) {
            this.sessionValues.put(key, defaultValue);
            return defaultValue;
        }
        
        return (T)this.sessionValues.get(key);
    }

    /**
     * Get an value from the session
     * @param <T>
     * @param key
     * @return
     */
    public <T extends Serializable> T get(String key) {
        if (!this.sessionValues.containsKey(key))
            return null;
        
        return (T)this.sessionValues.get(key);
    }

    /**
     * Invalidates the flash content (sets all to be marked for deletion)
     */
    public void invalidateFlash() {
        for (String key : flashedSessionValues.keySet()) 
            this.flashedSessionValues.put(key, false);
    }
    
    /**
     * Reflash all contents in temporary story to the next request
     */
    public void reflash() {
        for (String key : flashedSessionValues.keySet()) 
            this.flashedSessionValues.put(key, true);
    }

    /**
     * Clear's all flashed content from the session
     */
    public void clearFlash() {
        // Remove any entries that have their value set to false.
        for (Map.Entry<String, Boolean> entry : flashedSessionValues.entrySet()) {
            // Check if the flash is marked for deletion
            if (entry.getValue() == false) {
                // Delete the session value and the flash key
                sessionValues.remove(entry.getKey());
                flashedSessionValues.remove(entry.getKey());
            }
        }
    }

    /**
     * Keep variables inside the flash storage for the next session
     * @param keys
     */
    public void keep(String ... keys) {
        for (String key : keys) 
            this.flashedSessionValues.put(key, true);
    }

    /**
     * Remove an item from the session
     * @param key   The key
     */
    public void remove(String key) {
        // If the key is in flash, then remove it.
        if (this.flashedSessionValues.containsKey(key))
            this.flashedSessionValues.remove(key);

        this.sessionValues.remove(key);
    }

    /**
     * Remove an item from the session
     * @param <T>
     * @param key       The key
     * @param object    The value in the session
     * @return
     */
    public <T extends Serializable> boolean remove(String key, T object) {
        return this.sessionValues.remove(key, object);
    }

    public boolean containsKey(String key) {
        return this.sessionValues.containsKey(key);
    }

    /**
     * Check if the session has a value in it
     * @param <T>
     * @param value  The value 
     * @return
     */
    public <T extends Serializable> boolean containsValue(T value) {
        return this.sessionValues.containsValue(value);
    }

    /**
     * Set a value in the session
     * <i>This will permantly set the value in session, (stops flash vars from removing after next request)</i>
     * @param <T>
     * @param key       The key used in the session
     * @param value     The value to place into the session
     * @return          The value placed into the session
     */
    public <T extends Serializable> T set(String key, T value) {
        // If the key is in flash, then remove it.
        if (this.flashedSessionValues.containsKey(key))
            this.flashedSessionValues.remove(key);

        this.sessionValues.put(key, value);
        return value;
    }

    /**
     * Gets the session key used to identify this session
     * @return
     */
    public String getSessionKey() {
        return this.sessionKey;
    }
}