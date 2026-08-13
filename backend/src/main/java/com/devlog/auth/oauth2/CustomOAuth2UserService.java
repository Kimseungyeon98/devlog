package com.devlog.auth.oauth2;

import com.devlog.user.domain.User;
import com.devlog.user.domain.UserAuthProvider;
import com.devlog.user.repository.UserAuthProviderRepository;
import com.devlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        GithubUserInfo userInfo = new GithubUserInfo(oAuth2User.getAttributes());

        String provider = "GITHUB";
        String providerUserId = userInfo.getId();

        // 기존 소셜 연동 계정 조회
        Optional<UserAuthProvider> existingAuth =
                userAuthProviderRepository.findByProviderAndProviderUserId(provider, providerUserId);

        User user;
        if (existingAuth.isPresent()) {
            // 기존 유저 → 로그인
            user = existingAuth.get().getUser();
        } else {
            // 신규 유저 → 회원가입
            String email = userInfo.getEmail() != null
                    ? userInfo.getEmail()
                    : userInfo.getLogin() + "@users.noreply.github.com";
            String username = generateUsername(userInfo.getLogin());
            String nickname = userInfo.getName();

            // 이메일로 기존 계정 있으면 연동, 없으면 신규 생성
            user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(
                            User.createOAuthUser(username, email, nickname)
                    ));

            userAuthProviderRepository.save(
                    UserAuthProvider.of(user, provider, providerUserId)
            );
        }

        String role = user.getRole().name();
        return new OAuth2UserAdapter(user, oAuth2User.getAttributes(), role);
    }

    // username 중복 방지
    private String generateUsername(String login) {
        String base = login.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String username = base;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + "_" + suffix++;
        }
        return username;
    }
}