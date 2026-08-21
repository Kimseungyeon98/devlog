package com.devlog.series.controller;

import com.devlog.series.dto.request.AddPostToSeriesRequest;
import com.devlog.series.dto.request.CreateSeriesRequest;
import com.devlog.series.dto.request.UpdateSeriesRequest;
import com.devlog.series.dto.response.SeriesResponse;
import com.devlog.series.dto.response.SeriesSummaryResponse;
import com.devlog.series.service.SeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateSeriesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seriesService.create(userId, request));
    }

    @PutMapping("/{seriesId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long seriesId,
            @RequestBody @Valid UpdateSeriesRequest request) {
        seriesService.update(userId, seriesId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{seriesId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long seriesId) {
        seriesService.delete(userId, seriesId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{seriesId}")
    public ResponseEntity<SeriesResponse> getDetail(@PathVariable Long seriesId) {
        return ResponseEntity.ok(seriesService.getSeriesDetail(seriesId));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<SeriesSummaryResponse>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(seriesService.getSeriesByUser(username));
    }

    @PostMapping("/{seriesId}/posts")
    public ResponseEntity<Void> addPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long seriesId,
            @RequestBody @Valid AddPostToSeriesRequest request) {
        seriesService.addPost(userId, seriesId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{seriesId}/posts/{postId}")
    public ResponseEntity<Void> removePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long seriesId,
            @PathVariable Long postId) {
        seriesService.removePost(userId, seriesId, postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<SeriesResponse> getByPost(@PathVariable Long postId) {
        return seriesService.getSeriesByPost(postId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
