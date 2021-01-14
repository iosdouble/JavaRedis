package com.nh.cache;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * com.nh.cache
 * create by admin nihui
 * create time 2021/1/14
 * version 1.0
 **/
public class RedisCacheService {

    /* 空值缓存时间5分钟 */
    private static final int NULL_VALUE_KEY_CACHE_MILLISECONDS = 5 * 60 * 1000;

    //分布式map集合
    private RMap rMap;

    //集合可缓存空值
    private RSetCache rSetCache;

    // redisson客户端
    private RedissonClient redisson;

    /**
     * 批量操作选项 （目前redisson版本不支持）
     */
    //private BatchOptions options = BatchOptions.defaults();

    // 是否有ttl控制
    private boolean ttlControl;

    //缓存名称
    private String cacheName;

    // 集合名称
    private String cacheSetName;

    // 是否缓存null值
    private boolean cacheNull;

    /**
     * 静态构造方法
     *
     * @param redisson   redisson客户端
     * @param cacheName  缓存名称
     * @param ttlControl 是否有缓存时间控制
     * @param cacheNull  是否缓存null
     * @return
     */
    public static RedisCacheService of(RedissonClient redisson, String cacheName, boolean ttlControl, boolean cacheNull) {
        return new RedisCacheService(redisson, cacheName, ttlControl, cacheNull);
    }

    private RedisCacheService(RedissonClient redisson, String cacheName, boolean ttlControl, boolean cacheNull) {
        this.redisson = redisson;
        this.cacheName = cacheName;
        this.cacheSetName = cacheName + "_null_value_key_set";
        if (ttlControl) {
            //RMapCache继承RMap，并提供元素淘汰功能，根据过期时间清理过期缓存元素
            this.rMap = redisson.getMapCache(this.cacheName);
        } else {
            this.rMap = redisson.getMap(this.cacheName);
        }
        if (cacheNull) {
            //RSetCache继承Set，并提供元素淘汰功能，根据过期时间清理过期缓存元素
            this.rSetCache = redisson.getSetCache(this.cacheSetName);
        }
        this.ttlControl = ttlControl;
        this.cacheNull = cacheNull;
    }

    /**
     * 判断是否存在key
     *
     * @param key
     * @return
     */
    public boolean containsKey(Object key) {
        boolean result = rMap.containsKey(key);
        if (!result && cacheNull) {
            result = rSetCache.contains(key);
        }
        return result;
    }

    /**
     * 清除全部缓存
     */
    public void clear() {
        rMap.clear();
        if (cacheNull) {
            rSetCache.clear();
        }
    }

    /**
     * 获取数据集合
     *
     * @param key
     * @return
     */
    public Map get(Object key) {
        Map map = Maps.newHashMapWithExpectedSize(1);
        Object result = rMap.get(key);
        if ((result == null && cacheNull && rSetCache.contains(key)) || result != null) {
            map.put(key, result);
        }
        return map;
    }

    /**
     * 存入map
     *
     * @param key
     * @param value
     * @return
     */
    public Object put(Object key, Object value) {
        if (value == null) {
            if (cacheNull) {
                rSetCache.add(key, NULL_VALUE_KEY_CACHE_MILLISECONDS, TimeUnit.MILLISECONDS);
            }
        } else {
            rMap.put(key, value);
        }
        return value;
    }

    /**
     * 移除缓存元素
     *
     * @param key
     * @return
     */
    public Object remove(Object key) {
        Object o = rMap.remove(key);
        if (cacheNull) {
            rSetCache.remove(key);
        }
        return o;
    }

    /**
     * 批量存储
     *
     * @param map
     */
    /*public void putAll(Map map) {
        RBatch batch = redisson.createBatch(options);
        RMapAsync mapCache = ttlControl ? batch.getMapCache(this.cacheName) : batch
                .getMap(this.cacheName);
        RSetCacheAsync setCache = batch.getSetCache(this.cacheSetName);
        Set<Map.Entry> entrySet = map.entrySet();
        Iterator<Map.Entry> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry e = it.next();
            if (e.getValue() == null) {
                if (cacheNull) {// 缓存null
                    setCache.addAsync(e.getKey(),
                            NULL_VALUE_KEY_CACHE_MILLISECONDS,
                            TimeUnit.MILLISECONDS);
                }
            } else {
                mapCache.putAsync(e.getKey(), e.getValue());
            }
        }
        batch.execute();
    }*/

    /**
     * 根据键值获取缓存集合
     *
     * @param keys
     * @return
     */
    public Map getAll(Set keys) {
        Map map = rMap.getAll(keys);
        if (keys.size() == map.size()) {
            return map;
        } else if (cacheNull) {
            Set set = rSetCache.readAll();
            for (Object key : keys) {
                if (!map.containsKey(key) && set.contains(key)) {
                    map.put(key, null);
                }
            }
        }
        return map;
    }

    /**
     * 根据key批量移除缓存
     *
     * @param keys
     * @return
     */
    public long fastRemove(Object... keys) {
        long result = rMap.fastRemove(keys);
        if (cacheNull) {
            rSetCache.removeAll(Sets.newHashSet(keys));
        }
        return result;
    }

    /**
     * 快速存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean fastPut(Object key, Object value) {
        boolean result = true;
        if (value != null) {
            result = rMap.fastPut(key, value);
        } else if (cacheNull) {
            result = rSetCache.add(key, NULL_VALUE_KEY_CACHE_MILLISECONDS,
                    TimeUnit.MILLISECONDS);
        }
        return result;
    }

    /**
     * 带过期时间存
     *
     * @param key
     * @param value
     * @param ttl   单位毫秒
     * @return
     */
    public Object put(Object key, Object value, long ttl) {
        if (ttl <= 0) {
            return put(key, value);
        }
        if (value != null) {
            if (ttlControl) {
                ((RMapCache) rMap).put(key, value, ttl, TimeUnit.MILLISECONDS);
            } else {
                throw new IllegalArgumentException("can not support ttl");
            }
        } else if (cacheNull) {
            rSetCache.add(key, NULL_VALUE_KEY_CACHE_MILLISECONDS,
                    TimeUnit.MILLISECONDS);
        }
        return value;
    }

    /**
     * 带过期时间的putAll
     *
     * @param map
     * @param ttl
     */
    /*public void putAll(Map map, long ttl) {
        if(ttl <= 0){
            putAll(map);
            return;
        }
        RBatch batch = redisson.createBatch(options);
        RMapCacheAsync rMapCache = batch.getMapCache(this.cacheName);
        Set<Map.Entry> entrySet = map.entrySet();
        Iterator<Map.Entry> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry e = it.next();
            if (e.getValue() != null) {
                if (ttlControl) {
                    rMapCache.putAsync(e.getKey(), e.getValue(), ttl,
                            TimeUnit.MILLISECONDS);
                } else {
                    throw new IllegalArgumentException("can not support ttl");
                }
            } else if (cacheNull) {
                rSet.add(e.getKey(), NULL_VALUE_KEY_CACHE_MILLISECONDS,
                        TimeUnit.MILLISECONDS);
            }
        }
        // 批量操作
        batch.execute();
    }*/

    /**
     * 带过期时间快速存
     *
     * @param key
     * @param value
     * @param ttl
     * @return
     */
    public boolean fastPut(Object key, Object value, long ttl) {
        boolean result = true;
        if (value != null) {
            if (ttlControl) {
                result = ((RMapCache) rMap).fastPut(key, value, ttl,
                        TimeUnit.MILLISECONDS);
            } else {
                throw new IllegalArgumentException("can not support ttl");
            }
        } else if (cacheNull) {
            rSetCache.add(key, NULL_VALUE_KEY_CACHE_MILLISECONDS,
                    TimeUnit.MILLISECONDS);
        }
        return result;
    }
}
