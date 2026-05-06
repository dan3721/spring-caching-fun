package com.example.pokemon.cache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Distributed single-flight cache: coordinates loads across JVMs using a Redis lock
 * (see {@link com.example.pokemon.cache.CacheableDAspect}) in addition to Spring's {@code CacheManager}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheableD {

    /** Spring Cache cache name(s); the aspect uses the first entry. */
    String[] cacheNames();

    /**
     * Optional SpEL for the cache key; if empty, {@link org.springframework.cache.interceptor.SimpleKeyGenerator}
     * is used (same idea as {@code @Cacheable} with no {@code key}).
     */
    String key() default "";
}
