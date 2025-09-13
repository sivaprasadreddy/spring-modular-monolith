package com.sivalabs.bookstore.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    @Order(2)
    SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        String[] publicPaths = {
            "/",
            "/favicon.ico",
            "/actuator/**",
            "/error",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/assets/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/login",
            "/registration",
            "/registration-success"
        };
        http.securityMatcher("/**");

        http.authorizeHttpRequests(r -> r.requestMatchers(publicPaths)
                .permitAll()
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/products")
                .permitAll()
                .requestMatchers("/buy", "/cart", "/update-cart")
                .permitAll()
                .anyRequest()
                .authenticated());

        http.formLogin(formLogin -> formLogin.loginPage("/login").permitAll().defaultSuccessUrl("/", true));

        http.logout(logout -> logout.logoutRequestMatcher(
                        PathPatternRequestMatcher.withDefaults().matcher("/logout"))
                .logoutSuccessUrl("/")
                .permitAll());

        return http.build();
    }
}
