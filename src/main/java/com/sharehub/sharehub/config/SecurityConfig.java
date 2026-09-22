package com.sharehub.sharehub.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;


    // 1. Main Security Configuration
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http
            // CORS Configuration
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )

            // CSRF Disabled
            .csrf(csrf -> csrf.disable())

            // Session Management
            .sessionManagement(session -> session

                .sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )

                // Maximum 1 Active Session
                .maximumSessions(1)

                // Allow New Login
                .maxSessionsPreventsLogin(false)

                // Session Expired URL
                .expiredUrl(
                    "/api/auth/session-expired"
                )
            )

            // Authorization Rules
            .authorizeHttpRequests(auth -> auth

                // Thymeleaf pages and their static assets
                .requestMatchers(
                    "/",
                    "/login",
                    "/register",
                    "/css/**",
                    "/js/**"
                )
                .permitAll()

                // Public Authentication APIs
                .requestMatchers("/api/auth/**")
                .permitAll()

                // Swagger Documentation
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                )
                .permitAll()

                // All Other APIs Require Authentication
                .anyRequest()
                .authenticated()
            )

            // Authentication Provider
            .authenticationProvider(
                authenticationProvider()
            )

            // Logout Configuration
            .logout(logout -> logout

                .logoutUrl("/api/auth/logout")

                .invalidateHttpSession(true)

                .deleteCookies(
                    "SHAREHUB_SESSION",
                    "JSESSIONID"
                )

                .clearAuthentication(true)

                .logoutSuccessUrl(
                     "/login?logout"
                )

                .permitAll()
            );


        return http.build();
    }


    // 2. CORS Configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Allowed Frontend Origins
        config.setAllowedOrigins(
            List.of(
                "http://localhost:3000",
                "http://localhost:8081"
            )
        );

        // Allowed HTTP Methods
        config.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
            )
        );

        // Allowed Headers
        config.setAllowedHeaders(
            List.of("*")
        );

        // Allow Session Cookies
        config.setAllowCredentials(true);


        // Apply Configuration to All Endpoints
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            config
        );


        return source;
    }


    // 3. AuthenticationManager Bean
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config)
            throws Exception {

        return config.getAuthenticationManager();
    }


    // 4. Authentication Provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();  // ✅ সঠিক!

        provider.setUserDetailsService(userDetailsService);   // ← আলাদা setter
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }


    // 5. Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

}