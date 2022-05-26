package com.hero.sms.common.configure;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CommenReidsConfigure {

    @Value("${spring.redis.host:172.31.76.208}")
    private String host;
    @Value("${spring.redis.port:64379}")
    private int port;
    @Value("${spring.redis.password:Sms0413@$!@#}")
    private String password;
    @Value("${spring.redis.timeout:5000}")
    private int timeout;

    //通用redis库号
    private final int database = 4;


    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minIdle;
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private long maxWait;

    @Bean
    @Qualifier("commenRedisTemplate")
    @ConditionalOnClass(RedisOperations.class)
    public RedisTemplate<String, Object> commenRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(getFactory());
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(mapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // key采用 String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        // hash的 key也采用 String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        // value序列化方式采用 jackson
        template.setValueSerializer(jackson2JsonRedisSerializer);
        // hash的 value序列化方式采用 jackson
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }
    public GenericObjectPoolConfig getGenericObjectPoolConfig(){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(minIdle);
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxActive);
        config.setMaxWaitMillis(maxWait);
        return config;

    }
    public RedisStandaloneConfiguration getRedisConfig() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host,port);
        redisConfig.setDatabase(database);
        redisConfig.setPassword(RedisPassword.of(password));
        return redisConfig;
    }

    public LettuceConnectionFactory getFactory(){
        LettuceClientConfiguration configuration
                =  LettucePoolingClientConfiguration.builder().poolConfig(getGenericObjectPoolConfig()).build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(getRedisConfig(), configuration);
        factory.afterPropertiesSet();
        return factory;
    }
}
