package com.kraft.config.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Configuration
public class RuntimeClientRegistrationConfig {

    private static final Logger log = LoggerFactory.getLogger(RuntimeClientRegistrationConfig.class);

    @Bean
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> regs = new ArrayList<>();

        String gId = System.getenv("GOOGLE_CLIENT_ID");
        String gSecret = System.getenv("GOOGLE_CLIENT_SECRET");

        log.debug("GOOGLE_CLIENT_ID from env: {}", gId != null && !gId.isBlank() ? "***SET***" : "NOT SET");
        log.debug("GOOGLE_CLIENT_SECRET from env: {}", gSecret != null && !gSecret.isBlank() ? "***SET***" : "NOT SET");

        if (gId != null && !gId.isBlank() && gSecret != null && !gSecret.isBlank()) {
            regs.add(googleRegistration(gId, gSecret));
            log.info("Google OAuth2 client registered");
        } else {
            log.warn("Google OAuth2 credentials not found in environment variables");
        }

        String nId = System.getenv("NAVER_CLIENT_ID");
        String nSecret = System.getenv("NAVER_CLIENT_SECRET");

        log.debug("NAVER_CLIENT_ID from env: {}", nId != null && !nId.isBlank() ? "***SET***" : "NOT SET");
        log.debug("NAVER_CLIENT_SECRET from env: {}", nSecret != null && !nSecret.isBlank() ? "***SET***" : "NOT SET");

        if (nId != null && !nId.isBlank() && nSecret != null && !nSecret.isBlank()) {
            regs.add(naverRegistration(nId, nSecret));
            log.info("Naver OAuth2 client registered");
        } else {
            log.warn("Naver OAuth2 credentials not found in environment variables");
        }

        if (regs.isEmpty()) {
            log.warn("No OAuth2 clients registered. Using NoopClientRegistrationRepository. OAuth login will not work.");
            log.warn("To enable OAuth login:");
            log.warn("  1. Set environment variables: GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, NAVER_CLIENT_ID, NAVER_CLIENT_SECRET");
            log.warn("  2. OR use IntelliJ EnvFile plugin to load .env file");
            log.warn("  3. OR add them to Run Configuration -> Environment variables");
            return new NoopClientRegistrationRepository();
        }

        log.info("Created ClientRegistrationRepository with {} client(s)", regs.size());
        return new InMemoryClientRegistrationRepository(regs);
    }

    private ClientRegistration googleRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/google")
                .scope("profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
    }

    private ClientRegistration naverRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("naver")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/naver")
                .scope("name", "email", "profile_image")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .clientName("Naver")
                .build();
    }

    // Defensive no-op implementation
    private static class NoopClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {
        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return null;
        }

        @Override
        @NonNull
        public Iterator<ClientRegistration> iterator() {
            return Collections.emptyIterator();
        }
    }
}
