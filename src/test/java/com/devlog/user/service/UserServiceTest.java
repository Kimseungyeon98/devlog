package com.devlog.user.service;

import com.devlog.user.domain.Follow;
import com.devlog.user.domain.User;
import com.devlog.user.dto.request.SignUpRequest;
import com.devlog.user.repository.FollowRepository;
import com.devlog.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock FollowRepository followRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    User userA;
    User userB;

    @BeforeEach
    void setUp() {
        userA = User.createLocalUser("userA", "a@test.com", "hashed", "A닉네임");
        ReflectionTestUtils.setField(userA, "id", 1L);

        userB = User.createLocalUser("userB", "b@test.com", "hashed", "B닉네임");
        ReflectionTestUtils.setField(userB, "id", 2L);
    }

    @Nested
    @DisplayName("signUp")
    class SignUp {
        @Test
        @DisplayName("정상 회원가입 시 저장된 ID를 반환한다")
        void success() {
            given(userRepository.existsByEmail("new@test.com")).willReturn(false);
            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(passwordEncoder.encode("password")).willReturn("hashed");
            User newUser = User.createLocalUser("newuser", "new@test.com", "hashed", "신규");
            ReflectionTestUtils.setField(newUser, "id", 99L);
            given(userRepository.save(any(User.class))).willReturn(newUser);

            userService.signUp(new SignUpRequest("newuser", "new@test.com", "password", "신규"));

            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("이미 사용 중인 이메일이면 예외를 던진다")
        void duplicateEmail() {
            given(userRepository.existsByEmail("a@test.com")).willReturn(true);

            assertThatThrownBy(() -> userService.signUp(
                    new SignUpRequest("userA", "a@test.com", "pw", "A")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 이메일입니다.");
        }

        @Test
        @DisplayName("이미 사용 중인 아이디면 예외를 던진다")
        void duplicateUsername() {
            given(userRepository.existsByEmail("new@test.com")).willReturn(false);
            given(userRepository.existsByUsername("userA")).willReturn(true);

            assertThatThrownBy(() -> userService.signUp(
                    new SignUpRequest("userA", "new@test.com", "pw", "A")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이미 사용 중인 아이디입니다.");
        }
    }

    @Nested
    @DisplayName("follow")
    class FollowTest {
        @Test
        @DisplayName("정상 팔로우 시 Follow 엔티티가 저장된다")
        void success() {
            given(userRepository.findById(1L)).willReturn(Optional.of(userA));
            given(userRepository.findById(2L)).willReturn(Optional.of(userB));
            given(followRepository.existsByFollowerAndFollowing(userA, userB)).willReturn(false);

            userService.follow(1L, 2L);

            then(followRepository).should().save(any(Follow.class));
        }

        @Test
        @DisplayName("자기 자신을 팔로우하면 예외를 던진다")
        void selfFollow() {
            assertThatThrownBy(() -> userService.follow(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("자기 자신을 팔로우할 수 없습니다.");
        }

        @Test
        @DisplayName("이미 팔로우 중이면 예외를 던진다")
        void alreadyFollowing() {
            given(userRepository.findById(1L)).willReturn(Optional.of(userA));
            given(userRepository.findById(2L)).willReturn(Optional.of(userB));
            given(followRepository.existsByFollowerAndFollowing(userA, userB)).willReturn(true);

            assertThatThrownBy(() -> userService.follow(1L, 2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 팔로우하고 있습니다.");
        }
    }

    @Nested
    @DisplayName("unfollow")
    class UnfollowTest {
        @Test
        @DisplayName("언팔로우 시 Follow 레코드가 삭제된다")
        void success() {
            given(userRepository.findById(1L)).willReturn(Optional.of(userA));
            given(userRepository.findById(2L)).willReturn(Optional.of(userB));

            userService.unfollow(1L, 2L);

            then(followRepository).should().deleteByFollowerAndFollowing(userA, userB);
        }
    }
}
