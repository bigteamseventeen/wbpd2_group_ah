package com.bigteamseventeen.wpd2_ah.milestones;

import java.util.Map;

/**
 * A map builder for {@link Map}'s
 *  (replacement for ImmutableMap as it does not support null)
 * @param <K> key type
 * @param <V> value type
 */
public class MapBuilder<K, V> {
    /**
     * Base map object
     */
    private Map<K, V> map = null;

    @SuppressWarnings("unchecked")
    private MapBuilder(
        @SuppressWarnings("rawtypes") 
        Class<? extends Map> mapClass,
        Boolean throwException
    ) throws InstantiationException, IllegalAccessException {
        try {
            // Create a new instance
            map = mapClass.newInstance();
        } catch( InstantiationException | IllegalAccessException e) {
            if (throwException)
                throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private MapBuilder(
        @SuppressWarnings("rawtypes") 
        Class<? extends Map> mapClass
    ) {
        try {
            // Create a new instance
            map = mapClass.newInstance();
        } catch( InstantiationException | IllegalAccessException e) {
            map = null;
        }
        
    }

    /**
     * Create a builder instance
     * @param <K>
     * @param <V>
     * @param mapClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <K, V> MapBuilder<K, V> builder_s(
        @SuppressWarnings("rawtypes")
        Class<? extends Map> mapClass
    ) {
        return new MapBuilder<K, V>(mapClass);
    }

    /**
     * Create a builder instance
     * @param <K>
     * @param <V>
     * @param mapClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <K, V> MapBuilder<K, V> builder(
        @SuppressWarnings("rawtypes")
        Class<? extends Map> mapClass,
        Boolean throwException
    ) throws InstantiationException, IllegalAccessException {
        try {
            return new MapBuilder<K, V>(mapClass, true);
        } catch( InstantiationException | IllegalAccessException e) {
            if (throwException)
                throw e;
            return null;
        }
    }

    /**
     * Put an item in the map
     * @param key
     * @param value
     * @return
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Get the builder
     * @return
     */
    public Map<K, V> build() {
        return map;
    }
}