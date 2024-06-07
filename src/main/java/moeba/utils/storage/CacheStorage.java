package moeba.utils.storage;

import java.util.function.Function;

public interface CacheStorage<K,V> {

    public boolean containsKey(K key);
    public V get(K key);
    public void put(K key, V value);
    public int getNumGetters();
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

}
