package com.brevery.config;

import com.brevery.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // H2 console
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // PUBLIC — Auth
                    .requestMatchers("/api/v1/auth/**").permitAll()

                    // PUBLIC — Product (GET only)
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                    // PUBLIC — Guest order tracking
                    .requestMatchers(HttpMethod.GET, "/api/v1/orders/track").permitAll()

                    // PUBLIC — Contact form
                    .requestMatchers(HttpMethod.POST, "/api/v1/contact").permitAll()

                    // PUBLIC — Swagger & H2
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/api-docs/**",
                            "/h2-console/**"
                    ).permitAll()

                    // PUBLIC — Actuator health
                    .requestMatchers("/actuator/health").permitAll()

                    // ADMIN only
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                    // USER hoặc ADMIN — Cart, Orders, AI Chat, Profile
                    .requestMatchers("/api/v1/cart/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/v1/orders/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/v1/ai/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/v1/profile/**").hasAnyRole("USER", "ADMIN")
                    .requestMatchers("/api/v1/addresses/**").hasAnyRole("USER", "ADMIN")

                    // Còn lại phải authenticated
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength 12 theo spec
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
