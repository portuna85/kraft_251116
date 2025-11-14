package com.kraft.config.auth;

import com.kraft.config.auth.dto.OAuthAttributes;
import com.kraft.config.auth.dto.SessionUser;
import com.kraft.domain.user.User;
import com.kraft.domain.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * OAuth2 사용자 정보를 처리하는 서비스
 * - Google, Naver 등의 OAuth2 Provider로부터 사용자 정보를 로드
 * - 사용자 정보를 DB에 저장/업데이트
 * - 세션에 사용자 정보 저장
 *
 * @Profile("!test"): 테스트 환경을 제외한 모든 프로파일에서 활성화
 * - OAuth 클라이언트가 설정된 경우에만 실제로 사용됨
 */
@RequiredArgsConstructor
@Service
@Profile("!test")
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    /**
     * HttpSession은 request-scoped 빈이므로 ObjectProvider를 통해 지연 주입
     */
    private final ObjectProvider<HttpSession> httpSessionProvider;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(
                registrationId,
                userNameAttributeName,
                oAuth2User.getAttributes()
        );

        User user = saveOrUpdate(attributes);

        // ObjectProvider를 통해 현재 요청의 HttpSession 획득
        HttpSession httpSession = httpSessionProvider.getObject();
        if (httpSession != null) {
            httpSession.setAttribute("user", new SessionUser(user));
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    /**
     * 사용자 정보를 DB에 저장하거나 업데이트
     * @param attributes OAuth2 Provider로부터 받은 사용자 속성
     * @return 저장/업데이트된 User 엔티티
     */
    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }
}