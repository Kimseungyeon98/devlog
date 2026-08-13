package com.devlog.auth.oauth2;

import java.util.Map;

public class GithubUserInfo {

    private final Map<String, Object> attributes;

    public GithubUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    public String getEmail() {
        String email = (String) attributes.get("email");
        // 이메일 비공개 설정 시 GitHub 기본 이메일로 대체
        if (email == null) {
            email = getLogin() + "@users.noreply.github.com";
        }
        return email;
    }

    public String getName() {
        // name 없으면 login(아이디)으로 대체
        String name = (String) attributes.get("name");
        return name != null ? name : (String) attributes.get("login");
    }

    public String getLogin() {
        return (String) attributes.get("login");
    }
}