package com.smartlogi.sdms.domain.model.entity;

import org.springframework.data.annotation.Id;

import org.springframework.data.redis.core.RedisHash;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@RedisHash(value = "blacklist_tokens", timeToLive = 900)
@AllArgsConstructor // 👈 ضروري باش تقدري ديري new BlackListToken(token)
@NoArgsConstructor  // 👈 Redis كيحتاج Constructor خاوي
@Getter
@Setter
public class BlackListToken {

    @Id // دابا هادي ديال org.springframework.data.annotation.Id
    private String accessToken;
}