package com.kraft.config.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import jakarta.annotation.PostConstruct;

@Configuration
@Profile("oauth")
public class OAuthClientDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientDiagnostics.class);
    private final ClientRegistrationRepository repository;

    public OAuthClientDiagnostics(ClientRegistrationRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void logClientConfigs() {
        try {
            log.info("[OAuth Diagnostics] Checking client registrations...");
            // attempt to find google/naver but avoid failing if not present
            ClientRegistration google = safeGet("google");
            if (google != null) {
                log.info("[OAuth Diagnostics] google: clientId={}, redirectUri={}, authMethod={}, scopes={}",
                        mask(google.getClientId()), google.getRedirectUri(),
                        google.getClientAuthenticationMethod().getValue(), google.getScopes());
            } else {
                log.warn("[OAuth Diagnostics] google registration NOT FOUND. Check environment variables and active profiles.");
            }

            ClientRegistration naver = safeGet("naver");
            if (naver != null) {
                log.info("[OAuth Diagnostics] naver: clientId={}, redirectUri={}, authMethod={}, scopes={}",
                        mask(naver.getClientId()), naver.getRedirectUri(),
                        naver.getClientAuthenticationMethod().getValue(), naver.getScopes());
            } else {
                log.warn("[OAuth Diagnostics] naver registration NOT FOUND. Check environment variables and active profiles.");
            }
        } catch (Exception e) {
            log.warn("[OAuth Diagnostics] Could not log client registrations: {}", e.getMessage());
        }
    }

    private ClientRegistration safeGet(String id) {
        try {
            return get(id);
        } catch (Exception ex) {
            return null;
        }
    }

    private ClientRegistration get(String id) {
        if (repository instanceof org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository in) {
            for (ClientRegistration cr : in) {
                if (cr.getRegistrationId().equals(id)) return cr;
            }
        }
        return null;
    }

    private String mask(String s) {
        if (s == null) return null;
        int n = Math.min(4, s.length());
        return "*".repeat(Math.max(0, s.length() - n)) + s.substring(s.length() - n);
    }
}
