package com.example.saml_demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/home", "/error").permitAll()
                // SAML protocol endpoints that must be public
                .requestMatchers("/saml2/service-provider-metadata/**").permitAll()
                .requestMatchers("/saml2/authenticate/**").permitAll()
                .requestMatchers("/login/saml2/sso/**").permitAll()
                .requestMatchers("/logout/saml2/**").permitAll()
                // User Management API endpoints - allow public access for now
                .requestMatchers("/api/users/**").permitAll()
                // SAML diagnostics endpoints - require authentication
                .requestMatchers("/saml/registrations").authenticated()
                .requestMatchers("/saml/metadata/**").authenticated()
                // User and attribute endpoints - require authentication
                .requestMatchers("/user").authenticated()
                .requestMatchers("/keycloak/**").authenticated()
                .anyRequest().authenticated()
            )
            .saml2Login(withDefaults())
            .saml2Logout(withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/saml2/service-provider-metadata/**"),
                    new AntPathRequestMatcher("/login/saml2/sso/**"),
                    new AntPathRequestMatcher("/logout/saml2/**"),
                    new AntPathRequestMatcher("/api/users/**")
                )
            );

        return http.build();
    }
}
