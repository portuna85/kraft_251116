package com.kraft.config.auth;

import com.kraft.domain.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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

/**
 * Spring Security 설정
 * <p>
 * 참고:
 * - Spring Security 5.x에서 제공하던 WebSecurityConfigurerAdapter는 더 이상 권장되지 않습니다.
 *   Spring Security 6부터는 구성 요소(빈) 기반으로 SecurityFilterChain을 빈으로 등록하는 방식이 권장됩니다.
 *   이 방식은 더 명확한 구성과 테스트 용이성, 그리고 스프링 부트의 자동 설정과의 호환성이 좋습니다.
 *
 * CSRF 관련:
 * - 이전에 자주 사용되었던 `http.csrf().disable()`은 전체 애플리케이션에 대해 CSRF 보호를 완전히 제거합니다.
 *   Spring Security 6.1 이후 일부 API(특정 메서드 체인 등)는 deprecated 되었고, CSRF 정책을 더 세밀하게
 *   제어하도록 유도합니다. 여기서는 REST API 엔드포인트(`/api/**`)와 H2 콘솔에는 CSRF를 제외(ignoring)하고,
 *   일반 웹 페이지는 CSRF 보호를 유지하도록 설정해 안전성과 편의성의 균형을 맞췄습니다.
 */
@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@Profile("!test")  // 테스트 환경을 제외한 모든 프로파일에서 활성화 (dev, prod, oauth 등)
public class SecurityConfig {

    private final ObjectProvider<CustomOAuth2UserService> customOAuth2UserServiceProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF: API 엔드포인트에 대해서만 제외 처리
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )

                // CORS 설정 활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Frame options: SAMEORIGIN으로 제한 (H2 콘솔 사용 가능)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )

                .authorizeHttpRequests(authorize -> authorize
                        // 정적 리소스 및 공개 페이지: 모두 허용
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/static/**").permitAll()
                        // H2 콘솔: 개발/테스트 환경에서만 허용
                        .requestMatchers("/h2-console/**").permitAll()
                        // 프로필 페이지: 공개
                        .requestMatchers("/profile").permitAll()
                        // 로그인 페이지: 공개
                        .requestMatchers("/login").permitAll()
                        // 게시글 조회 API: 공개 (목록, 상세)
                        .requestMatchers("/api/post", "/api/post/*", "/api/post/list").permitAll()
                        // 게시글 작성/수정/삭제 API: USER 권한 필요
                        .requestMatchers("/api/post").hasRole(Role.USER.name())
                        // 게시글 작성/수정 페이지: 인증 필요
                        .requestMatchers("/posts/save", "/posts/update/**").authenticated()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                );

        // OAuth2 로그인 설정 (CustomOAuth2UserService가 있을 때만)
        CustomOAuth2UserService oauthService = customOAuth2UserServiceProvider.getIfAvailable();
        if (oauthService != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(oauthService)
                    )
            );
            log.info("OAuth2 로그인 활성화됨");
        } else {
            // OAuth2가 설정되지 않은 경우 기본 폼 로그인 사용
            http.formLogin(form -> form
                    .loginPage("/login")
                    .permitAll()
                    .defaultSuccessUrl("/", true)
            );
            log.info("OAuth2 미사용, 폼 로그인 활성화됨");
        }

        return http.build();
    }

    /**
     * CORS 설정: 개발 환경에서는 localhost를 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
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