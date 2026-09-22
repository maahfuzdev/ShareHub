package com.sharehub.sharehub.service;


import org.springframework.security.authentication.  AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sharehub.sharehub.dto.request.LoginRequest;
import com.sharehub.sharehub.dto.request.RegisterRequest;
import com.sharehub.sharehub.dto.response.AuthResponse;
import com.sharehub.sharehub.entity.User;
import com.sharehub.sharehub.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * REGISTER - creates a new User + creates a Session
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(request.getRole());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        user.setActive(true);

        user = userRepository.save(user);
        log.info("User registered: {} with role: {}", user.getEmail(), user.getRole());

        // Auto Login - create Authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        log.info("Session created for user: {} | Session ID: {}", user.getEmail(), session.getId());

        return new AuthResponse(
                "Registration successful",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                session.getId()
        );
    }

    /**
     * LOGIN - authenticates an existing User + creates a Session
     */
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        session.setMaxInactiveInterval(30 * 60); // half hour 

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User logged in: {} | Session ID: {}", user.getEmail(), session.getId());

        return new AuthResponse(
                "Login successful",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                session.getId()
        );
    }

    /**
     * LOGOUT - deletes the Session
     */
    public void logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            session.invalidate();
            log.info("Session invalidated: {}", sessionId);
        }
        SecurityContextHolder.clearContext();
    }
}