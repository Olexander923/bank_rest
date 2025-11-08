package com.example.bankcards.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;// токен в переменных окружения

    @Value("${jwt.expiration}")
    private Long expiration;//время жизни токена

    public String tokenGeneration(UserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512,secret)
                .compact();
    }

    public boolean tokenValidate(String token){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            System.err.println("Invalid token signature: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("Token expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Empty token: " + e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }
}

