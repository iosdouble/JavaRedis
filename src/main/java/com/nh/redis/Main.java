package com.nh.redis;

import org.redisson.Redisson;
import org.redisson.RedissonKeys;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.command.CommandAsyncExecutor;

/**
 * com.nh.redis
 * create by admin nihui
 * create time 2021/1/14
 * version 1.0
 **/
public class Main {
    public static void main(String[] args) {
        Redisson redisson = RedissonManager.getRedisson();
    }
}
