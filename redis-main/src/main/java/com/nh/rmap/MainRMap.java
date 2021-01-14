package com.nh.rmap;

import com.nh.redis.RedissonManager;
import org.redisson.Redisson;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;

import java.util.Iterator;

/**
 * com.nh.rmap
 * create by admin nihui
 * create time 2021/1/14
 * version 1.0
 **/
public class MainRMap {
    public static void main(String[] args) {
        Redisson redisson = RedissonManager.getRedisson();
        RKeys keys = redisson.getKeys();
        Iterable<String> keys1 = keys.getKeys();
        Iterator<String> iterator = keys1.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
