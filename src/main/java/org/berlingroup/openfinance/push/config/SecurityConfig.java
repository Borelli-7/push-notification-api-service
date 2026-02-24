package org.berlingroup.openfinance.push.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Resource Status Notification service.
 * <p>
 * As per the Berlin Group openFinance spec, the POST /Client-Notification-URL endpoint
 * supports both unauthenticated access ({@code - {}}) and OAuth2 Bearer Token.
 * For dev/test, all requests are permitted. In production, configure the OAuth2 
 * resource server JWT validation via application properties.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        // H2 console for dev
                        .requestMatchers("/h2-console/**").permitAll()
                        // The notification endpoint — permits all per spec (security is optional)
                        .requestMatchers("/Client-Notification-URL").permitAll()
                        .anyRequest().authenticated()
                )
                // Allow H2 console frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
