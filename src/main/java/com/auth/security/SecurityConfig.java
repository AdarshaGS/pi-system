package com.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.Arrays;
import com.audit.filter.RequestAuditFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final CustomUserDetailsService userDetailsService;
        private final JwtAuthenticationEntryPoint authEntryPoint;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;

        @Autowired(required = false)
        private ClientRegistrationRepository clientRegistrationRepository;

        @Autowired(required = false)
        private RequestAuditFilter requestAuditFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService,
                        JwtAuthenticationEntryPoint authEntryPoint,
                        OAuth2SuccessHandler oAuth2SuccessHandler) {
                this.jwtAuthFilter = jwtAuthFilter;
                this.userDetailsService = userDetailsService;
                this.authEntryPoint = authEntryPoint;
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/configuration/ui",
                                                                "/configuration/security",
                                                                "/actuator/**",
                                                                "/",
                                                                "/index.html",
                                                                "/static/**",
                                                                "/api/v1/test-runner/**",
                                                                "/reports/**")

                                                .permitAll()
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**").permitAll()
                                                .requestMatchers("/api/health/**").permitAll()
                                                .requestMatchers("/api/v1/dev/**").permitAll() // Keep dev tools open
                                                .requestMatchers("/open/dev/**").permitAll() // Migration generator APIs
                                                                                               // for now or secure them
                                                .requestMatchers("/api/v1/user/**").hasRole("USER_READ_ONLY")
                                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/v1/super-admin/**").hasRole("SUPER_ADMIN")
                                                .requestMatchers("/api/v1/**").authenticated()
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authenticationProvider(authenticationProvider())
                                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authEntryPoint));

                // Only configure OAuth2 if ClientRegistrationRepository is available
                if (clientRegistrationRepository != null) {
                        http.oauth2Login(oauth2 -> oauth2
                                        .successHandler(oAuth2SuccessHandler));
                }

                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                if (requestAuditFilter != null) {
                        http.addFilterAfter(requestAuditFilter, JwtAuthenticationFilter.class);
                }

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOriginPatterns(
                                Arrays.asList("http://localhost:*", "http://127.0.0.1:*", "http://[::1]:*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
