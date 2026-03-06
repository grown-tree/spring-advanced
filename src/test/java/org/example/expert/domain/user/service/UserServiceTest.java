package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // ========== getUser() ==========

    @Test
    @DisplayName("존재하지 않는 userId로 조회하면 예외가 발생한다")
    void 존재하지_않는_userId로_조회하면_예외가_발생한다() {
        // given
        long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.getUser(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("유저 조회가 정상적으로 동작한다")
    void 유저_조회가_정상적으로_동작한다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(userId);

        // then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@test.com", response.getEmail());
    }

    // ========== changePassword() ==========

    @Test
    @DisplayName("현재 비밀번호가 틀리면 예외가 발생한다")
    void 현재_비밀번호가_틀리면_예외가_발생한다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserChangePasswordRequest request = new UserChangePasswordRequest("wrongOld1!", "NewPass1!");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongOld1!", "encodedPassword")).willReturn(false);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("새 비밀번호가 현재 비밀번호와 같으면 예외가 발생한다")
    void 새_비밀번호가_현재_비밀번호와_같으면_예외가_발생한다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass1!", "OldPass1!");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 현재 비밀번호 일치 → true (1번 if 통과)
        given(passwordEncoder.matches("OldPass1!", "encodedPassword")).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> userService.changePassword(userId, request));
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("비밀번호가 정상적으로 변경된다")
    void 비밀번호가_정상적으로_변경된다() {
        // given
        long userId = 1L;
        User user = new User("test@test.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPass1!", "NewPass1!");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // 현재 비밀번호 일치 확인 → true (1번 if 통과)
        given(passwordEncoder.matches("OldPass1!", "encodedPassword")).willReturn(true);
        // 새 비밀번호 중복 확인 → false (2번 if 통과)
        given(passwordEncoder.matches("NewPass1!", "encodedPassword")).willReturn(false);
        given(passwordEncoder.encode("NewPass1!")).willReturn("newEncodedPassword");

        // when
        userService.changePassword(userId, request);

        // then
        verify(passwordEncoder, times(1)).encode("NewPass1!");
        assertEquals("newEncodedPassword", user.getPassword());
    }
}
