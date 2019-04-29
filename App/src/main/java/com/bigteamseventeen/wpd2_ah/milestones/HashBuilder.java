package com.bigteamseventeen.wpd2_ah.milestones;

import java.util.HashMap;
import java.util.Map;

public class HashBuilder<K, V> {

    private MapBuilder <K, V> builder;
    private HashBuilder() {}

    /**
     * Create a builder instance
     * 
     * @param <K>
     * @param <V>
     * @param mapClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <K, V> HashBuilder<K, V> builder() {
        HashBuilder<K,V> inst = new HashBuilder<>();
        inst.builder = MapBuilder.<K, V>builder_s(HashMap.class);
        return inst;
    }

    /**
     * Put an item in the map
     * 
     * @param key
     * @param value
     * @return
     */
    public HashBuilder<K, V> put(K key, V value) {
        builder.put(key, value);
        return this;
    }

    /**
     * Get the builder
     * @return
     */
    public Map<K, V> build() {
        return builder.build();
    }
}