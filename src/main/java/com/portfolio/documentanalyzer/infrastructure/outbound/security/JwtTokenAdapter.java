package com.portfolio.documentanalyzer.infrastructure.outbound.security;

import com.portfolio.documentanalyzer.application.port.in.command.AuthToken;
import com.portfolio.documentanalyzer.application.port.out.TokenIssuePort;
import com.portfolio.documentanalyzer.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenAdapter implements TokenIssuePort {

    private final SecretKey key;
    private final Duration expiration;

    public JwtTokenAdapter(JwtProperties properties){
        this.key= Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expiration=properties.expiration();
    }

    @Override
    public AuthToken issue(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expiration);

        String jwt = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();

        return new AuthToken(jwt, exp);
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
