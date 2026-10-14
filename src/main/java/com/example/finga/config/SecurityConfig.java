/**
package com.example.finga.config;
import org.springframework.context.annotation.*; import org.springframework.security.config.annotation.web.builders.HttpSecurity; import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; import org.springframework.security.config.http.SessionCreationPolicy; import org.springframework.security.web.SecurityFilterChain;
@Configuration @EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)throws Exception{
        http.csrf(c->c.disable())
                .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a->a.requestMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/**","/error")
                        .permitAll().anyRequest().authenticated()).httpBasic(b->{});
        return http.build();
    }
}
**/