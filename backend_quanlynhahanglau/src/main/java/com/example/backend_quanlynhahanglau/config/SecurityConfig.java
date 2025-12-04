package com.example.backend_quanlynhahanglau.config;

import com.example.backend_quanlynhahanglau.security.AuthEntryPointJwt;
import com.example.backend_quanlynhahanglau.security.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/ws/**", "/app/**", "/topic/**", "/queue/**").permitAll()
                        // Cho phép kiểm tra và đặt bàn không cần đăng nhập
                        .requestMatchers(HttpMethod.GET, "/api/tables/filter").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tables/check-availability").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/public").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/public/*/cancel").permitAll()
                        // Cho phép các endpoint feedbacks public
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks/reservation/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/feedbacks/average-rating").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/feedbacks/public").permitAll()
                        // Cho phép các endpoint chat public (cho customer)
                        .requestMatchers("/api/chat/public/**").permitAll()
                        // Cho phép customer thêm món vào order qua QR code (không cần auth)
                        .requestMatchers(HttpMethod.GET, "/api/orders/public/by-table/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/orders/public/*/add-items").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/public/*/items/*").permitAll()
                        // Cho phép customer xem danh sách món ăn (không cần auth)
                        .requestMatchers(HttpMethod.GET, "/api/dishes/available").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow OPTIONS requests for CORS preflight
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001", "http://localhost:4200", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
