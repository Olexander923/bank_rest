package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final CustomUserDetailService customUserDetailService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailService customUserDetailService) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (path.startsWith("/web/") ||
                path.startsWith("/admin/") ||
                path.startsWith("/user/") ||
                path.equals("/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        //получить заголовок
        final String authHeader = request.getHeader("Authorization");
        //проверяем формат заголовка
//        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
//            System.out.println("No valid authorization header");
//            filterChain.doFilter(request, response);
//            return;
//        }
        if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ")) {
            jwt = authHeader.substring(7);
        }
        if (jwt == null) {
            jwt = jwtUtils.getJwtFromCookies(request);
        }

        if (jwt != null) {
            System.out.println("JWT received: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
            if (jwtUtils.tokenValidate(jwt)) {
                String username = jwtUtils.getUsernameFromToken(jwt);//получили пользователя
                System.out.println("Token valid for user: " + username);

                try {
                    UserDetails userDetails = customUserDetailService.loadUserByUsername(username);//загрузили из БД
                    System.out.println("UserDetails loaded. Authorities: " + userDetails.getAuthorities());

                    //создать объект аутентификации
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);//сохранение в контекст
                    System.out.println("Authentication set in SecurityContext");
                } catch (Exception e) {
                    System.err.println("Failed to load user details: " + e.getMessage());
                }
            } else {
                System.err.println("JWT token is NOT valid");
            }
        }
        filterChain.doFilter(request, response);//передать по цепочке

    }
}

