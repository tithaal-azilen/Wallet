package com.Tithaal.Wallet;

import com.Tithaal.Wallet.redis.IdempotencyRecordRepository;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@TestConfiguration(proxyBeanMethods = false)
public class TestRedisAutoConfiguration {

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        Mockito.when(template.opsForValue()).thenReturn(ops);
        Mockito.when(ops.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any())).thenReturn(true);
        return template;
    }

    @Bean
    @Primary
    public IdempotencyRecordRepository idempotencyRecordRepository() {
        return Mockito.mock(IdempotencyRecordRepository.class);
    }
}
