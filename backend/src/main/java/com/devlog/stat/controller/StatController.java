package com.devlog.stat.controller;

import com.devlog.stat.dto.response.StatsResponse;
import com.devlog.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/my")
    public ResponseEntity<StatsResponse> getMyStats(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(statService.getMyStats(userId));
    }
}
