package com.example.golfgame.utils;

/**
 * A simple generic class to hold pairs of related objects.
 * 
 * @param <K> the type of the first element (key)
 * @param <V> the type of the second element (value)
 */
public class Pair<K, V> {
    private K key;
    private V value;

    /**
     * Constructs a new Pair with the specified key and value.
     *
     * @param key the first element of the pair
     * @param value the second element of the pair
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of this pair.
     *
     * @return the key of this pair
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of this pair.
     *
     * @return the value of this pair
     */
    public V getValue() {
        return value;
    }
}