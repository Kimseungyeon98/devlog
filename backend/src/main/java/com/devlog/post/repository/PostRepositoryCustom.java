package com.devlog.post.repository;

import com.devlog.post.domain.Post;
import com.devlog.post.dto.request.PostSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    Page<Post> search(PostSearchRequest request, Pageable pageable);
}
