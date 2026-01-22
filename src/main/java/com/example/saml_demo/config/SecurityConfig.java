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
                .requestMatchers("/saml2/service-provider-metadata/**").permitAll()
                .requestMatchers("/saml/**").permitAll() // diagnostics and static saml paths
                .requestMatchers("/saml2/**").permitAll() // diagnostics and static saml paths
                .requestMatchers("/saml2/authenticate/**").permitAll()
                .anyRequest().authenticated()
            )
            .saml2Login(withDefaults())
            .saml2Logout(withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/saml2/**"),
                    new AntPathRequestMatcher("/logout/saml2/**")
                )
            );

        return http.build();
    }
}
