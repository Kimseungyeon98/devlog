package com.devlog.post.service;

import com.devlog.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostRepository postRepository;

    private static final String VIEW_COUNT_PATTERN = "views:*";
    private static final String VIEW_COUNT_KEY = "views:";

    // 5분마다 실행
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void syncViewCount() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_PATTERN);
        if (keys == null || keys.isEmpty()) return;

        log.info("조회수 동기화 시작 - {} 건", keys.size());

        for (String key : keys) {
            try {
                Long postId = Long.parseLong(key.replace(VIEW_COUNT_KEY, ""));
                String value = redisTemplate.opsForValue().get(key);
                if (value == null) continue;

                Long viewCount = Long.parseLong(value);

                postRepository.findById(postId).ifPresent(post -> {
                    post.updateViewCount(viewCount);
                });

            } catch (Exception e) {
                log.error("조회수 동기화 실패 - key: {}", key, e);
            }
        }

        log.info("조회수 동기화 완료");
    }
}