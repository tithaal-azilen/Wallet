package com.Tithaal.Wallet.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import lombok.*;
import java.time.Instant;

@RedisHash(value = "IdempotencyRecord", timeToLive = 900L)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {
    @Id
    private String idempotencyKey;

    private String requestPath;

    private IdempotencyStatus status;

    private String responseBody;

    private Integer responseStatusCode;

    private Instant createdAt;

    private Instant updatedAt;
}
