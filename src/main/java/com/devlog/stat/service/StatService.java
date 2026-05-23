package com.devlog.stat.service;

import com.devlog.post.domain.Post;
import com.devlog.post.domain.PostStatus;
import com.devlog.post.repository.PostRepository;
import com.devlog.stat.dto.response.PostStatResponse;
import com.devlog.stat.dto.response.StatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatService {

    private final PostRepository postRepository;

    public StatsResponse getMyStats(Long userId) {
        List<Post> posts = postRepository.findByAuthorIdWithAuthor(userId);

        List<PostStatResponse> publishedStats = posts.stream()
                .filter(p -> p.getStatus() == PostStatus.PUBLISHED)
                .map(p -> new PostStatResponse(p.getId(), p.getTitle(), p.getViewCount(), p.getCreatedAt()))
                .sorted(Comparator.comparingLong(PostStatResponse::viewCount).reversed())
                .toList();

        long totalViews = publishedStats.stream()
                .mapToLong(PostStatResponse::viewCount)
                .sum();

        return new StatsResponse(publishedStats.size(), totalViews, publishedStats);
    }
}
