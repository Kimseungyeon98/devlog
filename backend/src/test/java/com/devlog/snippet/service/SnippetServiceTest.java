package com.devlog.snippet.service;

import com.devlog.snippet.domain.Snippet;
import com.devlog.snippet.dto.request.SnippetRequest;
import com.devlog.snippet.dto.response.SnippetResponse;
import com.devlog.snippet.repository.SnippetRepository;
import com.devlog.user.domain.User;
import com.devlog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("SnippetService 단위 테스트")
class SnippetServiceTest {

    @Mock SnippetRepository snippetRepository;
    @Mock UserRepository userRepository;
    @InjectMocks SnippetService snippetService;

    User author;
    Snippet snippet;
    SnippetRequest request;

    @BeforeEach
    void setUp() {
        author = User.createLocalUser("user1", "user1@test.com", "hashed", "닉네임");
        ReflectionTestUtils.setField(author, "id", 1L);

        snippet = Snippet.create(author, "제목", "설명", "System.out.println()", "Java");
        ReflectionTestUtils.setField(snippet, "id", 10L);

        request = new SnippetRequest("제목", "설명", "System.out.println()", "Java");
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        @DisplayName("정상 등록 시 저장된 ID를 반환한다")
        void success() {
            given(userRepository.findById(1L)).willReturn(Optional.of(author));
            given(snippetRepository.save(any(Snippet.class))).willReturn(snippet);

            Long result = snippetService.create(1L, request);

            assertThat(result).isEqualTo(10L);
            then(snippetRepository).should().save(any(Snippet.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자면 예외를 던진다")
        void userNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> snippetService.create(99L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("사용자를 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("getSnippet")
    class GetSnippet {
        @Test
        @DisplayName("조회 시 viewCount가 증가하고 응답을 반환한다")
        void success() {
            given(snippetRepository.findById(10L)).willReturn(Optional.of(snippet));

            SnippetResponse response = snippetService.getSnippet(10L);

            assertThat(response.title()).isEqualTo("제목");
            assertThat(snippet.getViewCount()).isEqualTo(1L);
        }

        @Test
        @DisplayName("존재하지 않는 스니펫이면 예외를 던진다")
        void notFound() {
            given(snippetRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> snippetService.getSnippet(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("스니펫을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("작성자가 수정하면 성공한다")
        void success() {
            given(snippetRepository.findById(10L)).willReturn(Optional.of(snippet));
            SnippetRequest updateReq = new SnippetRequest("수정제목", "수정설명", "new code", "Python");

            snippetService.update(1L, 10L, updateReq);

            assertThat(snippet.getTitle()).isEqualTo("수정제목");
            assertThat(snippet.getLanguage()).isEqualTo("Python");
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void notAuthor() {
            given(snippetRepository.findById(10L)).willReturn(Optional.of(snippet));

            assertThatThrownBy(() -> snippetService.update(2L, 10L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("권한이 없습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 스니펫이면 예외를 던진다")
        void notFound() {
            given(snippetRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> snippetService.update(1L, 99L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("스니펫을 찾을 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        @DisplayName("작성자가 삭제하면 성공한다")
        void success() {
            given(snippetRepository.findById(10L)).willReturn(Optional.of(snippet));

            snippetService.delete(1L, 10L);

            then(snippetRepository).should().delete(snippet);
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void notAuthor() {
            given(snippetRepository.findById(10L)).willReturn(Optional.of(snippet));

            assertThatThrownBy(() -> snippetService.delete(2L, 10L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("권한이 없습니다.");
        }
    }

    @Nested
    @DisplayName("search")
    class Search {
        @Test
        @DisplayName("키워드와 언어로 검색하면 결과를 반환한다")
        void success() {
            PageRequest pageable = PageRequest.of(0, 12);
            given(snippetRepository.search("제목", "Java", pageable))
                    .willReturn(new PageImpl<>(List.of(snippet)));

            Page<?> result = snippetService.search("제목", "Java", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 키워드는 null로 변환하여 검색한다")
        void blankKeywordBecomesNull() {
            PageRequest pageable = PageRequest.of(0, 12);
            given(snippetRepository.search(null, null, pageable))
                    .willReturn(new PageImpl<>(List.of(snippet)));

            Page<?> result = snippetService.search("", "", pageable);

            assertThat(result).isNotNull();
            then(snippetRepository).should().search(null, null, pageable);
        }
    }
}
