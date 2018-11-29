package com.artlongs.act.mq.plugin.redis;

import com.artlongs.act.mq.plugin.core.MqConfig;
import org.osgl.util.S;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Singleton;

/**
 * 连接REDIS
 * Created by leeton on 9/12/17.
 */
@Singleton
public class RedisConfig {

    public static JedisPool buildConnetion() {
        String host = MqConfig.redis_host.get();
        Integer port = MqConfig.redis_port.get();
        String username = MqConfig.redis_username.get();
        String password = MqConfig.redis_password.get();
        Integer db = MqConfig.redis_database.get();
        Integer timeout = MqConfig.mq_redis_connetion_timeout.get();

        JedisPool jedisPool = null;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        if (S.isNotBlank(password)) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, db, username);
        }
        if (S.isNotBlank(username) && S.isNotBlank(password)) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, db, username);
        }
        if (S.isBlank(username) || S.isBlank(password)) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, null, db, null);
        }

        return jedisPool;
    }


}
