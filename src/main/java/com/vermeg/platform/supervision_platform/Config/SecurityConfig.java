package com.vermeg.platform.supervision_platform.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Désactiver CSRF (API REST)
                .csrf(csrf -> csrf.disable())

                // 🔐 Sécurité des endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )

                // 🔑 Basic Auth (temporaire)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}