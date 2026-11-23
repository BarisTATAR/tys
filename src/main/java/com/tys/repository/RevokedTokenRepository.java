package com.tys.repository;

import com.tys.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {
    void deleteByExpiresAtBefore(LocalDateTime now);
}
