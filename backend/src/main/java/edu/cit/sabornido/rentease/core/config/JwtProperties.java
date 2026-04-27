package edu.cit.sabornido.rentease.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private String secret = "RentEaseJwtSecretKeyForSigningTokensMinimum256BitsRequired";
    private long accessExpirationMs = 3600000;  // 1 hour
    private long refreshExpirationMs = 604800000; // 7 days
}
