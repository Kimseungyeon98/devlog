package com.devlog.auth.oauth2;

import com.devlog.user.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OAuth2UserAdapter implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;
    private final String role; // ← 추가

    public OAuth2UserAdapter(User user, Map<String, Object> attributes, String role) {
        this.user = user;
        this.attributes = attributes;
        this.role = role; // ← 추가
    }

    public User getUser() {
        return user;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role)); // ← role 필드 사용
    }

    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }
}