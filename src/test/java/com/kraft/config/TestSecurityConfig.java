package com.kraft.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 테스트 환경용 Security 설정
 * - OAuth2 로그인 비활성화
 * - 모든 요청에 대해 인증 필요
 * - CSRF 비활성화 (테스트 편의)
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {});  // 테스트에서 @WithMockUser 사용 가능

        return http.build();
    }
}

