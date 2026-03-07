package edu.cit.sabornido.rentease.service;

import edu.cit.sabornido.rentease.dto.auth.AuthResponse;
import edu.cit.sabornido.rentease.dto.auth.RegisterRequest;
import edu.cit.sabornido.rentease.entity.User;
import edu.cit.sabornido.rentease.exception.AppException;
import edu.cit.sabornido.rentease.repository.UserRepository;
import edu.cit.sabornido.rentease.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AppException("DB-002", "Duplicate entry", Map.of("email", "Email already registered"), HttpStatus.CONFLICT);
        }

        User user = User.builder()
            .email(req.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .firstname(req.getFirstname().trim())
            .lastname(req.getLastname().trim())
            .role(User.UserRole.valueOf(req.getRole().toUpperCase()))
            .build();

        user = userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return AuthResponse.builder()
            .user(new AuthResponse.UserInfo(user.getEmail(), user.getFirstname(), user.getLastname(), user.getRole().name()))
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase())
            .orElseThrow(() -> new AppException("AUTH-001", "Invalid credentials", "Email or password is incorrect", HttpStatus.UNAUTHORIZED));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AppException("AUTH-001", "Invalid credentials", "Email or password is incorrect", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return AuthResponse.builder()
            .user(new AuthResponse.UserInfo(user.getEmail(), user.getFirstname(), user.getLastname(), user.getRole().name()))
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
