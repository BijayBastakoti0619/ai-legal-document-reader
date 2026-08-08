package com.aidocumentreader.backend.auth.jwttoken;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService{

    private String secretKey;
    private long jwtExpirationMs;

    public JwtService(
            @Value("${spring.security.jwt.secret-key}") String secretKey,
            @Value("${spring.security.jwt.expiration-ms}") long jwtExpirationMs
    ){
        this.secretKey = secretKey;
        this.jwtExpirationMs=jwtExpirationMs;
    }

    public String generateAccessToken(String userEmail){
        return Jwts.builder()
                .subject(userEmail)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpirationMs)) //15 minutes expiration\
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private SecretKey getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}