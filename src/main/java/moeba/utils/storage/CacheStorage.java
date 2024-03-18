package moeba.utils.storage;

public interface CacheStorage<K,V> {

    public boolean containsKey(K key);
    public V get(K key);
    public void put(K key, V value);
    public int getNumGetters();

}
