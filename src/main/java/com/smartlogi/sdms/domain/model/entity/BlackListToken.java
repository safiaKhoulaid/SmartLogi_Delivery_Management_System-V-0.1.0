package com.smartlogi.sdms.domain.model.entity;


import jakarta.persistence.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "blacklist_tokens", timeToLive = 900)
public class BlackListToken {
    @Id
    private String accessToken;
}
