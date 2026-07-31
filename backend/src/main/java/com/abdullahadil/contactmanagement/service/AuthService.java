package com.abdullahadil.contactmanagement.service;

import com.abdullahadil.contactmanagement.dto.AuthResponse;
import com.abdullahadil.contactmanagement.dto.LoginRequest;
import com.abdullahadil.contactmanagement.dto.RegisterRequest;
import com.abdullahadil.contactmanagement.entity.User;
import com.abdullahadil.contactmanagement.exception.DuplicateResourceException;
import com.abdullahadil.contactmanagement.exception.InvalidCredentialsException;
import com.abdullahadil.contactmanagement.repository.UserRepository;
import com.abdullahadil.contactmanagement.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.email() != null && userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        if (request.phoneNumber() != null && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateResourceException("An account with this phone number already exists");
        }

        User user = User.builder()
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build();
        user = userRepository.save(user);

        log.info("Registered new user id={}", user.getId());
        return new AuthResponse(jwtService.generateToken(user.getId()), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        User user = findByIdentifier(request.identifier())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        log.info("User id={} logged in", user.getId());
        return new AuthResponse(jwtService.generateToken(user.getId()), user.getId());
    }

    private Optional<User> findByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier));
    }
}
