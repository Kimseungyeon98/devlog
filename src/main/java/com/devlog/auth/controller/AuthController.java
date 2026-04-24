package com.devlog.auth.controller;

import com.devlog.auth.dto.request.LoginRequest;
import com.devlog.auth.dto.response.TokenResponse;
import com.devlog.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestHeader("Refresh-Token") String token) {
        return ResponseEntity.ok(authService.reissue(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId,
                                       @RequestHeader("Authorization") String bearer) {
        authService.logout(userId, bearer.substring(7));
        return ResponseEntity.noContent().build();
    }
}