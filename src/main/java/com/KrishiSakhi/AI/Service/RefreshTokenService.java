package com.KrishiSakhi.AI.Service;


import com.KrishiSakhi.AI.Model.RefreshToken;
import com.KrishiSakhi.AI.Repository.RefreshTokenRepository;
import com.KrishiSakhi.AI.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${krishi.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // Creates or updates the Refresh Token for a user
    public RefreshToken createRefreshToken(Long userId) throws Exception {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(userRepository.findById(userId).get())
                .orElse(new RefreshToken());

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString()); // Use UUID for security

        return refreshTokenRepository.save(refreshToken);
    }

    // Checks if the refresh token is expired
    public RefreshToken verifyExpiration(RefreshToken token) throws Exception {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new Exception("Refresh token was expired. Please make a new sign-in request.");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        // Log out functionality: deletes the refresh token
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}