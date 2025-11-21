package com.KrishiSakhi.AI.Security;


import com.KrishiSakhi.AI.Security.jwt.AuthEntryPointJwt;
import com.KrishiSakhi.AI.Security.jwt.AuthTokenFilter;
import com.KrishiSakhi.AI.Service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler, JwtUtils jwtUtils, UserService userService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    // --- Bean Definitions ---

    // Defines the JWT filter and wires in the required utilities and services
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter(jwtUtils, userService);
    }

    // Standard password encoder (required by Spring Security)
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
        http.csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs
                // Set the unauthorized handler for 401 errors
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                // Set session management to stateless (no HttpSession is created)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define access rules
                .authorizeHttpRequests(auth ->
                        // PUBLIC ENDPOINTS: OTP generation, verification, and token refresh must be accessible to anyone
                        auth.requestMatchers("/api/v1/auth/**").permitAll()
                                // All other API requests require a valid JWT
                                .anyRequest().authenticated()
                );

        // Insert the JWT filter before the standard Spring Security filter
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}