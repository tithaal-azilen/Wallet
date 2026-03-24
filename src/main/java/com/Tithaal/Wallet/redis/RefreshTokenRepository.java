package com.Tithaal.Wallet.redis;

import com.Tithaal.Wallet.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUserId(Long userId);

    void deleteByTokenHash(String tokenHash);
}
