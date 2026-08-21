package com.devlog.post.repository;

import com.devlog.post.domain.*;
import com.devlog.post.dto.request.PostSearchRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QPost post = QPost.post;
    private final QPostTag postTag = QPostTag.postTag;
    private final QTag tag = QTag.tag;

    @Override
    public Page<Post> search(PostSearchRequest request, Pageable pageable) {
        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.author).fetchJoin()
                .leftJoin(post.postTags, postTag).fetchJoin()
                .leftJoin(postTag.tag, tag).fetchJoin()
                .where(
                        statusPublished(),
                        keywordSearch(request.keyword()),
                        tagNameEq(request.tag())
                )
                .distinct()
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.countDistinct())
                .from(post)
                .leftJoin(post.postTags, postTag)
                .leftJoin(postTag.tag, tag)
                .where(
                        statusPublished(),
                        keywordSearch(request.keyword()),
                        tagNameEq(request.tag())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression statusPublished() {
        return post.status.eq(PostStatus.PUBLISHED);
    }

    private BooleanExpression keywordSearch(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;
        return post.title.containsIgnoreCase(keyword)
                .or(post.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression tagNameEq(String tagName) {
        return StringUtils.hasText(tagName) ? tag.name.eq(tagName.toLowerCase()) : null;
    }
}
