package com.example.pokemon.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class DistributedLockConfig {

    /** Lock lease in Redis for {@link com.example.pokemon.cache.CacheableDAspect} only (not a global default). */
    public static final Duration SERIES_NAMES_DISTRIBUTED_LOCK_TTL = Duration.ofMinutes(5);

    @Bean(name = "seriesNamesDistributedLockRegistry")
    public RedisLockRegistry seriesNamesDistributedLockRegistry(RedisConnectionFactory connectionFactory) {
        return new RedisLockRegistry(
                connectionFactory,
                "pokemon-cacheable-d-series-names",
                SERIES_NAMES_DISTRIBUTED_LOCK_TTL.toMillis());
    }
}
