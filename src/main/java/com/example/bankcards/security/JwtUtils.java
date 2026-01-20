package com.example.bankcards.security;

import com.example.bankcards.util.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;//время жизни токена

    @Value("${token.jwt_cookie_name}")
    private String jwtCookie;

    @Value("${token.jwt_refresh_cookie_name}")
    private String jwtRefreshCookie;

    @PostConstruct
    public void logJwtSecret() {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured! Check your .env and application.yml.");
        }
        System.out.println("JWT secret loaded. Length: " + secret.length());

    }

    public String tokenGeneration(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public boolean tokenValidate(String token) {
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

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

    private ResponseCookie generateCookie(String name,String value,String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .secure(true)
                .sameSite("Strict")
                .path(path).maxAge(24*60*60).httpOnly(true).build();

        return cookie;
    }

    private String getCookieValueByName(HttpServletRequest request,String name) {
        Cookie cookie = WebUtils.getCookie(request,name);
        return (cookie != null) ? cookie.getValue() : null;
    }

    public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
        return generateCookie(jwtRefreshCookie,refreshToken,"/api/auth/refresh");
    }

    public ResponseCookie generateJwtCookie(CustomUserDetails userPrincipal) {
        String jwt = tokenGeneration(userPrincipal);
        return generateCookie(jwtCookie,jwt,"/api");
    }


    public String getJwtFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request,jwtCookie);
    }

    public String getJwtRefreshFromCookies(HttpServletRequest request) {
        return getCookieValueByName(request,jwtRefreshCookie);
    }

    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtCookie,null).path("/api").build();
        return cookie;
    }

    public ResponseCookie getCleanJwtRefreshCookie() {
        ResponseCookie cookie = ResponseCookie.from(jwtRefreshCookie,null).path("/api/auth/refresh").build();
        return cookie;
    }

}

