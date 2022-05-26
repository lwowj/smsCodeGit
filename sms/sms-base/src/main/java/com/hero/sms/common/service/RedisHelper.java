package com.hero.sms.common.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public interface RedisHelper {
    Boolean expire(String key, Long time);

    Long getExpire(String key);

    Boolean hasKey(String key);

    void del(String... key);

    Object get(String key);

    Boolean set(String key, Object value);

    Boolean set(String key, Object value, Long time);

    Long incr(String key, Long delta);

    Long decr(String key, Long delta);

    Object hget(String key, String item);

    Map<Object, Object> hmget(String key);

    Boolean hmset(String key, Map<String, Object> map);

    Boolean hmset(String key, Map<String, Object> map, Long time);

    Boolean hset(String key, String item, Object value);

    Boolean hset(String key, String item, Object value, Long time);

    void hdel(String key, Object... item);

    Boolean hHasKey(String key, String item);

    Double hincr(String key, String item, Double by);

    Double hdecr(String key, String item, Double by);

    Set<Object> sGet(String key);

    Boolean sHasKey(String key, Object value);

    Long sSet(String key, Object... values);

    Long sSetAndTime(String key, Long time, Object... values);

    Long sGetSetSize(String key);

    Long setRemove(String key, Object... values);

    List<Object> lGet(String key, Long start, Long end);

    Long lGetListSize(String key);

    Object lGetIndex(String key, Long index);

    Object lPopFirstThanPushLast(String key);

    Boolean lSet(String key, Object value);

    Boolean lSet(String key, Object value, Long time);

    Boolean lSet(String key, List<Object> value);

    Boolean lSet(String key, List<Object> value, Long time);

    Boolean lUpdateIndex(String key, Long index, Object value);

    Long lRemove(String key, Long count, Object value);
}
