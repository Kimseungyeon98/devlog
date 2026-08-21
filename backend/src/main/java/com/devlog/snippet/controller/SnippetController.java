package com.devlog.snippet.controller;

import com.devlog.snippet.dto.request.SnippetRequest;
import com.devlog.snippet.dto.response.SnippetResponse;
import com.devlog.snippet.dto.response.SnippetSummaryResponse;
import com.devlog.snippet.service.SnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/snippets")
public class SnippetController {

    private final SnippetService snippetService;

    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid SnippetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(snippetService.create(userId, request));
    }

    @GetMapping("/{snippetId}")
    public ResponseEntity<SnippetResponse> getSnippet(@PathVariable Long snippetId) {
        return ResponseEntity.ok(snippetService.getSnippet(snippetId));
    }

    @GetMapping
    public ResponseEntity<Page<SnippetSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String language,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(snippetService.search(keyword, language, pageable));
    }

    @PutMapping("/{snippetId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long snippetId,
            @RequestBody @Valid SnippetRequest request) {
        snippetService.update(userId, snippetId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{snippetId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long snippetId) {
        snippetService.delete(userId, snippetId);
        return ResponseEntity.noContent().build();
    }
}
