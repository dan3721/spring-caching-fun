package com.example.pokemon.cache;

import java.lang.reflect.Method;
import java.util.concurrent.locks.Lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class CacheableDAspect {

    private final CacheManager cacheManager;
    private final RedisLockRegistry redisLockRegistry;
    private final SimpleKeyGenerator keyGenerator = new SimpleKeyGenerator();
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public CacheableDAspect(
            CacheManager cacheManager,
            @Qualifier("seriesNamesDistributedLockRegistry") RedisLockRegistry redisLockRegistry) {
        this.cacheManager = cacheManager;
        this.redisLockRegistry = redisLockRegistry;
    }

    @Around("@annotation(com.example.pokemon.cache.CacheableD)")
    public Object aroundCacheableD(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), pjp.getTarget().getClass());
        CacheableD cacheableD = AnnotatedElementUtils.findMergedAnnotation(method, CacheableD.class);
        if (cacheableD == null) {
            return pjp.proceed();
        }

        String[] names = cacheableD.cacheNames();
        if (names.length == 0) {
            throw new IllegalStateException("@CacheableD.cacheNames must not be empty on " + method);
        }
        String cacheName = names[0];

        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Unknown cache '" + cacheName + "' for @CacheableD on " + method);
        }

        Object cacheKey = resolveCacheKey(pjp, method, cacheableD);

        Cache.ValueWrapper hit = cache.get(cacheKey);
        if (hit != null) {
            log.debug("Cache hit (before lock) cache={} method={} key={}", cacheName, method.getName(), cacheKey);
            return hit.get();
        }

        String lockKey = cacheName + "::" + cacheKey;
        log.debug("Cache miss, acquiring distributed lock lockKey={} cache={} method={}", lockKey, cacheName, method.getName());
        Lock lock = redisLockRegistry.obtain(lockKey);
        lock.lockInterruptibly();
        log.debug("Acquired distributed lock lockKey={} cache={} method={}", lockKey, cacheName, method.getName());
        try {
            // Double-check: another thread or JVM may have filled the cache while we blocked on the lock.
            Cache.ValueWrapper second = cache.get(cacheKey);
            if (second != null) {
                log.debug(
                        "Cache hit after lock (double-check) lockKey={} cache={} method={} key={}",
                        lockKey,
                        cacheName,
                        method.getName(),
                        cacheKey);
                return second.get();
            }
            Object value = pjp.proceed();
            if (value != null) {
                cache.put(cacheKey, value);
            }
            return value;
        } finally {
            log.debug("Releasing distributed lock lockKey={} cache={} method={}", lockKey, cacheName, method.getName());
            lock.unlock();
        }
    }

    private Object resolveCacheKey(ProceedingJoinPoint pjp, Method method, CacheableD cacheableD) {
        if (!StringUtils.hasText(cacheableD.key())) {
            return keyGenerator.generate(pjp.getTarget(), method, pjp.getArgs());
        }
        MethodBasedEvaluationContext ctx =
                new MethodBasedEvaluationContext(pjp.getTarget(), method, pjp.getArgs(), parameterNameDiscoverer);
        Object key = parser.parseExpression(cacheableD.key()).getValue(ctx);
        if (key == null) {
            throw new IllegalStateException("@CacheableD key SpEL evaluated to null on " + method);
        }
        return key;
    }
}
