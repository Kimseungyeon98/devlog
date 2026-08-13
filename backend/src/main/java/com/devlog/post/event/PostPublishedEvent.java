package com.devlog.post.event;

public record PostPublishedEvent(Long postId, Long authorId, String postTitle) {}
