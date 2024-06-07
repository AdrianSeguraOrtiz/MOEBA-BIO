package moeba.utils.storage.impl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import moeba.utils.storage.CacheStorage;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

public class HybridCache<K,V> implements CacheStorage<K,V> {
    private Cache<K,V> cache;
    private AtomicInteger numGetters;

    public HybridCache(CacheManager cacheManager, String cacheName, Class<K> keyClass, Class<V> valueClass, int heapSize) {
        this.cache = cacheManager.createCache(cacheName, CacheConfigurationBuilder.newCacheConfigurationBuilder(keyClass, valueClass, ResourcePoolsBuilder.heap(heapSize)).build());
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

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V value = cache.get(key);
        if (value == null) {
            value = mappingFunction.apply(key);
            cache.put(key, value);
        }
        return value;
    }

}
