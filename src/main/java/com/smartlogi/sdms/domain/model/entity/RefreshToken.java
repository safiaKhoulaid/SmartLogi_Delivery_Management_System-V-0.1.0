package com.smartlogi.sdms.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "refresh_tokens", timeToLive = 604800)
public class RefreshToken {

    @Id
    private String id;

    @Indexed // Hadi bach t-9llbi b username
    private String username;


}