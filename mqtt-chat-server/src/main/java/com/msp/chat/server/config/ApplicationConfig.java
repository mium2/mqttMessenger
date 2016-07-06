package com.msp.chat.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 4. 1..
 */
@ComponentScan(basePackages = {"com.msp.chat.server"})
@Configuration
public class ApplicationConfig {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Bean
    public StringRedisSerializer stringRedisSerializer(){
        return new StringRedisSerializer();
    }
/*
    // REDIS-SENTINEL을 사용 할 때
    @Bean
    public RedisNode myMaster(){
        RedisNode redisNode = new RedisNode("211.241.199.215",6379);
        redisNode.setName("mymaster");
        return redisNode;
    }

    @Bean
    public RedisNode mySent1(){
        RedisNode redisSent1 = new RedisNode("211.241.199.214",26379);
        return redisSent1;
    }

    @Bean
    public RedisNode mySent2(){
        RedisNode redisSent1 = new RedisNode("211.241.199.215",26379);
        return redisSent1;
    }

    @Bean
    public RedisNode mySent3(){
        RedisNode redisSent1 = new RedisNode("211.241.199.217",26379);
        return redisSent1;
    }

    @Bean
    public HashSet mySentSet(){
        List<RedisNode> sentList = new ArrayList<RedisNode>();
        sentList.add(mySent1());
        sentList.add(mySent2());
        sentList.add(mySent3());
        HashSet mySentHashSet = new HashSet(sentList);
        return mySentHashSet;
    }

    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration(){
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setMaster(myMaster());
        redisSentinelConfiguration.setSentinels(mySentSet());
        return redisSentinelConfiguration;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        return new JedisPoolConfig();
    }

    @Bean
    public JedisConnectionFactory jedisConnFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration());
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.setTimeout(1200000);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig());
        jedisConnectionFactory.setDatabase(5);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate redisTemplate(){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnFactory());
        redisTemplate.setKeySerializer(stringRedisSerializer());
        redisTemplate.setValueSerializer(stringRedisSerializer());
        redisTemplate.setHashKeySerializer(stringRedisSerializer());
        redisTemplate.setHashKeySerializer(stringRedisSerializer());
        redisTemplate.setHashValueSerializer(stringRedisSerializer());
        redisTemplate.setStringSerializer(stringRedisSerializer());
        return redisTemplate;
    }
  */

    // Sentinel 없이 Redis 한대만 사용시
    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(10);
        jedisPoolConfig.setMaxWaitMillis(3000);
        jedisPoolConfig.setMaxIdle(50);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setNumTestsPerEvictionRun(10);

        return jedisPoolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("211.241.199.139");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setDatabase(3);
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.setTimeout(1200000);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig());
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate masterRedisTemplate(){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setExposeConnection(true);
        redisTemplate.setKeySerializer(stringRedisSerializer());
        redisTemplate.setValueSerializer(stringRedisSerializer());
        redisTemplate.setHashKeySerializer(stringRedisSerializer());
        redisTemplate.setHashKeySerializer(stringRedisSerializer());
        redisTemplate.setHashValueSerializer(stringRedisSerializer());
        redisTemplate.setStringSerializer(stringRedisSerializer());
        return redisTemplate;
    }


    @Bean
    public JedisConnectionFactory slaveJedisConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName("211.241.199.139");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setDatabase(3);
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.setTimeout(1200000);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig());
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate slaveRedisTemplate(){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(slaveJedisConnectionFactory());
        redisTemplate.setExposeConnection(true);
        redisTemplate.setKeySerializer(stringRedisSerializer());
        redisTemplate.setValueSerializer(stringRedisSerializer());
        redisTemplate.setHashKeySerializer(stringRedisSerializer());
        redisTemplate.setHashKeySerializer(stringRedisSerializer());
        redisTemplate.setHashValueSerializer(stringRedisSerializer());
        redisTemplate.setStringSerializer(stringRedisSerializer());
        return redisTemplate;
    }

}

