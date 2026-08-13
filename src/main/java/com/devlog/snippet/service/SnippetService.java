package com.devlog.snippet.service;

import com.devlog.snippet.domain.Snippet;
import com.devlog.snippet.dto.request.SnippetRequest;
import com.devlog.snippet.dto.response.SnippetResponse;
import com.devlog.snippet.dto.response.SnippetSummaryResponse;
import com.devlog.snippet.repository.SnippetRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnippetService {

    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long userId, SnippetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Snippet snippet = Snippet.create(user, request.title(), request.description(), request.code(), request.language());
        return snippetRepository.save(snippet).getId();
    }

    @Transactional
    public SnippetResponse getSnippet(Long snippetId) {
        Snippet snippet = findById(snippetId);
        snippet.incrementViewCount();
        return SnippetResponse.of(snippet);
    }

    public Page<SnippetSummaryResponse> search(String keyword, String language, Pageable pageable) {
        String kw = StringUtils.hasText(keyword) ? keyword : null;
        String lang = StringUtils.hasText(language) ? language : null;
        return snippetRepository.search(kw, lang, pageable).map(SnippetSummaryResponse::of);
    }

    @Transactional
    public void update(Long userId, Long snippetId, SnippetRequest request) {
        Snippet snippet = findById(snippetId);
        if (!snippet.isAuthor(userId)) throw new IllegalArgumentException("권한이 없습니다.");
        snippet.update(request.title(), request.description(), request.code(), request.language());
    }

    @Transactional
    public void delete(Long userId, Long snippetId) {
        Snippet snippet = findById(snippetId);
        if (!snippet.isAuthor(userId)) throw new IllegalArgumentException("권한이 없습니다.");
        snippetRepository.delete(snippet);
    }

    private Snippet findById(Long snippetId) {
        return snippetRepository.findById(snippetId)
                .orElseThrow(() -> new IllegalArgumentException("스니펫을 찾을 수 없습니다."));
    }
}
