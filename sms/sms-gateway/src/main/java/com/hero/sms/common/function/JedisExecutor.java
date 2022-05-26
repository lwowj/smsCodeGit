package com.hero.sms.common.function;

import com.hero.sms.common.exception.RedisConnectException;

/**
 * @author Administrator
 */
@FunctionalInterface
public interface JedisExecutor<T, R> {
    R excute(T t) throws RedisConnectException;
}
