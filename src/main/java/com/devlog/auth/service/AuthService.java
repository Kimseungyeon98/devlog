package com.devlog.auth.service;

import com.devlog.auth.dto.request.LoginRequest;
import com.devlog.auth.dto.response.TokenResponse;
import com.devlog.jwt.JwtProvider;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return issueTokens(user);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.isValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        Long userId = jwtProvider.getUserId(refreshToken);
        String savedToken = redisTemplate.opsForValue().get("refresh:" + userId);

        if (!refreshToken.equals(savedToken)) {
            throw new IllegalArgumentException("이미 사용된 Refresh Token입니다.");
        }

        User user = userRepository.findById(userId).orElseThrow();
        return issueTokens(user); // Refresh Token Rotation
    }

    public void logout(Long userId, String accessToken) {
        // Access Token 블랙리스트 등록 (남은 TTL만큼만 유지)
        Claims claims = jwtProvider.parseToken(accessToken);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "logout",
                        Duration.ofMillis(remaining));

        // Refresh Token 삭제
        redisTemplate.delete("refresh:" + userId);
    }

    private TokenResponse issueTokens(User user) {
        String accessToken  = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // Refresh Token Redis 저장 (Rotation 방식)
        redisTemplate.opsForValue()
                .set("refresh:" + user.getId(), refreshToken,
                        Duration.ofMillis(refreshTokenExpiry));

        return new TokenResponse(accessToken, refreshToken);
    }
}