package com.devlog.series.service;

import com.devlog.post.domain.Post;
import com.devlog.post.repository.PostRepository;
import com.devlog.series.domain.Series;
import com.devlog.series.domain.SeriesPost;
import com.devlog.series.dto.request.AddPostToSeriesRequest;
import com.devlog.series.dto.request.CreateSeriesRequest;
import com.devlog.series.dto.request.UpdateSeriesRequest;
import com.devlog.series.dto.response.SeriesResponse;
import com.devlog.series.dto.response.SeriesSummaryResponse;
import com.devlog.series.repository.SeriesPostRepository;
import com.devlog.series.repository.SeriesRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesPostRepository seriesPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long create(Long userId, CreateSeriesRequest request) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        return seriesRepository.save(Series.create(author, request.title(), request.description())).getId();
    }

    @Transactional
    public void update(Long userId, Long seriesId, UpdateSeriesRequest request) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new EntityNotFoundException("시리즈를 찾을 수 없습니다."));
        if (!series.isAuthor(userId)) throw new IllegalArgumentException("시리즈 수정 권한이 없습니다.");
        series.update(request.title(), request.description());
    }

    @Transactional
    public void delete(Long userId, Long seriesId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new EntityNotFoundException("시리즈를 찾을 수 없습니다."));
        if (!series.isAuthor(userId)) throw new IllegalArgumentException("시리즈 삭제 권한이 없습니다.");
        seriesRepository.delete(series);
    }

    public List<SeriesSummaryResponse> getSeriesByUser(String username) {
        return seriesRepository.findByAuthorUsernameWithAuthor(username)
                .stream()
                .map(SeriesSummaryResponse::of)
                .toList();
    }

    public SeriesResponse getSeriesDetail(Long seriesId) {
        Series series = seriesRepository.findByIdWithDetails(seriesId)
                .orElseThrow(() -> new EntityNotFoundException("시리즈를 찾을 수 없습니다."));
        return SeriesResponse.of(series);
    }

    @Transactional
    public void addPost(Long userId, Long seriesId, AddPostToSeriesRequest request) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new EntityNotFoundException("시리즈를 찾을 수 없습니다."));
        if (!series.isAuthor(userId)) throw new IllegalArgumentException("시리즈 수정 권한이 없습니다.");
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        int order = request.orderNum() > 0 ? request.orderNum() : series.getSeriesPosts().size() + 1;
        seriesPostRepository.save(SeriesPost.of(series, post, order));
    }

    @Transactional
    public void removePost(Long userId, Long seriesId, Long postId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new EntityNotFoundException("시리즈를 찾을 수 없습니다."));
        if (!series.isAuthor(userId)) throw new IllegalArgumentException("시리즈 수정 권한이 없습니다.");
        SeriesPost sp = seriesPostRepository.findBySeriesIdAndPostId(seriesId, postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 시리즈에서 찾을 수 없습니다."));
        seriesPostRepository.delete(sp);
    }

    public Optional<SeriesResponse> getSeriesByPost(Long postId) {
        return seriesPostRepository.findByPostIdWithSeries(postId)
                .map(sp -> SeriesResponse.of(sp.getSeries()));
    }
}
