package com.kraft.config.auth;

import com.kraft.domain.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Iterator;
import java.util.List;

/**
 * Spring Security 설정
 *
 * OAuth2 로그인 활성화 시 실제로 등록된 ClientRegistration이 있는지 안전하게 검사하도록 개선했습니다.
 */
@Slf4j
@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@Profile("!test")
public class SecurityConfig {

    private final ObjectProvider<CustomOAuth2UserService> customOAuth2UserServiceProvider;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/static/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/profile").permitAll()
                        .requestMatchers("/login/oauth2/code/*", "/oauth2/**", "/oauth2/authorization/*").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/post", "/api/post/*", "/api/post/list").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/post").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.PUT, "/api/post/*").hasRole(Role.USER.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/post/*").hasRole(Role.USER.name())
                        .requestMatchers("/posts/save", "/posts/update/**").authenticated()
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                );

        // OAuth2 로그인 설정: CustomOAuth2UserService 존재 + ClientRegistrationRepository에 실제 등록된 클라이언트가 있을 때만 활성화
        CustomOAuth2UserService oauthService = customOAuth2UserServiceProvider.getIfAvailable();
        ClientRegistrationRepository clientRegRepo = clientRegistrationRepositoryProvider.getIfAvailable();

        boolean hasClientRegistrations = false;
        if (clientRegRepo != null) {
            // 1) Iterable 지원 구현체 (InMemoryClientRegistrationRepository 등)
            if (clientRegRepo instanceof Iterable<?>) {
                try {
                    Iterator<?> it = ((Iterable<?>) clientRegRepo).iterator();
                    hasClientRegistrations = it != null && it.hasNext();
                } catch (Exception ex) {
                    log.debug("Error iterating ClientRegistrationRepository: {}", ex.getMessage());
                }
            }

            // 2) Iterable이 아니면, 잘 알려진 registrationId를 직접 조회해보는 폴백
            if (!hasClientRegistrations) {
                try {
                    // 안전하게 google/naver 같은 등록 아이디가 존재하는지 확인
                    ClientRegistration g = clientRegRepo.findByRegistrationId("google");
                    ClientRegistration n = clientRegRepo.findByRegistrationId("naver");
                    hasClientRegistrations = (g != null) || (n != null);
                } catch (Exception ex) {
                    log.debug("ClientRegistrationRepository.findByRegistrationId check failed: {}", ex.getMessage());
                }
            }
        }

        if (oauthService != null && hasClientRegistrations) {
            http.oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(oauthService)
                    )
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/?error=oauth")
            );
            log.info("OAuth2 로그인 활성화됨 (등록된 클라이언트 존재)");
        } else {
            http.formLogin(form -> form
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/?error=login")
                    .permitAll()
            );
            if (oauthService == null) {
                log.info("CustomOAuth2UserService bean 없음. 폼 로그인 활성화됨");
            } else {
                log.info("OAuth 클라이언트가 등록되지 않음. 폼 로그인 활성화됨");
            }
        }

        return http.build();
    }

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