package com.KrishiSakhi.AI.Repository;

// RefreshTokenRepository.java
//package com.krishisakhi.app.repository;
import com.KrishiSakhi.AI.Model.RefreshToken;
import com.KrishiSakhi.AI.Model.User;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    int deleteByUser(User user);
}

