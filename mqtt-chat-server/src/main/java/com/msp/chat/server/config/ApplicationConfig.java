package com.msp.chat.server.config;

import com.msp.chat.server.commons.utill.BrokerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.StringTokenizer;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
@ComponentScan(basePackages = {"com.msp.chat.server"})
@Configuration
public class ApplicationConfig {
    private Logger logger = LoggerFactory.getLogger("server");
    private static String[] sentinelIpArr;
    private static int[] sentinePortlArr;

    @Bean
    public StringRedisSerializer stringRedisSerializer(){
        return new StringRedisSerializer();
    }

    // REDIS-SENTINEL을 사용 할 때
    @Bean
    public RedisNode myMaster(){
        RedisNode redisNode = new RedisNode(BrokerConfig.getProperty(BrokerConfig.REDIS_MASTER_HOST),BrokerConfig.getIntProperty(BrokerConfig.REDIS_MASTER_PORT));
        redisNode.setName("mymaster");
        return redisNode;
    }

    @Bean
    public RedisNode mySent1(){
        String sentinelIp = null;
        int sentinelPort = 6379;
        String senips = BrokerConfig.getProperty(BrokerConfig.REDIS_SENTINELS_IPS);
        String ports = BrokerConfig.getProperty(BrokerConfig.REDIS_SENTINEL_PORTS);
        if(!"".equals(senips) && senips.indexOf(",")<0){
            sentinelIpArr = new String[1];
            sentinePortlArr = new int[1];

            sentinelIp = senips.trim();
            sentinelPort = Integer.parseInt(ports.trim());

            sentinelIpArr[0] = sentinelIp;
            sentinePortlArr[0] = sentinelPort;
        }else{
            StringTokenizer ipSt = new StringTokenizer(senips,",");
            StringTokenizer portSt = new StringTokenizer(ports,",");

            sentinelIpArr = new String[ipSt.countTokens()];
            sentinePortlArr = new int[portSt.countTokens()];

            int i =0;
            while(ipSt.hasMoreTokens()){
                sentinelIp = ipSt.nextToken();
                sentinelPort = Integer.parseInt(portSt.nextToken());
                sentinelIpArr[i] = sentinelIp;
                sentinePortlArr[i] = sentinelPort;
                i++;
            }
        }
        logger.info(("## [ApplicationConfig mySent1] sentinel1 connect info :" + sentinelIpArr[0] + ":" + sentinePortlArr[0]));
        return new RedisNode(sentinelIpArr[0], sentinePortlArr[0]);
    }

    @Bean
    public RedisNode mySent2(){
        if(sentinelIpArr.length>1) {
            logger.info(("## [ApplicationConfig mySent2] sentinel2 connect info :" + sentinelIpArr[1] + ":" + sentinePortlArr[1]));
            return new RedisNode(sentinelIpArr[1], sentinePortlArr[1]);
        }
        return null;
    }

    @Bean
    public RedisNode mySent3(){
        if(sentinelIpArr.length>2) {
            logger.info(("## [ApplicationConfig mySent3] sentinel3 connect info :" + sentinelIpArr[2] + ":" + sentinePortlArr[2]));
            return new RedisNode(sentinelIpArr[2], sentinePortlArr[2]);
        }
        return null;
    }

    @Bean
    public HashSet mySentSet(){
        List<RedisNode> sentList = new ArrayList<RedisNode>();
        sentList.add(mySent1());
        if(sentinelIpArr.length>1) {
            sentList.add(mySent2());
        }
        if(sentinelIpArr.length>2) {
            sentList.add(mySent3());
        }
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
    public JedisConnectionFactory jedisConnFactory(){
        JedisConnectionFactory jedisConnectionFactory;
        if(BrokerConfig.getProperty(BrokerConfig.REDIS_SENTINELS_USE).equals("Y")) {
            logger.info("### USE SENTINEL");
            jedisConnectionFactory = new JedisConnectionFactory(redisSentinelConfiguration());
        }else {
            logger.info("### NOT USE SENTINEL");
            jedisConnectionFactory = new JedisConnectionFactory();
            jedisConnectionFactory.setHostName(BrokerConfig.getProperty(BrokerConfig.REDIS_MASTER_HOST));
            jedisConnectionFactory.setPort(BrokerConfig.getIntProperty(BrokerConfig.REDIS_MASTER_PORT));
        }
        jedisConnectionFactory.setUsePool(true);
        jedisConnectionFactory.setTimeout(1200000);
        jedisConnectionFactory.setPoolConfig(jedisPoolConfig());
        jedisConnectionFactory.setDatabase(BrokerConfig.getIntProperty(BrokerConfig.REDIS_MASTER_DB));
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate masterRedisTemplate(){
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

    @Bean
    public JedisConnectionFactory slaveJedisConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(BrokerConfig.getProperty(BrokerConfig.REDIS_SLAVE_HOST));
        jedisConnectionFactory.setPort(BrokerConfig.getIntProperty(BrokerConfig.REDIS_SLAVE_PORT));
        jedisConnectionFactory.setDatabase(BrokerConfig.getIntProperty(BrokerConfig.REDIS_SLAVE_DB));
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

