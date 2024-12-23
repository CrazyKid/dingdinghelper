package com.crazykid.support;

import java.util.concurrent.TimeUnit;

/**
 * 缓存报警次数计数的接口
 *
 * @author arthur
 * @date 2024/12/23 18:20
 */
public interface AlarmCacheDriver {
    /**
     * 增加报警次数
     *
     * @param key
     * @param time
     * @param unit
     */
    void cacheIncrementTimes(String key, long time, TimeUnit unit);

    /**
     * 缓存信息
     *
     * @param key
     * @param v
     * @param time
     * @param unit
     */
    void cache(String key, Object v, long time, TimeUnit unit);

    /**
     * 获取缓存的数据
     *
     * @param key
     * @return
     */
    Object getCache(String key);
}
