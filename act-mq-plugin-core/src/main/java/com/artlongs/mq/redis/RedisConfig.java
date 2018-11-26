package com.artlongs.mq.redis;

import com.artlongs.mq.MqConfig;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 连接REDIS
 * Created by leeton on 9/12/17.
 */
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
        if (StringUtils.isNoneBlank(username) && StringUtils.isNoneBlank(password)) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password, db, username);
        }
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, null, db, null);
        }

        return jedisPool;
    }


}
