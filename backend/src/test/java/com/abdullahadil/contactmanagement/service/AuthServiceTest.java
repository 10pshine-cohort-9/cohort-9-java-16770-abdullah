package com.abdullahadil.contactmanagement.service;

import com.abdullahadil.contactmanagement.dto.AuthResponse;
import com.abdullahadil.contactmanagement.dto.ChangePasswordRequest;
import com.abdullahadil.contactmanagement.dto.LoginRequest;
import com.abdullahadil.contactmanagement.dto.RegisterRequest;
import com.abdullahadil.contactmanagement.entity.User;
import com.abdullahadil.contactmanagement.exception.DuplicateResourceException;
import com.abdullahadil.contactmanagement.exception.InvalidCredentialsException;
import com.abdullahadil.contactmanagement.exception.ResourceNotFoundException;
import com.abdullahadil.contactmanagement.repository.UserRepository;
import com.abdullahadil.contactmanagement.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerSavesUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("new@example.com", null, "password123");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(1L)).thenReturn("token123");

        AuthResponse response = authService.register(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.token()).isEqualTo("token123");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("taken@example.com", null, "password123");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = User.builder().id(1L).email("user@example.com").passwordHash("hashed").build();
        LoginRequest request = new LoginRequest("user@example.com", "password123");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(1L)).thenReturn("token123");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token123");
        assertThat(response.userId()).isEqualTo(1L);
    }

    @Test
    void loginRejectsUnknownIdentifier() {
        LoginRequest request = new LoginRequest("nobody@example.com", "password123");
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = User.builder().id(1L).email("user@example.com").passwordHash("hashed").build();
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(anyLong());
    }

    @Test
    void getProfileReturnsUserDetails() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .phoneNumber("03001234567")
                .passwordHash("hashed")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var profile = authService.getProfile(1L);

        assertThat(profile.id()).isEqualTo(1L);
        assertThat(profile.email()).isEqualTo("user@example.com");
        assertThat(profile.phoneNumber()).isEqualTo("03001234567");
    }

    @Test
    void getProfileRejectsUnknownUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePasswordUpdatesHashWhenCurrentPasswordCorrect() {
        User user = User.builder().id(1L).email("user@example.com").passwordHash("old-hash").build();
        ChangePasswordRequest request = new ChangePasswordRequest("current-password", "new-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        authService.changePassword(1L, request);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        User user = User.builder().id(1L).email("user@example.com").passwordHash("old-hash").build();
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-current", "new-password");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-current", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getPasswordHash()).isEqualTo("old-hash");
    }

    @Test
    void changePasswordRejectsUnknownUser() {
        ChangePasswordRequest request = new ChangePasswordRequest("current-password", "new-password");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.changePassword(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
