package com.example.demo.config;

import com.example.demo.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/accounts/login", "/api/accounts/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/accounts/exists/**").permitAll()
                        .requestMatchers("/api/products/user/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/variants/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/variants/quantity").permitAll()
                        .requestMatchers("/api/product-types/**", "/api/colors/**", "/api/sizes/**", "/api/products/**", "/api/product-images/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/variants/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/variants/**").hasRole("ADMIN")
                        .requestMatchers("/api/wishlist/**", "/api/addresses/**", "/api/notifications/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/coupons/preview").hasRole("USER")
                        .requestMatchers("/api/coupons/admin", "/api/coupons/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/reviews/admin", "/api/reviews/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/reviews/me").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/orders/checkout").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/me").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/orders/*").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/orders/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")
                        .requestMatchers("/api/accounts/**").authenticated()
                        .requestMatchers("/api/detail-account/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/cart/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
