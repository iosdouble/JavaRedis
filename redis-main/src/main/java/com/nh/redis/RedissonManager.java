package com.nh.redis;

import com.sun.org.apache.bcel.internal.generic.ACONST_NULL;
import org.redisson.Redisson;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * com.nh.redis
 * create by admin nihui
 * create time 2021/1/14
 * version 1.0
 **/
public class RedissonManager {
    private static Config config = new Config();
    //声明redisso对象
    private static Redisson redisson = null;
    //实例化redisson
    static{
        config.useSingleServer().setAddress("redis://192.168.74.131:6379");
        config.setCodec(new JsonJacksonCodec());
        config.setThreads(8);
        config.setNettyThreads(8);
        //得到redisson对象
        redisson = (Redisson) Redisson.create(config);
    }

    //获取redisson对象的方法
    public static Redisson getRedisson(){
        return redisson;
    }
}
