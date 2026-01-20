package com.example.bankcards.controller;

import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.RefreshToken;
import com.example.bankcards.constants.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.ValidationException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.service.RefreshTokenService;
import com.example.bankcards.util.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WebAuthController {
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        System.out.println("WebAuthController loaded!");
    }

    @GetMapping("/web/register")
    public String register() {
           return "register_page";
    }

    @PostMapping("/web/register")
    public String register(@Valid RegisterRequest request, BindingResult bindingResult, Model model) {
           if (bindingResult.hasErrors()) {
               model.addAttribute("error", "Validation failed!");
               return "register_page";
           }
        System.out.println("Received email: " + request.getEmail());
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new com.example.bankcards.exception.ValidationException("Email is required!");
        }
        //проверяем пользователя по username и email, создаем нового
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("User name is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("This email already exist!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));//шифр пароля
        user.setEmail(request.getEmail());

        Role userRole = Role.USER;
        user.setRole(userRole);
        userRepository.save(user);
        return "redirect:/web/user/login?registered=true";
    }


    @PostMapping("/web/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response) {
        //создание аутентификации
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,password
                )
        );
        System.out.println("Authentication successful!");

        SecurityContextHolder.getContext().setAuthentication(authentication);//теперь сохраняем
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        //генерация куки
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUserId());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());

        //теперь добавление куки в ответ
        response.addHeader(HttpHeaders.SET_COOKIE,jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE,jwtRefreshCookie.toString());
        return userDetails.getRole() == Role.ADMIN
                ? "admin_profile" : "user_profile";
    }

    @GetMapping("/web/admin/login")
    public String adminLoginForm(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/web/admin/profile";
        }
        return "admin_login_page";
    }

    @GetMapping("/web/user/login")
    public String userLoginForm(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/web/user/profile";
        }
        return "user_login_page";
    }

    @GetMapping("/") //для редиректа на форму
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/web/user/login";
        }
        return userDetails.getRole() == Role.ADMIN ? "redirect:/web/admin/profile" : "redirect:/web/user/profile";
    }

    @GetMapping("/web/admin/profile")
    public String adminProfile() {
         return "admin_profile";
    }

    @GetMapping("/web/user/profile")
    public String userProfile() {
        return "user_profile";
    }
}
