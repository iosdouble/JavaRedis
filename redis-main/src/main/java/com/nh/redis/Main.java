package com.nh.redis;

import org.redisson.Redisson;

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
