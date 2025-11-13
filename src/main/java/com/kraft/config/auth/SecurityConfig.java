package com.kraft.config.auth;

import com.kraft.domain.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@Profile("oauth")
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: API 엔드포인트에 대해서만 비활성화 (RESTful API는 stateless이므로)
                // 웹 페이지는 CSRF 보호 유지
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )

                // CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Frame options: SAMEORIGIN으로 제한 (H2 콘솔 사용 가능하되 보안 강화)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 및 공개 페이지: 모두 허용
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/static/**").permitAll()
                        // H2 콘솔: 개발/테스트 환경에서만 허용 (프로덕션에서는 제거 권장)
                        .requestMatchers("/h2-console/**").permitAll()
                        // 프로필 페이지: 인증 필요
                        .requestMatchers("/profile").permitAll()
                        // API 엔드포인트: USER 권한 필요
                        .requestMatchers("/api/**").hasRole(Role.USER.name())
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }

    /**
     * CORS 설정: 외부 도메인에서 API 호출 시 정책 정의
     * 프로덕션에서는 allowedOrigins를 특정 도메인으로 제한하세요
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 개발 환경: localhost 허용
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 자격 증명(쿠키 등) 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    /**
     * 프로덕션 환경용 CORS 설정: 더 엄격한 정책
     */
    @Bean
    @Profile("prod")
    public CorsConfigurationSource prodCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프로덕션: 실제 도메인으로 제한 (예시)
        configuration.setAllowedOrigins(List.of("https://yourdomain.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        log.info("Production CORS configuration loaded for: {}", configuration.getAllowedOrigins());

        return source;
    }
}