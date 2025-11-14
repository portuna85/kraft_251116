package com.kraft.web;

import com.kraft.config.auth.LoginUser;
import com.kraft.config.auth.dto.SessionUser;
import com.kraft.service.PostService;
import com.kraft.web.dto.PostResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final PostService postsService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @GetMapping("/")
    public String index(Model model, @LoginUser SessionUser user) {
        model.addAttribute("posts", postsService.findAllDesc());
        if (user != null) {
            model.addAttribute("userName", user.getName());
            model.addAttribute("userPicture", user.getPicture());
        }

        // OAuth2 활성화 여부 전달
        boolean oauthEnabled = isOAuth2Enabled();
        model.addAttribute("oauthEnabled", oauthEnabled);

        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model) {
        PostResponseDto dto = postsService.findById(id);
        model.addAttribute("post", dto);

        return "posts-update";
    }

    /**
     * OAuth2 클라이언트가 실제로 등록되어 있는지 확인
     */
    private boolean isOAuth2Enabled() {
        ClientRegistrationRepository repo = clientRegistrationRepositoryProvider.getIfAvailable();
        if (repo == null) {
            return false;
        }

        // Iterable 구현체인 경우
        if (repo instanceof Iterable<?>) {
            try {
                return ((Iterable<?>) repo).iterator().hasNext();
            } catch (Exception e) {
                // NoopClientRegistrationRepository 등에서 예외 발생 가능
                return false;
            }
        }

        // 잘 알려진 registrationId 확인
        try {
            return repo.findByRegistrationId("google") != null
                || repo.findByRegistrationId("naver") != null;
        } catch (Exception e) {
            return false;
        }
    }
}
