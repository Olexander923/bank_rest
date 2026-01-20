package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.constants.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.TokenRefreshException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.RefreshTokenRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.CustomUserDetailService;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.util.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailService userDetailService;

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        //создание аутентификации
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        System.out.println("Authentication successful!");
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        SecurityContextHolder.getContext().setAuthentication(authentication);//теперь сохраняем
        //генерируем и возвращаем
        System.out.println("Auth successful for: " + request.getUsername());
        String jwt = jwtUtils.tokenGeneration((UserDetails) authentication.getPrincipal());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken()));
    }


    @PostMapping("/api/auth/logout")
    public ResponseEntity<MessageResponseDTO> logout(HttpServletResponse response) {
        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principle instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principle;
            Long userId = userDetails.getUserId();
            refreshTokenService.deleteByUserId(userId);
        }
        //очистка куки
        ResponseCookie cleanCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie refreshCleanCookie = jwtUtils.getCleanJwtRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, cleanCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCleanCookie.toString());

        return ResponseEntity.ok(new MessageResponseDTO("Logged out successfully"));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponseDTO("Refresh token is empty!"));
        }
        return refreshTokenService.findByToken(refreshToken)
                .map(foundRefreshToken -> {
                    refreshTokenService.verifyExpiration(foundRefreshToken);
                    User user = foundRefreshToken.getUser();

                    refreshTokenRepository.delete(foundRefreshToken);

                    String newJwt = jwtUtils.tokenGeneration(
                            new CustomUserDetails(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getPassword(),
                                    userDetailService.getAuthorities(user), // ← используй поле контроллера
                                    user.getRole()
                            )
                    );
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

                    // Передай userDetailService в метод
                    CustomUserDetails userDetails = new CustomUserDetails(
                            user.getId(),
                            user.getUsername(),
                            user.getPassword(),
                            userDetailService.getAuthorities(user),
                            user.getRole()
                    );
                    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
                    ResponseCookie refreshCookie = jwtUtils.generateRefreshJwtCookie(newRefreshToken.getToken());

                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                            .body(new JwtResponse(newJwt,newRefreshToken.getToken()));
                })
                .orElseThrow(() -> new TokenRefreshException("Refresh token doesn't exist in data base!"));
    }
}




