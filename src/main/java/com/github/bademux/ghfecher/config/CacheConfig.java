package com.github.bademux.ghfecher.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public Map<String, List> cache(
            @Value("${cache.maximumSize}") Long maximumSize,
            @Value("${cache.expireAfterWriteMin}") Long expireAfterWriteMin
    ) {
        return Caffeine
                .newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(expireAfterWriteMin, TimeUnit.MINUTES)
                .<String, List>build()
                .asMap();
    }

}
