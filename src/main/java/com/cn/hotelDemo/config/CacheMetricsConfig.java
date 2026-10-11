package com.cn.hotelDemo.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class CacheMetricsConfig {

	@Bean
	public RedisCacheManagerBuilderCustomizer metricsCustomizer(MeterRegistry registry) {
		return builder -> builder.withCacheConfiguration("roomAvailability",
				RedisCacheConfiguration.defaultCacheConfig()
						.entryTtl(Duration.ofMinutes(10)));
	}
}
