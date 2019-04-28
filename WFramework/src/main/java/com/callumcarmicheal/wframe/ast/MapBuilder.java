package com.callumcarmicheal.wframe.ast;

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
    private Map<K, V> map;

    @SuppressWarnings("unchecked")
    private MapBuilder(
        @SuppressWarnings("rawtypes") 
        Class<? extends Map> mapClass
    ) throws InstantiationException, IllegalAccessException {
        // Create a new instance
        map = mapClass.newInstance();
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
        Class<? extends Map> mapClass
    ) throws InstantiationException, IllegalAccessException {
        return new MapBuilder<K, V>(mapClass);
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