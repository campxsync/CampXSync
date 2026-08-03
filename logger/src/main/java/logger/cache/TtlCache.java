package logger.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TtlCache<K, V> {
    private final Map<K, CacheEntry<V>> cacheMap = new ConcurrentHashMap<>();
    private final long defaultTtlMs;

    public TtlCache(long defaultTtlMs) {
        this.defaultTtlMs = defaultTtlMs;
    }

    public void put(K key, V value) {
        put(key, value, defaultTtlMs);
    }

    public void put(K key, V value, long ttlMs) {
        if (key == null) {
            return;
        }
        long expireTime = System.currentTimeMillis() + ttlMs;
        cacheMap.put(key, new CacheEntry<>(value, expireTime));
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }
        CacheEntry<V> entry = cacheMap.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cacheMap.remove(key);
            return null;
        }
        return entry.getValue();
    }

    public void remove(K key) {
        if (key != null) {
            cacheMap.remove(key);
        }
    }

    public void clear() {
        cacheMap.clear();
    }

    public int size() {
        cleanExpired();
        return cacheMap.size();
    }

    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cacheMap.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    private static class CacheEntry<V> {
        private final V value;
        private final long expireTime;

        public CacheEntry(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        public V getValue() {
            return value;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        public boolean isExpired(long now) {
            return now > expireTime;
        }
    }
}
