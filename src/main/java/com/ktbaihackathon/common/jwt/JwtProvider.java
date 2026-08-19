package com.ktbaihackathon.common.jwt;

import com.ktbaihackathon.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(
            Long userId,
            Map<String, Object> claims,
            long expSeconds
    ) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expSeconds)))
                .signWith((SecretKey) key, Jwts.SIG.HS256)
                .compact();
    }

    public String createAccessToken(Long userId, String name) {
        return createToken(
                userId,
                Map.of("type","access","name", name),
                jwtProperties.getAccessTokenExpiredSeconds()
        );
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parse(token).getPayload().get("type", String.class));
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

}
