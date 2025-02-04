package org.example.marrakech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(auth -> auth
            .anyRequest().authenticated() // Все запросы требуют аутентификации
        )
        .formLogin(form -> form
            .defaultSuccessUrl("/home", true) // После входа перенаправляем на /home
        )
        .build();
  }
}
