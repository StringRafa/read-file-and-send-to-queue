package com.panamby.readfile.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.panamby.readfile.consts.RedisConstants;

@Configuration
public class RedisConfig {
	
	@Value("${retry.service.cache.redis.ttl.minutes}")
	private String retryServiceCacheTTL;
	
	@Value("${properties-config.service.cache.redis.ttl.minutes}")
	private String propertiesConfigServiceCacheTTL;
	
	@Value("${backlog-manager.retry.service.cache.redis.ttl.minutes}")
	private String backlogManagerServiceCacheTTL;
	
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration
        		.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(Integer.parseInt(retryServiceCacheTTL)))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration(RedisConstants.RETRY_SERVIVE_CACHE,
                        RedisCacheConfiguration
                        	    .defaultCacheConfig()
                        	    .entryTtl(Duration.ofMinutes(Integer.parseInt(retryServiceCacheTTL)))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())))
                .withCacheConfiguration(RedisConstants.READ_FILE_PROPERTIES_CONFIG_CACHE,
                        RedisCacheConfiguration
                        	    .defaultCacheConfig()
                        	    .entryTtl(Duration.ofMinutes(Integer.parseInt(propertiesConfigServiceCacheTTL)))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())))
                .withCacheConfiguration(RedisConstants.BACKLOG_MANAGER_RETRY_TIME_CONFIG_CACHE,
                        RedisCacheConfiguration
                        	    .defaultCacheConfig()
                        	    .entryTtl(Duration.ofMinutes(Integer.parseInt(backlogManagerServiceCacheTTL)))
                                .disableCachingNullValues()
                                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())));
    }
}