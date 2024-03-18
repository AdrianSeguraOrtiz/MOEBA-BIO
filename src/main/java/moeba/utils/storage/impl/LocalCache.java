package moeba.utils.storage.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import moeba.utils.storage.CacheStorage;

public class LocalCache<K,V> implements CacheStorage<K,V> {
    private ConcurrentHashMap<K,V> cache;
    private AtomicInteger numGetters;

    public LocalCache() {
        this.cache = new ConcurrentHashMap<>();
        this.numGetters = new AtomicInteger();
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public V get(K key) {
        numGetters.incrementAndGet();
        return cache.get(key);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public int getNumGetters() {
        return numGetters.get();
    }

}
