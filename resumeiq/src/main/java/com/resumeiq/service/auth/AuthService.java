package com.resumeiq.service.auth;

import com.resumeiq.dto.request.LoginRequest;
import com.resumeiq.dto.request.RefreshTokenRequest;
import com.resumeiq.dto.request.RegisterRequest;
import com.resumeiq.dto.response.AuthResponse;
import com.resumeiq.dto.response.UserResponse;
import com.resumeiq.entity.RefreshToken;
import com.resumeiq.entity.User;
import com.resumeiq.exception.EmailAlreadyExistsException;
import com.resumeiq.exception.InvalidCredentialsException;
import com.resumeiq.exception.InvalidTokenException;
import com.resumeiq.exception.ResourceNotFoundException;
import com.resumeiq.repository.RefreshTokenRepository;
import com.resumeiq.repository.UserRepository;
import com.resumeiq.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-token-expiry-days}")
    private int refreshTokenExpiryDays;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        userRepository.save(user);

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (stored.getUsed()) {
            refreshTokenRepository.deleteAllByUserId(stored.getUser().getId());
            throw new InvalidTokenException(
                    "Refresh token already used. All sessions have been invalidated — please log in again.");
        }

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired. Please log in again.");
        }

        stored.setUsed(true);
        refreshTokenRepository.save(stored);

        return issueTokenPair(stored.getUser());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> {
                    token.setUsed(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private AuthResponse issueTokenPair(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpiryDays));
        refreshToken.setUsed(false);
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                accessToken,
                refreshToken.getToken(),
                jwtService.getAccessTokenExpiryMs() / 1000);
    }
}