package com.example.bankcards.util;

import com.example.bankcards.security.CustomUserDetailService;
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
    private final CustomUserDetailService userDetailService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailService userDetailService) {
        this.jwtUtils = jwtUtils;
        this.userDetailService = userDetailService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        //получить заголовок
        final String authHeader = request.getHeader("Authorization");
        //проверяем формат заголовка
        if (authHeader == null || !authHeader.startsWith("bearer")) filterChain.doFilter(request,response);

        //извлечь токен
        final String jwt = authHeader.substring(7);
        if(jwtUtils.tokenValidate(jwt)) {
            String username = jwtUtils.getUsernameFromToken(jwt);//получили пользователя
            UserDetails userDetails = userDetailService.loadUserByUsername(username);//загрузили из БД

            //создать объект аутентификации
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);//сохранение в контекст
            filterChain.doFilter(request,response);//передать по цепочке фильтров
        }
    }
}
